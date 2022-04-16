package com.zhangyu.intervalalarmclock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmActivity extends AppCompatActivity {
    private static final String TAG ="zhy";
    private String mContentText;
    private final String sPrefFileName="alarmClockData";
    private  boolean isStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Toast.makeText(this, "间隔闹钟开始！", Toast.LENGTH_SHORT).show();

        TextView mTextView_CurrentTime = (TextView) findViewById(R.id.textView_CurrentTime);

        SimpleDateFormat sdf_DateTime = new SimpleDateFormat(MainActivity.strDateTimeFormat);
        String strDateTime = sdf_DateTime.format(new Date());
        String strTime = strDateTime.substring(11,16);
        mTextView_CurrentTime.setText(strTime);

        String strMessage = MainActivity.strMessage;

        TextView mTextView_Message = findViewById(R.id.textView_Message);
        mTextView_Message.setText(strMessage);

        isStop = false;

        Button mButton_Stop = findViewById(R.id.button_Stop);
        mButton_Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStop=true;
                AlarmActivity.this.finish();
            }
        });

        try {
            //wakeUpAndUnlock(this, 15000);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "锁屏解锁错误");
        }

        Handler handler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        SimpleDateFormat sdf_DateTime = new SimpleDateFormat("HH:mm:ss");
                        String strTime = sdf_DateTime.format(new Date());
                        mTextView_CurrentTime.setText(strTime);
                }
            }
        };

        //响铃耗时，开新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                mContentText = strTime + " " + strMessage;
                sendNotification(AlarmActivity.this,AlarmActivity.this,mContentText);

                Ringtone mRingtone = RingtoneManager.getRingtone(AlarmActivity.this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

                Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                long[] pattern = {800, 500, 400, 300};

                Date mAlarmTime;
                try {
                    mAlarmTime = sdf_DateTime.parse(strDateTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, "时间解析错误");

                    return;
                }
                long startMillis = mAlarmTime.getTime();

                while (true) {
                    Message mMessage = new Message();
                    mMessage.what=0;
                    handler.sendMessage(mMessage);

                    if(isStop) break;

                    mVibrator.vibrate(pattern, -1);   //第二个参数-1只振动1次，其他数是从下标开始循环//振动
                    mRingtone.play();   //铃声

                    if((new Date().getTime() - startMillis) > 60*1000-600 ) break;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.i(TAG, "Thread.sleep错误");
                    }
                }
                mRingtone.stop();   //停止铃声
                mVibrator.cancel();  //停止振动

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.cancel(2333);  //取消通知

                AlarmActivity.this.finish();
            }
        }).start();

        //开新线程
        new Thread(new Runnable() {
            @Override
            public void run() {

                //更新数据库
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(AlarmActivity.this, "AlarmTimeDB.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("update AlarmTime set mark = '完成' where strDateTime =? and mark is null", new String[]{strDateTime});
            }
        }).start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            sendNotification(this,this,this.mContentText);
//            moveTaskToBack(false);

            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    /**
     * 唤醒手机屏幕并解锁
     */
    public void wakeUpAndUnlock(Context context,long timeOut) {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpeTimer");
            wl.acquire(timeOut); // 点亮屏幕
            wl.release(); // 释放

        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        // 屏幕锁定
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard(); // 解锁
    }

    /**
     * 发送通知
     * @param mContext
     * @param mActivity
     * @param mContentText
     */
    private void sendNotification(Context mContext, AppCompatActivity mActivity, String mContentText) {
        // Create an explicit intent for an Activity in your app
        Intent mIntent = mActivity.getIntent();
//        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder mBuilder = new Notification.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("正在运行程序："+ getResources().getString(R.string.app_name))
                .setContentText(mContentText)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)    // Set the intent that will fire when the user taps the notification
                .setAutoCancel(false)   //点击后不自动取消
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        //.setWhen(System.currentTimeMillis());

        //调用方法
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationId is a unique int for each notification that you must define
        mNotificationManager.notify(2333, mBuilder.build());
    }
}