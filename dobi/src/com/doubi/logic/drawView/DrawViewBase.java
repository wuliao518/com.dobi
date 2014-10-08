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
 * �Զ�����ݿؼ�����
 * 
 * @author YHZ
 */
public class DrawViewBase extends ImageView {

	protected static Bitmap sceneBitmap; // ����
	/**
	 * �ؼ��ڵ�����������ͼƬ����
	 */
	protected Bmp[] mBmps;
	protected ImageManager mImageManager;

	/**
	 * ����ʾͼƬ����
	 */
	protected int imgCount = 0;

	//
	protected boolean matrixCheck = false;

	protected Bmp tempBitmap = null;// һ���������ͼƬ
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
	 * ÿ��ִ�зŴ󣬷Ŵ�ǰ����ָ����
	 */
	protected float preLength = 480.0f;
	/**
	 * ÿ��ִ�зŴ󣬷Ŵ�����ֻ����
	 */
	protected float length = 480.0f;
	/**
	 * ��תǰ��ָ�Ƕ�
	 */
	protected float preCos = 0f;
	/**
	 * ��ת����ָ�Ƕ�
	 */
	protected float cos = 0f;
	protected boolean bool = true;
	/**
	 * ��ʱ�������ͼƬ�õ�Canvas���ڲ�����ȫ�ֱ�����saveBitmap
	 */
	protected Canvas saveCanvas;
	/**
	 * ��ʱ�������ͼƬ
	 */
	protected Bitmap saveBitmap;
	/**
	 * �����������ͼƬ����
	 */
	protected int baseImgCount = 0;

	/**
	 * �������
	 */
	protected int cjWidth;
	/**
	 * �����߶�
	 */
	protected int cjHeight;

	/**
	 * �������������
	 */
	protected final float SCALE_PRE = 7f;

	// ��¼��Ļ��Χ
	protected int widthScreen;
	protected int heightScreen;

	protected final static int PROP_COUNT = 20;// ����Ĭ������
	/**
	 * �����б�
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
	 * ��ʼ��
	 * 
	 * @param activity
	 * @param mBitmap
	 *            ��Activity��ͼƬ�ؼ��ϵ�ͼƬ��Դ
	 * @param cjwidth
	 *            �ؼ����
	 * @param cjheight
	 *            �ؼ��߶�
	 */
	protected void Inteligense(Activity activity, Bitmap mBitmap, int cjwidth,
			int cjheight) {
		cjWidth = cjwidth;
		cjHeight = cjheight;
		// ������Ļ�߽�
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		widthScreen = dm.widthPixels;
		heightScreen = dm.heightPixels;

		// ���峡����Ҫ��ʾͼƬ������
		mBmps = new Bmp[imgCount];
		
		
		// ���س���
		if (sceneBitmap == null) {
			sceneBitmap = Bitmap.createScaledBitmap(mBitmap, cjWidth, cjHeight,
					false);
		}
		mBitmap.recycle();
	}

