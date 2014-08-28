package com.example.myinformation;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.example.util.AndroidUtil;



import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
public class InformationActivity extends Activity {

	private static Toast mToast;
	private static Context mContext;
	
	List<MessageInfo> mis=null;
	ListView listView=null;
	Set<Integer> selectedSet =null;
	private ProgressDialog progressDialog = null;
	private MessageDBService dbService=null;
	private GetMessageInfo messageService=null;
	private Set<CheckBox> listCheckBox=null ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.information_main);
		listView = (ListView) this.findViewById(R.id.informationlistview);
		initgg();
		mContext=this;
		messageService=new GetMessageInfo(this);
		progressDialog=ProgressDialog.show(this, "短信列表显示", "加载中，请稍后……");
		//加载进度
		new Thread(new Runnable(){
             @Override  
             public void run() {
            	 mis = messageService.getMessageList();
                 handler.sendEmptyMessage(0);// 执行耗时的方法之后发送消给handler  
             }  

         }).start();
		//创建操作数据库的实例
		new Thread(new Runnable(){
			@Override
			public void run() {
				dbService=new MessageDBService(getApplicationContext());
			}
			
		}).start();
		
		
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ListView view = (ListView) arg0;
				MessageInfo info=(MessageInfo)view.getItemAtPosition(arg2);
				for(CheckBox box:listCheckBox){
					MessageInfo mi=(MessageInfo)box.getTag();
					if(mi.getId().equals(info.getId())){
						if(box.isChecked()){//取消选中
							box.setChecked(false);
							selectedSet.remove(new Integer(arg2));
						}else{//选中
							box.setChecked(true);
							selectedSet.add(arg2);
						}
						break;
					}
				}
				
			}
		});
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// handler接收到消息后就会执行此方法 
			listView.setAdapter(new MyAdapter(getApplicationContext()));
            progressDialog.dismiss();// 关闭ProgressDialog  
        }
    };  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**获取string.xml资源*/  
    public String getResource(int id){  
        return getResources().getString(id);  
    }
    /**
     * 初始化广告条
     */
    public void initgg(){
    	LinearLayout ll=(LinearLayout)this.findViewById(R.id.gg);
    	AppConnect.getInstance(this).showBannerAd(this, ll);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.decode://触发加密菜单
			decodeMessage();
			break;
		case R.id.encode://解密成明文
			encodeMessage();
			break;
		case R.id.about:
			aboutSoft();
			break;
		case R.id.tuijian:
			tuijian();
			break;	
		case R.id.exit:
			AppConnect.getInstance(this).close();
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private String isShowGG() throws InterruptedException{
		String isWhiteUser=null;
		while(isWhiteUser==null){
			showToast("null");
			isWhiteUser=AppConnect.getInstance(this).getConfig("IS_WHITE_USER");
			Thread.sleep(2000);
		}
		showToast(isWhiteUser);
		return isWhiteUser;
	}
	
	/**
	 * 推荐列表
	 */
	private void tuijian() {
		AppConnect.getInstance(this).showOffers(this);
	}

	/**
	 * 加密短信内容
	 */
	private void decodeMessage(){
		
		if(checkBoxSize()==0){
			showToast("请选择要加密的短信");
			return;
		}
		
		if(dbService.getMessageDecodeCount(getids())>0){
			showToast("您选择的短信是已加过密的短信，无法二次加密");
			return;
		}
		for(CheckBox c:checkBoxList()){
			MessageInfo info=(MessageInfo)c.getTag();
			dbService.add(info);//把要加密短信的明文保存到数据库中
			messageService.updateMessage(info.getId(),AndroidUtil.getMD5Str(info.getMessageContent()));//修改短信内容
		}
		showToast("成功加密了"+checkBoxSize()+"条短信");
		getSMSWindow();
	}
	/**
	 * 跳转到系统短信界面
	 */
	private void getSMSWindow(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("vnd.android-dir/mms-sms");
		startActivity(intent);
	}
	
	/**
	 * 获取数据库中的短信内容
	 * @param id
	 * @return
	 */
	private List<MessageInfo> getInfos(){
		List<MessageInfo> infoList=dbService.getPerson(getids());
		return infoList;
	}
	/**
	 * 获取选中短信id的列表
	 * @return
	 */
	private String[] getids(){
		String ids[]=new String[checkBoxSize()];
		int count=0;
		for(CheckBox c:checkBoxList()){
			ids[count]=((MessageInfo)c.getTag()).getId();
			count++;
		}
		return ids;
	}
	
	/**
	 * 解密
	 */
	private void encodeMessage(){
		if(checkBoxSize()==0){
			showToast("请选择要解密的短信");
			return;
		}
		if(dbService.getMessageDecodeCount(getids())==0){
			showToast("您选择的短信不是加密短信，无法解密");
			return;
		}
		List<MessageInfo> messegeList=getInfos();
		for(MessageInfo msg:messegeList){
			messageService.updateMessage(msg.getId(),msg.getMessageContent());//修改短信内容
		}
		dbService.delete(getids());
		showToast("成功解密了"+checkBoxSize()+"条短信");
		getSMSWindow();
	}
	/**
	 * 选中的checkbox
	 * @return
	 */
	private int checkBoxSize(){
		int count=0;
		for(CheckBox box:listCheckBox){
			if(box.isChecked()){
				count++;
			}
		}
		return count;
	}
	/**
	 * 已经选中的checkbox列表
	 * @return
	 */
	private List<CheckBox> checkBoxList(){
		List<CheckBox> list=new ArrayList<CheckBox>();
		for(CheckBox box:listCheckBox){
			if(box.isChecked()){
				list.add(box);
			}
		}
		return list;
	}
	
	public void aboutSoft() {

		AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("关于")
				.setIcon(R.drawable.ic_menu_about).setMessage("该应用为短信加密的1.0版本，如要卸载请先把已加密的短信解密，否则加密后的短信将无法还原！")
				.setPositiveButton("确定", null).show();

//		Window alertWin = mDialog.getWindow();
//		WindowManager.LayoutParams lp = alertWin.getAttributes();
//		lp.alpha = 0.35f;
		// 设置对话框在屏幕的底部显示，当然还有上下左右，任意位置
		// mWindow.setGravity(Gravity.LEFT);
//		alertWin.setGravity(Gravity.BOTTOM);
		/*
		 * 
		 * 这里是设置偏移量，这里的x,y并不是相对于屏幕的绝对坐标，而是相对于对话框在中心位置(默认的对话框一般显示在屏幕的中心)而言的 lp.x
		 * = -20;// 设置水平偏移量 lp.y = -90;// 设置竖直偏移量
		 */
		// 设置Window的属性
//		alertWin.setAttributes(lp);
//		mDialog.show();
	}
	
	@SuppressLint("ShowToast")
	public static Toast getInstance(){
		if(mToast == null){
			mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
		}
		return mToast;
	}
	
	public void showToast(String str){
		Toast toast = getInstance();
		toast.setText(str);
		toast.show();
		
	}
	
	
	/**
	 * 展示listview适配器
	 * @author Administrator
	 *
	 */
	@SuppressWarnings("static-access")
	private class MyAdapter extends BaseAdapter{

		private LayoutInflater mInflater;// 动态布局映射,把xml转换成对象
		
		public MyAdapter(Context c){
			mInflater=getLayoutInflater().from(c);
			listCheckBox=new HashSet<CheckBox>(mis.size());
			selectedSet=new HashSet<Integer>();  //重点
		}
		
		@Override
		public int getCount() {
			return mis.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mis.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder=null;
			if(convertView==null){
				convertView = this.mInflater.inflate(R.layout.information_content, null);
				holder = new ViewHolder();
				holder.phoneNum=(TextView)convertView.findViewById(R.id.phonenum);
				holder.time=(TextView)convertView.findViewById(R.id.time);
				holder.messageContent=(TextView)convertView.findViewById(R.id.content);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder)convertView.getTag();
			}
			String content=mis.get(position).getMessageContent();
			holder.messageContent.setText(content.length()>15?content.substring(0, 15)+"...":content);
			holder.phoneNum.setText(mis.get(position).getPhoneNum());
			holder.time.setText(mis.get(position).getTime());
			final CheckBox ceb=(CheckBox)convertView.findViewById(R.id.check);
			MessageInfo infoObj=new MessageInfo(mis.get(position).getId(), mis.get(position).getMessageContent());
			ceb.setTag(infoObj);//为checkBox设值
			listCheckBox.add(ceb);
		    //重点开始
			    if (selectedSet.contains(position)) {
			    	ceb.setChecked(true);
			    } else {
			    	ceb.setChecked(false);
			    }
			ceb.setOnClickListener(new View.OnClickListener() {
			    @Override
			    public void onClick(View v) {
			    	 CheckBox cb = (CheckBox) v;
	                    if (cb.isChecked()) {
	                        selectedSet.add(position);
	                    } else {
	                        selectedSet.remove(new Integer(position));
	                    }
			    }
			});
			return convertView;
		}
	}
	
	static class ViewHolder {
		public TextView phoneNum;//电话号码
		public TextView time;//时间
		public TextView messageContent;//短信内容
	}
}
