package com.example.myinformation;

public class MessageInfo {

	private String id;
	private String name;
	private String messageContent;
	private String time;
	private String phoneNum;
	private String type;// 收发类型 1表示接受 2表示发
	
	public MessageInfo(String id,String messageContent){
		this.id=id;
		this.messageContent=messageContent;
	}
	
	public MessageInfo(){}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
