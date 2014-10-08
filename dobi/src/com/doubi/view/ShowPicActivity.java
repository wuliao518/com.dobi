package com.doubi.view;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.exception.ExitAppUtils;
import com.doubi.logic.ClipImgView;
import com.doubi.logic.ImageManager;
import com.doubi.logic.TouchImageView;
import com.doubi.logic.drawView.SingleDrawViewBase;
import com.umeng.analytics.MobclickAgent;

public class ShowPicActivity extends Activity {
	private Bitmap cameraBitmap = null;// 渲染前
	private TouchImageView mTouchImageView;// 裁剪前图片的容器
	private ClipImgView mClipImgView; // 裁剪后图片的容器
	private ImageManager mImageManager;
	// private final String IMG = "img";
	// private final String SURE = "btnSure";
	private ImageButton btnWeak, btnMedium, btnStrong;
	private int moreIndex;

	/**
	 * Activity在创建的时候回调的函数 主要用来初始化一些变量
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		setContentView(R.layout.activity_showpic);
		// 注：在android系统上，手机图片尺寸尽量控制在一定范围内,否则在高斯运算时可以造成内存溢出的问题
		mTouchImageView = (TouchImageView) this.findViewById(R.id.ivPic);
		mClipImgView = (ClipImgView) this.findViewById(R.id.ivPicClip);
		mImageManager = new ImageManager();
		moreIndex = -1;
		setImageBitmap();
		// 初始化 cameraBitmap，并顯示刚才拍摄的头像
		mTouchImageView.Inteligense(ShowPicActivity.this, cameraBitmap);
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		View view = inflater.inflate(R.layout.dialog_tip, null);
		final Dialog dialog=new Dialog(this, R.style.Translucent_NoTitle);        
	    dialog.setContentView(R.layout.dialog_tip);
	    int height = (int) (CommonMethod.GetDensity(ShowPicActivity.this)*180+0.5);
		int width = (int) (CommonMethod.GetDensity(ShowPicActivity.this)*200+0.5);
		LayoutParams params = new LayoutParams(width, height);
		// 设置对话框大小（不好用）
		WindowManager.LayoutParams params1 = dialog.getWindow().getAttributes();
		params1.width = width;
		params1.height = height;
		params1.x = 0;
		params1.y = 0;
		dialog.getWindow().setAttributes(params1);
		dialog.addContentView(view, params);
	    RelativeLayout iv = (RelativeLayout)view.findViewById(R.id.rl_word);  
	    iv.setOnClickListener(new OnClickListener() {
		    @Override  
		    public void onClick(View v) {  
		        dialog.dismiss();  
		    }  
	    });  
	    dialog.show(); 
	}

	@Override
	public void onResume() {
		super.onResume();
		// 去掉美化功能
		// loadImageFilter(1);
		// new processImageTask().execute();
		// 友盟统计
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// 友盟统计
		MobclickAgent.onPause(this);
	}

	/**
	 * 将MainActivity传过来的图片显示在界面当中
	 * 
	 * @param bytes
	 */
	public void setImageBitmap() {
		String path = "";

		path = Environment.getExternalStorageDirectory() + ConstValue.ROOT_PATH
				+ ConstValue.ImgName.photo.toString() + "jpg";

		String photoType = this.getIntent().getStringExtra(
				ConstValue.ExtruaKey.PhotoType.toString());
		// if (CommonMethod.GetSingleOrMore() == 0) {
		// path = Environment.getExternalStorageDirectory()
		// + ConstValue.ROOT_PATH
		// + ConstValue.ImgName.photo.toString() + ".jpg";
		// }

		// else {// 拍照情况
		// // 获取正在给哪张脸拍照
		// int index = 0;
		// List<MoreFaceItem> list = MoreSceneDrawView.GetMoreFaceItems();
		// if (list != null && list.size() != 0) {
		// for (MoreFaceItem mMoreFaceItem : list) {
		// if (mMoreFaceItem.isHangest()) {
		// index = mMoreFaceItem.getIndex();
		// break;
		// }
		// }
		// }
		// path = Environment.getExternalStorageDirectory()
		// + ConstValue.ROOT_PATH + ConstValue.MORE_CLIP_FACE
		// + ConstValue.ImgName.morePhotoClip.toString() + index
		// + ".jpg";
		// }
		// }

		File mFile = new File(path);
		// 若该文件存在
		if (mFile.exists()) {
			cameraBitmap = mImageManager.getBitmapFromFile(mFile,
					mTouchImageView.getWidth());
		}

		if (photoType.equals(ConstValue.ImgSourceType.select.toString())) {// 选择照片情况

			if (CommonMethod.GetSingleOrMore() != 0) {
				moreIndex = this.getIntent().getIntExtra(
						ConstValue.ExtruaKey.MoreFaceIndex.toString(), 1);
				// path = Environment.getExternalStorageDirectory()
				// + ConstValue.ROOT_PATH + ConstValue.MORE_CLIP_FACE
				// + ConstValue.ImgName.morePhotoClip.toString() + index
				// + ".jpg";

			}
		} else if (photoType.equals(ConstValue.ImgSourceType.front.toString())) {
			int degree = CommonMethod.GetSharepreferenceValue(this,
					ConstValue.SharepreferenceKey.CameraFrontDegree);
			cameraBitmap = mImageManager.getNewDegreeMap(cameraBitmap, degree);
		} else if (photoType.equals(ConstValue.ImgSourceType.back.toString())) {
			int degree = CommonMethod.GetSharepreferenceValue(this,
					ConstValue.SharepreferenceKey.CameraBackDegree);
			cameraBitmap = mImageManager.getNewDegreeMap(cameraBitmap, degree);
		}

	}

