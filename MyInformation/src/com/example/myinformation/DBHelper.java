package com.example.myinformation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 数据库辅助类
 * @author Administrator
 *
 */
public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context contex) {
		super(contex,"ningshuaichen.db",null,3);//最后一个参数是数据库的版本号，如果发生改变onUpgrade方法会被调用 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {//是在数据库每一次被创建的时候调用的
		db.execSQL("create table message (id varchar(200),messagecontent varchar(200))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("alter table message add keyid integer");
	}

}
