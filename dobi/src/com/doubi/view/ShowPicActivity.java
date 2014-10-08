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
	private Bitmap cameraBitmap = null;// ��Ⱦǰ
	private TouchImageView mTouchImageView;// �ü�ǰͼƬ������
	private ClipImgView mClipImgView; // �ü���ͼƬ������
	private ImageManager mImageManager;
	// private final String IMG = "img";
	// private final String SURE = "btnSure";
	private ImageButton btnWeak, btnMedium, btnStrong;
	private int moreIndex;

	/**
	 * Activity�ڴ�����ʱ��ص��ĺ��� ��Ҫ������ʼ��һЩ����
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		setContentView(R.layout.activity_showpic);
		// ע����androidϵͳ�ϣ��ֻ�ͼƬ�ߴ羡��������һ����Χ��,�����ڸ�˹����ʱ��������ڴ����������
		mTouchImageView = (TouchImageView) this.findViewById(R.id.ivPic);
		mClipImgView = (ClipImgView) this.findViewById(R.id.ivPicClip);
		mImageManager = new ImageManager();
		moreIndex = -1;
		setImageBitmap();
		// ��ʼ�� cameraBitmap�����@ʾ�ղ������ͷ��
		mTouchImageView.Inteligense(ShowPicActivity.this, cameraBitmap);
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		View view = inflater.inflate(R.layout.dialog_tip, null);
		final Dialog dialog=new Dialog(this, R.style.Translucent_NoTitle);        
	    dialog.setContentView(R.layout.dialog_tip);
	    int height = (int) (CommonMethod.GetDensity(ShowPicActivity.this)*180+0.5);
		int width = (int) (CommonMethod.GetDensity(ShowPicActivity.this)*200+0.5);
		LayoutParams params = new LayoutParams(width, height);
		// ���öԻ����С�������ã�
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
		// ȥ����������
		// loadImageFilter(1);
		// new processImageTask().execute();
		// ����ͳ��
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// ����ͳ��
		MobclickAgent.onPause(this);
	}

	/**
	 * ��MainActivity��������ͼƬ��ʾ�ڽ��浱��
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

		// else {// �������
		// // ��ȡ���ڸ�����������
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
		// �����ļ�����
		if (mFile.exists()) {
			cameraBitmap = mImageManager.getBitmapFromFile(mFile,
					mTouchImageView.getWidth());
		}

		if (photoType.equals(ConstValue.ImgSourceType.select.toString())) {// ѡ����Ƭ���

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
	 * ����ͼƬfilter������ͼƬ��ͼƬ����ǿ��: 0:�� , 1:��, 2:ǿ
	 */
	private void loadImageFilter(int filterType) {
		// new processImageTask(filterType, IMG).execute();

		// ͬ��ִ��
		Bitmap mBitmap = null;

		mBitmap = mImageManager.LoadBitmapFilter(cameraBitmap, filterType, 4);
		mTouchImageView.Inteligense(this, mBitmap);
		mTouchImageView.invalidate();
	}

	/**
	 * �_��
	 * 
	 * @param v
	 */
	public void btnSureOnclick(View v) {
		// �ü������¼���ͼƬ,���Զ�����
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
	 * ȡ����ͼ
	 * 
	 * @param v
	 */
	public void btnCancelOnclick(View v) {
		// btnWeak.setBackgroundResource(R.drawable.ruo);
		// btnMedium.setBackgroundResource(R.drawable.zhong);
		// btnStrong.setBackgroundResource(R.drawable.qiang);
		// loadImageFilter(3);

		// ��ʱ��Ϊ�������չ���
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
	 * ��������
	 * 
	 * @param v
	 */
	public void btnReShootOnclick(View v) {
		Intent intent = new Intent(this, PhotoActivity.class);
		this.startActivity(intent);
		this.finish();
	}

	/**
	 * ������ҳ
	 * 
	 * @param v
	 */
	public void btnGoMainOnclick(View v) {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		this.finish();
	}

	/**
	 * ����ͼƬ��������
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
	 * ����ͼƬ��������
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
	 * ����ͼƬ������ǿ
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
	 * �첽
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