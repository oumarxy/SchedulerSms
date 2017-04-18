package com.ngocsang.smscode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;


public class MessageBootReceiver extends BroadcastReceiver {
    MessageAlarmReceiver alarm = new MessageAlarmReceiver();

    private ArrayList<String> phoneDataset = new ArrayList<>();
    private ArrayList<String> messageContentDataset = new ArrayList<>();
    private ArrayList<Calendar> calendarDataset = new ArrayList<>();
    private ArrayList<Integer> alarmNumberDateset = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            readFromSQLDatabase(context);
            for (int i = 0; i < phoneDataset.size(); i++) {
                ArrayList<String> phoneNumbers = parsePhoneNumbers(phoneDataset.get(i));
                alarm.setAlarm(
                        context,
                        calendarDataset.get(i),
                        phoneNumbers,
                        messageContentDataset.get(i),
                        alarmNumberDateset.get(i));
            }
        }
    }

    private ArrayList<String> parsePhoneNumbers(String phoneList) {
        ArrayList<String> phoneNumbers = new ArrayList<>();
        phoneList = phoneList.replace("[", "");
        phoneList = phoneList.replace("]", "");
        for (String phone: phoneList.split(",")) {
            phoneNumbers.add(phone);
        }
        return phoneNumbers;
    }

    private void readFromSQLDatabase(Context context) {
        MessageDbHelper mDbHelper = new MessageDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                MessageContract.MessageEntry.PHONE,
                MessageContract.MessageEntry.MESSAGE,
                MessageContract.MessageEntry.YEAR,
                MessageContract.MessageEntry.MONTH,
                MessageContract.MessageEntry.DAY,
                MessageContract.MessageEntry.HOUR,
                MessageContract.MessageEntry.MINUTE,
                MessageContract.MessageEntry.ALARM_NUMBER,
                MessageContract.MessageEntry.ARCHIVED
        };

        String sortOrder =
                MessageContract.MessageEntry.NAME + " DESC";

        Cursor cursor = db.query(
                MessageContract.MessageEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            int archived = cursor.getInt(cursor.getColumnIndexOrThrow
                    (MessageContract.MessageEntry.ARCHIVED));
            if (archived == 0) {

                int year = cursor.getInt(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.YEAR));
                int month = cursor.getInt(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.MONTH));
                int day = cursor.getInt(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.DAY));
                int hour = cursor.getInt(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.HOUR));
                int minute = cursor.getInt(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.MINUTE));
                messageContentDataset.add(cursor.getString(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.MESSAGE)));
                phoneDataset.add(cursor.getString(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.PHONE)));
                alarmNumberDateset.add(cursor.getInt(cursor.getColumnIndexOrThrow
                        (MessageContract.MessageEntry.ALARM_NUMBER)));


                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                calendarDataset.add(cal);
            }

            cursor.moveToNext();
        }

        cursor.close();
        mDbHelper.close();
    }
}
