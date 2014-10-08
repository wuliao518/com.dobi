package com.doubi.logic.drawView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.doubi.common.ConstValue;
import com.doubi.logic.ImageManager;
import com.doubi.logic.ImageManager.RectangleManager;

/**
 * 自定义扮演控件基类
 * 
 * @author YHZ
 */
public class DrawViewBase extends ImageView {

	protected static Bitmap sceneBitmap; // 场景
	/**
	 * 控件内道具以外所有图片集合
	 */
	protected Bmp[] mBmps;
	protected ImageManager mImageManager;

	/**
	 * 可显示图片数量
	 */
	protected int imgCount = 0;

	//
	protected boolean matrixCheck = false;

	protected Bmp tempBitmap = null;// 一般代表最顶层的图片
	protected Canvas canvas;
	protected float X = 0f;
	protected float Y = 0f;
	protected float DownX = 0f;
	protected float DownY = 0f;
	protected float CX = 0f;
	protected float CY = 0f;
	protected boolean Begin = true;
	protected float rotalC[] = new float[2];
	protected float rotalP[] = new float[2];
	protected float rotalP_2[] = new float[2];
	protected int twoPoint = 0;

	/**
	 * 每次执行放大，放大前的两指距离
	 */
	protected float preLength = 480.0f;
	/**
	 * 每次执行放大，放大后的两只距离
	 */
	protected float length = 480.0f;
	/**
	 * 旋转前两指角度
	 */
	protected float preCos = 0f;
	/**
	 * 旋转后两指角度
	 */
	protected float cos = 0f;
	protected boolean bool = true;
	/**
	 * 临时保存组合图片用的Canvas，内部包含全局变量：saveBitmap
	 */
	protected Canvas saveCanvas;
	/**
	 * 临时保存组合图片
	 */
	protected Bitmap saveBitmap;
	/**
	 * 除道具以外的图片数量
	 */
	protected int baseImgCount = 0;

	/**
	 * 场景宽度
	 */
	protected int cjWidth;
	/**
	 * 场景高度
	 */
	protected int cjHeight;

	/**
	 * 计算放缩比例用
	 */
	protected final float SCALE_PRE = 7f;

	// 记录屏幕范围
	protected int widthScreen;
	protected int heightScreen;

	protected final static int PROP_COUNT = 20;// 道具默认上限
	/**
	 * 道具列表
	 */
	protected static Bmp[] propBmps;

	public DrawViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageManager = new ImageManager();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	/**
	 * 初始化
	 * 
	 * @param activity
	 * @param mBitmap
	 *            该Activity的图片控件上的图片资源
	 * @param cjwidth
	 *            控件宽度
	 * @param cjheight
	 *            控件高度
	 */
	protected void Inteligense(Activity activity, Bitmap mBitmap, int cjwidth,
			int cjheight) {
		cjWidth = cjwidth;
		cjHeight = cjheight;
		// 计算屏幕边界
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		widthScreen = dm.widthPixels;
		heightScreen = dm.heightPixels;

		// 定义场景需要显示图片的数量
		mBmps = new Bmp[imgCount];
		
		
		// 加载场景
		if (sceneBitmap == null) {
			sceneBitmap = Bitmap.createScaledBitmap(mBitmap, cjWidth, cjHeight,
					false);
		}
		mBitmap.recycle();
	}

	/**
	 * 获取包含当前所有装扮内容的Bitmap，由子类调用
	 * 
	 * @return
	 */
	public Bitmap getCurrentPic() {
		Bitmap resultBitmap = Bitmap.createBitmap(sceneBitmap.getWidth(),
				sceneBitmap.getHeight(), ConstValue.MY_CONFIG_8888);
		Canvas saveToPhoneCanvas = new Canvas(resultBitmap);

		saveToPhoneCanvas.drawBitmap(sceneBitmap, 0, 0, null);
		for (Bmp mBmp : mBmps) {
			if (mBmp != null) {
				mBmp.cancelHighLight();
				saveToPhoneCanvas.drawBitmap(mBmp.getPic(), mBmp.matrix, null);
			}
		}

		return resultBitmap;
	}

