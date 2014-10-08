package com.doubi.logic.drawView;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.AttributeSet;

import com.dobi.R;
import com.doubi.common.ConstValue;
import com.doubi.logic.ImageManager.RectangleManager;
import com.doubi.logic.svgResolve.SVG;
import com.doubi.logic.svgResolve.SVGParser;

/**
 * 自定义扮演控件-化妆
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class HeadDrawView extends SingleDrawViewBase {
	private final float FACE_EXP_SIZE = 0.23f;// 正比放大脸部，补充透明部分.
	/**
	 * 构造函数
	 * 
	 * @param context
	 * @param attrs
	 */
	public HeadDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseImgCount = 6;
		// 定义场景需要显示图片的数量
		imgCount = baseImgCount + PROP_COUNT;
	}
	/**
	 * 初始化
	 * 
	 * @param
	 * @param mBitmap
	 *            该Activity的图片控件上的图片资源
	 */
	public void Inteligense(Activity activity, Bitmap mBitmap, int cj_width,
			int cj_height) {
		// 必需调用
		super.Inteligense(activity, mBitmap, cj_width, cj_height);
		if (saveHeadBmps == null||clothesBmp.getPic()==null) {
			// 加载头部、身体
			Bitmap lianBitmap = BitmapFactory.decodeFile(Environment
					.getExternalStorageDirectory()
					+ ConstValue.ROOT_PATH
					+ ConstValue.ImgName.singlePhotoClip.toString() + "jpg");
			// 第一次进入改成瓜子脸
			InputStream mInputStream = getResources().openRawResource(
					R.raw.default_face);
			SVG mSVG = SVGParser.getSVGFromResource(mInputStream);
			lianBitmap = mImageManager.ClipBitmapOnSVG(lianBitmap, mSVG);
			List<Bitmap> bodyList = mImageManager.GetAllBitmaps(Environment

			.getExternalStorageDirectory() + ConstValue.ROOT_PATH
					+ ConstValue.DEFAULT_CLOTHES_PATH);
			Bitmap bodyBitmap = bodyList.get(0);
			List<Bitmap> hairlList = mImageManager.GetAllBitmaps(Environment
			.getExternalStorageDirectory() + ConstValue.ROOT_PATH
					+ ConstValue.DEFAULT_HAIR_PATH);
			Bitmap hairBitmap = hairlList.get(0);
			// 计算脸部合适宽度，计算放缩比例，同比例处理其他图片
			scale = (cj_width / SCALE_PRE) / ConstValue.FACE_BASE_WIDTH;// 整体放缩比例
			// 正比调整发饰相对衣服的Y坐标
			float expHeadTop = 3f;
			// 正比调整脸部相对发饰Y坐标
			float expFaceTop = 4.4f;
			// 控制显示比例
			lianBitmap = Bitmap.createScaledBitmap(lianBitmap,
					(int) (lianBitmap.getWidth() * this.FACE_EXP_SIZE),
					(int) (lianBitmap.getHeight() * this.FACE_EXP_SIZE), true);
			hairBitmap = Bitmap.createScaledBitmap(hairBitmap,
					(int) (hairBitmap.getWidth() * scale),
					(int) (hairBitmap.getHeight() * scale), false);
			bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap,
					(int) (bodyBitmap.getWidth() * scale),
					(int) (bodyBitmap.getHeight() * scale), false);

			// 确定坐标
			float body_x = cj_width * 71 / 100;
			float body_y = cj_height * 59 / 100;

			float head_x = cj_width / 2 + hairBitmap.getWidth() * 78 / 100;
			float head_y = (float) (body_y - bodyBitmap.getHeight() * 45 / 100 - hairBitmap
					.getHeight() / expHeadTop);
			float face_x = head_x - lianBitmap.getWidth() * 2 / 100;
			float face_y = head_y + cj_height * expFaceTop / 100;

			// 设置图片数量
			int len = imgCount;

			Bmp bmp[] = new Bmp[len];
			{
				// 越排在后面，显示级别越高 0,1,2,3,4,5： 衣服，脸，眉毛，腮红，胡子，发饰
				bmp[0] = new Bmp(bodyBitmap, 0, body_x, body_y, false, false,
						false);
				bmp[1] = new Bmp(lianBitmap, 1, face_x, face_y, true, false,
						false);
				bmp[5] = new Bmp(hairBitmap, 5, head_x, head_y, false, false,
						false);
			}

			this.mBmps = bmp;
			faceBmp=lianBmp;
			headX = this.mBmps[1].getXY(1);
			headY = this.mBmps[1].getXY(2);
			this.intAllBmps();
		} else {
			this.mBmps = saveHeadBmps;
			if (clothesBmp != null) {
				System.out.println("clothesBmp");
				int i = 0;
				mBmps[i] = new Bmp(clothesBmp.getPic(), i, clothesBmp.getXY(1),
						clothesBmp.getXY(2), false, false, false);
				mBmps[i].matrix=clothesBmp.matrix;
				mBmps[i].setRotateSize(clothesBmp.getRotateSize());
				intBmp(i);

			}
			if (hairBmp != null) {
				System.out.println("hairBmp");
				int i = 5;
				mBmps[i] = new Bmp(hairBmp.getPic(), i, hairBmp.getXY(1),
						hairBmp.getXY(2), false, false, false);
				intBmp(i);
			}
			if(lianBmp!=null){
				System.out.println("lianBmp");
				int i = 1;
				mBmps[i] = new Bmp(lianBmp.getPic(), i, lianBmp.getXY(1),
						lianBmp.getXY(2), true, false, false);
				intBmp(i);
			}

			if(eyebrowBmp!=null){
				System.out.println("eyebrowBmp");
				int i = 2;
				if(mBmps[2]!=null){
					float newFaceX = eyebrowBmp.getXY(1);
					float newFacey = eyebrowBmp.getXY(2);
					mBmps[i].setPreX(newFaceX);
					mBmps[i].setPreY(newFacey);
					intBmp(i);
				}
				
			}
			if(saihongBmp!=null){
				int i = 3;
				if(mBmps[3]!=null){
					float newFaceX = saihongBmp.getXY(1);
					float newFacey = saihongBmp.getXY(2);
					mBmps[i].setPreX(newFaceX);
					mBmps[i].setPreY(newFacey);
					intBmp(i);
				}
				
			}
			if(huziBmp!=null){
				int i = 4;
				if(mBmps[4]!=null){
					float newFaceX = huziBmp.getXY(1);
					float newFacey = huziBmp.getXY(2);
					mBmps[i].setPreX(newFaceX);
					mBmps[i].setPreY(newFacey);
					intBmp(i);
				}
			}
		}

		if (propBmps != null) {
			for (int i = 0; i < 20; i++) {
				if (propBmps[i] != null) {
					propBmps[i].setCanChange(false);
				}
				mBmps[i + baseImgCount] = propBmps[i];
				if (mBmps[i + baseImgCount] != null) {
					mBmps[i + baseImgCount].setImgId(i + baseImgCount);
				}
			}
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mBmps != null) {
			// 保存记录
			lianBmp=mBmps[1];
			clothesBmp = mBmps[0];
			hairBmp = mBmps[5];
			if(mBmps[2]!=null){
				eyebrowBmp=mBmps[2];
			}
			if(mBmps[3]!=null){
				saihongBmp=mBmps[3];
			}
			if(mBmps[4]!=null){
				huziBmp=mBmps[4];
			}
		}
		saveHeadBmps = mBmps;
	}

	/**
	 * 设置脸型
	 * 
	 */
	public void setFace(SVG mSVG) {
		// 切割图片
		Bitmap faceBitmap = BitmapFactory.decodeFile(Environment
				.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH
				+ ConstValue.ImgName.singlePhotoClip.toString() + "jpg");

		// InputStream mInputStream = getResources().openRawResource(
		// R.raw.base_face);
		// mSVG = SVGParser.getSVGFromResource(mInputStream);

		faceBitmap = mImageManager.ClipBitmapOnSVG(faceBitmap, mSVG);
		// 控制显示比例
		faceBitmap = Bitmap.createScaledBitmap(faceBitmap,
				(int) (faceBitmap.getWidth() * this.FACE_EXP_SIZE),
				(int) (faceBitmap.getHeight() * this.FACE_EXP_SIZE), true);

		int bmpsIndex = 1;
		if (faceBitmap != null) {
			float x = this.mBmps[1].getXY(1);
			float y = this.mBmps[1].getXY(2);

			this.intmaps(bmpsIndex, x, y, faceBitmap);
		} else {
			cancelMsaps(bmpsIndex);
		}
	}

	/**
	 * 加载眉毛
	 * 
	 */
	public void setEyebrows(Bitmap mBitmap) {
		Bitmap eyebrowsBitmap = mBitmap;
		int bmpsIndex = 2;
		if (eyebrowsBitmap != null) {
			// 放大
			eyebrowsBitmap = Bitmap.createScaledBitmap(eyebrowsBitmap,
					(int) (eyebrowsBitmap.getWidth() * scale),
					(int) (eyebrowsBitmap.getHeight() * scale), false);

			float x = this.mBmps[1].getXY(1);
			float scale = 7f;// 正比例影响相对脸部位置
			float y = this.mBmps[1].getXY(2) - this.mBmps[1].getHeight()
					/ scale;

			this.intmaps(bmpsIndex, x, y, eyebrowsBitmap);

		} else {
			cancelMsaps(bmpsIndex);
		}
	}

	/**
	 * 加载腮红
	 * 
	 */
	public void setBlusher(Bitmap mBitmap) {
		Bitmap blusherBitmap = mBitmap;
		int bmpsIndex = 3;
		if (blusherBitmap != null) {
			// 放大
			blusherBitmap = Bitmap.createScaledBitmap(blusherBitmap,
					(int) (blusherBitmap.getWidth() * scale),
					(int) (blusherBitmap.getHeight() * scale), false);

			float x = this.mBmps[1].getXY(1);
			float scale = 80f;// 反比例影响相对脸部位置
			float y = this.mBmps[1].getXY(2) + this.mBmps[1].getHeight()
					/ scale;

			this.intmaps(bmpsIndex, x, y, blusherBitmap);
		} else {
			cancelMsaps(bmpsIndex);
		}
	}

	/**
	 * 增加胡子
	 * 
	 */
	public void setBeard(Bitmap mBitmap) {
		Bitmap beardBitmap = mBitmap;
		int bmpsIndex = 4;
		if (beardBitmap != null) {
			// 放大
			beardBitmap = Bitmap.createScaledBitmap(beardBitmap,
					(int) (beardBitmap.getWidth() * scale),
					(int) (beardBitmap.getHeight() * scale), false);

			float x = this.mBmps[1].getXY(1);
			float scale = 8.5f;// 反比例影响相对脸部位置
			float y = this.mBmps[1].getXY(2) + this.mBmps[1].getHeight()
					/ scale;
			this.intmaps(bmpsIndex, x, y, beardBitmap);
		} else {
			cancelMsaps(bmpsIndex);
		}
	}

	/**
	 * 初始化五官素材
	 * 
	 * @param bmpsIndex
	 *            排序
	 * @param eyebrows_x
	 *            X坐标
	 * @param eyebrows_y
	 *            Y坐标
	 */
	private void intmaps(int bmpsIndex, float eyebrows_x, float eyebrows_y,
			Bitmap mBitmap) {
		this.mBmps[bmpsIndex] = new Bmp(mBitmap, bmpsIndex, eyebrows_x,
				eyebrows_y, true, true, false);

		this.mBmps[bmpsIndex].width = this.mBmps[bmpsIndex].getPic().getWidth();
		this.mBmps[bmpsIndex].height = this.mBmps[bmpsIndex].getPic()
				.getHeight();
		// 根据添加顺序为图片设立唯一标示
		this.mBmps[bmpsIndex].setImgId(bmpsIndex);
		this.mBmps[bmpsIndex].intBitmap();

		this.selectMap(mBmps[bmpsIndex]);

		this.invalidate();
	}

	/**
	 * 取消无关素材
	 * 
	 * @param bmpsIndex
	 */
	private void cancelMsaps(int bmpsIndex) {
		this.mBmps[bmpsIndex] = null;
		this.invalidate();
	}

	/**
	 * 保存脸部组合图片到内存，变量名：faceBmp
	 */
	public void saveFaceBitmap() {
		for (Bmp bmp : mBmps) {
			if (bmp != null && bmp.getImgId() != 0 && bmp.getImgId() != 5
					&& bmp.getImgId() < this.baseImgCount) {
				if (bmp.isFocus()) {
					bmp.cancelHighLight();
				}
				saveCanvas.drawBitmap(bmp.getPic(), bmp.matrix, null);
			}
		}
		// 计算坐标
		RectangleManager mRectangleManager = mImageManager.getNewRectangle(
				mBmps[1].getRectangle(),
				mBmps[2] == null ? null : mBmps[2].getRectangle(),
				mBmps[3] == null ? null : mBmps[3].getRectangle(),
				mBmps[4] == null ? null : mBmps[4].getRectangle());

		faceBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * 化妆直接进入选择场景的保存
	 * 
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
				mBmps[2] == null ? null : mBmps[2].getRectangle(),
				mBmps[3] == null ? null : mBmps[3].getRectangle(),
				mBmps[4] == null ? null : mBmps[4].getRectangle(),
				mBmps[5].getRectangle());
		bodyBmp = this.getBodyBmpForScene(mRectangleManager);
	}

	/**
	 * 使相应图片进入选中状态
	 * 
	 * @param index
	 *            0,1,2,3,4,5： 衣服，脸，眉毛，腮红，胡子，发饰
	 */
	@Override
	public void selectWidget(int index) {
		super.selectWidget(index);
	}
	
	
	
	
	
//	
//	 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//	  //文件夹aaaa
//	  String path = Environment.getExternalStorageDirectory().toString()+"/aaaa";
//	  File path1 = new File(path);
//	  if(!path1.exists()){
//	   path1.mkdirs();
//	  }
//	  File file = new File(path1,System.currentTimeMillis()+".jpg");
//	  mOutPutFileUri = Uri.fromFile(file);
//	  intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
//	  startActivityForResult(intent, 1);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
