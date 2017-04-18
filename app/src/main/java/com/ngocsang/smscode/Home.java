package com.ngocsang.smscode;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toolbar;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.kyleszombathy.sms_scheduler.R;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Objects;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

public class Home extends Activity {
    private static final String TAG = "HOME";
    private static final String ALARM_EXTRA = "alarmNumber";
    private static final String EDIT_MESSAGE_EXTRA = "EDIT_MESSAGE";
    public final int circleImageViewWidth = 56;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView mRecyclerView;
    public RecyclerView.Adapter mAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    public ArrayList<String> nameDataset = new ArrayList<>();
    public ArrayList<String> messageContentDataset = new ArrayList<>();
    public ArrayList<String> dateDataset = new ArrayList<>();
    public ArrayList<String> timeDataSet = new ArrayList<>();
    public ArrayList<Integer> alarmNumberDataset = new ArrayList<>();
    public ArrayList<String> uriDataset = new ArrayList<>();
    private ArrayList<Bitmap> photoDataset= new ArrayList<>();
    private final int MAX_INT = Integer.MAX_VALUE ;
    private final int MIN_INT = 1;
    private static final int NEW_MESSAGE = 1;
    private static final int EDIT_MESSAGE = 0;
    private int tempSelectedPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade());

        setContentView(R.layout.activity_home);
        ObjectAnimator mAnimator;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        readFromSQLDatabase();
        setUpRecyclerView();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        readFromSQLDatabase();
                        mAdapter.notifyDataSetChanged();
                        swipeContainer.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));



        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int alarmNumber = getRandomInt(MIN_INT, MAX_INT);

                Intent intent = new Intent(new Intent(Home.this, AddMessage.class));
                Bundle extras = new Bundle();
                extras.putInt(ALARM_EXTRA, alarmNumber);
                extras.putBoolean(EDIT_MESSAGE_EXTRA, false);
                intent.putExtras(extras);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(Home.this);
                stackBuilder.addParentStack(AddMessage.class);
                stackBuilder.addNextIntent(intent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                startActivityForResult(intent, NEW_MESSAGE,
                        ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
            }
        });
    }


    private int getRandomInt(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    private void readFromSQLDatabase() {
        nameDataset.clear();
        messageContentDataset.clear();
        dateDataset.clear();
        timeDataSet.clear();
        alarmNumberDataset.clear();
        uriDataset.clear();
        photoDataset.clear();

        MessageDbHelper mDbHelper = new MessageDbHelper(Home.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
            MessageContract.MessageEntry.NAME,
            MessageContract.MessageEntry.MESSAGE,
            MessageContract.MessageEntry.YEAR,
            MessageContract.MessageEntry.MONTH,
            MessageContract.MessageEntry.DAY,
            MessageContract.MessageEntry.HOUR,
            MessageContract.MessageEntry.MINUTE,
            MessageContract.MessageEntry.ALARM_NUMBER,
            MessageContract.MessageEntry.PHOTO_URI
        };

        String sortOrder =
                MessageContract.MessageEntry.DATETIME+ " ASC";
        String selection = MessageContract.MessageEntry.ARCHIVED + " LIKE ?";
        String[] selectionArgs = { String.valueOf(0) };

        Cursor cursor = db.query(
                MessageContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );


        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            extractName(cursor.getString(cursor.getColumnIndexOrThrow
                    (MessageContract.MessageEntry.NAME)));
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
            alarmNumberDataset.add(cursor.getInt(cursor.getColumnIndexOrThrow
                    (MessageContract.MessageEntry.ALARM_NUMBER)));
            ArrayList<String> tempUri = Tools.parseString(cursor.getString(cursor.getColumnIndexOrThrow
                    (MessageContract.MessageEntry.PHOTO_URI)));

            if (tempUri.size() == 1 && !Objects.equals(tempUri.get(0).trim(), "null")) {
                uriDataset.add(tempUri.get(0).trim());
            } else {
                uriDataset.add(null);
            }

            setFullDateAndTime(year, month, day, hour, minute);

            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        mDbHelper.close();


        getPhotoBytes();
    }


    private void getPhotoBytes() {
        for (int i = 0; i < uriDataset.size(); i++) {
            if (uriDataset.get(i)!= null) {

                byte[] byteArray = Tools.getPhotoValuesFromSQL(Home.this, uriDataset.get(i));

                ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(byteArray);
                Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
                photoDataset.add(bitmap);
            } else {

                Character nameFirstLetter = nameDataset.get(i).charAt(0);

                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(nameDataset.get(i));
                TextDrawable drawable = TextDrawable.builder()
                        .beginConfig()
                        .useFont(Typeface.DEFAULT_BOLD)
                        .fontSize(60)
                        .height(circleImageViewWidth * 2)
                        .width(circleImageViewWidth * 2)
                        .endConfig()
                        .buildRound(nameFirstLetter.toString().toUpperCase(), color);
                Bitmap bitmap = Tools.drawableToBitmap(drawable);
                photoDataset.add(bitmap);
            }
        }
    }


    private void extractName(String names) {
        String nameCondensedString;
        ArrayList<String> nameList = new ArrayList<>();

        names = names.replace("[", "");
        names = names.replace("]", "");

        if(names.contains(",")) {
            for (String name: names.split(",")) {
                name = name.trim();
                nameList.add(name);
            }
            nameCondensedString = nameList.remove(0) + ", " + nameList.remove(0);
        } else {
            nameCondensedString = names;
        }

        int nameListSize = nameList.size();
        if (nameListSize > 0) {
            nameCondensedString += ", +" + (nameListSize);
        }
        nameDataset.add(nameCondensedString.trim());
    }


    public void setFullDateAndTime(int year, int month, int day, int hour, int minute) {
        GregorianCalendar date = new GregorianCalendar(year, month, day, hour, minute);
        GregorianCalendar dateNow = new GregorianCalendar();

        long time = date.getTimeInMillis();
        long timeNow = dateNow.getTimeInMillis();
        CharSequence dateString;
        String timeString = "";

        dateString = DateUtils.getRelativeTimeSpanString(time, timeNow, DateUtils.MINUTE_IN_MILLIS);

        dateDataset.add(dateString.toString());
        timeDataSet.add(timeString);
    }


    private void setAsArchived(int alarmNumb) {
        MessageDbHelper mDbHelper = new MessageDbHelper(Home.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        ContentValues values = new ContentValues();
        values.put(MessageContract.MessageEntry.
                ARCHIVED, 1);


        String selection = MessageContract.MessageEntry.ALARM_NUMBER + " LIKE ?";
        String[] selectionArgs = { String.valueOf(alarmNumb) };

        int count = db.update(
                MessageContract.MessageEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();
        mDbHelper.close();
    }

    private void deleteFromDatabase(int alarmNumb) {
        MessageDbHelper mDbHelper = new MessageDbHelper(Home.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        String selection = MessageContract.MessageEntry.ALARM_NUMBER + " LIKE ?";
        String[] selectionArgs = { String.valueOf(alarmNumb) };

        db.delete(
                MessageContract.MessageEntry.TABLE_NAME,
                selection,
                selectionArgs);
        db.close();
        mDbHelper.close();
    }

    private void getNextAlarmClock() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Long alarmTime = alarmManager.getNextAlarmClock().getTriggerTime();
        Log.i(TAG, "Next Alarm : " + alarmTime);
    }


    private void cancelAlarm(int alarmNumb) {
        Intent intent = new Intent(this, MessageAlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, alarmNumb, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.i(TAG, "Alarm Canceled");
    }



    private void setUpRecyclerView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new SlideInDownAnimator());

        updateRecyclerViewAdapter();


        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(Home.this,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                tempSelectedPosition = position;


                                Intent intent = new Intent(new Intent(Home.this, AddMessage.class));
                                Bundle extras = new Bundle();

                                extras.putInt(ALARM_EXTRA, alarmNumberDataset.get(position));

                                extras.putInt("NEW_ALARM", getRandomInt(MIN_INT, MAX_INT));

                                extras.putBoolean(EDIT_MESSAGE_EXTRA, true);
                                intent.putExtras(extras);

                                startActivityForResult(intent, EDIT_MESSAGE,
                                        ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                            }
                        })
        );
    }

    private void updateRecyclerViewAdapter() {
        mAdapter = new RecyclerAdapter(
                nameDataset, messageContentDataset, dateDataset, timeDataSet, uriDataset, photoDataset);
        mRecyclerView.setAdapter(mAdapter);
    }


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                Canvas c;
                RecyclerView recyclerView;
                RecyclerView.ViewHolder viewHolder;
                int actionState;
                boolean isCurrentlyActive;

                @Override
                public boolean onMove(RecyclerView recyclerView,
                                      RecyclerView.ViewHolder viewHolder,
                                      RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                    final int position = viewHolder.getAdapterPosition();
                    final String TEMP_NAME = nameDataset.remove(position);
                    final String TEMP_CONTENT = messageContentDataset.remove(position);
                    final String TEMP_DATE = dateDataset.remove(position);
                    final String TEMP_TIME = timeDataSet.remove(position);
                    final Integer TEMP_ALARM = alarmNumberDataset.remove(position);
                    final String TEMP_URI = uriDataset.remove(position);
                    final Bitmap TEMP_PHOTO = photoDataset.remove(position);
                    mAdapter.notifyItemRemoved(position);


                    Snackbar.make(findViewById(R.id.coordLayout),
                            "1 "+ getString(R.string.archived),
                            Snackbar.LENGTH_LONG).setCallback( new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            switch(event) {

                                case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                                case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                                    if (alarmNumberDataset.indexOf(TEMP_ALARM) == -1) {
                                        cancelAlarm(TEMP_ALARM);
                                        setAsArchived(TEMP_ALARM);
                                    }
                                    returnToDefaultPosition();

                                    mAdapter.notifyItemRangeChanged(position,nameDataset.size());
                                    break;
                            }
                        }
                        @Override
                        public void onShown(Snackbar snackbar) {
                        }
                    }).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            nameDataset.add(position, TEMP_NAME);
                            messageContentDataset.add(position, TEMP_CONTENT);
                            dateDataset.add(position, TEMP_DATE);
                            timeDataSet.add(position, TEMP_TIME);
                            alarmNumberDataset.add(position, TEMP_ALARM);
                            uriDataset.add(position, TEMP_URI);
                            photoDataset.add(position, TEMP_PHOTO);
                            returnToDefaultPosition();
                            mAdapter.notifyItemInserted(position);
                            mRecyclerView.scrollToPosition(position);
                        }
                    }).show();
                }

                private void returnToDefaultPosition() {
                    getDefaultUIUtil().onDraw(c, recyclerView, ((RecyclerAdapter.ViewHolder) viewHolder).getSwipableView(), 0, 0,    actionState, isCurrentlyActive);
                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    if (viewHolder instanceof RecyclerAdapter.ViewHolder) {
                        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                        return makeMovementFlags(0, swipeFlags);
                    } else
                        return 0;
                }

                @Override
                public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                    if (viewHolder != null) {
                        getDefaultUIUtil().onSelected(((RecyclerAdapter.ViewHolder) viewHolder).getSwipableView());
                    }
                }

                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    this.c =c;
                    this.recyclerView = recyclerView;
                    this.viewHolder = viewHolder;
                    this.actionState = actionState;
                    this.isCurrentlyActive = isCurrentlyActive;
                    if (dX < 0) {
                        ((RecyclerAdapter.ViewHolder) viewHolder).getmRevealRightView().setVisibility(View.GONE);
                        ((RecyclerAdapter.ViewHolder) viewHolder).getmRevealLeftView().setVisibility(View.VISIBLE);
                    } else {
                        ((RecyclerAdapter.ViewHolder) viewHolder).getmRevealRightView().setVisibility(View.VISIBLE);
                        ((RecyclerAdapter.ViewHolder) viewHolder).getmRevealLeftView().setVisibility(View.GONE);
                    }
                    getDefaultUIUtil().onDraw(c, recyclerView, ((RecyclerAdapter.ViewHolder) viewHolder).getSwipableView(), dX, dY,    actionState, isCurrentlyActive);
                }

                public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    getDefaultUIUtil().onDrawOver(c, recyclerView, ((RecyclerAdapter.ViewHolder) viewHolder).getSwipableView(), dX, dY,    actionState, isCurrentlyActive);
                }
            };
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);


    private void showcase() {
        Target viewTarget = new ViewTarget(R.id.fab, this);
        new ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(viewTarget)
                .setContentTitle("Welcome to SMS Scheduler")
                .setContentText("Click here to add a message")
                .singleShot(42)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {


            if (requestCode == EDIT_MESSAGE) {
                int oldAlarmNumber = alarmNumberDataset.get(tempSelectedPosition);
                cancelAlarm(oldAlarmNumber);
                deleteFromDatabase(oldAlarmNumber);
            }


            readFromSQLDatabase();


            Bundle extras = data.getExtras();
            int position = alarmNumberDataset.indexOf(extras.getInt("ALARM_EXTRA"));



            if (requestCode == EDIT_MESSAGE) {
                mAdapter.notifyItemChanged(position);
                mRecyclerView.scrollToPosition(position);
            }

            if (requestCode == NEW_MESSAGE) {
                mAdapter.notifyItemInserted(position);
                mRecyclerView.scrollToPosition(position);
            }
        }
    }


    private void removeFromDataset(int position) {

        nameDataset.remove(position);
        messageContentDataset.remove(position);
        dateDataset.remove(position);
        timeDataSet.remove(position);
        alarmNumberDataset.remove(position);
        uriDataset.remove(position);
        photoDataset.remove(position);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int alarmNumber = intent.getIntExtra("alarmNumber", -1);
            if (alarmNumber != -1) {
                setAsArchived(alarmNumber);
            }
            int position = alarmNumberDataset.indexOf(alarmNumber);
            if(position != -1) {
                removeFromDataset(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    };

    @Override
    protected void onDestroy() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}


