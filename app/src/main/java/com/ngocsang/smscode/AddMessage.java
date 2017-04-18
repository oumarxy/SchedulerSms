package com.ngocsang.smscode;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.kyleszombathy.sms_scheduler.R;
import com.simplicityapks.reminderdatepicker.lib.ReminderDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class AddMessage extends AppCompatActivity
        implements
        DatePickerFragment.OnCompleteListener, TimePickerFragment.OnCompleteListener
{
    private static final String TAG = "AddMessage";
    private int year, month, day, hour, minute;
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> phone = new ArrayList<>();
    private ArrayList<String> fullChipString = new ArrayList<>();
    private ReminderDatePicker datePicker;
    private RecipientEditTextView phoneRetv;
    private DrawableRecipientChip[] chips;
    private ArrayList<String> photoUri = new ArrayList<>();
    private int smsMaxLength = 140;
    private TextView counterTextView;
    private EditText messageContentEditText;
    int alarmNumber;
    int editMessageNewAlarmNumber;
    private Calendar calendar = Calendar.getInstance();

    long sqlRowId;
    private TextView phoneRetvErrorMessage;
    private TextView messageContentErrorMessage;
    private String messageContentString = "";
    private boolean editMessage = false;
    private String phoneNumbTempString = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        alarmNumber = extras.getInt("alarmNumber", -1);
        editMessageNewAlarmNumber = extras.getInt("NEW_ALARM", -1);
        editMessage = extras.getBoolean("EDIT_MESSAGE", false);
        Toolbar myChildToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
        AddMessageFragment firstFragment = new AddMessageFragment();
        firstFragment.setArguments(getIntent().getExtras());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, firstFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        counterTextView = (TextView) findViewById(R.id.count);
        messageContentEditText = (EditText) findViewById(R.id.messageContent);
        messageContentEditText.addTextChangedListener(messageContentEditTextWatcher);
        messageContentErrorMessage = (TextView) findViewById(R.id.messageContentError);
        messageContentEditText.getBackground().setColorFilter(getResources().
                getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        messageContentErrorMessage.setText("");
        phoneRetv = (RecipientEditTextView) findViewById(R.id.phone_retv);
        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        phoneRetv.setAdapter(new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, this));
        phoneRetv.addTextChangedListener(phoneRetvEditTextWatcher);
        phoneRetvErrorMessage = (TextView) findViewById(R.id.phone_retv_error);
        phoneRetv.getBackground().setColorFilter(getResources().
                getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        phoneRetvErrorMessage.setText("");
        datePicker = (ReminderDatePicker) findViewById(R.id.date_picker);
        datePicker.setCustomDatePicker(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        datePicker.setCustomTimePicker(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });
        if (editMessage) {
            try {
                getValuesFromSQL();
                phoneRetv.requestFocus();
                phoneRetv.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {

                                for (int i = 0; i < name.size(); i++) {
                                    System.out.println(name.get(i));
                                    System.out.println(phone.get(i));
                                    System.out.println(photoUri.get(i));
                                    byte[] byteArray = Tools.getPhotoValuesFromSQL(AddMessage.this, photoUri.get(i).trim());
                                    phoneRetv.submitItem(name.get(i), phone.get(i), Uri.parse(photoUri.get(i).trim()), byteArray);
                                }

                                name.clear();
                                phone.clear();
                                photoUri.clear();
                                alarmNumber = editMessageNewAlarmNumber;
                                phoneRetv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                });
                messageContentEditText.setText(messageContentString);
                datePicker.setSelectedDate(new GregorianCalendar(year, month, day));
                datePicker.setSelectedTime(hour, minute);
            } catch (Exception e) {
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_message, menu);
        return true;
    }

    private void getValuesFromSQL() {
        MessageDbHelper mDbHelper = new MessageDbHelper(AddMessage.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String selection = MessageContract.MessageEntry.ALARM_NUMBER + " LIKE ?";
        String[] selectionArgs = { String.valueOf(alarmNumber) };
        String[] projection = {
                MessageContract.MessageEntry.NAME,
                MessageContract.MessageEntry.MESSAGE,
                MessageContract.MessageEntry.PHONE,
                MessageContract.MessageEntry.YEAR,
                MessageContract.MessageEntry.MONTH,
                MessageContract.MessageEntry.DAY,
                MessageContract.MessageEntry.HOUR,
                MessageContract.MessageEntry.MINUTE,
                MessageContract.MessageEntry.PHOTO_URI,
                MessageContract.MessageEntry.ALARM_NUMBER
        };

        Cursor cursor = db.query(
                MessageContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );


        cursor.moveToFirst();
        name = Tools.parseString(cursor.getString(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.NAME)));
        phoneNumbTempString = cursor.getString(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.PHONE));
        phone = Tools.parseString(phoneNumbTempString);
        year = cursor.getInt(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.YEAR));
        month = cursor.getInt(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.MONTH));
        day = cursor.getInt(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.DAY));
        hour = cursor.getInt(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.HOUR));
        minute = cursor.getInt(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.MINUTE));
        photoUri = Tools.parseString(cursor.getString(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.PHOTO_URI)));
        messageContentString = cursor.getString(cursor.getColumnIndexOrThrow
                (MessageContract.MessageEntry.MESSAGE));

        cursor.close();
        mDbHelper.close();
    }



    public void showTimePickerDialog() {
        TimePickerFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog() {
        DatePickerFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }


    public void onComplete(int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        datePicker.setSelectedDate(calendar);
    }

    @Override

    public void onComplete(int hourOfDay, int minute) {
        datePicker.setSelectedTime(hourOfDay, minute);
    }



    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                messageContentString = messageContentEditText.getText().toString();

                messageContentEditText.requestFocus();
                phoneRetv.requestFocus();
                chips = phoneRetv.getSortedRecipients();

                Calendar cal = datePicker.getSelectedDate();
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
                hour = cal.get(Calendar.HOUR_OF_DAY);
                minute = cal.get(Calendar.MINUTE);

                if (verifyData()) {

                    addDataToSQL();
                    addPhotoDataToSQL();
                    scheduleMessage();
                    hideKeyboard();
                    createSnackBar(getString(R.string.success));


                    Intent returnIntent = new Intent();
                    Bundle extras = new Bundle();
                    extras.putString("NAME_EXTRA", name.toString());
                    extras.putString("CONTENT_EXTRA", messageContentString);
                    extras.putInt("ALARM_EXTRA", alarmNumber);
                    extras.putInt("YEAR_EXTRA", year);
                    extras.putInt("MONTH_EXTRA", month);
                    extras.putInt("DAY_EXTRA", day);
                    extras.putInt("HOUR_EXTRA", hour);
                    extras.putInt("MINUTE_EXTRA", minute);
                    returnIntent.putExtras(extras);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    return true;
                } else {
                    return false;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean verifyData() {
        boolean result = true;
        if (chips == null) {
            result = errorChipsEmpty();
        } else if (chips.length == 0) {
            result = errorChipsEmpty();
        } else if (chips.length > 0) {
            for (DrawableRecipientChip chip : chips) {
                String str = chip.toString();
                String phoneTemp = getPhoneNumberFromString(str);
                Log.i(TAG, "phoneTemp: " + phoneTemp);

                if (phoneTemp.length() == 0) {
                    result = errorPhoneWrong();
                } else {

                    for (char c: phoneTemp.toCharArray()) {
                        if (Character.isLetter(c) || c == '<' || c == '>' || c == ',') {
                            result = errorPhoneWrong();
                            break;
                        }
                    }
                }
            }
        }
        if (messageContentString.length() == 0) {
            messageContentErrorMessage.setText(getResources().
                    getString(R.string.error_message_content));
            messageContentEditText.getBackground().setColorFilter(getResources().
                    getColor(R.color.error_primary), PorterDuff.Mode.SRC_ATOP);
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .playOn(findViewById(R.id.messageContent));
            result = false;
        }


        if (result) {
            for (DrawableRecipientChip chip : chips) {
                String str = chip.toString().trim();
                String nameTemp = getNameFromString(str);
                String phoneTemp = getPhoneNumberFromString(str);


                phone.add(phoneTemp);
                name.add(nameTemp);
                fullChipString.add(str);

                Uri uri = chip.getEntry().getPhotoThumbnailUri();
                if (uri != null) {
                    photoUri.add(uri.toString().trim());
                } else {
                    photoUri.add(null);
                }
            }
        }

        return result;
    }

    private boolean errorPhoneWrong() {

        phoneRetvErrorMessage.setText(getResources().getString(R.string.invalid_entry));
        phoneRetv.getBackground().setColorFilter(getResources().
                getColor(R.color.error_primary), PorterDuff.Mode.SRC_ATOP);
        YoYo.with(Techniques.Shake)
                .duration(700)
                .playOn(findViewById(R.id.phone_retv));
        phoneRetv.addTextChangedListener(phoneRetvEditTextWatcher);
        return false;
    }

    private boolean errorChipsEmpty() {

        phoneRetvErrorMessage.setText(getResources().getString(R.string.error_recipient));
        phoneRetv.getBackground().setColorFilter(getResources().
                getColor(R.color.error_primary), PorterDuff.Mode.SRC_ATOP);
        YoYo.with(Techniques.Shake)
                .duration(700)
                .playOn(findViewById(R.id.phone_retv));
        return false;
    }

    private void scheduleMessage() {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);


        MessageAlarmReceiver receiver = new MessageAlarmReceiver();
        receiver.setAlarm(this, cal, phone, messageContentString, alarmNumber);
    }

    private void addDataToSQL() {

        MessageDbHelper mDbHelper = new MessageDbHelper(AddMessage.this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        System.out.println(name.toString());
        System.out.println(phone.toString());
        System.out.println(photoUri.toString());
        System.out.println(alarmNumber);


        ContentValues values = new ContentValues();
        values.put(MessageContract.MessageEntry.
                NAME, name.toString());
        values.put(MessageContract.MessageEntry.
                PHONE, phone.toString());
        values.put(MessageContract.MessageEntry.
                NAME_PHONE_FULL, fullChipString.toString());
        values.put(MessageContract.MessageEntry.
                MESSAGE, messageContentString);
        values.put(MessageContract.MessageEntry.
                YEAR, year);
        values.put(MessageContract.MessageEntry.
                MONTH, month);
        values.put(MessageContract.MessageEntry.
                DAY, day);
        values.put(MessageContract.MessageEntry.
                HOUR, hour);
        values.put(MessageContract.MessageEntry.
                MINUTE, minute);
        values.put(MessageContract.MessageEntry.
                ALARM_NUMBER, alarmNumber);
        values.put(MessageContract.MessageEntry.
                PHOTO_URI, photoUri.toString());
        values.put(MessageContract.MessageEntry.
                ARCHIVED, 0);
        values.put(MessageContract.MessageEntry.
                DATETIME, getFullDateString());

        sqlRowId = db.insert(
                MessageContract.MessageEntry.TABLE_NAME,
                MessageContract.MessageEntry.NULLABLE,
                values);

        mDbHelper.close();
        db.close();
    }


    private void addPhotoDataToSQL() {
        MessageDbHelper mDbHelper = new MessageDbHelper(AddMessage.this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        for (int i = 0; i < phoneRetv.getRecipients().length; i++) {

            if (photoUri.get(i) != null) {
                if (Tools.getPhotoValuesFromSQL(AddMessage.this, photoUri.get(i)) == null) {
                    byte[] photoBytes = chips[i].getEntry().getPhotoBytes();


                    ContentValues values = new ContentValues();
                    values.put(MessageContract.MessageEntry.
                            PHOTO_URI_1, photoUri.get(i));
                    values.put(MessageContract.MessageEntry.
                            PHOTO_BYTES, photoBytes);
                    sqlRowId = db.insert(
                            MessageContract.MessageEntry.TABLE_PHOTO,
                            MessageContract.MessageEntry.NULLABLE,
                            values);
                    Log.i(TAG, "New Row Created. Row ID: " + String.valueOf(sqlRowId) + " URI: " + photoUri.get(i));
                } else {
                    Log.i(TAG, "Row not created. Value already exists for " + name.get(i));
                }
            }
        }

        mDbHelper.close();
        db.close();
    }
    private final TextWatcher messageContentEditTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            int length = s.length();

            if (length == 1) {
                messageContentEditText.getBackground().setColorFilter(getResources().
                        getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                messageContentErrorMessage.setText("");
            }

            if (length <= smsMaxLength) {
                counterTextView.setText(String.valueOf(smsMaxLength - length));
            } else {
                counterTextView.setText(String.valueOf(smsMaxLength - length % smsMaxLength)
                        + " / " + String.valueOf(1 + (length / smsMaxLength)));
            }
        }
        public void afterTextChanged(Editable s) {}
    };


    private final TextWatcher phoneRetvEditTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        public void afterTextChanged(Editable s) {

            phoneRetv.getBackground().setColorFilter(getResources().
                    getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
            phoneRetvErrorMessage.setText("");
        }
    };


    private String getFullDateString() {
        GregorianCalendar date = new GregorianCalendar(year, month, day, hour, minute);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date.getTime());
    }


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void createSnackBar(String str) {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), str, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public String getPhoneNumberFromString(String str) {

        String[] retval = str.split("<|>");
        return retval[1].trim();
    }

    public String getNameFromString(String str) {
        String temp = "";
        for (int i =0; i < str.length(); i++) {
            char c = str.charAt(i);
            char d = str.charAt(i + 1);
            temp += c;
            if (d == '<') {
                break;
            }
        } return temp.trim();
    }
}
