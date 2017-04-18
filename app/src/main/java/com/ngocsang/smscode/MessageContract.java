package com.ngocsang.smscode;

import android.provider.BaseColumns;

public final class MessageContract {



    public MessageContract() {}


    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "scheduledMessage";
        public static final String NAME = "name";
        public static final String PHONE = "phone";
        public static final String NAME_PHONE_FULL = "namePhoneFull";
        public static final String MESSAGE = "message";
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DAY = "day";
        public static final String HOUR = "hour";
        public static final String MINUTE = "minute";
        public static final String ALARM_NUMBER = "alarmNumber";
        public static final String ARCHIVED = "archived";
        public static final String PHOTO_URI = "photoUri";
        public static final String NULLABLE = "nullable";
        public static final String DATETIME = "dateTime";
        public static final String TABLE_PHOTO = "photoTable";
        public static final String PHOTO_URI_1 = "photoUri1";
        public static final String PHOTO_BYTES = "photoBytes";
    }
}