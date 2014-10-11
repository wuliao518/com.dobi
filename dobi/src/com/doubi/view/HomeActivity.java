package com.doubi.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.exception.ExitAppUtils;
import com.doubi.frist.activity_main.DuxiActivity;
import com.doubi.frist.activity_main.FamilyActivity;
import com.doubi.frist.activity_main.MaliangActivity;
import com.doubi.logic.ImageManager;
import com.doubi.logic.drawView.MoreDrawViewBase;
import com.doubi.logic.drawView.SingleDrawViewBase;
import com.umeng.analytics.MobclickAgent;

public class HomeActivity extends Activity {
	private ImageView btnduxi, btnfamily, btnmaliang;
	private ImageManager mImageManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		setContentView(R.layout.activity_home);
		mImageManager=new ImageManager();
		btnduxi = (ImageView) findViewById(R.id.btnduxi);
		btnfamily = (ImageView) findViewById(R.id.btnfamily);
		btnmaliang = (ImageView) findViewById(R.id.btnmaliang);
		View.OnClickListener vivo = new OnClickListener(){
			public void onClick(View v) {
				Intent intent = new Intent();
				// �ҵİ��ݺͶ��˰��ݰ�ť
				switch (v.getId()) {
				case R.id.btnduxi:
					intent.setClass(HomeActivity.this, DuxiActivity.class);
					startActivity(intent);
				
					// HomeActivity.this.finish();
					break;
				case R.id.btnfamily:
					intent.setClass(HomeActivity.this, FamilyActivity.class);
					startActivity(intent);
				
					break;
				case R.id.btnmaliang:
					intent.setClass(HomeActivity.this, MaliangActivity.class);
					startActivity(intent);
				
					break;
				default:
					break;
				}
			}
		};
		btnduxi.setOnClickListener(vivo);
		btnfamily.setOnClickListener(vivo);
		btnmaliang.setOnClickListener(vivo);

