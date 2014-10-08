package com.doubi.view.adapter.item;

import android.graphics.Bitmap;
import android.widget.ImageButton;

/**
 * 多人扮演脸部
 * 
 * @author Administrator
 *
 */
public class MoreFaceItem {
	/**
	 * 唯一标示，按扫描到的先后顺序排列，标识场景中得第几个脸部
	 */
	private int index;
	/**
	 * 坐标，0:x，1:y
	 */
	private int[] location;
	/**
	 * 脸部图片
	 */
	private Bitmap mBitmap;
	/**
	 * 最新获取的照片是否放在该点
	 */
	private boolean isHangest;

	/**
	 * 用来显示的按钮
	 */
	private ImageButton mButton;

	public MoreFaceItem() {
		isHangest = true;
	}

	/**
	 * 唯一标示，按扫描到的先后顺序排列
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 唯一标示，按扫描到的先后顺序排列
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * 坐标，0:x，1:y
	 */
	public int[] getLocation() {
		return location;
	}

	/**
	 * 坐标，0:x，1:y
	 */
	public void setLocation(int[] location) {
		this.location = location;
	}

	public Bitmap getmBitmap() {
		return mBitmap;
	}

	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	/**
	 * 最新获取的照片是否放在该点
	 */
	public boolean isHangest() {
		return isHangest;
	}

	/**
	 * 最新获取的照片是否放在该点
	 */
	public void setHangest(boolean isHangest) {
		this.isHangest = isHangest;
	}

	public ImageButton getmButton() {
		return mButton;
	}

	public void setmButton(ImageButton mButton) {
		this.mButton = mButton;
	}

}
