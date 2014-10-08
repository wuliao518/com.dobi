package com.doubi.logic.drawView;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.doubi.common.ConstValue;
import com.doubi.logic.ImageManager;
import com.doubi.view.adapter.item.MoreFaceItem;

/**
 * �Զ�����ݿؼ�����
 * 
 * @author YHZ
 */
public class MoreDrawViewBase extends DrawViewBase {
	private float startX, startY;
	private float CCX, CCY;// ������Ļ��ʼ�����꣬ÿ̧��һ�θ���һ��
	private float tempCCX = 0, tempCCY = 0;// �ƶ�֮ǰ���ִ�λ�ã�ͼƬ�����ƶ�����һ��
	private Handler handler;
	/**
	 * ���浱ǰװ��״̬
	 */
	public static ConstValue.Stage CurrentStage;
	/**
	 * ͼ����ʾ��ķ�����������ʼ�������п��Ʊ���
	 */
	protected static double scale = 1;
	/**
	 * �л�������������浱ǰ����
	 */
	protected static Bmp[] saveSceneBmps;
	/**
	 * ����
	 */
	protected static Bmp[] savePropBmps;

	// ��¼��ǰװ��
	protected static Bmp clothesBmp;// ��װ

	/**
	 * ��������
	 */
	protected static Bmp[] headBmps;

	/**
	 * �����༯��
	 */
	protected static List<MoreFaceItem> moreFaceItems;

