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
 * 自定义扮演控件基类
 * 
 * @author YHZ
 */
public class MoreDrawViewBase extends DrawViewBase {
	private float startX, startY;
	private float CCX, CCY;// 触摸屏幕起始点坐标，每抬手一次更新一次
	private float tempCCX = 0, tempCCY = 0;// 移动之前的手触位置，图片不能移动更新一次
	private Handler handler;
	/**
	 * 保存当前装扮状态
	 */
	public static ConstValue.Stage CurrentStage;
	/**
	 * 图像显示后的放缩倍数，初始化函数中控制比例
	 */
	protected static double scale = 1;
	/**
	 * 切换步骤后用来保存当前设置
	 */
	protected static Bmp[] saveSceneBmps;
	/**
	 * 道具
	 */
	protected static Bmp[] savePropBmps;

	// 记录当前装扮
	protected static Bmp clothesBmp;// 服装

	/**
	 * 人脸集合
	 */
	protected static Bmp[] headBmps;

	/**
	 * 脸部类集合
	 */
	protected static List<MoreFaceItem> moreFaceItems;

	public MoreDrawViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageManager = new ImageManager();
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
		// super.Inteligense(activity, mBitmap, cjwidth, cjheight);
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
		// 向上弹起
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
		 * 两指移动
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
				// 获取角度和长度
				length = spacing(event);
				cos = cos(event);
				// 放大和缩小
				if (length - preLength != 0) {
					float newWidth, newHeight, oldWidth;// w1,h1为图片的宽和高w,h为实时的图片宽高
					oldWidth = tempBitmap.width;
					newWidth = tempBitmap.width
							* (1.0f + (length - preLength) / length);
					newHeight = tempBitmap.height
							* (1.0f + (length - preLength) / length);

					if (newWidth > oldWidth) {
						// 放大限制条件
						if (newWidth < cjWidth && newHeight < cjHeight) {
							tempBitmap.width = newWidth;
							tempBitmap.height = newHeight;
						}
					}
					if (newWidth < oldWidth) {
						// 缩小限制条件
						if ((newWidth > cjWidth / 20 && newHeight > cjHeight / 20)) {
							tempBitmap.width = newWidth;
							tempBitmap.height = newHeight;
						}
					}

					// tempBitmap.width *= (1.0f + (length - preLength) /
					// length);
					// tempBitmap.height *= (1.0f + (length - preLength) /
					// length);

					// 再次加载一遍，保证基础位置不变，并取消光圈
					tempBitmap.cancelHighLight();
					scale(tempBitmap.width / 2, tempBitmap.height / 2,
							tempBitmap.getXY(1), tempBitmap.getXY(2),
							tempBitmap.matrix);
				}

				// 旋转
				if (Math.abs(cos) > imgCount - 1 && Math.abs(cos) < 177
						&& Math.abs(cos - preCos) < 15) {
					// 旋转
					tempBitmap.matrix.postRotate(cos - preCos);
					// 保存角度
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
		} else if (event.getAction() == MotionEvent.ACTION_MOVE && Begin // 单指拖动
				&& event.getPointerCount() == 1) {
			this.X = event.getX();
			this.Y = event.getY();
			// 触摸点是否在顶层图片内部
			if (tempBitmap != null && tempBitmap.isCanChange()) {
				// 平移
				float bitX = tempBitmap.getXY(1);
				float bitY = tempBitmap.getXY(2);
				boolean isPassX = false;
				boolean isPassY = false;

				if (tempBitmap.getImgId() < this.baseImgCount
						&& tempBitmap.getImgId() % 2 == 0) {
					Bmp headBmp = mBmps[tempBitmap.getImgId() + 1];

					// 将要移动到的位置在目前位置和头部正方形中心之前可以移动
					float toX = X + CX;
					if ((toX > bitX && toX < headBmp.getXY(1))
							|| (toX < bitX && toX > headBmp.getXY(1))) {
						isPassX = true;
					} else { // 否则不能超出边界
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
					} else { // 否则不能超出边界
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
			// 还原排序
			for (Bmp bmp : mBmps) {
				if (bmp != null) {
					bmp.setPiority(bmp.priorityBase);
				}

				// bmp.cancelHighLight();
			}
			if (CurrentStage == ConstValue.Stage.Prop) {
				// 当手指点击范围在道具图片上时发送Massage
				if (tempBitmap != null && tempBitmap.getImgId() != 0) {
					if (startX > tempBitmap.getXY(1)
							- tempBitmap.getPic().getWidth() / 2
							&& startX < tempBitmap.getXY(1)
									+ tempBitmap.getPic().getWidth() / 2
							&& startY > tempBitmap.getXY(2)
									- tempBitmap.getPic().getHeight() / 2
							&& startY < tempBitmap.getXY(2)
									+ tempBitmap.getPic().getHeight() / 2) {

						Message msg = new Message();// 获取Message对象
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
	 * 清空缓存
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

	// 获取适应控件后的Bitmap
	public static Bitmap getSceneBitmap() {
		return sceneBitmap;
	}

	/**
	 * 脸部类集合
	 * 
	 * @return
	 */
	public List<MoreFaceItem> getMoreFaceItems() {
		return moreFaceItems;
	}

	/**
	 * 脸部类集合
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