	/**
	 * 加载图片filter，美白图片。图片美化强度: 0:弱 , 1:中, 2:强
	 */
	private void loadImageFilter(int filterType) {
		// new processImageTask(filterType, IMG).execute();

		// 同步执行
		Bitmap mBitmap = null;

		mBitmap = mImageManager.LoadBitmapFilter(cameraBitmap, filterType, 4);
		mTouchImageView.Inteligense(this, mBitmap);
		mTouchImageView.invalidate();
	}

	/**
	 * 確定
	 * 
	 * @param v
	 */
	public void btnSureOnclick(View v) {
		// 裁剪，重新加载图片,并自动保存
		mClipImgView.setMoreIndex(moreIndex);
		mClipImgView.SetBitmap(mTouchImageView.CreatNewPhoto());
		mClipImgView.invalidate();
		Intent intent = new Intent(ShowPicActivity.this,
				SingleActivity.class);
		ShowPicActivity.this.startActivity(intent);
		if(cameraBitmap!=null){
			cameraBitmap.recycle();
		}
		
//		if (CommonMethod.GetSingleOrMore() == 0) {
//			Intent intent = new Intent(ShowPicActivity.this,
//					SingleActivity.class);
//			ShowPicActivity.this.startActivity(intent);
//		} else if (CommonMethod.GetSingleOrMore() == 1) {
//
//		} else if (CommonMethod.GetSingleOrMore() == 2) {
//			CommonMethod.SetSingleOrMore(1);
//			Intent intent = new Intent(ShowPicActivity.this, MoreActivity.class);
//			ShowPicActivity.this.startActivity(intent);
//		}
		if (CommonMethod.GetSingleOrMore() == 0) {
			SingleDrawViewBase.clearBuffer();
			Intent intent1 = new Intent(ShowPicActivity.this,
					SingleActivity.class);
			ShowPicActivity.this.startActivity(intent1);
		} else if (CommonMethod.GetSingleOrMore() == 1) {

		} else if (CommonMethod.GetSingleOrMore() == 2) {
			CommonMethod.SetSingleOrMore(1);
			Intent intent2 = new Intent(ShowPicActivity.this, MoreActivity.class);
			ShowPicActivity.this.startActivity(intent2);
		}
		this.finish();

		
	}

	/**
	 * 取消截图
	 * 
	 * @param v
	 */
	public void btnCancelOnclick(View v) {
		// btnWeak.setBackgroundResource(R.drawable.ruo);
		// btnMedium.setBackgroundResource(R.drawable.zhong);
		// btnStrong.setBackgroundResource(R.drawable.qiang);
		// loadImageFilter(3);

		// 暂时改为重新拍照功能
		//Intent intent = new Intent(this, PhotoActivity.class);
		//this.startActivity(intent);
		Intent intent=getIntent();
		String name=(String) intent.getExtras().get("name");
		if(name!=null){
			if(name.equals("single")){
				Intent comit= new Intent(this, SingleActivity.class);
				this.startActivity(comit);
			}
			if(name.equals("single")){
				Intent comit= new Intent(this, SingleActivity.class);
				this.startActivity(comit);
			}
		}
		if(cameraBitmap!=null){
			cameraBitmap.recycle();
		}
		this.finish();
	}

	/**
	 * 重新拍摄
	 * 
	 * @param v
	 */
	public void btnReShootOnclick(View v) {
		Intent intent = new Intent(this, PhotoActivity.class);
		this.startActivity(intent);
		this.finish();
	}

	/**
	 * 返回主页
	 * 
	 * @param v
	 */
	public void btnGoMainOnclick(View v) {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		this.finish();
	}

	/**
	 * 美白图片，级别：弱
	 * 
	 * @param v
	 */
	public void btnWeakOnclick(View v) {
		loadImageFilter(0);
		btnWeak.setBackgroundResource(R.drawable.weak);
		btnMedium.setBackgroundResource(R.drawable.zhong);
		btnStrong.setBackgroundResource(R.drawable.qiang);
	}

	/**
	 * 美白图片，级别：中
	 * 
	 * @param v
	 */
	public void btnMediumOnclick(View v) {
		loadImageFilter(1);
		btnMedium.setBackgroundResource(R.drawable.medium);
		btnWeak.setBackgroundResource(R.drawable.ruo);
		btnStrong.setBackgroundResource(R.drawable.qiang);
	}

	/**
	 * 美白图片，级别：强
	 * 
	 * @param v
	 */
	public void btnStrongOnclick(View v) {
		loadImageFilter(2);
		btnStrong.setBackgroundResource(R.drawable.strong);
		btnWeak.setBackgroundResource(R.drawable.ruo);
		btnMedium.setBackgroundResource(R.drawable.zhong);
	}

	/**
	 * 异步
	 */
	// private class processImageTask extends AsyncTask<Void, Void, Void> {
	//
	// @Override
	// protected void onPreExecute() {
	// super.onPreExecute();
	// CommonMethod.ShowMyDialog(ShowPicActivity.this);
	// }
	//
	// public Void doInBackground(Void... params) {
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(Void result) {
	// super.onPostExecute(result);
	// CommonMethod.CloseDialog();
	// }
	// }
	
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}

}