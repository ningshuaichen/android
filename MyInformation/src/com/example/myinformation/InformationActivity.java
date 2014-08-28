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
		progressDialog=ProgressDialog.show(this, "�����б���ʾ", "�����У����Ժ󡭡�");
		//���ؽ���
		new Thread(new Runnable(){
             @Override  
             public void run() {
            	 mis = messageService.getMessageList();
                 handler.sendEmptyMessage(0);// ִ�к�ʱ�ķ���֮��������handler  
             }  

         }).start();
		//�����������ݿ��ʵ��
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
						if(box.isChecked()){//ȡ��ѡ��
							box.setChecked(false);
							selectedSet.remove(new Integer(arg2));
						}else{//ѡ��
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
		public void handleMessage(Message msg) {// handler���յ���Ϣ��ͻ�ִ�д˷��� 
			listView.setAdapter(new MyAdapter(getApplicationContext()));
            progressDialog.dismiss();// �ر�ProgressDialog  
        }
    };  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**��ȡstring.xml��Դ*/  
    public String getResource(int id){  
        return getResources().getString(id);  
    }
    /**
     * ��ʼ�������
     */
    public void initgg(){
    	LinearLayout ll=(LinearLayout)this.findViewById(R.id.gg);
    	AppConnect.getInstance(this).showBannerAd(this, ll);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.decode://�������ܲ˵�
			decodeMessage();
			break;
		case R.id.encode://���ܳ�����
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
	 * �Ƽ��б�
	 */
	private void tuijian() {
		AppConnect.getInstance(this).showOffers(this);
	}

	/**
	 * ���ܶ�������
	 */
	private void decodeMessage(){
		
		if(checkBoxSize()==0){
			showToast("��ѡ��Ҫ���ܵĶ���");
			return;
		}
		
		if(dbService.getMessageDecodeCount(getids())>0){
			showToast("��ѡ��Ķ������Ѽӹ��ܵĶ��ţ��޷����μ���");
			return;
		}
		for(CheckBox c:checkBoxList()){
			MessageInfo info=(MessageInfo)c.getTag();
			dbService.add(info);//��Ҫ���ܶ��ŵ����ı��浽���ݿ���
			messageService.updateMessage(info.getId(),AndroidUtil.getMD5Str(info.getMessageContent()));//�޸Ķ�������
		}
		showToast("�ɹ�������"+checkBoxSize()+"������");
		getSMSWindow();
	}
	/**
	 * ��ת��ϵͳ���Ž���
	 */
	private void getSMSWindow(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("vnd.android-dir/mms-sms");
		startActivity(intent);
	}
	
	/**
	 * ��ȡ���ݿ��еĶ�������
	 * @param id
	 * @return
	 */
	private List<MessageInfo> getInfos(){
		List<MessageInfo> infoList=dbService.getPerson(getids());
		return infoList;
	}
	/**
	 * ��ȡѡ�ж���id���б�
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
	 * ����
	 */
	private void encodeMessage(){
		if(checkBoxSize()==0){
			showToast("��ѡ��Ҫ���ܵĶ���");
			return;
		}
		if(dbService.getMessageDecodeCount(getids())==0){
			showToast("��ѡ��Ķ��Ų��Ǽ��ܶ��ţ��޷�����");
			return;
		}
		List<MessageInfo> messegeList=getInfos();
		for(MessageInfo msg:messegeList){
			messageService.updateMessage(msg.getId(),msg.getMessageContent());//�޸Ķ�������
		}
		dbService.delete(getids());
		showToast("�ɹ�������"+checkBoxSize()+"������");
		getSMSWindow();
	}
	/**
	 * ѡ�е�checkbox
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
	 * �Ѿ�ѡ�е�checkbox�б�
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

		AlertDialog mDialog = new AlertDialog.Builder(this).setTitle("����")
				.setIcon(R.drawable.ic_menu_about).setMessage("��Ӧ��Ϊ���ż��ܵ�1.0�汾����Ҫж�����Ȱ��Ѽ��ܵĶ��Ž��ܣ�������ܺ�Ķ��Ž��޷���ԭ��")
				.setPositiveButton("ȷ��", null).show();

//		Window alertWin = mDialog.getWindow();
//		WindowManager.LayoutParams lp = alertWin.getAttributes();
//		lp.alpha = 0.35f;
		// ���öԻ�������Ļ�ĵײ���ʾ����Ȼ�����������ң�����λ��
		// mWindow.setGravity(Gravity.LEFT);
//		alertWin.setGravity(Gravity.BOTTOM);
		/*
		 * 
		 * ����������ƫ�����������x,y�������������Ļ�ľ������꣬��������ڶԻ���������λ��(Ĭ�ϵĶԻ���һ����ʾ����Ļ������)���Ե� lp.x
		 * = -20;// ����ˮƽƫ���� lp.y = -90;// ������ֱƫ����
		 */
		// ����Window������
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
	 * չʾlistview������
	 * @author Administrator
	 *
	 */
	@SuppressWarnings("static-access")
	private class MyAdapter extends BaseAdapter{

		private LayoutInflater mInflater;// ��̬����ӳ��,��xmlת���ɶ���
		
		public MyAdapter(Context c){
			mInflater=getLayoutInflater().from(c);
			listCheckBox=new HashSet<CheckBox>(mis.size());
			selectedSet=new HashSet<Integer>();  //�ص�
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
			ceb.setTag(infoObj);//ΪcheckBox��ֵ
			listCheckBox.add(ceb);
		    //�ص㿪ʼ
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
		public TextView phoneNum;//�绰����
		public TextView time;//ʱ��
		public TextView messageContent;//��������
	}
}
