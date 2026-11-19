package com.familyworship.schedule;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String PREFS = "family_reading_reminders";
    private static final String CHANNEL_ID = "family_reading";
    private static final int ALARM_ID = 3403;

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Reading reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Personal reminders for assigned family readings.");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void scheduleNext(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String profile = prefs.getString("profile", "");
        String raw = prefs.getString("schedule", "[]");
        String reminderTime = prefs.getString("reminderTime", "03:00");
        int reminderHour = 3, reminderMinute = 0;
        try { String[] timeParts = reminderTime.split(":"); reminderHour = Integer.parseInt(timeParts[0]); reminderMinute = Integer.parseInt(timeParts[1]); if (reminderHour < 0 || reminderHour > 23 || reminderMinute < 0 || reminderMinute > 59) throw new Exception("Invalid reminder time"); } catch (Exception ignored) { reminderHour = 3; reminderMinute = 0; }
        if (profile.isEmpty()) return;
        try {
            JSONArray entries = new JSONArray(raw);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            TimeZone ghana = TimeZone.getTimeZone("Africa/Accra");
            dateFormat.setTimeZone(ghana);
            String today = dateFormat.format(new Date());
            Calendar now = Calendar.getInstance(ghana);
            JSONObject next = null;
            for (int i = 0; i < entries.length(); i++) {
                JSONObject item = entries.getJSONObject(i);
                String date = item.optString("date", "");
                if (date.compareTo(today) < 0) continue;
                Calendar alarm = Calendar.getInstance(ghana);
                String[] bits = date.split("-");
                alarm.set(Integer.parseInt(bits[0]), Integer.parseInt(bits[1]) - 1, Integer.parseInt(bits[2]), reminderHour, reminderMinute, 0);
                alarm.set(Calendar.MILLISECOND, 0);
                if (alarm.getTimeInMillis() <= now.getTimeInMillis()) continue;
                next = item;
                break;
            }
            AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pending = PendingIntent.getBroadcast(context, ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarms.cancel(pending);
            if (next != null) {
                String date = next.optString("date");
                Calendar alarm = Calendar.getInstance(ghana);
                String[] bits = date.split("-");
                alarm.set(Integer.parseInt(bits[0]), Integer.parseInt(bits[1]) - 1, Integer.parseInt(bits[2]), reminderHour, reminderMinute, 0);
                alarm.set(Calendar.MILLISECOND, 0);
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pending);
            }
        } catch (Exception ignored) { }
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            scheduleNext(context);
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String profile = prefs.getString("profile", "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Africa/Accra"));
        String today = dateFormat.format(new Date());
        try {
            JSONArray entries = new JSONArray(prefs.getString("schedule", "[]"));
            for (int i = 0; i < entries.length(); i++) {
                JSONObject item = entries.getJSONObject(i);
                if (today.equals(item.optString("date"))) {
                    String parts = item.optString("parts", "your reading assignment");
                    Intent open = new Intent(context, MainActivity.class);
                    open.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent tap = PendingIntent.getActivity(context, 3404, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                        ? new Notification.Builder(context, CHANNEL_ID)
                        : new Notification.Builder(context);
                    Notification notification = builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Good morning, " + profile)
                        .setContentText("You have a reading assignment today.")
                        .setStyle(new Notification.BigTextStyle().bigText(parts))
                        .setContentIntent(tap).setAutoCancel(true).setCategory(Notification.CATEGORY_REMINDER).build();
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        manager.notify(3405, notification);
                    }
                    break;
                }
            }
        } catch (Exception ignored) { }
        scheduleNext(context);
    }
}