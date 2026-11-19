package com.familyworship.schedule;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://ebduodu-stud.github.io/The-Family-App/";
    private static final String APP_HOST = "ebduodu-stud.github.io";
    private static final int NOTIFICATION_PERMISSION_REQUEST = 2401;
    WebView web;
    private boolean localFallbackLoaded = false;
    private boolean pendingReminderSetup = false;
    private boolean pendingTestNotification = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        ReminderReceiver.createChannel(this);
        web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        web.addJavascriptInterface(new ScheduleBridge(), "Android");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                Uri uri = request.getUrl();
                if (APP_HOST.equals(uri.getHost()) && uri.getPath() != null && uri.getPath().startsWith("/The-Family-App/")) return false;
                openExternal(uri.toString());
                return true;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) {
                Uri uri = Uri.parse(url);
                if (APP_HOST.equals(uri.getHost()) && uri.getPath() != null && uri.getPath().startsWith("/The-Family-App/")) return false;
                if (url.startsWith("file:///android_asset/")) return false;
                openExternal(url);
                return true;
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                if (request.isForMainFrame() && !localFallbackLoaded) {
                    localFallbackLoaded = true;
                    view.loadUrl("file:///android_asset/index.html");
                    Toast.makeText(MainActivity.this, "Offline mode: showing the built-in copy. Connect to the internet to receive updates.", Toast.LENGTH_LONG).show();
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        setContentView(web);
        web.loadUrl(APP_URL);
    }

    private void openExternal(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(MainActivity.this, "No app found to open this link.", Toast.LENGTH_SHORT).show(); }
    }

    private void requestOrScheduleReminders() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pendingReminderSetup = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        } else {
            ReminderReceiver.scheduleNext(this);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST && pendingReminderSetup) {
            pendingReminderSetup = false;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ReminderReceiver.scheduleNext(this);
                Toast.makeText(this, "Your personal reading reminders are enabled.", Toast.LENGTH_LONG).show();
                if (pendingTestNotification) showTestNotification();
            } else {
                pendingTestNotification = false;
                Toast.makeText(this, "Notifications are off. You can enable them in Android Settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ScheduleBridge {
        @JavascriptInterface public void saveScheduleFile(String name) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Saving " + name + "…", Toast.LENGTH_SHORT).show());
            new Thread(() -> {
                try (InputStream in = getAssets().open("schedule/" + name)) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, name);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                    if (Build.VERSION.SDK_INT >= 29) values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) throw new Exception("Could not create download");
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        if (out == null) throw new Exception("Could not write download");
                        byte[] buffer = new byte[8192]; int count;
                        while ((count = in.read(buffer)) > 0) out.write(buffer, 0, count);
                    }
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Saved to Downloads", Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Could not save the PDF. Please try again.", Toast.LENGTH_LONG).show());
                }
            }).start();
        }

        @JavascriptInterface public void setReadingReminders(String profile, String scheduleJson, String reminderTime) {
            SharedPreferences prefs = getSharedPreferences(ReminderReceiver.PREFS, MODE_PRIVATE);
            prefs.edit().putString("profile", profile).putString("schedule", scheduleJson).putString("reminderTime", reminderTime).apply();
            runOnUiThread(() -> requestOrScheduleReminders());
        }
        @JavascriptInterface public void sendTestReminder() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    pendingTestNotification = true;
                    requestOrScheduleReminders();
                } else showTestNotification();
            });
        }

        @JavascriptInterface public void openNotificationSettings() {
            runOnUiThread(() -> {
                Intent settingsIntent = new Intent();
                if (Build.VERSION.SDK_INT >= 26) {
                    settingsIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    settingsIntent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                    settingsIntent.putExtra("android.provider.extra.CHANNEL_ID", "family_reading");
                } else {
                    settingsIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    settingsIntent.setData(Uri.parse("package:" + getPackageName()));
                }
                try { startActivity(settingsIntent); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "Could not open notification settings.", Toast.LENGTH_SHORT).show(); }
            });
        }

    }

    private void showTestNotification() {
        pendingTestNotification = false;
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Allow notifications for this app first.", Toast.LENGTH_LONG).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) ReminderReceiver.createChannel(this);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (!manager.areNotificationsEnabled()) {
            Toast.makeText(this, "Notifications are blocked for this app. Open Notification settings and allow them.", Toast.LENGTH_LONG).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            android.app.NotificationChannel channel = manager.getNotificationChannel("family_reading");
            if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                Toast.makeText(this, "The Reading reminders channel is turned off. Enable it in Notification settings.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Notification notification = new Notification.Builder(this, Build.VERSION.SDK_INT >= 26 ? "family_reading" : "")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Family Morning Worship")
            .setContentText("Test notification: your reminders are working.")
            .setAutoCancel(true).build();
        manager.notify(3499, notification);
        Toast.makeText(this, "Test notification sent. Check your notification shade.", Toast.LENGTH_LONG).show();
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}