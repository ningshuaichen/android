package com.example.myinformation;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MessageDBService {

	private DBHelper db;
	
	public MessageDBService(Context c){
		this.db=new DBHelper(c);
	}
	/**
	 * ��Ӷ�����Ϣ�����ݿ�
	 * @param info
	 */
	public void add(MessageInfo info){
		SQLiteDatabase database = db.getWritableDatabase();
		database.execSQL("insert into message (id,messagecontent) values(?,?)", new Object[]{info.getId(),info.getMessageContent()});
		database.close();
	}
	/**
	 * ɾ������
	 * @param id
	 */
	public void delete(String ...ids) {

		SQLiteDatabase database = db.getWritableDatabase();
		database.execSQL("delete from message where id in("+getinSql(ids)+")",
				ids);
		database.close();
	}
	/**
	 * ��ȡ��������
	 * @param id
	 * @return
	 */
	public List<MessageInfo> getPerson(String ...ids) {
		List<MessageInfo> infoList=new ArrayList<MessageInfo>(ids.length);
		SQLiteDatabase database = db.getReadableDatabase();
		Cursor c = database.rawQuery("select * from message where id in("+getinSql(ids)+")",
				ids);
		while(c.moveToNext()){
			String id=c.getString(c.getColumnIndex("id"));
			String messagecontent = c.getString(c.getColumnIndex("messagecontent"));
			infoList.add(new MessageInfo(id, messagecontent));
		}
		database.close();
		return infoList;
	}
	/**
	 * �ж��Ƿ��Ѿ��ӹ���
	 * @param ids
	 * @return
	 */
	public long getMessageDecodeCount(String ...ids){
		
		SQLiteDatabase database = db.getReadableDatabase();
		Cursor c = database.rawQuery("select count(*) from message where id in("+getinSql(ids)+")",
				ids);
		c.moveToFirst();
		long count=c.getLong(0);
		database.close();
		return count;
	}
	
	/**
	 * ������ѯ����
	 * @param ids
	 * @return
	 */
	private String getinSql(String ...ids){
		StringBuilder su=new StringBuilder();
		for(String id:ids){
			su.append("?").append(",");
		}
		su.deleteCharAt(su.length()-1);
		return su.toString();
	}
}
