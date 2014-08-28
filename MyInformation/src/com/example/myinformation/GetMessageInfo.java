package com.example.myinformation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;

@SuppressLint("SimpleDateFormat")
public class GetMessageInfo {
	List<MessageInfo> list;
	private Context c;

	public GetMessageInfo(Context c) {
		list = new ArrayList<MessageInfo>();
		this.c = c;
	}

	public List<MessageInfo> getMessageList() {
		final String SMS_URI_INBOX = "content://sms/";// ���ж���
		MessageInfo messageInfo = null;
		ContentResolver cr = c.getContentResolver();
		String[] projection = new String[] { "_id", "address", "person",
				"body", "date", "type" };
		Uri uri = Uri.parse(SMS_URI_INBOX);
		Cursor cur = cr.query(uri, projection, null, null, "date desc");
		if (cur != null) {
			while (cur.moveToNext()) {
				int id = cur.getColumnIndex("_id");// ��ϵ�������б����
				int phoneNumberColumn = cur.getColumnIndex("address");// �ֻ���
				int smsbodyColumn = cur.getColumnIndex("body");// ��������
				int dateColumn = cur.getColumnIndex("date");// ����
				int typeColumn = cur.getColumnIndex("type");// �շ����� 1��ʾ���� 2��ʾ��

				messageInfo = new MessageInfo();
				// -----------------------��Ϣ----------------
				String messageId = cur.getString(id);
				String phoneNumber = cur.getString(phoneNumberColumn);
				String smsbody = cur.getString(smsbodyColumn);
				Date d = new Date(Long.parseLong(cur.getString(dateColumn)));
				String phoneNum=getPeopleNameFromPerson(phoneNumber);
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						phoneNum.length()<=18?"yyyy-MM-dd hh:mm:ss":"yyyy-MM-dd");
				String date = dateFormat.format(d);
				String type = cur.getColumnName(typeColumn);
				
				messageInfo.setId(messageId);
				messageInfo.setMessageContent(smsbody);
				messageInfo.setTime(date);
				messageInfo.setPhoneNum(phoneNum);
				messageInfo.setType(type.equals("1") ? "����" : "����");
				list.add(messageInfo);
			}
		}
		if (!cur.isClosed()) {
			cur.close();
		}
		return list;
	}

	public String getPeopleNameFromPerson(String address) {
		if (address == null || address == "") {
			return "";
		}
		String strPerson = null;
		if(address.trim().startsWith("+")){
			address=address.substring(3);
		}
		Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/"+address);
		ContentResolver resolver = c.getContentResolver();
		Cursor cursor = resolver.query(uri, new String[]{Data.DISPLAY_NAME}, null, null, null);
		if (cursor.moveToFirst()) {
			strPerson=cursor.getString(0);
			cursor.close();
		}
		return strPerson == null || "".equals(strPerson) ? address : strPerson;
	}
	
	
	/**
	 * ���¶�������
	 * @param id
	 * @param content
	 */
	public void updateMessage(String id,String messagecontent) {
		ContentValues cv = new ContentValues(); // ����޸���Ϣ
		cv.put("body", messagecontent);// ��body��ֵ���м���
		c.getContentResolver().update(Uri.parse("content://sms/inbox"), cv,
				"_id=?", new String[] { id });
	}
}
