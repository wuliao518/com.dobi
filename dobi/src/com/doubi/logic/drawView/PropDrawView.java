package com.doubi.logic.drawView;

import java.util.Random;

import com.doubi.logic.drawView.DrawViewBase.Bmp;

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
public class PropDrawView extends SingleDrawViewBase {

	public PropDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseImgCount = 1;
		// ���峡����Ҫ��ʾͼƬ������
		imgCount = baseImgCount + PROP_COUNT + 1;
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
		if (propBmps == null) {
			propBmps = new Bmp[PROP_COUNT];
		}
		if (propBmps != null) {
			for (int i = 0; i < 20; i++) {
				if (propBmps[i] != null) {
					propBmps[i].setCanChange(true);
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
	 * ���ӵ���
	 * 
	 * @param mBitmap
	 */
	public void addPic(Bitmap mBitmap) {

		for (int i = 0; i < 20; i++) {
			if (propBmps[i] == null) {
				int x = (new Random()).nextInt(cjWidth * 3 / 4 - 1) + cjWidth
						/ 8 + 1;
				int y = (new Random()).nextInt(cjHeight * 3 / 4 - 1) + 1;
				propBmps[i] = new Bmp(mBitmap, baseImgCount + i + 1, x, y,
						true, false, false);
				mBmps[i + baseImgCount] = propBmps[i];
				this.intBmp(i + baseImgCount);
				break;
			}
		}

		this.invalidate();
	}

	/**
	 * ���ٵ���
	 * 
	 * @param mBitmap
	 */
	public void delPic() {
		for (int i = 19; i >= 0; i--) {
			if (propBmps[i] != null) {
				propBmps[i] = null;
				mBmps[i + baseImgCount] = propBmps[i];
				break;
			}
		}

		this.invalidate();
	}

	/**
	 * ɾ������
	 */
	public void delProp(int id) {
		if (propBmps[id - 1] != null) {
			propBmps[id - 1] = null;
			mBmps[id - 1 + baseImgCount] = propBmps[id - 1];
		}
		this.invalidate();
	}

}
