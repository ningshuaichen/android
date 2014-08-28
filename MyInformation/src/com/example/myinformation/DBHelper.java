package com.example.myinformation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * ���ݿ⸨����
 * @author Administrator
 *
 */
public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context contex) {
		super(contex,"ningshuaichen.db",null,3);//���һ�����������ݿ�İ汾�ţ���������ı�onUpgrade�����ᱻ���� 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {//�������ݿ�ÿһ�α�������ʱ����õ�
		db.execSQL("create table message (id varchar(200),messagecontent varchar(200))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("alter table message add keyid integer");
	}

}
