package com.doubi.logic.drawView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.doubi.common.ConstValue;
import com.doubi.logic.ImageManager.RectangleManager;
import com.doubi.logic.drawView.DrawViewBase.Bmp;

/**
 * �Զ�����ݿؼ�-װ��
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class HairDrawView extends SingleDrawViewBase {

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
	 * @param isUpdate
	 *            �Ƿ���Ҫ����
	 */
	public void Inteligense(Activity activity, Bitmap mBitmap, int cj_width,
			int cj_height) {
		// �������
		super.Inteligense(activity, mBitmap, cj_width, cj_height);
		saveHairBmps=null;
		if (saveHairBmps == null||clothesBmp==null) {

			// �����������ʿ�ȣ��������������ͬ������������ͼƬ,����Ϊ�ؼ����1/7Ϊ��׼��
			scale = (cj_width / SCALE_PRE) / ConstValue.FACE_BASE_WIDTH;// �����������

			// ����ͼƬ����
			int len = imgCount;

			Bmp bmp[] = new Bmp[len];
			{
				// Խ���ں��棬��ʾ����Խ�� 0:�·���1��������2������
				bmp[0] = new Bmp(clothesBmp.getPic(), 0, clothesBmp.getXY(1),
						clothesBmp.getXY(2), false, false, false);
				bmp[1] = new Bmp(faceBmp.getPic(), 1, faceBmp.getXY(1),
						faceBmp.getXY(2), false, false, false);
				bmp[2] = new Bmp(hairBmp.getPic(), 2, hairBmp.getXY(1),
						hairBmp.getXY(2), true, false, false);
			}
			this.mBmps = bmp;
			this.intAllBmps();
		} else {
			this.mBmps = saveHairBmps;
				if (clothesBmp != null) {
					int i = 0;
					mBmps[i] = new Bmp(clothesBmp.getPic(), i, clothesBmp.getXY(1),
							clothesBmp.getXY(2), true, false, false);
					intBmp(i);
				}
				if (hairBmp != null) {
					int i = 2;
					mBmps[i] = new Bmp(hairBmp.getPic(), i, hairBmp.getXY(1),
							hairBmp.getXY(2), false, false, true);
					intBmp(i);
				}
				if (faceBmp != null) {
					int i = 1;
					//mBmps[i].SetBmpInfo(faceBmp.getPic());
					mBmps[i].setPreX(faceBmp.getXY(1));
					mBmps[i].setPreY(faceBmp.getXY(2));
					intBmp(i);
				}
		}
		// ���ص���
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

	public HairDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);

		baseImgCount = 3;
		// ���峡����Ҫ��ʾͼƬ������
		imgCount = baseImgCount + PROP_COUNT;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// �����¼
		if (mBmps != null) {
			clothesBmp = mBmps[0];
			hairBmp = mBmps[2];
			faceBmp=mBmps[1];
			saveHairBmps = mBmps;
		}
	}

	/**
	 * �ı�װ���ز�
	 * 
	 * @param mBitmap
	 * @param bmpsIndex
	 *            0���·���2������
	 */
	public void updatePic(Bitmap mBitmap, int bmpsIndex) {

		mBitmap = Bitmap.createScaledBitmap(mBitmap,
				(int) (mBitmap.getWidth() * scale),
				(int) (mBitmap.getHeight() * scale), true);

		this.mBmps[bmpsIndex].setBasePic(mBitmap);
		this.mBmps[bmpsIndex].setPic(mBitmap);

		mBmps[bmpsIndex].width = mBmps[bmpsIndex].getPic().getWidth();
		mBmps[bmpsIndex].height = mBmps[bmpsIndex].getPic().getHeight();
		mBmps[bmpsIndex].intBitmap();

		this.invalidate();
	}

	/**
	 * ѡ���ν���ѡ���·�����ı���
	 */
	public void saveHeadBitmap() {
		for (Bmp bmp : mBmps) {
			if (bmp != null && bmp.getImgId() != 0
					&& bmp.getImgId() < this.baseImgCount) {
				if (bmp.isFocus()) {
					bmp.cancelHighLight();
				}
				saveCanvas.drawBitmap(bmp.getPic(), bmp.matrix, null);
			}
		}
		// ��������
		RectangleManager mRectangleManager = mImageManager.getNewRectangle(
				mBmps[1].getRectangle(), mBmps[2].getRectangle());
		headBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * ѡ����ֱ�����������ı���
	 * 
	 * @return ����ͼƬ��Ӧ��Bmp
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

		// ��������
		RectangleManager mRectangleManager = mImageManager.getNewRectangle(
				mBmps[0].getRectangle(), mBmps[1].getRectangle(),
				mBmps[2].getRectangle());
		bodyBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * ʹ��ӦͼƬ����ѡ��״̬
	 * 
	 * @param index
	 *            0:�·���1��������2������
	 */
	@Override
	public void selectWidget(int index) {
		super.selectWidget(index);
	}
}