	/**
	 * 控制上下显示顺序，点击那个图片，那个图片显示到上面
	 * 
	 * @param event
	 */
	protected void order(float x, float y) {

		Bmp temp = null;
		for (int i = imgCount - 1; i > -1; i--) {

			Bmp mBmp = this.findByPiority(mBmps, i);
			if (mBmp != null) {

				// 更新该值后，isPoint才能判断正确
				rotalP = rotalPoint(new float[] { x, y }, mBmp.preX, mBmp.preY,
						mBmp.width / 2, mBmp.height / 2, mBmp.matrix);

				if (isPoint(i, x, y)) {
					selectMap(mBmp);
					temp = mBmp;
					for (Bmp bmp : mBmps) {
						if (bmp != null
								&& bmp.getPriority() > mBmp.getPriority()) {
							bmp.priority--;
						}
					}
					temp.setPiority(imgCount - 1);
					Begin = true;
					Log.i("jiang", "选中");
					return;
				}
			}
		}
	}

	/**
	 * 次序为i的图片是否被点击到
	 * 
	 * @param i
	 * @return
	 */
	private boolean isPoint(int i, float X, float Y) {
		boolean result = true;
		Bmp bmp = this.findByPiority(mBmps, i);
		// X坐标是否在图片范围内
		if (Math.abs(bmp.getXY(1) - rotalP[0]) > bmp.width / 2) {
			result = false;
		}
		// Y坐标是否在图片范围内
		if (result && Math.abs(bmp.getXY(2) - rotalP[1]) > bmp.height / 2) {
			result = false;
		}
		// 点击的是否为透明部分
		if (!bmp.isEasySelect) {
			if ((int) (bmp.getWidth() / 2 + (int) X - (int) bmp.getXY(1)) > bmp
					.getWidth()
					|| (int) (bmp.getHeight() / 2 + (int) Y - (int) bmp
							.getXY(2)) > bmp.getHeight()) {
				result = false;
			}
			if (result) {
				int PixelX = (int) (bmp.getWidth() / 2 + (int) X - (int) bmp
						.getXY(1));
				int PixelY = (int) (bmp.getHeight() / 2 + (int) Y - (int) bmp
						.getXY(2));
				if (bmp.getPic().getWidth() > PixelX
						&& bmp.getPic().getHeight() > PixelY) {
					try {
						if (bmp.getPic().getPixel(PixelX, PixelY) == 0) {
							result = false;
						}
					} catch (Exception e) {

					}
				}
			}
		}
		return result;
	}

	/**
	 * 根据显示级别获取图片
	 * 
	 * @param bmps
	 * @param priority
	 * @return
	 */
	protected Bmp findByPiority(Bmp[] bmps, int priority) {
		for (Bmp p : bmps) {
			if (p != null && p.priority == priority) {
				return p;
			}
		}
		return null;
	}

	/**
	 * 选中某图片，取消其他图片的选中状态
	 * 
	 * @param bm
	 */
	protected void selectMap(Bmp bm) {
		if (bm.canChange) {
			for (Bmp mBmp : mBmps) {
				if (mBmp != null && mBmp.isFocus()) {
					// 取消其他选中光圈
					mBmp.cancelHighLight();
					mBmp.setFocus(false);
				}
			}

			bm.addHighLight();
			bm.setFocus(true);
		}
	}

	/**
	 * 取消当前选中图片的光圈
	 * 
	 * @param index
	 */
	public void CancelAllSelect() {
		if (mBmps != null) {
			for (Bmp mBmp : mBmps) {
				if (mBmp != null && mBmp.isFocus()) {
					mBmp.cancelHighLight();
				}
			}
		}
	}