	/**
	 * ��ȡ������ǰ����װ�����ݵ�Bitmap�����������
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
	 * ����������ʾ˳�򣬵���Ǹ�ͼƬ���Ǹ�ͼƬ��ʾ������
	 * 
	 * @param event
	 */
	protected void order(float x, float y) {

		Bmp temp = null;
		for (int i = imgCount - 1; i > -1; i--) {

			Bmp mBmp = this.findByPiority(mBmps, i);
			if (mBmp != null) {

				// ���¸�ֵ��isPoint�����ж���ȷ
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
					Log.i("jiang", "ѡ��");
					return;
				}
			}
		}
	}

	/**
	 * ����Ϊi��ͼƬ�Ƿ񱻵����
	 * 
	 * @param i
	 * @return
	 */
	private boolean isPoint(int i, float X, float Y) {
		boolean result = true;
		Bmp bmp = this.findByPiority(mBmps, i);
		// X�����Ƿ���ͼƬ��Χ��
		if (Math.abs(bmp.getXY(1) - rotalP[0]) > bmp.width / 2) {
			result = false;
		}
		// Y�����Ƿ���ͼƬ��Χ��
		if (result && Math.abs(bmp.getXY(2) - rotalP[1]) > bmp.height / 2) {
			result = false;
		}
		// ������Ƿ�Ϊ͸������
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
	 * ������ʾ�����ȡͼƬ
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
	 * ѡ��ĳͼƬ��ȡ������ͼƬ��ѡ��״̬
	 * 
	 * @param bm
	 */
	protected void selectMap(Bmp bm) {
		if (bm.canChange) {
			for (Bmp mBmp : mBmps) {
				if (mBmp != null && mBmp.isFocus()) {
					// ȡ������ѡ�й�Ȧ
					mBmp.cancelHighLight();
					mBmp.setFocus(false);
				}
			}

			bm.addHighLight();
			bm.setFocus(true);
		}
	}

	/**
	 * ȡ����ǰѡ��ͼƬ�Ĺ�Ȧ
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
	 * ��ȡͼƬ����
	 * 
	 * @return
	 */
	public Bmp[] getmBmps() {
		return mBmps;
	}

	/**
	 * ��ȡ���ͼƬ
	 * 
	 * @return
	 */
	protected Bmp getTopMap() {
		return this.findByPiority(mBmps, imgCount - 1);
	}

	/**
	 * ��ȡѡ�е�ͼƬ
	 * 
	 * @return
	 */
	protected Bmp getFoucusMap() {
		Bmp bmp = null;
		for (Bmp mBmp : mBmps) {
			// ȡ������ѡ�й�Ȧ
			if (mBmp != null && mBmp.isFocus()) {
				bmp = mBmp;
			}
		}
		return bmp;
	}

	protected boolean matrixCheck(Matrix matrix1, Bitmap bitmap) {
		float[] f = new float[9];
		matrix1.getValues(f);
		// ͼƬ4�����������
		float x1 = f[0] * 0 + f[1] * 0 + f[2];
		float y1 = f[3] * 0 + f[4] * 0 + f[5];
		float x2 = f[0] * bitmap.getWidth() + f[1] * 0 + f[2];
		float y2 = f[3] * bitmap.getWidth() + f[4] * 0 + f[5];
		float x3 = f[0] * 0 + f[1] * bitmap.getHeight() + f[2];
		float y3 = f[3] * 0 + f[4] * bitmap.getHeight() + f[5];
		float x4 = f[0] * bitmap.getWidth() + f[1] * bitmap.getHeight() + f[2];
		float y4 = f[3] * bitmap.getWidth() + f[4] * bitmap.getHeight() + f[5];
		// ͼƬ�ֿ��
		double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		// ���ű����ж�
		if (width < widthScreen / 3 || width > widthScreen * 3) {
			return true;
		}
		// �����ж�
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
	 * ��ʼ�������������飬mBmps��Ŀ��������´��������������ø÷���
	 */
	protected void intAllBmps() {
		for (int i = 0; i < imgCount; i++) {
			if (mBmps[i] != null) {
				// ��������ߴ�
				mBmps[i].width = mBmps[i].getPic().getWidth();
				mBmps[i].height = mBmps[i].getPic().getHeight();
				// �������˳��ΪͼƬ����Ψһ��ʾ
				mBmps[i].setImgId(i);
				mBmps[i].intBitmap();
			}
		}
	}

	/**
	 * ��ʼ����������������ĳ��ͼƬ��
	 */
	protected void intBmp(int i) {
		if (mBmps[i] != null) {
			// ��������ߴ�
			mBmps[i].width = mBmps[i].getPic().getWidth();
			mBmps[i].height = mBmps[i].getPic().getHeight();
			// �������˳��ΪͼƬ����Ψһ��ʾ
			mBmps[i].setImgId(i);
			mBmps[i].intBitmap();
		}
	}
	protected void intBmp(int i,float width,float height) {
		if (mBmps[i] != null) {
			// ��������ߴ�
			mBmps[i].width = width;
			mBmps[i].height = height;
			// �������˳��ΪͼƬ����Ψһ��ʾ
			float scale=width/mBmps[i].width;
			mBmps[i].setImgId(i);
			mBmps[i].intBitmap();
		}
	}

	/**
	 * �ͷ�����ͼ��
	 */
	protected void recycleBmps() {
		for (Bmp mBmp : mBmps) {
			if (mBmp != null) {
				mBmp.recycleMap();
			}
		}
	}

	/** ============�ڲ��㷨���������===========start======= */

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
	//λ�ƹ���
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

	// ���㳤��
	@SuppressLint("FloatMath")
	protected float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	// ��������
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

	/** ===============�ڲ��㷨��������� ===========end====================* */

	/**
	 * ÿ��ͼƬ������
	 * 
	 * @author Administrator
	 *
	 */
	public class Bmp {
		/**
		 * �����ƶ�����������תbitmap����
		 */
		protected Matrix matrix;

		// ����ǰ�ߴ�
		protected float width = 0;
		protected float height = 0;
		/**
		 * ͼƬ
		 */
		private Bitmap pic = null;

		/**
		 * ͼƬ������ʾ�㼶
		 */
		protected int priorityBase = 0;

		/**
		 * ͼƬ�����ڿؼ���X����
		 */
		private float preX = 0;
		/**
		 * ͼƬ�����ڿؼ���Y����
		 */
		private float preY = 0;

		/**
		 * bitmap��Ӧ�ĵ�ǰ��ʾ�㼶���index
		 */
		private int priority = 0;
		/**
		 * ͼƬ��ʶ
		 */
		private int imgId = 0;
		/**
		 * �Ƿ�ɵ���
		 */
		private boolean canChange;
		/**
		 * �Ƿ�ֻ��ƽ��
		 */
		private boolean onlyCanTranslation;

		/**
		 * �Ƿ�ѡ��
		 */
		private boolean isFocus;
		/**
		 * ����ͼƬ
		 */
		private Bitmap basePic;

		/**
		 * �Ƿ�����ѡ�У��ж�ѡ��״̬�Ƿ�֧�ִ���͸�����֣�
		 */
		private boolean isEasySelect;

		/**
		 * ͼƬ����ת�ĽǶ�
		 */
		private float rotateSize;

		// ������
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
		 *            : bitmap��Ӧ��index
		 * @param preX
		 *            ����
		 * @param preY
		 *            ����
		 * @param iscanChange
		 *            �Ƿ�ɱ�ѡ��
		 * @param isEasySelect
		 *            �Ƿ�ɵ���͸������
		 * @param isOnlyCanTranslation
		 *            �Ƿ�ֻ��ƽ��
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
		 * �Գ�ʼ������Bmp���¸�ֵ����֤��Ӱ��ԭ����ת�ĽǶ�
		 * 
		 * @param pic
		 * @param priority
		 *            bitmap��Ӧ��index
		 * @param preX
		 *            ����
		 * @param preY
		 *            ����
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
		 * ��ȡͼƬ����λ����������
		 * 
		 * @param i
		 *            1X���꣬2Y����
		 * @return ͼƬ����λ����������
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
		 * ����ͼƬ����λ��X����
		 * 
		 * @param x
		 */
		protected void setPreX(float x) {
			this.preX = x;
		}

		/**
		 * ����ͼƬ����λ��Y����
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
		 * ��ȡ����ǰ���
		 * 
		 * @return
		 */
		protected float getWidth() {
			return width;
		}

		/**
		 * ��ȡ����ǰ�߶�
		 * 
		 * @return
		 */
		protected float getHeight() {
			return height;
		}

		/**
		 * ��ȡͼƬΨһ��ʾ����0��ʼ
		 * 
		 * @return
		 */
		protected int getImgId() {
			return imgId;
		}

		/**
		 * ����Ψһ��ʾ
		 * 
		 * @param imgId
		 */
		protected void setImgId(int imgId) {
			this.imgId = imgId;
		}

		/**
		 * ��ȡ�Ƿ�ɵ���
		 * 
		 * @return
		 */
		protected boolean isCanChange() {
			return canChange;
		}

		/**
		 * �Ƿ�ɵ���
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
		 * ���ӹ�Ȧ
		 */
		protected void addHighLight() {

			/*
			 * ����ͼƬ��Եģʽ��������������δ��� Paint p = new Paint();
			 * p.setColor(Color.BLUE);// ��ɫ�Ĺ��� p.setStyle(Paint.Style.STROKE);//
			 * ���ÿ��� p.setStrokeWidth(40f);
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
			RectF rec = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());// ���߿�

			Paint paint = new Paint(); // ���ñ߿���ɫ
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.STROKE); // ���ñ߿���
			paint.setStrokeWidth(3f);

			tempcanvas.drawBitmap(b, 0, 0, null);
			tempcanvas.drawRoundRect(rec, 20f, 20f, paint);
			this.pic = bitmap;
		}

		/**
		 * ȡ����Ȧ
		 */
		protected void cancelHighLight() {
			if (this.width > 0 && this.height > 0) {
				this.pic = null;
				this.pic = Bitmap.createScaledBitmap(this.basePic,
						(int) this.width, (int) this.height, false);
			}
		}

		/**
		 * �Ƿ�ӵ�н���
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
		 * �Ƿ�ɵ���͸������
		 * 
		 * @return
		 */
		protected boolean isEasySelect() {
			return isEasySelect;
		}

		/**
		 * ���μ��ص�ͼƬ��ʼ��λ�ã���֤������Ϊ����ת
		 */
		protected void intBitmap() {
			this.matrix = new Matrix();
			this.matrix.preTranslate(this.preX - this.width / 2, this.preY
					- this.height / 2);
		}

		/**
		 * �Ƿ�ֻ��ƽ��
		 * 
		 * @return
		 */
		protected boolean isOnlyCanTranslation() {
			return onlyCanTranslation;
		}

		/**
		 * ��ȡ�Ѿ���ת�ĽǶ�
		 * 
		 * @return
		 */
		protected float getRotateSize() {
			return rotateSize;
		}

		/**
		 * �����Ѿ���ת�ĽǶ�
		 * 
		 * @param rotateSize
		 */
		protected void setRotateSize(float rotateSize) {
			this.rotateSize = rotateSize;
		}

		/**
		 * ��ȡ��Ӧ�ľ�����
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
		 * �ͷ�ͼƬ�ڴ�
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
