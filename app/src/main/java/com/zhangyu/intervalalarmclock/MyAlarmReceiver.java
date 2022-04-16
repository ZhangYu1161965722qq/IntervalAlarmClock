package com.zhangyu.intervalalarmclock;

import android.content.Context;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyAlarmReceiver extends BroadcastReceiver {
    //不适合复杂逻辑或耗时操作，广播中不允许开启线程
    @Override
    public void onReceive(final Context context, Intent intent) {
        //闹钟服务
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        String flag_StartOrCancel = intent.getStringExtra("flag_StartOrCancel");

        if (flag_StartOrCancel.equals("start")) {
            startAlarm(context, intent, mAlarmManager);
        } else if (flag_StartOrCancel.equals("cancel")) {
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 2333, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.cancel(mPendingIntent);
        }

    }

//    //当前闹钟时间索引
//    private int indexOfList;

    private void startAlarm(Context context, Intent intent,AlarmManager mAlarmManager){

        boolean isStop = intent.getBooleanExtra("isFist", true);

        if(isStop) {
            intent.putExtra("isFist",false);
        }else{
            //启动闹钟activity
            Intent mIntent = new Intent(context, AlarmActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //5.0版要这一句，否则报错;7.0版这一句加不加都一样无效;理论上这叫动态注册启动模式，但实际无效
            context.startActivity(mIntent);
        }

        //数据库
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(context, MyDatabaseHelper.databaseName, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        SimpleDateFormat sdf_DateTime = new SimpleDateFormat(MainActivity.strDateTimeFormat);
        String strNow = sdf_DateTime.format(new Date());

        Cursor mCursor = db.rawQuery("select min(id) as nextId, strDateTime from AlarmTime" +
                                            " where datetime(strDateTime) > datetime(?) group by strDateTime",
                                    new String[]{strNow});

        String strNextDateTime =""; //下次运行时间

        //判断是否有记录：mCursor.moveToFirst()==false 或者 mCursor.getCount==0取数时要moveToFirst
        if(mCursor.moveToFirst()==false){
            //没有记录就退出
            return;
        }else{
            //如果存在，就取出
            int columnIndex = mCursor.getColumnIndex("strDateTime");
            strNextDateTime = mCursor.getString(columnIndex);
        }
        mCursor.close();

        Calendar mCalendar =Calendar.getInstance();
        try {
            Date nextTime = sdf_DateTime.parse(strNextDateTime);
            mCalendar.setTime(nextTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        //下次运行时间
        long triggerAtTime = mCalendar.getTimeInMillis();

        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 2333, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime, mPendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, mPendingIntent);
        } else {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, mPendingIntent);
        }


//        //当前闹钟时间索引
//        indexOfList = intent.getIntExtra("Index_List", -1);
//
//        if (indexOfList > 0) {
//            //启动闹钟activity
//            Intent mIntent = new Intent(context, AlarmActivity.class);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //5.0版要这一句，7.0版这一句加不加都一样无效，理论上这叫动态注册启动模式，但实际无效
//            mIntent.putExtra("intent_mark", "alarm");
//            context.startActivity(mIntent);
//        }
//
//        ArrayList<String> listTime = intent.getStringArrayListExtra("List_Time");
//
//        if (indexOfList >= listTime.size()-1){return;}

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Calendar mCalendar =Calendar.getInstance();
//                SimpleDateFormat sdf_DateTime = new SimpleDateFormat(MainActivity.strDateTimeFormat);
//
//                //下次运行时间
//                String strDateTime = listTime.get(++indexOfList);
//
//                try {
//                    Date nextTime = sdf_DateTime.parse(strDateTime);
//                    mCalendar.setTime(nextTime);
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//                //下次运行时间
//                long triggerAtTime = mCalendar.getTimeInMillis();
//
//                intent.putExtra("Index_List", indexOfList);
//
//                PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 2333, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime, mPendingIntent);
//                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, mPendingIntent);
//                }else{
//                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, mPendingIntent);
//                }
//            }
//        }).start();
    }
}
