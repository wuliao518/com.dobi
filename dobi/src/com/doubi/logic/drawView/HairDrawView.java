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
 * 自定义扮演控件-装扮
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class HairDrawView extends SingleDrawViewBase {

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
	 * @param isUpdate
	 *            是否需要更新
	 */
	public void Inteligense(Activity activity, Bitmap mBitmap, int cj_width,
			int cj_height) {
		// 必需调用
		super.Inteligense(activity, mBitmap, cj_width, cj_height);
		saveHairBmps=null;
		if (saveHairBmps == null||clothesBmp==null) {

			// 计算脸部合适宽度，计算放缩比例，同比例处理其他图片,脸部为控件宽度1/7为标准。
			scale = (cj_width / SCALE_PRE) / ConstValue.FACE_BASE_WIDTH;// 整体放缩比例

			// 设置图片数量
			int len = imgCount;

			Bmp bmp[] = new Bmp[len];
			{
				// 越排在后面，显示级别越高 0:衣服，1：脸部，2：发饰
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
		// 加载道具
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

	public HairDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);

		baseImgCount = 3;
		// 定义场景需要显示图片的数量
		imgCount = baseImgCount + PROP_COUNT;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 保存记录
		if (mBmps != null) {
			clothesBmp = mBmps[0];
			hairBmp = mBmps[2];
			faceBmp=mBmps[1];
			saveHairBmps = mBmps;
		}
	}

	/**
	 * 改变装扮素材
	 * 
	 * @param mBitmap
	 * @param bmpsIndex
	 *            0：衣服，2：发饰
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
	 * 选择发饰进入选择衣服步骤的保存
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
		// 计算坐标
		RectangleManager mRectangleManager = mImageManager.getNewRectangle(
				mBmps[1].getRectangle(), mBmps[2].getRectangle());
		headBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * 选择发饰直接跳到场景的保存
	 * 
	 * @return 保存图片对应的Bmp
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

		// 计算坐标
		RectangleManager mRectangleManager = mImageManager.getNewRectangle(
				mBmps[0].getRectangle(), mBmps[1].getRectangle(),
				mBmps[2].getRectangle());
		bodyBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * 使相应图片进入选中状态
	 * 
	 * @param index
	 *            0:衣服，1：脸部，2：发饰
	 */
	@Override
	public void selectWidget(int index) {
		super.selectWidget(index);
	}
}
