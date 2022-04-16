package com.zhangyu.intervalalarmclock;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;

import android.app.TimePickerDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zhy";
    private Calendar mCalendar;
    private final String strDateFormat="%d-%02d-%02d";
    private final String strTimeFormat ="%02d:%02d";
    static final String strDateTimeFormat = "yyyy-MM-dd HH:mm";
    static String strMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //设置开机启动 推荐用静态注册（设置：权限和<action>）
//        ComponentName mComponentName = new ComponentName(this, MainActivity.class);
//        PackageManager mPackageManager = getPackageManager();
//
//        mPackageManager.setComponentEnabledSetting(mComponentName,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);

        //文本框：闹钟提示语
        TextView mTextView_Message = findViewById(R.id.textView_Message);
        mTextView_Message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mEditText = new EditText(MainActivity.this);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("请输入闹钟提示文字")
                        .setIcon(R.mipmap.ic_launcher_round)
                        .setView(mEditText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strIput = mEditText.getText().toString();
                                if (!strIput.trim().equals("")) {
                                    mTextView_Message.setText(strIput);
                                }
                            }
                        }).show();
            }
        });


        //文本视图：间隔时间
        TextView mTextView_Interval = findViewById(R.id.textView_Interval);
        mTextView_Interval.setText("00:01");

        mTextView_Interval.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new TimePickerDialog(MainActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mTextView_Interval.setText(String.format(strTimeFormat, hourOfDay ,minute));
                    }
                },0,1,true).show();
            }
        });

        //文本视图：选择日期
        TextView mTextView_Date = findViewById(R.id.textView_Date);
        mCalendar = Calendar.getInstance();
        mTextView_Date.setText(String.format(strDateFormat,
                            mCalendar.get(Calendar.YEAR),
                            mCalendar.get(Calendar.MONTH)+1, //月份是从0开始的
                            mCalendar.get(Calendar.DAY_OF_MONTH)));

        mTextView_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCalendar = Calendar.getInstance();
                new DatePickerDialog(MainActivity.this,DatePickerDialog.THEME_HOLO_LIGHT,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        mTextView_Date.setText(String.format(strDateFormat,year,month+1,day));    //月份是从0开始的
                    }
                }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //文本视图：选择时间
        TextView mTextView_Time = findViewById(R.id.textView_Time);
        mTextView_Time.setText(String.format(strTimeFormat, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE)));

        mTextView_Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar = Calendar.getInstance();
                new TimePickerDialog(MainActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mTextView_Time.setText(String.format(strTimeFormat, hourOfDay ,minute));
                    }
                }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE),true).show();
            }
        });

        //列表视图
//        ListView mListView = findViewById(R.id.list_Time);

        //数据库
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this, MyDatabaseHelper.databaseName, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        RecyclerView mRecyclerView = findViewById(R.id.recyclerView_DateTime);
        refreshRecyclerView(db, this, mRecyclerView);
//        refreshListView(db, "select strDateTime from AlarmTime",mListView);

        //按钮：添加闹钟
        Button mButton_AddAlarm = findViewById(R.id.button_AddAlarm);

//        ArrayList<String> listTime = new ArrayList<String>();   //新加的闹钟时间

        //按钮：添加闹钟
        mButton_AddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                listTime.clear();

                String strEndTime = String.format("%s %s", mTextView_Date.getText(), mTextView_Time.getText());

                String strInterval = mTextView_Interval.getText().toString();
                long intervalHour = Long.parseLong(strInterval.substring(0, 2));
                long intervalMinute = Long.parseLong(strInterval.substring(3, 5));

                SimpleDateFormat sdf_DateTime = new SimpleDateFormat(strDateTimeFormat);
                String strStartTime = sdf_DateTime.format(new Date());

                Date endTime;
                Date startTime;

                try {
                    startTime = sdf_DateTime.parse(strStartTime);
                    endTime = sdf_DateTime.parse(strEndTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, "“添加闹钟“错误: 时间解析错误");

                    return;
                }

