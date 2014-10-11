package com.doubi.frist.activity_main;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.exception.ExitAppUtils;
import com.doubi.frist.date.NetManager;
import com.doubi.frist.date.SharedConfig;
import com.doubi.logic.Intelegence;
import com.doubi.logic.update.UpdateAPK;
import com.doubi.view.HomeActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

//�����ڣ��������档
public class MainActivity extends Activity {
	private boolean first; // �ж��Ƿ��һ�δ����
	private View view;
	private Context context;
	private Animation animation;
	private NetManager netManager;
	private SharedPreferences shared;
	private static int TIME = 1000; // ������������ӳ�ʱ��

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ��鲢�����ز�Ŀ¼
		(new Intelegence()).CheckAndCreatRoot();
		view = View.inflate(this, R.layout.activity_main, null);
		setContentView(view);
		context = this; // �õ�������
		// ��������
		PushAgent mPushAgent = PushAgent.getInstance(context);
		mPushAgent.enable();
		shared = new SharedConfig(context).GetConfig(); // �õ������ļ�
		netManager = new NetManager(context); // �õ����������

		
	}

	@Override
	protected void onResume() {
		super.onResume();
		UpdateAPK update = new UpdateAPK(MainActivity.this);
		update.checkUpdateInfo(MainActivity.this);
		// ����ͳ��
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// ����ͳ��
		MobclickAgent.onPause(this);
	}

	// ����������ķ���
	public void into(Boolean flag) {
		 if (netManager.isOpenNetwork()||netManager.isOpenWifi()||flag) {
		// �������������ж��Ƿ��һ�ν��룬����ǵ�һ������뻶ӭ����
		first = shared.getBoolean("First", true);
		Intent intent;
		// �����һ�Σ����������ҳWelcomeActivity
		if (first) {
			intent = new Intent(MainActivity.this,
					WelcomeActivity.class);
		} else {
			intent = new Intent(MainActivity.this,
					HomeActivity.class);
		}
		startActivity(intent);
		// ����Activity���л�Ч��
		overridePendingTransition(R.anim.in_from_right,
				R.anim.out_to_left);
		MainActivity.this.finish();
		 } else {
		 // ������粻���ã��򵯳��Ի��򣬶������������
//		 final Builder builder = new Builder(context);
//		 builder.setTitle("��Ҫ�����������زģ�");
//		 builder.setMessage("�Ƿ�������������?");
//		 builder.setCancelable(false);
//		 builder.setPositiveButton("ȷ��",
//		 new android.content.DialogInterface.OnClickListener() {
//		 @Override
//		 public void onClick(DialogInterface dialog, int which) {
//		 Intent intent = null;
//		 try {
//		 String sdkVersion = android.os.Build.VERSION.SDK;
//		 if (Integer.valueOf(sdkVersion) > 10) {
//		 intent = new Intent(
//		 android.provider.Settings.ACTION_WIRELESS_SETTINGS);
//		 } else {
//		 intent = new Intent();
//		 ComponentName comp = new ComponentName(
//		 "com.android.settings",
//		 "com.android.settings.WirelessSettings");
//		 intent.setComponent(comp);
//		 intent.setAction("android.intent.action.VIEW");
//		 }
//		 MainActivity.this.startActivity(intent);
//		 } catch (Exception e) {
//		 e.printStackTrace();
//		 }
//		 }
//		 });
//		 builder.setNegativeButton("ȡ��",
//		 new android.content.DialogInterface.OnClickListener() {
//		 @Override
//		 public void onClick(DialogInterface dialog, int which) {
//			 builder.create().dismiss();
//			 MainActivity.this.finish();
//		 }
//		 });
//		 builder.create().show();
//		 }
			final Dialog note;
			RelativeLayout relativeLayout;
			// ��Ⱦ���֣���ȡ��Ӧ�ؼ�
			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
			View view = inflater.inflate(R.layout.net_dialog, null);
			Button one=(Button)view.findViewById(R.id.button1);
			Button two=(Button)view.findViewById(R.id.button2);
			relativeLayout=(RelativeLayout) view.findViewById(R.id.rl_layout);
			// ��ȡprogress�ؼ��Ŀ��
			int height = (int) (CommonMethod.GetDensity(MainActivity.this)*160+0.5);
			int width = (int) (CommonMethod.GetDensity(MainActivity.this)*240+0.5);
			// �½�Dialog
			note = new Dialog(this, R.style.Translucent_NoTitle);
			// note.requestWindowFeature(Window.FEATURE_NO_TITLE);
			LayoutParams params = new LayoutParams(width, height);
			// ���öԻ����С�������ã�
			WindowManager.LayoutParams params1 = note.getWindow().getAttributes();
			params1.width = width;
			params1.height = height;
			params1.x = 0;
			params1.y = 0;
			note.getWindow().setAttributes(params1);
			note.addContentView(view, params);
			note.setCancelable(false);
			note.show();
			one.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					note.dismiss();
					MainActivity.this.finish();
				}
			});
			two.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					note.dismiss();
					Intent intent = null;
					 try {
					 String sdkVersion = android.os.Build.VERSION.SDK;
					 if (Integer.valueOf(sdkVersion) > 10) {
					 intent = new Intent(
					 android.provider.Settings.ACTION_WIRELESS_SETTINGS);
					 } else {
					 intent = new Intent();
					 ComponentName comp = new ComponentName(
					 "com.android.settings",
					 "com.android.settings.WirelessSettings");
					 intent.setComponent(comp);
					 intent.setAction("android.intent.action.VIEW");
					 }
					 MainActivity.this.startActivity(intent);
					 } catch (Exception e) {
					 e.printStackTrace();
					 }
				}
			});
		}

	}
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}
	
	/**
	 * �˵������ؼ���Ӧ
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return true;
		
	}
	
	
}
