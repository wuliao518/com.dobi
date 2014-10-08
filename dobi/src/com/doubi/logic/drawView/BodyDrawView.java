package com.doubi.logic.drawView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.doubi.logic.ImageManager.RectangleManager;
import com.doubi.logic.drawView.DrawViewBase.Bmp;

/**
 * �Զ�����ݿؼ�-װ��
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class BodyDrawView extends SingleDrawViewBase {

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
		if (saveBodyBmps == null) {
			// ����ͼƬ����
			int len = imgCount;
			Bmp bmp[] = new Bmp[len];
			{
				// Խ���ں��棬��ʾ����Խ�� 0:�·���1��ͷ��
				bmp[0] = new Bmp(clothesBmp.getPic(), 0, clothesBmp.getXY(1),
						clothesBmp.getXY(2), true, false, false);

				bmp[1] = new Bmp(headBmp.getPic(), 1, headBmp.getXY(1),
						headBmp.getXY(2), true, false, false);
			}
			this.mBmps = bmp;
			this.intAllBmps();
		} else {
			this.mBmps = saveBodyBmps;
			// �ӷ���װ���������
			if (headBmp != null) {
				int i = 1;
				this.mBmps[i] = new Bmp(headBmp.getPic(), i, headBmp.getXY(1),
						headBmp.getXY(2), true, false, false);
				intBmp(i);
			}
			if (clothesBmp != null) {
				int i = 0;
				mBmps[i] = new Bmp(clothesBmp.getPic(), i, clothesBmp.getXY(1),
						clothesBmp.getXY(2), true, false, false);
				intBmp(i);
			}
			
			
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

	public BodyDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);

		baseImgCount = 2;
		// ���峡����Ҫ��ʾͼƬ������
		imgCount = baseImgCount + PROP_COUNT;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mBmps != null) {
			// �����¼
			clothesBmp = mBmps[0];
			saveBodyBmps = mBmps;
			headBmp = mBmps[1];
		}
	}

	/**
	 * �ı�װ���ز�
	 * 
	 * @param mBitmap
	 */
	public void updatePic(Bitmap mBitmap) {
		if (mBitmap != null) {
			int bmpsIndex = 0;
			mBitmap = Bitmap.createScaledBitmap(mBitmap,
					(int) (mBitmap.getWidth() * scale),
					(int) (mBitmap.getHeight() * scale), false);

			this.mBmps[bmpsIndex].setBasePic(mBitmap);
			this.mBmps[bmpsIndex].setPic(mBitmap);

			mBmps[bmpsIndex].width = mBmps[bmpsIndex].getPic().getWidth();
			mBmps[bmpsIndex].height = mBmps[bmpsIndex].getPic().getHeight();
			mBmps[bmpsIndex].intBitmap();
			this.invalidate();
		}
	}

	/**
	 * ��ױ����ͼƬ�ı���
	 * 
	 */
	public void saveBitmap() {
		for (Bmp bmp : mBmps) {
			if (bmp != null && bmp.getImgId() < this.baseImgCount) {
				if (bmp.isFocus()) {
					bmp.cancelHighLight();
				}
				saveCanvas.drawBitmap(bmp.getPic(), bmp.matrix, null);
			}
		}

		// ��������
		RectangleManager mRectangleManager = mImageManager.getNewRectangle(
				mBmps[0].getRectangle(), mBmps[1].getRectangle());
		bodyBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * ʹ��ӦͼƬ����ѡ��״̬
	 * 
	 * @param index
	 *            0:�·���1��ͷ������
	 */
	@Override
	public void selectWidget(int index) {
		super.selectWidget(index);
	}
}
