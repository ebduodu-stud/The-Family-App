package com.familyworship.schedule;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
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
    WebView web;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.addJavascriptInterface(new ScheduleBridge(), "Android");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                String u = request.getUrl().toString();
                if (u.startsWith("file:///android_asset/")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u))); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "No app found to open this link.", Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, String u) {
                if (u.startsWith("file:///android_asset/")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u))); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "No app found to open this link.", Toast.LENGTH_SHORT).show(); }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
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
    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
