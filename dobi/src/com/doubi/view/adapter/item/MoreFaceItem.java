package com.doubi.view.adapter.item;

import android.graphics.Bitmap;
import android.widget.ImageButton;

/**
 * ���˰�������
 * 
 * @author Administrator
 *
 */
public class MoreFaceItem {
	/**
	 * Ψһ��ʾ����ɨ�赽���Ⱥ�˳�����У���ʶ�����еõڼ�������
	 */
	private int index;
	/**
	 * ���꣬0:x��1:y
	 */
	private int[] location;
	/**
	 * ����ͼƬ
	 */
	private Bitmap mBitmap;
	/**
	 * ���»�ȡ����Ƭ�Ƿ���ڸõ�
	 */
	private boolean isHangest;

	/**
	 * ������ʾ�İ�ť
	 */
	private ImageButton mButton;

	public MoreFaceItem() {
		isHangest = true;
	}

	/**
	 * Ψһ��ʾ����ɨ�赽���Ⱥ�˳������
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Ψһ��ʾ����ɨ�赽���Ⱥ�˳������
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * ���꣬0:x��1:y
	 */
	public int[] getLocation() {
		return location;
	}

	/**
	 * ���꣬0:x��1:y
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
	 * ���»�ȡ����Ƭ�Ƿ���ڸõ�
	 */
	public boolean isHangest() {
		return isHangest;
	}

	/**
	 * ���»�ȡ����Ƭ�Ƿ���ڸõ�
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
