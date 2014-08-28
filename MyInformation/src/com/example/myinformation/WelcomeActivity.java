package com.example.myinformation;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class WelcomeActivity extends Activity {

	private static long STRAT_TIME=3000;
	
	private static int STATE_WHAT=1000;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		init();
	}
	
	Handler handler =new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==1000){
				goGuide();
			}
			super.handleMessage(msg);
		}
	};
	
	private void init(){
		handler.sendEmptyMessageDelayed(STATE_WHAT, STRAT_TIME);
	}
	
	public void goGuide(){
		Intent intent=new Intent(this,InformationActivity.class);
		this.startActivity(intent);
		this.finish();
	}
	
}
