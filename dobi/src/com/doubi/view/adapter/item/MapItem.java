package com.doubi.view.adapter.item;

import android.graphics.Bitmap;

/**
 * 
 * @author Administrator
 *
 */
public class MapItem {
	/**
	 * ����ͼƬ
	 */
	private Bitmap mBitmap;
	/**
	 * �ó���ͼƬ����·��
	 */
	private String imgPath;

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public void setBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public void disBitmap() {
		if (mBitmap != null) {
			mBitmap.recycle();
		}
	}
}