	/**
	 * 获取图片集合
	 * 
	 * @return
	 */
	public Bmp[] getmBmps() {
		return mBmps;
	}

	/**
	 * 获取最顶层图片
	 * 
	 * @return
	 */
	protected Bmp getTopMap() {
		return this.findByPiority(mBmps, imgCount - 1);
	}

	/**
	 * 获取选中的图片
	 * 
	 * @return
	 */
	protected Bmp getFoucusMap() {
		Bmp bmp = null;
		for (Bmp mBmp : mBmps) {
			// 取消其他选中光圈
			if (mBmp != null && mBmp.isFocus()) {
				bmp = mBmp;
			}
		}
		return bmp;
	}

	protected boolean matrixCheck(Matrix matrix1, Bitmap bitmap) {
		float[] f = new float[9];
		matrix1.getValues(f);
		// 图片4个顶点的坐标
		float x1 = f[0] * 0 + f[1] * 0 + f[2];
		float y1 = f[3] * 0 + f[4] * 0 + f[5];
		float x2 = f[0] * bitmap.getWidth() + f[1] * 0 + f[2];
		float y2 = f[3] * bitmap.getWidth() + f[4] * 0 + f[5];
		float x3 = f[0] * 0 + f[1] * bitmap.getHeight() + f[2];
		float y3 = f[3] * 0 + f[4] * bitmap.getHeight() + f[5];
		float x4 = f[0] * bitmap.getWidth() + f[1] * bitmap.getHeight() + f[2];
		float y4 = f[3] * bitmap.getWidth() + f[4] * bitmap.getHeight() + f[5];
		// 图片现宽度
		double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		// 缩放比率判断
		if (width < widthScreen / 3 || width > widthScreen * 3) {
			return true;
		}
		// 出界判断
		if ((x1 < widthScreen / 3 && x2 < widthScreen / 3
				&& x3 < widthScreen / 3 && x4 < widthScreen / 3)
				|| (x1 > widthScreen * 2 / 3 && x2 > widthScreen * 2 / 3
						&& x3 > widthScreen * 2 / 3 && x4 > widthScreen * 2 / 3)
				|| (y1 < heightScreen / 3 && y2 < heightScreen / 3
						&& y3 < heightScreen / 3 && y4 < heightScreen / 3)
				|| (y1 > heightScreen * 2 / 3 && y2 > heightScreen * 2 / 3
						&& y3 > heightScreen * 2 / 3 && y4 > heightScreen * 2 / 3)) {
			return true;
		}
		return false;
	}

	/**
	 * 初始化画面内容数组，mBmps项目所有项都是新创建的情况必须调用该方法
	 */
	protected void intAllBmps() {
		for (int i = 0; i < imgCount; i++) {
			if (mBmps[i] != null) {
				// 保存最初尺寸
				mBmps[i].width = mBmps[i].getPic().getWidth();
				mBmps[i].height = mBmps[i].getPic().getHeight();
				// 根据添加顺序为图片设立唯一标示
				mBmps[i].setImgId(i);
				mBmps[i].intBitmap();
			}
		}
	}

	/**
	 * 初始化画面内容数组中某个图片类
	 */
	protected void intBmp(int i) {
		if (mBmps[i] != null) {
			// 保存最初尺寸
			mBmps[i].width = mBmps[i].getPic().getWidth();
			mBmps[i].height = mBmps[i].getPic().getHeight();
			// 根据添加顺序为图片设立唯一标示
			mBmps[i].setImgId(i);
			mBmps[i].intBitmap();
		}
	}
	protected void intBmp(int i,float width,float height) {
		if (mBmps[i] != null) {
			// 保存最初尺寸
			mBmps[i].width = width;
			mBmps[i].height = height;
			// 根据添加顺序为图片设立唯一标示
			float scale=width/mBmps[i].width;
			mBmps[i].setImgId(i);
			mBmps[i].intBitmap();
		}
	}

