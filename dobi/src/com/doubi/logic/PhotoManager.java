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
 * 相机管理类
 * 
 * @author Administrator
 *
 */
public class PhotoManager implements OnClickListener {
	private ImageButton btnSelectOne, btnSelectTwo, btnSelectThree,
			btnSelectFour;

	/**
	 * 拍照预览图片的方向0为手机默认方向，1为旋转90度，2为旋转180度，3为旋转270度
	 */
	private int flagImageDirection;
	/**
	 * 前后摄像后有无矫正标志；
	 */
	private boolean flagBack;
	private boolean flagFront;
	// 照片矫正画面dialog
	private MyDialog dialog;

	// 延时执行自动拍照矫正画面
	private Handler handler;

	private CameraInfo cameraInfo;
	private Camera camera;
	/**
	 * 拍照的activity
	 */
	private Activity photoActivity;

	/**
	 * 0代表刚刚打开前置摄像头，1代表刚刚打开后置摄像头
	 */
	private int mCameraId;

	private SurfaceHolder mSurfaceHolder;

	ImageManager mImageManager;

	/**
	 * 构造
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
	 * 检查设备是否支持摄像头
	 */
	public boolean CheckCameraHardware(Context mContext) {

		if (mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// 摄像头存在
			return true;
		} else {
			// 摄像头不存在
			return false;
		}
	}

	// 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	public int getPreviewDegreeForPhoto(Activity activity) {
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
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

	// 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	public int getPreviewDegreeForShow(Activity activity, String photoType) {
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degree = 0;
		if (photoType.equals(ConstValue.ImgSourceType.front.toString())) {
			// 根据手机的方向计算相机预览画面应该选择的角度
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
			// 根据手机的方向计算相机预览画面应该选择的角度
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
	 * 拍照
	 */
	public void TakePicture() {
		if (camera != null) {
			camera.takePicture(null, null, new MyPictureCallback());
			// setCarmeraDisplayOrientation(photoActivity, 1, camera);
		}
	}

	/**
	 * 硬件按鈕拍照
	 */
	public void TakePictureByKey(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
			if (camera != null && event.getRepeatCount() == 0) {
				// 拍照
				// 注：调用takePicture()方法进行拍照是传入了一个PictureCallback对象——当程序获取了拍照所得的图片数据之后
				// ，PictureCallback对象将会被回调，该对象可以负责对相片进行保存或传入网络
				camera.takePicture(null, null, getMyPictureCallback());
				// setCarmeraDisplayOrientation(photoActivity, 1, camera);
			}
		}
	}

	/**
	 * 获取SurfaceCallback
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
	 * SurfaceView控件使用的回调
	 * 
	 * @author Administrator
	 *
	 */
	@SuppressLint("InflateParams")
	private final class SurfaceCallback implements Callback {
		private Camera.Parameters parameters = null;

		public SurfaceCallback(Activity activity) {
			photoActivity = activity;
			// 选择该时机实例化dialog
			if (dialog == null) {
				LayoutInflater inflater = LayoutInflater.from(photoActivity);
				View view = inflater.inflate(R.layout.dialog_camera, null);
				dialog = new MyDialog(photoActivity, view, R.style.Self_Dialog);
				dialog.setCancelable(false);
			}
		}

		// 拍照状态变化时调用该方法
		@SuppressLint({ "NewApi", "InflateParams" })
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if(camera==null){
				camera=Camera.open(0);
			}
			parameters = camera.getParameters(); // 获取各项参数
			parameters.setPictureFormat(ImageFormat.JPEG); // 设置图片格式
			parameters.setPreviewSize(width, height); // 设置预览大小
			
			parameters.setPreviewFpsRange(4, 4); // 设置每秒显示4帧
			parameters.setPictureSize(width, height); // 设置保存的图片尺寸
			parameters.setJpegQuality(100); // 设置照片质量
			// parameters.set("autofocus", true); // 自动对焦
			//parameters.setFocusMode("auto");
		}

		// 开始拍照时调用该方法
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