	public MoreDrawViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageManager = new ImageManager();
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
		// super.Inteligense(activity, mBitmap, cjwidth, cjheight);
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
			sceneBitmap = Bitmap.createScaledBitmap(mBitmap,
					cjHeight * mBitmap.getWidth() / mBitmap.getHeight(),
					cjHeight, false);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (sceneBitmap != null) {

			saveBitmap = Bitmap.createBitmap(sceneBitmap.getWidth(),
					sceneBitmap.getHeight(), ConstValue.MY_CONFIG_8888);
			saveCanvas = new Canvas(saveBitmap);

			canvas.drawBitmap(sceneBitmap, 0, 0, null);
			if (mBmps != null) {
				for (Bmp mBmp : mBmps) {
					if (mBmp != null) {
						canvas.drawBitmap(mBmp.getPic(), mBmp.matrix, null);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// ���ϵ���
		if (// event.getAction() == MotionEvent.ACTION_UP
		event.getAction() == MotionEvent.ACTION_POINTER_1_UP
				|| event.getAction() == MotionEvent.ACTION_POINTER_2_UP) {
			bool = true;
			if (twoPoint > 0)
				twoPoint--;
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN
				&& event.getPointerCount() == 1) {
			order(event.getX(), event.getY());

			tempBitmap = this.getFoucusMap();

			this.X = event.getX();
			this.Y = event.getY();
			startX = event.getX();
			startY = event.getY();
			if (tempBitmap != null) {
				CX = tempBitmap.getXY(1) - event.getX();
				CY = tempBitmap.getXY(2) - event.getY();
				CCX = event.getX();
				CCY = event.getY();
			}
			Begin = true;
		}

		/**
		 * ��ָ�ƶ�
		 */
		if (event.getPointerCount() >= 2
				&& event.getAction() == MotionEvent.ACTION_MOVE
				&& tempBitmap != null) {
			twoPoint = 2;

			rotalP = rotalPoint(new float[] { event.getX(0), event.getY(0) },
					tempBitmap.getXY(1), tempBitmap.getXY(2),
					tempBitmap.width / 2, tempBitmap.height / 2,
					tempBitmap.matrix);
			rotalP_2 = rotalPoint(new float[] { event.getX(1), event.getY(1) },
					tempBitmap.getXY(1), tempBitmap.getXY(2),
					tempBitmap.width / 2, tempBitmap.height / 2,
					tempBitmap.matrix);
			if (tempBitmap.isCanChange()) {
				if (bool) {
					preLength = spacing(event);
					preCos = cos(event);
					bool = false;
				}
				// ��ȡ�ǶȺͳ���
				length = spacing(event);
				cos = cos(event);
				// �Ŵ����С
				if (length - preLength != 0) {
					float newWidth, newHeight, oldWidth;// w1,h1ΪͼƬ�Ŀ�͸�w,hΪʵʱ��ͼƬ���
					oldWidth = tempBitmap.width;
					newWidth = tempBitmap.width
							* (1.0f + (length - preLength) / length);
					newHeight = tempBitmap.height
							* (1.0f + (length - preLength) / length);

					if (newWidth > oldWidth) {
						// �Ŵ���������
						if (newWidth < cjWidth && newHeight < cjHeight) {
							tempBitmap.width = newWidth;
							tempBitmap.height = newHeight;
						}
					}
					if (newWidth < oldWidth) {
						// ��С��������
						if ((newWidth > cjWidth / 20 && newHeight > cjHeight / 20)) {
							tempBitmap.width = newWidth;
							tempBitmap.height = newHeight;
						}
					}

					// tempBitmap.width *= (1.0f + (length - preLength) /
					// length);
					// tempBitmap.height *= (1.0f + (length - preLength) /
					// length);

					// �ٴμ���һ�飬��֤����λ�ò��䣬��ȡ����Ȧ
					tempBitmap.cancelHighLight();
					scale(tempBitmap.width / 2, tempBitmap.height / 2,
							tempBitmap.getXY(1), tempBitmap.getXY(2),
							tempBitmap.matrix);
				}

				// ��ת
				if (Math.abs(cos) > imgCount - 1 && Math.abs(cos) < 177
						&& Math.abs(cos - preCos) < 15) {
					// ��ת
					tempBitmap.matrix.postRotate(cos - preCos);
					// ����Ƕ�
					tempBitmap.setRotateSize(tempBitmap.getRotateSize() + cos
							- preCos);
					this.getT(tempBitmap.width / 2, tempBitmap.height / 2,
							tempBitmap.getXY(1), tempBitmap.getXY(2),
							tempBitmap.matrix);
				}
				preCos = cos;
				preLength = length;
				this.getT(tempBitmap.width / 2, tempBitmap.height / 2,
						tempBitmap.getXY(1), tempBitmap.getXY(2),
						tempBitmap.matrix);

			}
			Begin = false;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE && Begin // ��ָ�϶�
				&& event.getPointerCount() == 1) {
			this.X = event.getX();
			this.Y = event.getY();
			// �������Ƿ��ڶ���ͼƬ�ڲ�
			if (tempBitmap != null && tempBitmap.isCanChange()) {
				// ƽ��
				float bitX = tempBitmap.getXY(1);
				float bitY = tempBitmap.getXY(2);
				boolean isPassX = false;
				boolean isPassY = false;

				if (tempBitmap.getImgId() < this.baseImgCount
						&& tempBitmap.getImgId() % 2 == 0) {
					Bmp headBmp = mBmps[tempBitmap.getImgId() + 1];

					// ��Ҫ�ƶ�����λ����Ŀǰλ�ú�ͷ������������֮ǰ�����ƶ�
					float toX = X + CX;
					if ((toX > bitX && toX < headBmp.getXY(1))
							|| (toX < bitX && toX > headBmp.getXY(1))) {
						isPassX = true;
					} else { // �����ܳ����߽�
						if (toX < headBmp.getXY(1)
								&& toX - tempBitmap.getWidth() * 1.5f / 4 > headBmp
										.getXY(1) - headBmp.getWidth() / 2) {
							isPassX = true;
						}
						if (toX > headBmp.getXY(1)
								&& toX + tempBitmap.getWidth() * 1.5f / 4 < headBmp
										.getXY(1) + headBmp.getWidth() / 2) {
							isPassX = true;
						}
					}
					float toY = Y + CY;
					if ((toY > bitY && toY < headBmp.getXY(2))
							|| (toY < bitY && toY > headBmp.getXY(2))) {
						isPassY = true;
					} else { // �����ܳ����߽�
						if (toY < headBmp.getXY(2)
								&& toY - tempBitmap.getHeight() * 1.5f / 4 > headBmp
										.getXY(2) - headBmp.getWidth() / 2) {
							isPassY = true;
						}
						if (toY > headBmp.getXY(2)
								&& toY + tempBitmap.getHeight() / 4 < headBmp
										.getXY(2) + headBmp.getWidth() / 2) {
							isPassY = true;
						}
					}

				} else {
					if (X - CCX < 0 && bitX > 0) {
						isPassX = true;
					}
					if (X - CCX > 0 && bitX < cjWidth) {
						isPassX = true;
					}
					if (Y - CCY < 0 && bitY > 0) {
						isPassY = true;
					}
					if (Y - CCY > 0 && bitY < cjHeight) {
						isPassY = true;
					}
				}
				if (isPassX && isPassY) {
					rotalC = this.getT(tempBitmap.width / 2,
							tempBitmap.height / 2, X + CX, Y + CY,
							tempBitmap.matrix);
					tempBitmap.setPreX(X + CX);
					tempBitmap.setPreY(Y + CY);
				} else {
					tempCCX = event.getX();
					tempCCY = event.getY();
				}
			}
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			// ��ԭ����
			for (Bmp bmp : mBmps) {
				if (bmp != null) {
					bmp.setPiority(bmp.priorityBase);
				}

				// bmp.cancelHighLight();
			}
			if (CurrentStage == ConstValue.Stage.Prop) {
				// ����ָ�����Χ�ڵ���ͼƬ��ʱ����Massage
				if (tempBitmap != null && tempBitmap.getImgId() != 0) {
					if (startX > tempBitmap.getXY(1)
							- tempBitmap.getPic().getWidth() / 2
							&& startX < tempBitmap.getXY(1)
									+ tempBitmap.getPic().getWidth() / 2
							&& startY > tempBitmap.getXY(2)
									- tempBitmap.getPic().getHeight() / 2
							&& startY < tempBitmap.getXY(2)
									+ tempBitmap.getPic().getHeight() / 2) {

						Message msg = new Message();// ��ȡMessage����
						int a[] = { (int) tempBitmap.getXY(1),
								(int) tempBitmap.getXY(2) };
						msg.what = tempBitmap.getImgId();
						msg.obj = a;
						handler.sendMessage(msg);
					}
				}
			}

			CX = 0f;
			CY = 0f;
			Begin = false;
		}
		invalidate();
		return true;
	}

	/**
	 * ��ջ���
	 */
	public static void clearBuffer() {
		if (saveSceneBmps != null) {
			for (Bmp mBmp : saveSceneBmps) {
				if (mBmp != null) {
					mBmp.recycleMap();
				}
			}
			saveSceneBmps = null;
		}
		if (savePropBmps != null) {
			for (Bmp mBmp : savePropBmps) {
				if (mBmp != null) {
					mBmp.recycleMap();
				}
			}
			savePropBmps = null;
		}
		if (clothesBmp != null) {
			clothesBmp.recycleMap();
			clothesBmp = null;
		}
		if (sceneBitmap != null) {
			sceneBitmap.recycle();
			sceneBitmap = null;
		}
		if (propBmps != null) {
			for (Bmp mBmp : propBmps) {
				if (mBmp != null) {
					mBmp.recycleMap();
				}
			}
			propBmps = null;
		}
		if (headBmps != null) {
			for (Bmp mBmp : headBmps) {
				if (mBmp != null) {
					mBmp.recycleMap();
				}
			}
			headBmps = null;
		}

		if (moreFaceItems != null) {
			moreFaceItems.clear();
			moreFaceItems = null;
		}
	}

	// ��ȡ��Ӧ�ؼ����Bitmap
	public static Bitmap getSceneBitmap() {
		return sceneBitmap;
	}

	/**
	 * �����༯��
	 * 
	 * @return
	 */
	public List<MoreFaceItem> getMoreFaceItems() {
		return moreFaceItems;
	}

	/**
	 * �����༯��
	 * 
	 * @return
	 */
	public void setMoreFaceItems(List<MoreFaceItem> moreFaceItems) {
		MoreDrawViewBase.moreFaceItems = moreFaceItems;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

}
