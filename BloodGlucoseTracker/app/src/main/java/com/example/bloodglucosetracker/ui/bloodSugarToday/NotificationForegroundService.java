package com.example.bloodglucosetracker.ui.bloodSugarToday;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.bloodglucosetracker.Constants;
import com.example.bloodglucosetracker.R;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationForegroundService extends Service {

    private static final int[] NOTIFICATION_TIMES = {7, 13, 19, 23};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(Integer.parseInt(Constants.NOTIFICATION_FOREGROUND_SERVICE_ID), createNotification());

        scheduleNotifications();

        return START_STICKY;
    }

    private Notification createNotification() {
        createNotificationChannel();

        return new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Blood Glucose Tracker")
                .setContentText("Welcome to our app!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void scheduleNotifications() {
        Log.d("NotificationForegroundService", "Scheduling notifications...");

        for (int notificationTime : NOTIFICATION_TIMES) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, notificationTime);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            long delay = calendar.getTimeInMillis() - System.currentTimeMillis();
            WorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(this).enqueue(notificationRequest);
        }
    }

    public static class NotificationWorker extends Worker {

        public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            Context context = getApplicationContext();
            sendReminderNotification(context);
            return Result.success();
        }

        private void sendReminderNotification(Context context) {
            Log.d("NotificationWorker", "Sending reminder notification");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Blood Sugar Reminder")
                    .setContentText("Time to check your blood sugar!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(Constants.NOTIFICATION_REQUEST_CODE, builder.build());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