	/**
	 * 释放所有图层
	 */
	protected void recycleBmps() {
		for (Bmp mBmp : mBmps) {
			if (mBmp != null) {
				mBmp.recycleMap();
			}
		}
	}

	/** ============内部算法，不需调整===========start======= */

	public float[] getT(float preX, float preY, float x, float y, Matrix matrix) {
		float[] re = new float[2];
		float[] matrixArray = new float[9];
		matrix.getValues(matrixArray);
		float a = x - preX * matrixArray[0] - preY * matrixArray[1];
		float b = y - preX * matrixArray[3] - preY * matrixArray[4];
		matrixArray[2] = a;
		matrixArray[5] = b;
		matrix.setValues(matrixArray);
		re[0] = a;
		re[1] = b;
		return re;
	}

	public void scale(float preX, float preY, float x, float y, Matrix matrix) {
		float[] matrixArray = new float[9];
		matrix.getValues(matrixArray);
		float a = x - preX;
		float b = y - preY;
		matrixArray[2] = a;
		matrixArray[5] = b;
		matrix.setValues(matrixArray);
	}
	//位移归零
	public void setToO(Matrix matrix) {
		float[] matrixArray = new float[9];
		matrix.getValues(matrixArray);
		float a = 0f;
		float b = 0f;
		matrixArray[2] = a;
		matrixArray[5] = b;
		matrix.setValues(matrixArray);
	}

	public float[] rotalPoint(float[] p, float X, float Y, float width,
			float height, Matrix matrix) {
		float re[] = new float[2];
		float matrixArray[] = new float[9];
		matrix.getValues(matrixArray);
		float a = p[0] - X;
		float b = p[1] - Y;
		re[0] = a * matrixArray[0] - b * matrixArray[1] + X;
		re[1] = -a * matrixArray[3] + b * matrixArray[4] + Y;
		return re;
	}

	// 计算长度
	@SuppressLint("FloatMath")
	protected float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	// 计算余弦
	@SuppressLint("FloatMath")
	protected float cos(MotionEvent event) {
		if ((event.getX(0) - event.getX(1)) * (event.getY(0) - event.getY(1)) > 0) {
			return (float) ((float) Math.acos(Math.abs(event.getX(0)
					- event.getX(1))
					/ spacing(event))
					/ Math.PI * 180f);
		}
		if ((event.getX(0) - event.getX(1)) * (event.getY(0) - event.getY(1)) < 0) {
			return (float) ((float) Math.acos(-Math.abs(event.getX(0)
					- event.getX(1))
					/ spacing(event))
					/ Math.PI * 180f);
		}
		if (event.getX(0) - event.getX(1) == 0) {
			return (float) 90f;
		}
		if (event.getY(0) - event.getY(1) == 0) {
			return 0f;
		}
		return 45f;
	}

	/** ===============内部算法，不需调整 ===========end====================* */

	/**
	 * 每个图片单体类
	 * 
	 * @author Administrator
	 *
	 */
	public class Bmp {
		/**
		 * 负责移动、放缩、旋转bitmap的类
		 */
		protected Matrix matrix;

		// 放缩前尺寸
		protected float width = 0;
		protected float height = 0;
		/**
		 * 图片
		 */
		private Bitmap pic = null;

		/**
		 * 图片最终显示层级
		 */
		protected int priorityBase = 0;

		/**
		 * 图片中心在控件的X坐标
		 */
		private float preX = 0;
		/**
		 * 图片中心在控件的Y坐标
		 */
		private float preY = 0;

		/**
		 * bitmap对应的当前显示层级别的index
		 */
		private int priority = 0;
		/**
		 * 图片标识
		 */
		private int imgId = 0;
		/**
		 * 是否可调整
		 */
		private boolean canChange;
		/**
		 * 是否只能平移
		 */
		private boolean onlyCanTranslation;

		/**
		 * 是否被选中
		 */
		private boolean isFocus;
		/**
		 * 基础图片
		 */
		private Bitmap basePic;

