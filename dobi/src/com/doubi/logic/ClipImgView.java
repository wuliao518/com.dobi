package com.doubi.logic;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.logic.svgResolve.SVG;
import com.doubi.logic.svgResolve.SVGParser;

/**
 * ��ͼ
 * 
 * @author Administrator
 *
 */
public class ClipImgView extends ImageView {
	private Bitmap roundConcerImage;
	private ImageManager mImageManager;
	private int moreIndex; // ���˰��������޸ĵ�ͷ��
	public ClipImgView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageManager = new ImageManager();
	}
	/**
	 * ���ñ����е�ͼƬ
	 * 
	 * @param mBitmap
	 */
	public void SetBitmap(Bitmap mBitmap) {
		roundConcerImage = mBitmap;
	}
	public void setMoreIndex(int moreIndex) {
		this.moreIndex = moreIndex;
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int baseWidth = this.getWidth();
		InputStream mInputStream = getResources().openRawResource(
				R.raw.default_face);
		SVG mSVG = SVGParser.getSVGFromResource(mInputStream);
		Path mPath = mSVG.getPath();

		Bitmap mBitmap = mImageManager.getBitmapFromPath(mPath, mSVG
				.getPicture().getWidth(), mSVG.getPicture().getHeight());

		mBitmap = mImageManager.getNewSizeMap(mBitmap, baseWidth);

		canvas.drawBitmap(mBitmap, 0, 0, new Paint());
		// ���۾����ڲ�λ��
		// mPaint.setColor(Color.GREEN);
		// float eyex = baseWidth * 3.45f / 10f;
		// float eyey = baseHeight * 4.5f / 10f;
		// float radius = baseWidth / 45f;
		// canvas.drawCircle(eyex, eyey, radius, mPaint);
		// canvas.drawCircle(baseWidth - eyex, eyey, radius, mPaint);
		// float mouthx = baseWidth / 2f;
		// float mouthy = baseHeight * 7f / 10f;
		// canvas.drawCircle(mouthx, mouthy, radius, mPaint);

		// �����иʽ
		if (!mPath.isEmpty()) {
			canvas.clipPath(mPath, Region.Op.REPLACE);
		}
		if (roundConcerImage != null) {
			// ����
			try {
				if (CommonMethod.GetSingleOrMore() == 0) {
					mImageManager.saveToSDCard(roundConcerImage,
							ConstValue.ImgName.singlePhotoClip);
				} else {
					if (moreIndex == -1) { // ����
						mImageManager.saveToSDCard(roundConcerImage,
								ConstValue.ImgName.morePhotoClip);
					} else { // ѡ��
						mImageManager
								.saveToSDCard(
										ConstValue.MORE_CLIP_FACE,
										roundConcerImage,
										ConstValue.ImgName.morePhotoClip
												.toString() + moreIndex,
										Bitmap.CompressFormat.JPEG);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
