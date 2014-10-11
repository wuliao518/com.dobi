package com.doubi.logic;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.logic.update.MyDialog;
import com.doubi.view.ShowPicActivity;

/**
 * ���������
 * 
 * @author Administrator
 *
 */
public class PhotoManager implements OnClickListener {
	private ImageButton btnSelectOne, btnSelectTwo, btnSelectThree,
			btnSelectFour;

	/**
	 * ����Ԥ��ͼƬ�ķ���0Ϊ�ֻ�Ĭ�Ϸ���1Ϊ��ת90�ȣ�2Ϊ��ת180�ȣ�3Ϊ��ת270��
	 */
	private int flagImageDirection;
	/**
	 * ǰ����������޽�����־��
	 */
	private boolean flagBack;
	private boolean flagFront;
	// ��Ƭ��������dialog
	private MyDialog dialog;

	// ��ʱִ���Զ����ս�������
	private Handler handler;

	private CameraInfo cameraInfo;
	private Camera camera;
	/**
	 * ���յ�activity
	 */
	private Activity photoActivity;

	/**
	 * 0����ոմ�ǰ������ͷ��1����ոմ򿪺�������ͷ
	 */
	private int mCameraId;

	private SurfaceHolder mSurfaceHolder;

	ImageManager mImageManager;

	/**
	 * ����
	 */
	@SuppressLint("InflateParams")
	public PhotoManager() {
		flagImageDirection = 90;
		flagBack = false;
		flagFront = false;
		mCameraId = 1;
		mImageManager = new ImageManager();
	}

	/**
	 * ����豸�Ƿ�֧������ͷ
	 */
	public boolean CheckCameraHardware(Context mContext) {

		if (mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// ����ͷ����
			return true;
		} else {
			// ����ͷ������
			return false;
		}
	}

	// �ṩһ����̬���������ڸ����ֻ����������Ԥ��������ת�ĽǶ�
	public int getPreviewDegreeForPhoto(Activity activity) {
		// ����ֻ��ķ���
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		// �����ֻ��ķ���������Ԥ������Ӧ��ѡ��ĽǶ�
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}

	// �ṩһ����̬���������ڸ����ֻ����������Ԥ��������ת�ĽǶ�
	public int getPreviewDegreeForShow(Activity activity, String photoType) {
		// ����ֻ��ķ���
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		if (photoType.equals(ConstValue.ImgSourceType.front.toString())) {
			// �����ֻ��ķ���������Ԥ������Ӧ��ѡ��ĽǶ�
			switch (rotation) {
			case Surface.ROTATION_0:
				degree = 270;
				break;
			case Surface.ROTATION_90:
				degree = 180;
				break;
			case Surface.ROTATION_180:
				degree = 90;
				break;
			case Surface.ROTATION_270:
				degree = 0;
				break;
			}
		} else if (photoType.endsWith(ConstValue.ImgSourceType.back.toString())) {
			// �����ֻ��ķ���������Ԥ������Ӧ��ѡ��ĽǶ�
			switch (rotation) {
			case Surface.ROTATION_0:
				degree = 90;
				break;
			case Surface.ROTATION_90:
				degree = 0;
				break;
			case Surface.ROTATION_180:
				degree = 270;
				break;
			case Surface.ROTATION_270:
				degree = 180;
				break;
			}
		}
		return degree;
	}

	/**
	 * ����
	 */
	public void TakePicture() {
		if (camera != null) {
			camera.takePicture(null, null, new MyPictureCallback());
			// setCarmeraDisplayOrientation(photoActivity, 1, camera);
		}
	}