		/**
		 * 是否容易选中（判断选中状态是否支持触碰透明部分）
		 */
		private boolean isEasySelect;

		/**
		 * 图片已旋转的角度
		 */
		private float rotateSize;

		// 构造器
		private Bmp(Bitmap pic, int piority) {
			this.pic = pic;
			this.basePic = pic;
			this.priority = piority;
			this.priorityBase = piority;
		}

		/**
		 * @param pic
		 *            :the Bitmap to draw
		 * @param priority
		 *            : bitmap对应的index
		 * @param preX
		 *            坐标
		 * @param preY
		 *            坐标
		 * @param iscanChange
		 *            是否可被选中
		 * @param isEasySelect
		 *            是否可点中透明部分
		 * @param isOnlyCanTranslation
		 *            是否只能平移
		 */
		protected Bmp(Bitmap pic, int priority, float preX, float preY,
				boolean iscanChange, boolean isEasySelect,
				boolean isOnlyCanTranslation) {
			this(pic, priority);
			this.preX = preX;// + pic.getWidth() / 2 * 1.5f;
			this.preY = preY;// + pic.getHeight() / 2 * 1.5f;
			this.canChange = iscanChange;
			this.isEasySelect = isEasySelect;
			this.onlyCanTranslation = isOnlyCanTranslation;
			if (matrix == null) {
				matrix = new Matrix();
			}
		}

		/**
		 * 对初始化过的Bmp重新赋值，保证不影响原来旋转的角度
		 * 
		 * @param pic
		 * @param priority
		 *            bitmap对应的index
		 * @param preX
		 *            坐标
		 * @param preY
		 *            坐标
		 */
		protected void SetBmpInfo(Bitmap pic) {

			this.pic = pic;
			this.basePic = pic;
		}

		// set Priority
		protected void setPiority(int priority) {
			this.priority = priority;
		}

		// return Priority
		protected int getPriority() {
			return this.priority;
		}

		/**
		 * 获取图片中心位置所在坐标
		 * 
		 * @param i
		 *            1X坐标，2Y坐标
		 * @return 图片中心位置所在坐标
		 */
		@SuppressWarnings("null")
		protected float getXY(int i) {
			if (i == 1) {
				return this.preX;
			} else if (i == 2) {
				return this.preY;
			}
			return (Float) null;
		}

		/**
		 * 设置图片中心位置X坐标
		 * 
		 * @param x
		 */
		protected void setPreX(float x) {
			this.preX = x;
		}

		/**
		 * 设置图片中心位置Y坐标
		 * 
		 * @param x
		 */
		protected void setPreY(float y) {
			this.preY = y;
		}

		protected void setPic(Bitmap pic) {
			this.pic = pic;
		}

		/**
		 * getPicture
		 * 
		 * @return
		 */
		public Bitmap getPic() {
			return this.pic;
		}

		/**
		 * 获取放缩前宽度
		 * 
		 * @return
		 */
		protected float getWidth() {
			return width;
		}

		/**
		 * 获取放缩前高度
		 * 
		 * @return
		 */
		protected float getHeight() {
			return height;
		}

		/**
		 * 获取图片唯一标示，从0开始
		 * 
		 * @return
		 */
		protected int getImgId() {
			return imgId;
		}

		/**
		 * 设置唯一标示
		 * 
		 * @param imgId
		 */
		protected void setImgId(int imgId) {
			this.imgId = imgId;
		}

		/**
		 * 获取是否可调整
		 * 
		 * @return
		 */
		protected boolean isCanChange() {
			return canChange;
		}

		/**
		 * 是否可调整
		 * 
		 * @return
		 */
		public void setCanChange(boolean canChange) {
			this.canChange = canChange;
		}

		protected void setBasePic(Bitmap basePic) {
			this.basePic = basePic;
		}

