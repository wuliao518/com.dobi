package com.doubi.logic.drawView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * 自定义控件、场景控件
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class SceneDrawView extends SingleDrawViewBase {

	public SceneDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseImgCount = 1;
		// 定义场景需要显示图片的数量
		imgCount = baseImgCount + PROP_COUNT;
	}

	/**
	 * 初始化
	 * 
	 * @param activity
	 * @param mBitmap
	 *            该Activity的图片控件上的图片资源
	 * @param cj_width
	 * @param cj_height
	 * @param harimap
	 * @param bodymap
	 */
	public void Inteligense(Activity activity, Bitmap mBitmap, int cj_width,
			int cj_height) {
		// 必需调用
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
				// 重新设置id，保证排序，截图等正常
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
	 * 切换场景
	 * 
	 * @param mBitmap
	 */
	public void updatePic(Bitmap mBitmap) {
		if (mBitmap != null) {
			sceneBitmap.recycle();
			sceneBitmap = null;
			// 加载场景
			sceneBitmap = Bitmap.createScaledBitmap(mBitmap, cjWidth, cjHeight,
					false);
		}
		this.invalidate();
	}

	/**
	 * 保存
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

		// 计算场景中得bmp
		bodyBmp = mBmps[0];
	}

	/**
	 * 使相应图片进入选中状态
	 * 
	 * @param index
	 *            0:整个身体
	 */
	@Override
	public void selectWidget(int index) {
		super.selectWidget(index);
	}
}
