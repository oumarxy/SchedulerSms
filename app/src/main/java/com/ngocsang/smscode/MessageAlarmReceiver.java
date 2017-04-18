package com.ngocsang.smscode;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsManager;

import com.kyleszombathy.sms_scheduler.R;

import java.util.ArrayList;
import java.util.Calendar;




public class MessageAlarmReceiver extends WakefulBroadcastReceiver {
    private AlarmManager alarm;
    private PendingIntent pendingIntent;
    private SmsManager smsManager = SmsManager.getDefault();
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<String> phone = intent.getStringArrayListExtra("pNum");
        String messageContent = intent.getStringExtra("message");
        int alarmNumber = intent.getIntExtra("alarmNumber", -1);

        System.out.println(phone.toString());
        System.out.println(messageContent);

        ArrayList<String> messageArrayList;
        messageArrayList = smsManager.divideMessage(messageContent);


        for (int i=0; i < phone.size(); i++) {
            sendSMSMessage(phone.get(i), messageArrayList);
            markAsSent(context, alarmNumber);

        }
    }

    private void markAsSent(Context context, int alarmNumber) {

            Intent intent = new Intent("custom-event-name");

            intent.putExtra("alarmNumber", alarmNumber);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private void sendSMSMessage(String phoneNumber, ArrayList<String> messageArrayList) {

        if (messageArrayList.size() == 1) {
            smsManager.sendTextMessage(phoneNumber, null, messageArrayList.get(0), null, null);
        } else {
            smsManager.sendMultipartTextMessage(phoneNumber, null, messageArrayList, null, null);
        }
    }


    private void sendNotification(Context context, String msg) {
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, Home.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(context.getString(R.string.message_success))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void setAlarm(Context context, Calendar timeToSend,
                         ArrayList<String> phoneNumberList, String messageContent,
                         int alarmNumber) {
        this.context = context;


        alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(context, MessageAlarmReceiver.class);

        Bundle extras = new Bundle();
        extras.putStringArrayList("pNum", phoneNumberList);
        extras.putString("message", messageContent);
        extras.putInt("alarmNumber", alarmNumber);
        intentAlarm.putExtras(extras);

        pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmNumber,
                intentAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC_WAKEUP, timeToSend.getTimeInMillis(), pendingIntent);
        ComponentName receiver = new ComponentName(context, MessageBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {
        if (alarm != null) {
            alarm.cancel(pendingIntent);
        }

        ComponentName receiver = new ComponentName(context, MessageBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
