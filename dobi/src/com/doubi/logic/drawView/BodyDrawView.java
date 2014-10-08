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
 * 自定义扮演控件-装扮
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class BodyDrawView extends SingleDrawViewBase {

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
		if (saveBodyBmps == null) {
			// 设置图片数量
			int len = imgCount;
			Bmp bmp[] = new Bmp[len];
			{
				// 越排在后面，显示级别越高 0:衣服，1：头部
				bmp[0] = new Bmp(clothesBmp.getPic(), 0, clothesBmp.getXY(1),
						clothesBmp.getXY(2), true, false, false);

				bmp[1] = new Bmp(headBmp.getPic(), 1, headBmp.getXY(1),
						headBmp.getXY(2), true, false, false);
			}
			this.mBmps = bmp;
			this.intAllBmps();
		} else {
			this.mBmps = saveBodyBmps;
			// 从发饰装扮进入的情况
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
				// 重新设置id，保证排序，截图等正常
				if (mBmps[i + baseImgCount] != null) {
					mBmps[i + baseImgCount].setImgId(i + baseImgCount);
				}
			}
		}
	}

	public BodyDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);

		baseImgCount = 2;
		// 定义场景需要显示图片的数量
		imgCount = baseImgCount + PROP_COUNT;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mBmps != null) {
			// 保存记录
			clothesBmp = mBmps[0];
			saveBodyBmps = mBmps;
			headBmp = mBmps[1];
		}
	}

	/**
	 * 改变装扮素材
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
	 * 化妆最后对图片的保存
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

		// 计算坐标
		RectangleManager mRectangleManager = mImageManager.getNewRectangle(
				mBmps[0].getRectangle(), mBmps[1].getRectangle());
		bodyBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * 使相应图片进入选中状态
	 * 
	 * @param index
	 *            0:衣服，1：头部整体
	 */
	@Override
	public void selectWidget(int index) {
		super.selectWidget(index);
	}
}
