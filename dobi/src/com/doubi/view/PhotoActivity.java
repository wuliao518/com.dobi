package com.doubi.view;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.exception.ExitAppUtils;
import com.doubi.logic.ImageManager;
import com.doubi.logic.PhotoManager;
import com.umeng.analytics.MobclickAgent;

/**
 * Android��ָ����
 * 
 */
public class PhotoActivity extends Activity {
	private PhotoManager mPhotoManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		// ��ʾ����
		setContentView(R.layout.activity_photo);
		// this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		mPhotoManager = new PhotoManager();
		/** Ӳ����� **/
		if (mPhotoManager.CheckCameraHardware(this) == false) {
			CommonMethod.ShowMyToast(PhotoActivity.this,
					this.getString(R.string.sorry));
			return;
		}

		SurfaceView surfaceView = (SurfaceView) this
				.findViewById(R.id.surfaceView);
		surfaceView.getHolder().setFixedSize(176, 144); // ����Surface�ֱ���
		surfaceView.getHolder().setKeepScreenOn(true);// ��Ļ����
		surfaceView.getHolder().addCallback(
				mPhotoManager.getSurfaceCallback(PhotoActivity.this));// ΪSurfaceView�ľ�����һ���ص�����
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
		mPhotoManager.releaseCamera();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("myLog", "ִ����onDestroy()");
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}

	/**
	 * ��ť������������¼�
	 * 
	 * @param v
	 */
	public void btnOnclick(View v) {
		mPhotoManager.TakePicture();
	}

	/**
	 * ����ֻ���Ļ�ǣ���ʾ������ť
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		}
		return true;
	}

	/**
	 * �˵������ؼ���Ӧ
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		mPhotoManager.TakePictureByKey(keyCode, event);
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * ������ҳ
	 * 
	 * @param v
	 */
	public void btnMainPageOnclick(View v) {
		PhotoActivity.this.finish();
		Intent intent = new Intent(PhotoActivity.this, HomeActivity.class);
		startActivity(intent);
	}

	public void btnChangeOnclick(View v) {
		mPhotoManager.changeCamera();
	}

	/**
	 * ���������ҳ
	 * 
	 */

	// public Bitmap convertToBitmap(String path, int w, int h) {
	// BitmapFactory.Options opts = new BitmapFactory.Options();
	// // ����Ϊtureֻ��ȡͼƬ��С
	// opts.inJustDecodeBounds = true;
	// opts.inPreferredConfig = ConstValue.MY_CONFIG;
	// // ����Ϊ��
	// BitmapFactory.decodeFile(path, opts);
	// int width = opts.outWidth;
	// int height = opts.outHeight;
	// float scaleWidth = 0.f, scaleHeight = 0.f;
	// if (width > w || height > h) {
	// // ����
	// scaleWidth = ((float) width) / w;
	// scaleHeight = ((float) height) / h;
	// }
	// opts.inJustDecodeBounds = false;
	// float scale = Math.max(scaleWidth, scaleHeight);
	// opts.inSampleSize = (int)scale;
	// WeakReference<Bitmap> weak = new
	// WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
	// return Bitmap.createScaledBitmap(weak.get(), w, h, true);
	// }
	// public static String[] ListFile() {
	//
	// File file = new File("/sdcard/DCIM/Camera");
	// File[] f = file.listFiles();
	// String Path[] = new String[f.length];
	//
	// for (int i = 0; i < f.length; i++)
	//
	// {
	//
	// Path[i] = f[i].getPath();
	// }
	//
	// return Path;
	//
	// }

	public void btnSelectOnclick(View v) {

		Intent intent = new Intent();
		/* ����Pictures����Type�趨Ϊimage */
		intent.setType("image/*");
		/* ʹ��Intent.ACTION_GET_CONTENT���Action */
		intent.setAction(Intent.ACTION_GET_CONTENT);
		/* ȡ����Ƭ�󷵻ر����� */
		startActivityForResult(intent, 1);
	}

	/**
	 * ѡ��ͼƬ
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
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
				float width=display.getWidth()*1.5f;
				float height=display.getHeight()*1.5f;
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
					mImageManager
							.saveToSDCard(bitmap, ConstValue.ImgName.photo);
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
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}