//                listTime.add(strStartTime);

                if (endTime.after(startTime)) {

                    Date clockTime = startTime; //把开始时间设为闹钟时间

                    while (true) {

                        //下次闹钟时间=闹钟时间+间隔时间
                        clockTime = new Date((clockTime.getTime() + intervalHour * 60 * 60 * 1000L + intervalMinute * 60 * 1000L));
                        if (clockTime.after(endTime)) {
                            break;
                        }

                        String strClockTime = sdf_DateTime.format(clockTime);

                        //查询数据库
//                        Cursor mCursor = db.rawQuery("select strDateTime from AlarmTime where strDateTime = ? and mark is null", new String[]{strClockTime});
                        Cursor mCursor = db.rawQuery("select strDateTime from AlarmTime where strDateTime = ?", new String[]{strClockTime});

                        if (mCursor.moveToFirst() == false) {
                            //如果没有数据，就插入数据到数据库
//                            db.execSQL("insert into AlarmTime(strDateTime) " +
//                                            "select ? where not exists(select strDateTime from AlarmTime where strDateTime = ?)",
                            db.execSQL("insert into AlarmTime(strDateTime) values (?)",
                                    new String[]{strClockTime});

//                            listTime.add(strClockTime); //当前操作的列表
                        }

                        mCursor.close();    //关闭游标

//                        //下次闹钟时间=闹钟时间+间隔时间
//                        clockTime = new Date((clockTime.getTime() + intervalHour * 60 * 60 * 1000L + intervalMinute * 60 * 1000L));
//                        if (clockTime.after(endTime)) {
//                            break;
//                        }
                    }

                    int mPosition = mRecyclerView.getAdapter().getItemCount();

                    refreshRecyclerView(db,MainActivity.this, mRecyclerView);
//                    refreshListView(db, "select strDateTime from AlarmTime",mListView);//mark

                    mRecyclerView.scrollToPosition(mPosition);
                }

            }
        });

        //按钮：运行闹钟
        Button mButton_Run = findViewById(R.id.button_Run);

        mButton_Run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (listTime.isEmpty()) {
//                    return;
//                }

                Intent mIntent = new Intent(MainActivity.this,MyAlarmReceiver.class);   //显式意图

                String strButton = mButton_Run.getText().toString();
                String strStart = getResources().getString(R.string.run_start);
                String strStop = getResources().getString(R.string.run_stop);

                if (strButton.equals(strStart)) {
                    strMessage = mTextView_Message.getText().toString();
                    //传递信息
                    mIntent.putExtra("flag_StartOrCancel", "start");    //启动/取消闹钟的标志
//                    mIntent.putExtra("List_Time", listTime);
//                    mIntent.putExtra("Index_List", 0);
                    sendBroadcast(mIntent); //发送广播，触发广播接受器

                    mButton_AddAlarm.setEnabled(false);
                    mButton_Run.setBackgroundColor(Color.RED);
                    mButton_Run.setText(strStop);

                }else if(strButton.equals(strStop)){

                    mIntent.putExtra("flag_StartOrCancel", "cancel");
                    sendBroadcast(mIntent); //发送广播，触发广播接受器

                    //更新数据库
                    db.execSQL("delete from AlarmTime where mark is null");

                    refreshRecyclerView(db, MainActivity.this, mRecyclerView);
//                    refreshListView(db, "select strDateTime from AlarmTime", mListView);

                    mButton_Run.setBackgroundColor(getResources().getColor(R.color.blue));
                    mButton_Run.setText(strStart);
                    mButton_AddAlarm.setEnabled(true);
                }

            }

        });

        //按钮：清除列表
        Button mButton_ClearList = findViewById(R.id.button_ClearList);
        mButton_ClearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.execSQL("delete from AlarmTime");

                refreshRecyclerView(db,MainActivity.this, mRecyclerView);
//                refreshListView(db, "select strDateTime from AlarmTime",mListView);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("选择后台运行或退出")
                    .setMessage("请点击按钮选择")
                    .setPositiveButton("后台运行", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveTaskToBack(false);
                        }
                    })
                    .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent mIntent = new Intent(MainActivity.this, MyAlarmReceiver.class);
                            mIntent.putExtra("flag_StartOrCancel","cancel");
                            sendBroadcast(mIntent); //发送广播，触发广播接受器

                            //更新数据库
                            //数据库
                            MyDatabaseHelper dbHelper = new MyDatabaseHelper(MainActivity.this, "AlarmTimeDB.db", null, 1);
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.execSQL("delete from AlarmTime where mark is null");

                            finish();
                        }
                    }).show();

        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshRecyclerView(SQLiteDatabase db, Context mContext, RecyclerView mRecyclerView){

        String strSql="select strDateTime,mark from AlarmTime";

        Cursor mCursor = db.rawQuery(strSql,null);

        List<MyDateTime> myDateTimeList = new ArrayList<>();

        long num=0L;
        while (mCursor.moveToNext()) {
            num++;
            int columnIndex;
            columnIndex = mCursor.getColumnIndex("strDateTime");
            String dateTime = mCursor.getString(columnIndex);

            columnIndex = mCursor.getColumnIndex("mark");
            String mark = mCursor.getString(columnIndex);

            MyDateTime myDateTime;
            if(mark != null){
                myDateTime = new MyDateTime(num, dateTime, mark, true);
            }else{
                myDateTime = new MyDateTime(num, dateTime, mark, false);
            }

            myDateTimeList.add(myDateTime);
        }

        mCursor.close();    //关闭游标

        MyDateTimeAdapter mAdapter = new MyDateTimeAdapter(myDateTimeList);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();    //更新控件
//        mRecyclerView.scrollToPosition(myDateTimeList.size() - 1); //选择列表最后
        // mRecyclerView.smoothScrollToPosition(listTimeAll.size() - 1);  //滚动到最后
    }

    /**
     *
     * @param db    数据库
     * @param strSql    sql语句
     * @param mListView     ListView控件
     */
//    private void refreshListView(SQLiteDatabase db, String strSql,  ListView mListView){
//
//        ArrayList<String> listTimeAll = new ArrayList<String>();
//
//        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(
//                MainActivity.this, android.R.layout.simple_list_item_multiple_choice, listTimeAll
//        );
//        Cursor mCursor = db.rawQuery(strSql,null);
//
//        while (mCursor.moveToNext()) {
//            int columnIndex = mCursor.getColumnIndex("strDateTime");
//            String strValue = mCursor.getString(columnIndex);
//            listTimeAll.add(strValue);
//        }
//
//        mCursor.close();    //关闭游标
//
//        mListView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();    //更新控件
//        mListView.setSelection(listTimeAll.size() - 1); //选择列表最后
//        // mListView.smoothScrollToPosition(listTimeAll.size() - 1);  //滚动到最后
//    }
}