		/**
		 * 增加光圈
		 */
		protected void addHighLight() {

			/*
			 * 津贴图片边缘模式，不够清晰问题未解决 Paint p = new Paint();
			 * p.setColor(Color.BLUE);// 红色的光晕 p.setStyle(Paint.Style.STROKE);//
			 * 设置空心 p.setStrokeWidth(40f);
			 * 
			 * Bitmap b = this.pic; Bitmap bitmap =
			 * Bitmap.createBitmap(b.getWidth(), b.getHeight(),
			 * ConstValue.MY_CONFIG); Canvas canvas = new Canvas(bitmap);
			 * canvas.drawBitmap(b.extractAlpha(), 0, 0, p); StateListDrawable
			 * sld = new StateListDrawable(); sld.addState(new int[] {
			 * android.R.attr.state_pressed }, new BitmapDrawable(bitmap));
			 * DrawViewBase.this.setBackgroundDrawable(sld);
			 * 
			 * canvas.drawBitmap(b, 0, 0, null);
			 */

			Bitmap b = this.pic;
			Bitmap bitmap = Bitmap.createBitmap((int) this.getWidth(),
					(int) this.getHeight(), ConstValue.MY_CONFIG_8888);
			Canvas tempcanvas = new Canvas(bitmap);
			RectF rec = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());// 画边框

			Paint paint = new Paint(); // 设置边框颜色
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.STROKE); // 设置边框宽度
			paint.setStrokeWidth(3f);

			tempcanvas.drawBitmap(b, 0, 0, null);
			tempcanvas.drawRoundRect(rec, 20f, 20f, paint);
			this.pic = bitmap;
		}

		/**
		 * 取消光圈
		 */
		protected void cancelHighLight() {
			if (this.width > 0 && this.height > 0) {
				this.pic = null;
				this.pic = Bitmap.createScaledBitmap(this.basePic,
						(int) this.width, (int) this.height, false);
			}
		}

		/**
		 * 是否拥有焦点
		 * 
		 * @return
		 */
		protected boolean isFocus() {
			return isFocus;
		}

		protected void setFocus(boolean isFocus) {
			this.isFocus = isFocus;
		}

		/**
		 * 是否可点中透明部分
		 * 
		 * @return
		 */
		protected boolean isEasySelect() {
			return isEasySelect;
		}

		/**
		 * 初次加载的图片初始化位置，保证以中心为轴旋转
		 */
		protected void intBitmap() {
			this.matrix = new Matrix();
			this.matrix.preTranslate(this.preX - this.width / 2, this.preY
					- this.height / 2);
		}

		/**
		 * 是否只能平移
		 * 
		 * @return
		 */
		protected boolean isOnlyCanTranslation() {
			return onlyCanTranslation;
		}

		/**
		 * 获取已经旋转的角度
		 * 
		 * @return
		 */
		protected float getRotateSize() {
			return rotateSize;
		}

		/**
		 * 设置已经旋转的角度
		 * 
		 * @param rotateSize
		 */
		protected void setRotateSize(float rotateSize) {
			this.rotateSize = rotateSize;
		}

		/**
		 * 获取对应的矩形类
		 * 
		 * @return
		 */
		protected RectangleManager getRectangle() {
			RectangleManager result = mImageManager.new RectangleManager(
					this.preX, this.preY, this.pic.getWidth(),
					this.pic.getHeight(), this.rotateSize);
			return result;
		}

		/**
		 * 释放图片内存
		 */
		protected void recycleMap() {
			if (this.basePic != null) {
				this.basePic.recycle();
				this.basePic = null;
			}
			if (this.pic != null) {
				this.pic.recycle();
				this.pic = null;
			}
		}

		@Override
		public String toString() {
			return "Bmp [width=" + width + ", height=" + height
					+ ", priorityBase=" + priorityBase + ", preX=" + preX
					+ ", preY=" + preY + ", priority=" + priority + ", imgId="
					+ imgId + ",  rotateSize="
					+ rotateSize + "]";
		}
		
	}
}
