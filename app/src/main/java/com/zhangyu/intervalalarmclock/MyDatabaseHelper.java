package com.zhangyu.intervalalarmclock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    static final String databaseName = "AlarmTimeDB.db";

    private static final String CREATE_TABLE_ALARMTIME = "create table AlarmTime(" +
                                                            "id integer primary key autoincrement," +
                                                            "strDateTime text not null unique," +
                                                            "mark text default null)";
    private Context mContext;

    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ALARMTIME);
        Toast.makeText(mContext, "创建数据库成功，创建表成功！", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists AlarmTime");
        onCreate(db);
    }
}