		// ��ʼ��Ĭ��ͷ�Ρ��·�
//		UpdateAPK update = new UpdateAPK(this);
//		update.checkUpdateInfo();
		// ��ʽ�����´���ȡ��
		// Intent intent = new Intent(this, MainActivity.class);
		// this.startActivity(intent);
	}

	@Override
	protected void onStart() {
		SingleDrawViewBase.clearBuffer();
		MoreDrawViewBase.clearBuffer();
		/*
		 * if(SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Body))
		 * if(SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Prop))
		 * if(SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Face))
		 * if(SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Hair))
		 * if(SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Scene))
		 */
		MoreDrawViewBase.CurrentStage = ConstValue.Stage.None;
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		// ����ͳ��
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// ����ͳ��
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * ���˰���
	 * 
	 * @param v
	 */
	public void ImgBtnSingleOnclick(View v) {
		CommonMethod.SetSingleOrMore(0);
		// ���û��ͷ���ʹ�ã���Ҫ��ͷ��
		if (!mImageManager.loadImg()) {
			Dialog note;
			RelativeLayout relativeLayout;
			// ��Ⱦ���֣���ȡ��Ӧ�ؼ�
			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
			View view = inflater.inflate(R.layout.window_pop, null);
			ImageButton one=(ImageButton)view.findViewById(R.id.xiangji);
			ImageButton two=(ImageButton)view.findViewById(R.id.xiangce);
			relativeLayout=(RelativeLayout) view.findViewById(R.id.rl_layout);
			// ��ȡprogress�ؼ��Ŀ��
			int height = (int) (CommonMethod.GetDensity(HomeActivity.this)*180+0.5);
			int width = (int) (CommonMethod.GetDensity(HomeActivity.this)*200+0.5);
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
			note.show();
			one.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					  Uri mOutPutFileUri;
					  //�ļ���doubi
					  String path = Environment.getExternalStorageDirectory().toString()+"/doubi";
					  File path1 = new File(path);
					  if(!path1.exists()){
					   path1.mkdirs();
					  }
					  File file = new File(path1,"photo"+"jpg");
					  mOutPutFileUri = Uri.fromFile(file);
					  intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
					  startActivityForResult(intent, 0);
				}
			});
			two.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					/* ����Pictures����Type�趨Ϊimage */
					intent.setType("image/*");
					/* ʹ��Intent.ACTION_GET_CONTENT���Action */
					intent.setAction(Intent.ACTION_GET_CONTENT);
					/* ȡ����Ƭ�󷵻ر����� */
					startActivityForResult(intent, 1);
				}
			});


		} else {
			Intent intent = new Intent(this, SingleActivity.class);
			this.startActivity(intent);
		}
		
	}

	/**
	 * ����
	 * 
	 * @param v
	 */
	public void ImgBtnMoreOnclick(View v) {
		 //������ʾ
		 final ImageView img = (ImageView)
		 this.findViewById(R.id.imgvWaiting);
		 img.setVisibility(View.VISIBLE);
		
		 Handler mHandler = new Handler();
		 mHandler.postDelayed(new Runnable() {
		 public void run() {
		 img.setVisibility(View.INVISIBLE);
		 }
		 }, 2000);
		//���뻻��
		//CommonMethod.SetSingleOrMore(1);
		// ���û��ͷ���ʹ�ã���Ҫ��ͷ��
//		if (!(new ImageManager()).loadImgForMore()) {
//			
//			Dialog note;
//			RelativeLayout relativeLayout;
//			// ��Ⱦ���֣���ȡ��Ӧ�ؼ�
//			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//			View view = inflater.inflate(R.layout.window_pop, null);
//			ImageButton one=(ImageButton)view.findViewById(R.id.xiangji);
//			ImageButton two=(ImageButton)view.findViewById(R.id.xiangce);
//			relativeLayout=(RelativeLayout) view.findViewById(R.id.rl_layout);
//			// ��ȡprogress�ؼ��Ŀ��
//			int height = (int) (CommonMethod.GetDensity(HomeActivity.this)*180+0.5);
//			int width = (int) (CommonMethod.GetDensity(HomeActivity.this)*200+0.5);
//			// �½�Dialog
//			note = new Dialog(this, R.style.Translucent_NoTitle);
//			// note.requestWindowFeature(Window.FEATURE_NO_TITLE);
//			LayoutParams params = new LayoutParams(width, height);
//			// ���öԻ����С�������ã�
//			WindowManager.LayoutParams params1 = note.getWindow().getAttributes();
//			params1.width = width;
//			params1.height = height;
//			params1.x = 0;
//			params1.y = 0;
//			note.getWindow().setAttributes(params1);
//			note.addContentView(view, params);
//			note.show();
//			one.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//					  Uri mOutPutFileUri;
//					  //�ļ���doubi
//					  String path = Environment.getExternalStorageDirectory().toString()+"/doubi/moerClipFace/";
//					  File path1 = new File(path);
//					  if(!path1.exists()){
//					   path1.mkdirs();
//					  }
//					  File file = new File(path1,"photo"+"jpg");
//					  mOutPutFileUri = Uri.fromFile(file);
//					  intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
//					  startActivityForResult(intent, 0);
//				}
//			});
//			two.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent();
//					/* ����Pictures����Type�趨Ϊimage */
//					intent.setType("image/*");
//					/* ʹ��Intent.ACTION_GET_CONTENT���Action */
//					intent.setAction(Intent.ACTION_GET_CONTENT);
//					/* ȡ����Ƭ�󷵻ر����� */
//					startActivityForResult(intent, 1);
//				}
//			});
//			
//			
//			
//			
//		}else{
//			Intent intent=new Intent(HomeActivity.this, MoreActivity.class);
//			startActivity(intent);
//		}
		
	}

	/**
	 * �˵������ؼ���Ӧ
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click(); // ����˫���˳�����
		}
		return false;
	}
	/**
	 * ˫���˳�����
	 */
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // ׼���˳�
			CommonMethod.ShowMyToast(HomeActivity.this,
					this.getString(R.string.drop));
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // ȡ���˳�
				}
			}, 3000); // ���3������û�а��·��ؼ�����������ʱ��ȡ�����ղ�ִ�е�����

		} else {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
			System.exit(0);
		}
	}
	
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==Activity.RESULT_CANCELED){
			
		}else{
			switch (requestCode) {  
	         case 0:  
	        	 Intent intent1 = new Intent(this, ShowPicActivity.class);
	        	 intent1.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
	        			 ConstValue.ImgSourceType.front.toString());
				 this.startActivity(intent1);
	             break;  
	         case 1:  
	        	Uri uri = data.getData();
	 			ContentResolver cr = this.getContentResolver();
	 			try {
	 				BitmapFactory.Options options=new BitmapFactory.Options();
	 				options.inJustDecodeBounds=true;
	 				Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri),null,options);
	 				int scale=1;
	 				float bitWidth=options.outWidth;
	 				float bitHeight=options.outHeight;
	 				WindowManager wm=(WindowManager) getSystemService("window");
	 				Display display=wm.getDefaultDisplay();
	 				float width=display.getWidth()*1.0f;
	 				float height=display.getHeight()*1.0f;
	 				float scaleX=(float)bitWidth/width;
	 				float scaleY=(float)bitHeight/height;
	 				scale=(int) Math.max(scaleX,scaleY);
	 				if(scale>1){
	 					options.inJustDecodeBounds=false;
	 					options.inSampleSize=scale;
	 					bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri),null,options);
	 				}else{
	 					options.inJustDecodeBounds=false;
	 					options.inSampleSize=1;
	 					bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri),null,options);
	 				}
	 				
	 				ImageManager mImageManager = new ImageManager();
	 				try {
	 					if(CommonMethod.GetSingleOrMore()!=0){
	 						mImageManager.saveToSDCard(ConstValue.MORE_CLIP_FACE, bitmap, "photo", CompressFormat.JPEG);
	 					}else{
	 						mImageManager
 							.saveToSDCard(bitmap, ConstValue.ImgName.photo);
	 					}
	 				} catch (IOException e) {
	 					bitmap.recycle();
	 					e.printStackTrace();
	 				}

	 				Intent intent = new Intent(this, ShowPicActivity.class);
	 				intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
	 						ConstValue.ImgSourceType.select.toString());
	 				this.startActivity(intent);
	 				bitmap.recycle();

	 			} catch (FileNotFoundException e) {

	 			}
	            break;  
		}  
		}  
	}
	
	
	

}
