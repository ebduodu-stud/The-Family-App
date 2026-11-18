package com.familyworship.schedule;

import android.app.Activity;
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
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://ebduodu-stud.github.io/The-Family-App/";
    private static final String APP_HOST = "ebduodu-stud.github.io";
    WebView web;
    private boolean localFallbackLoaded = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
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
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