		// 停止拍照时调用该方法
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			releaseCamera();
		}
	}

	private final class MyPictureCallback implements PictureCallback {

		@SuppressLint({ "InflateParams", "NewApi" })
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			if (flagFront || flagBack) {// 如果是第一次打开，弹出dialog调整图片方向
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
				// 保存图片到sd卡中
				try {
					mImageManager.saveToSDCard(data);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				photoActivity.finish();
				Intent intent = new Intent(photoActivity,ShowPicActivity.class);
				if (mCameraId == 0) { // 说明刚刚打开了前置摄像头，下次打开后置摄像头
					intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
							ConstValue.ImgSourceType.front.toString());
				} else {
					intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
							ConstValue.ImgSourceType.back.toString());
				}
				photoActivity.startActivity(intent);
				
			}

			/************************************ 拍照图像校准 **********************************/
			/****************************** 开启showPicture画面 *******************************/
			/*
			 * mImageManager.saveToSDCard(bitmap, ConstValue.ImgName.photo);
			 * 
			 * photoActivity.finish(); Intent intent = new Intent(photoActivity,
			 * ShowPicActivity.class);
			 * 
			 * if (mCameraId == 0) { // 说明刚刚打开了前置摄像头，下次打开后置摄像头
			 * intent.putExtra(ConstValue.ExtruaKey.PHOTO_TYPE,
			 * ConstValue.ImgSourceType.front.toString()); } else {
			 * intent.putExtra(ConstValue.ExtruaKey.PHOTO_TYPE,
			 * ConstValue.ImgSourceType.back.toString()); }
			 * photoActivity.startActivity(intent);
			 */
			/****************************** 开启showPicture画面 *******************************/

		}
	}

	/**
	 * 异步弹出选择框   选择朝向
	 */
	private class processImageTask extends AsyncTask<byte[], Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@SuppressWarnings("deprecation")
		@SuppressLint({ "NewApi", "InflateParams" })
		public Void doInBackground(byte[]... params) {
			// 照片矫正框里的图片缩略图
			Bitmap bitmap;
			// Bitmap bitmapI;
			bitmap = mImageManager.BytesToBimap(params[0],photoActivity);
			/************************************ 拍照图像校准 **********************************/
			// 缩小尺寸

			bitmap = new ImageManager().getNewSizeMap(bitmap, 80);// 右下
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
				// 保存前或后摄像头拍照后图片正常显示的角度
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

			// 矫正图片选中状态的imagebutton监听处理
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
			// 已经矫正过照片方向
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
		int cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
		releaseCamera();
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息

			if (cameraCount == 1) {
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					camera = Camera.open(i);// 打开当前选中的摄像头
					mCameraId = 1;
					break;
				}
			}
			if (cameraCount > 1) {
				if (mCameraId == 1) {
					// 现在是后置，变更为前置
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
						camera = Camera.open(i);// 打开当前选中的摄像头
						mCameraId = 0;
						break;
					}
				} else {
					// 现在是前置， 变更为后置
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
						camera = Camera.open(i);// 打开当前选中的摄像头
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
		} // 设置用于显示拍照影像的SurfaceHolder对象

		camera.setDisplayOrientation(PhotoManager.this
				.getPreviewDegreeForPhoto(photoActivity));

		camera.startPreview(); // 开始预览
		AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {
				// TODO Auto-generated method stub
				if (success)// success表示对焦成功
				{
					// myCamera.setOneShotPreviewCallback(null);
				} else {
				}

			}
		};

		camera.autoFocus(myAutoFocusCallback);

		// 前后摄像后有没有矫正图片方向判断
		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
			int backAngle = CommonMethod.GetSharepreferenceValue(photoActivity,
					ConstValue.SharepreferenceKey.CameraBackDegree);
			if (backAngle == -1) {// 第一次进入进行自动拍照校准画面
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
			if (frontAngle == -1) {// 第一次进入进行自动拍照校准画面
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
	 * 释放相机资源
	 */
	public void releaseCamera() {
		if (camera != null) {
			camera.stopPreview();// 停掉原来摄像头的预览
			camera.release();// 释放资源
			camera = null;// 取消原来摄像头
		}
	}

	// 自定义手机摄像头倒立解决方案
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
	// .getWidth(); // 屏幕宽（像素，如：480px）
	// int screenHeight = photoActivity.getWindowManager().getDefaultDisplay()
	// .getHeight(); // 屏幕高（像素，如：
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
	// // 摄像机图像的方向顺时针旋转-屏幕的旋转角度+360
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