	/**
	 * Ӳ�����o����
	 */
	public void TakePictureByKey(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA: // �������հ�ť
			if (camera != null && event.getRepeatCount() == 0) {
				// ����
				// ע������takePicture()�������������Ǵ�����һ��PictureCallback���󡪡��������ȡ���������õ�ͼƬ����֮��
				// ��PictureCallback���󽫻ᱻ�ص����ö�����Ը������Ƭ���б����������
				camera.takePicture(null, null, getMyPictureCallback());
				// setCarmeraDisplayOrientation(photoActivity, 1, camera);
			}
		}
	}

	/**
	 * ��ȡSurfaceCallback
	 * 
	 * @param activity
	 * @param outCamera
	 * @return
	 */
	public SurfaceCallback getSurfaceCallback(Activity activity) {
		SurfaceCallback mSurfaceCallback = new SurfaceCallback(activity);
		return mSurfaceCallback;
	}

	/**
	 * 
	 * @return
	 */
	public MyPictureCallback getMyPictureCallback() {
		return new MyPictureCallback();
	}

	/**
	 * SurfaceView�ؼ�ʹ�õĻص�
	 * 
	 * @author Administrator
	 *
	 */
	@SuppressLint("InflateParams")
	private final class SurfaceCallback implements Callback {
		private Camera.Parameters parameters = null;

		public SurfaceCallback(Activity activity) {
			photoActivity = activity;
			// ѡ���ʱ��ʵ����dialog
			if (dialog == null) {
				LayoutInflater inflater = LayoutInflater.from(photoActivity);
				View view = inflater.inflate(R.layout.dialog_camera, null);
				dialog = new MyDialog(photoActivity, view, R.style.Self_Dialog);
				dialog.setCancelable(false);
			}
		}

		// ����״̬�仯ʱ���ø÷���
		@SuppressLint({ "NewApi", "InflateParams" })
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if(camera==null){
				camera=Camera.open(0);
			}
			parameters = camera.getParameters(); // ��ȡ�������
			parameters.setPictureFormat(ImageFormat.JPEG); // ����ͼƬ��ʽ
			parameters.setPreviewSize(width, height); // ����Ԥ����С
			
			parameters.setPreviewFpsRange(4, 4); // ����ÿ����ʾ4֡
			parameters.setPictureSize(width, height); // ���ñ����ͼƬ�ߴ�
			parameters.setJpegQuality(100); // ������Ƭ����
			// parameters.set("autofocus", true); // �Զ��Խ�
			//parameters.setFocusMode("auto");
		}

		// ��ʼ����ʱ���ø÷���
		@SuppressLint("NewApi")
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mSurfaceHolder = holder;
				openCamera();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		// ֹͣ����ʱ���ø÷���
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			releaseCamera();
		}
	}

	private final class MyPictureCallback implements PictureCallback {

		@SuppressLint({ "InflateParams", "NewApi" })
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			if (flagFront || flagBack) {// ����ǵ�һ�δ򿪣�����dialog����ͼƬ����
				//new processImageTask().execute(data);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					CommonMethod.SetSharepreferenceValue(photoActivity,
							ConstValue.SharepreferenceKey.CameraBackDegree,
							flagImageDirection);
					flagBack = false;
				} else {
					CommonMethod
							.SetSharepreferenceValue(
									photoActivity,
									ConstValue.SharepreferenceKey.CameraFrontDegree,
									flagImageDirection);
					flagFront = false;
				}
				if(PhotoManager.this.camera==null){
					openCamera();
				}
				
				PhotoManager.this.camera.startPreview();
				
			} else {
				// ����ͼƬ��sd����
				try {
					mImageManager.saveToSDCard(data);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				photoActivity.finish();
				Intent intent = new Intent(photoActivity,ShowPicActivity.class);
				if (mCameraId == 0) { // ˵���ոմ���ǰ������ͷ���´δ򿪺�������ͷ
					intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
							ConstValue.ImgSourceType.front.toString());
				} else {
					intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
							ConstValue.ImgSourceType.back.toString());
				}
				photoActivity.startActivity(intent);
				
			}

			/************************************ ����ͼ��У׼ **********************************/
			/****************************** ����showPicture���� *******************************/
			/*
			 * mImageManager.saveToSDCard(bitmap, ConstValue.ImgName.photo);
			 * 
			 * photoActivity.finish(); Intent intent = new Intent(photoActivity,
			 * ShowPicActivity.class);
			 * 
			 * if (mCameraId == 0) { // ˵���ոմ���ǰ������ͷ���´δ򿪺�������ͷ
			 * intent.putExtra(ConstValue.ExtruaKey.PHOTO_TYPE,
			 * ConstValue.ImgSourceType.front.toString()); } else {
			 * intent.putExtra(ConstValue.ExtruaKey.PHOTO_TYPE,
			 * ConstValue.ImgSourceType.back.toString()); }
			 * photoActivity.startActivity(intent);
			 */
			/****************************** ����showPicture���� *******************************/

		}
	}

	/**
	 * �첽����ѡ���   ѡ����
	 */
	private class processImageTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@SuppressWarnings("deprecation")
		@SuppressLint({ "NewApi", "InflateParams" })
		public Void doInBackground(byte[]... params) {
			// ��Ƭ���������ͼƬ����ͼ
			Bitmap bitmap;
			// Bitmap bitmapI;
			bitmap = mImageManager.BytesToBimap(params[0],photoActivity);
			/************************************ ����ͼ��У׼ **********************************/
			// ��С�ߴ�

			bitmap = new ImageManager().getNewSizeMap(bitmap, 80);// ����
			LayoutInflater inflater = LayoutInflater.from(photoActivity);
			View view = inflater.inflate(R.layout.dialog_camera, null);
			ImageView btnI = (ImageView) view.findViewById(R.id.dialogPhotoOne);

			Drawable drawable = new BitmapDrawable(bitmap);
			btnI.setBackground(drawable);

			ImageView btnII = (ImageView) view
					.findViewById(R.id.dialogPhotoTwo);
			bitmap = mImageManager.getNewDegreeMap(bitmap, 90);
			drawable = new BitmapDrawable(bitmap);
			btnII.setBackground(drawable);

			ImageView btnIII = (ImageView) view
					.findViewById(R.id.dialogPhotoThree);
			bitmap = mImageManager.getNewDegreeMap(bitmap, 90);
			drawable = new BitmapDrawable(bitmap);
			btnIII.setBackground(drawable);

			ImageView btnIV = (ImageView) view
					.findViewById(R.id.dialogPhotoFour);
			bitmap = mImageManager.getNewDegreeMap(bitmap, 90);
			drawable = new BitmapDrawable(bitmap);
			btnIV.setBackground(drawable);

			ImageButton btnCorrect = (ImageButton) view
					.findViewById(R.id.photoCorrect);
			btnCorrect.setOnClickListener(new OnClickListener() {
				// ����ǰ�������ͷ���պ�ͼƬ������ʾ�ĽǶ�
				@Override
				public void onClick(View v) {

					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						CommonMethod.SetSharepreferenceValue(photoActivity,
								ConstValue.SharepreferenceKey.CameraBackDegree,
								flagImageDirection);
						flagBack = false;
					} else {
						CommonMethod
								.SetSharepreferenceValue(
										photoActivity,
										ConstValue.SharepreferenceKey.CameraFrontDegree,
										flagImageDirection);
						flagFront = false;
					}

					dialog.dismiss();
				}
			});

			// ����ͼƬѡ��״̬��imagebutton��������
			btnSelectOne = (ImageButton) view.findViewById(R.id.PhotoOne);
			btnSelectTwo = (ImageButton) view.findViewById(R.id.PhotoTwo);
			btnSelectThree = (ImageButton) view.findViewById(R.id.PhotoThree);
			btnSelectFour = (ImageButton) view.findViewById(R.id.PhotoFour);
			btnSelectOne.setOnClickListener(PhotoManager.this);
			btnSelectTwo.setOnClickListener(PhotoManager.this);
			btnSelectThree.setOnClickListener(PhotoManager.this);
			btnSelectFour.setOnClickListener(PhotoManager.this);
			dialog.setContentView(view);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			dialog.show();
			// �Ѿ���������Ƭ����
			btnSelectOne.setBackgroundResource(R.drawable.photobackground);
			flagImageDirection = 0;
			if(PhotoManager.this.camera==null){
				openCamera();
			}
			PhotoManager.this.camera.startPreview();
		}
	}

	@SuppressLint("NewApi")
	public void changeCamera() {
		openCamera();
	}

	@SuppressLint({ "NewApi", "HandlerLeak" })
	private void openCamera() {
		cameraInfo = new CameraInfo();
		int cameraCount = Camera.getNumberOfCameras();// �õ�����ͷ�ĸ���
		releaseCamera();
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);// �õ�ÿһ������ͷ����Ϣ

			if (cameraCount == 1) {
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					camera = Camera.open(i);// �򿪵�ǰѡ�е�����ͷ
					mCameraId = 1;
					break;
				}
			}
			if (cameraCount > 1) {
				if (mCameraId == 1) {
					// �����Ǻ��ã����Ϊǰ��
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
						camera = Camera.open(i);// �򿪵�ǰѡ�е�����ͷ
						mCameraId = 0;
						break;
					}
				} else {
					// ������ǰ�ã� ���Ϊ����
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						camera = Camera.open(i);// �򿪵�ǰѡ�е�����ͷ
						mCameraId = 1;
						break;
					}
				}
			}
		}

		try {
			camera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			e.printStackTrace();
		} // ����������ʾ����Ӱ���SurfaceHolder����

		camera.setDisplayOrientation(PhotoManager.this
				.getPreviewDegreeForPhoto(photoActivity));

		camera.startPreview(); // ��ʼԤ��
		AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {
				// TODO Auto-generated method stub
				if (success)// success��ʾ�Խ��ɹ�
				{
					// myCamera.setOneShotPreviewCallback(null);
				} else {
				}

			}
		};

		camera.autoFocus(myAutoFocusCallback);

		// ǰ���������û�н���ͼƬ�����ж�
		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
			int backAngle = CommonMethod.GetSharepreferenceValue(photoActivity,
					ConstValue.SharepreferenceKey.CameraBackDegree);
			if (backAngle == -1) {// ��һ�ν�������Զ�����У׼����
				handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
					}
				};

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						flagBack = true;
						TakePicture();
					}
				};
				handler.postDelayed(runnable, 500);

			}

		}
		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			int frontAngle = CommonMethod.GetSharepreferenceValue(
					photoActivity,
					ConstValue.SharepreferenceKey.CameraFrontDegree);
			if (frontAngle == -1) {// ��һ�ν�������Զ�����У׼����
				handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
					}
				};
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						flagFront = true;
						TakePicture();
					}
				};
				handler.postDelayed(runnable, 500);

			}
		}
	}

	/**
	 * �ͷ������Դ
	 */
	public void releaseCamera() {
		if (camera != null) {
			camera.stopPreview();// ͣ��ԭ������ͷ��Ԥ��
			camera.release();// �ͷ���Դ
			camera = null;// ȡ��ԭ������ͷ
		}
	}

	// �Զ����ֻ�����ͷ�����������
	// @SuppressWarnings("deprecation")
	// @SuppressLint("NewApi")
	// public void setCarmeraDisplayOrientation(Activity activity, int cameraId,
	// android.hardware.Camera camera) {
	// android.hardware.Camera.CameraInfo info = new
	// android.hardware.Camera.CameraInfo();
	// int rotation = activity.getWindowManager().getDefaultDisplay()
	// .getRotation();
	// int degress = 0;
	// int screenWidth = photoActivity.getWindowManager().getDefaultDisplay()
	// .getWidth(); // ��Ļ�����أ��磺480px��
	// int screenHeight = photoActivity.getWindowManager().getDefaultDisplay()
	// .getHeight(); // ��Ļ�ߣ����أ��磺
	// if (screenWidth < screenHeight) {
	// switch (rotation) {
	// case Surface.ROTATION_0:
	// degress = 0;
	// break;
	// case Surface.ROTATION_90:
	// degress = 90;
	// break;
	// case Surface.ROTATION_180:
	// degress = 180;
	// break;
	// case Surface.ROTATION_270:
	// degress = 270;
	// break;
	// }
	// } else {
	// switch (rotation) {
	// case Surface.ROTATION_0:
	// degress = 90;
	// break;
	// case Surface.ROTATION_90:
	// degress = 0;
	// break;
	// case Surface.ROTATION_180:
	// degress = 270;
	// break;
	// case Surface.ROTATION_270:
	// degress = 180;
	// break;
	//
	// }
	// int result;
	// if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	// result = (info.orientation + degress) % 360;
	// result = (360 - result) % 360;
	// } else {
	// // �����ͼ��ķ���˳ʱ����ת-��Ļ����ת�Ƕ�+360
	// result = (info.orientation - degress + 360) % 360;
	// }
	// camera.setDisplayOrientation(result);
	// }
	// }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.PhotoOne:
			circleSelect();
			btnSelectOne.setBackgroundResource(R.drawable.photobackground);
			flagImageDirection = 0;
			break;
		case R.id.PhotoTwo:
			circleSelect();
			btnSelectTwo.setBackgroundResource(R.drawable.photobackground);
			flagImageDirection = 90;
			break;
		case R.id.PhotoThree:
			circleSelect();
			btnSelectThree.setBackgroundResource(R.drawable.photobackground);
			flagImageDirection = 180;
			break;
		case R.id.PhotoFour:
			circleSelect();
			btnSelectFour.setBackgroundResource(R.drawable.photobackground);
			flagImageDirection = 270;
			break;

		default:
			break;
		}

	}

	private void circleSelect() {
		btnSelectOne.setBackgroundResource(R.drawable.photoaaaaa);
		btnSelectTwo.setBackgroundResource(R.drawable.photoaaaaa);
		btnSelectThree.setBackgroundResource(R.drawable.photoaaaaa);
		btnSelectFour.setBackgroundResource(R.drawable.photoaaaaa);

	}
}
