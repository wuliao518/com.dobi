package com.doubi.view.adapter.item;

import android.graphics.Bitmap;

/**
 * 
 * @author Administrator
 *
 */
public class MapItem {
	/**
	 * 场景图片
	 */
	private Bitmap mBitmap;
	/**
	 * 该场景图片所在路径
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
