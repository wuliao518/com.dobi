package com.doubi.logic.drawView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * �Զ���ؼ��������ؼ�
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class SceneDrawView extends SingleDrawViewBase {

	public SceneDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseImgCount = 1;
		// ���峡����Ҫ��ʾͼƬ������
		imgCount = baseImgCount + PROP_COUNT;
	}

	/**
	 * ��ʼ��
	 * 
	 * @param activity
	 * @param mBitmap
	 *            ��Activity��ͼƬ�ؼ��ϵ�ͼƬ��Դ
	 * @param cj_width
	 * @param cj_height
	 * @param harimap
	 * @param bodymap
	 */
	public void Inteligense(Activity activity, Bitmap mBitmap, int cj_width,
			int cj_height) {
		// �������
		super.Inteligense(activity, mBitmap, cj_width, cj_height);
		mBmps = new Bmp[imgCount];
		if (bodyBmp != null) {
			int i = 0;
			this.mBmps[i] = new Bmp(bodyBmp.getPic(), i, bodyBmp.getXY(1),
					bodyBmp.getXY(2), true, false, false);
			intBmp(i);
		}
		if (propBmps != null) {
			for (int i = 0; i < 20; i++) {
				if (propBmps[i] != null) {
					propBmps[i].setCanChange(false);
				}
				mBmps[i + baseImgCount] = propBmps[i];
				// ��������id����֤���򣬽�ͼ������
				if (mBmps[i + baseImgCount] != null) {
					mBmps[i + baseImgCount].setImgId(i + baseImgCount);
				}
			}
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		bodyBmp=mBmps[0];
	}

	/**
	 * �л�����
	 * 
	 * @param mBitmap
	 */
	public void updatePic(Bitmap mBitmap) {
		if (mBitmap != null) {
			sceneBitmap.recycle();
			sceneBitmap = null;
			// ���س���
			sceneBitmap = Bitmap.createScaledBitmap(mBitmap, cjWidth, cjHeight,
					false);
		}
		this.invalidate();
	}

	/**
	 * ����
	 */
	public void saveBodyBitmap() {
		for (Bmp bmp : mBmps) {
			if (bmp != null && bmp.getImgId() < this.baseImgCount) {
				if (bmp.isFocus()) {
					bmp.cancelHighLight();
				}
				saveCanvas.drawBitmap(bmp.getPic(), bmp.matrix, null);
			}
		}

		// ���㳡���е�bmp
		bodyBmp = mBmps[0];
	}

	/**
	 * ʹ��ӦͼƬ����ѡ��״̬
	 * 
	 * @param index
	 *            0:��������
	 */
	@Override
	public void selectWidget(int index) {
		super.selectWidget(index);
	}
}
