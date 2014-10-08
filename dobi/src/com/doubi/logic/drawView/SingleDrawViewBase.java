package com.doubi.logic.drawView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.doubi.common.ConstValue;
import com.doubi.logic.ImageManager.RectangleManager;

/**
 * 自定义扮演控件基类
 * 
 * @author YHZ
 */
public class SingleDrawViewBase extends DrawViewBase {
	float CCX, CCY;// 触摸屏幕起始点坐标
	private float startX, startY;

	private Handler handler;
	protected float headX, headY;// 头部起始点坐标
	/**
	 * 
	 */
	public static ConstValue.Stage CurrentStage;

	/**
	 * 图像显示后的放缩倍数，初始化函数中控制比例
	 */
	protected static double scale = 1;
	/**
	 * 切换步骤后用来保存当前设置
	 */
	protected static Bmp[] saveHeadBmps;
	/**
	 * 切换步骤后用来保存当前设置
	 */
	protected static Bmp[] saveHairBmps;
	/**
	 * 切换步骤后用来保存当前设置
	 */
	protected static Bmp[] saveBodyBmps;
	
	
	// 记录当前装扮
	protected static Bmp hairBmp;// 发饰
	protected static Bmp clothesBmp;// 服装
	protected static Bmp eyebrowBmp;
	protected static Bmp saihongBmp;
	protected static Bmp huziBmp;
	protected static Bmp lianBmp;
	/**
	 * 保存脸部组合图片
	 */
	protected static Bmp faceBmp;
	/**
	 * 保存脸部与发饰的组合图片
	 */
	protected static Bmp headBmp;
	/**
	 * 保存整个身体，场景、道具步骤使用
	 */
	protected static Bmp bodyBmp;

	public SingleDrawViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	private float preX,preY;
	private float preRotate=0;
	private float preWidth=0; 
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
		super.Inteligense(activity, mBitmap, cjwidth, cjheight);
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (sceneBitmap != null) {
			if (saveBitmap != null) {
				saveBitmap.recycle();
				saveBitmap = null;
			}
			saveBitmap = Bitmap.createBitmap(sceneBitmap.getWidth(),
					sceneBitmap.getHeight(), ConstValue.MY_CONFIG_8888);
			saveCanvas = new Canvas(saveBitmap);
			canvas.drawBitmap(sceneBitmap, 0, 0, null);
			for (Bmp mBmp : mBmps) {
				if (mBmp != null&&mBmp.getPic()!=null) {
					canvas.drawBitmap(mBmp.getPic(), mBmp.matrix, null);
				}
			}
		}
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * 使相应图片进入选中状态
	 * 
	 * @param index
	 */
	protected void selectWidget(int index) {
		if (mBmps != null && mBmps[index] != null) {
			this.selectMap(mBmps[index]);
			this.invalidate();
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

			// 化妆步骤不能点击移动
			if (!CurrentStage.equals(ConstValue.Stage.Face)) {
				order(event.getX(), event.getY());
			}
			tempBitmap = this.getFoucusMap();
			// tempBitmap.getImgId();
			this.X = event.getX();
			this.Y = event.getY();
			startX = event.getX();
			startY = event.getY();
			if (tempBitmap != null) {
				preX=tempBitmap.getXY(1);
				preY=tempBitmap.getXY(2);
				preWidth=tempBitmap.width;
				preRotate=tempBitmap.getRotateSize();
				CX = tempBitmap.getXY(1) - event.getX();
				CY = tempBitmap.getXY(2) - event.getY();
				CCX = event.getX();
				CCY = event.getY();
			}else{
				return false;
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
			twoFinger(event,tempBitmap);
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
				//越界检查tempBitmap.getWidth()/2是否允许超出一半
				if (X - CCX < 0 && (bitX -tempBitmap.getWidth()/2) > 0) {
					isPassX = true;
				}
				if (X - CCX > 0 && (bitX +tempBitmap.getWidth()/2)< cjWidth) {
					isPassX = true;
				}
				if (Y - CCY < 0 && bitY > 0) {
					isPassY = true;
				}
				if (Y - CCY > 0 && bitY < cjHeight) {
					isPassY = true;
				}
				if (isPassX && isPassY) {
					rotalC = this.getT(tempBitmap.width / 2,
							tempBitmap.height / 2, X + CX, Y + CY,
							tempBitmap.matrix);
					tempBitmap.setPreX(X + CX);
					tempBitmap.setPreY(Y + CY);			
				}			
			}
			
			
			
			
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if(tempBitmap!=null){
				System.out.println("tempBitmap"+tempBitmap.toString());
			}
			// 还原排序
			for (Bmp bmp : mBmps) {
				if (bmp != null) {
					bmp.setPiority(bmp.priorityBase);
				}
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
			
			
			if (CurrentStage == ConstValue.Stage.Prop&&mBmps[0].isFocus()) {
				if(hairBmp!=null){
					changeBmp(hairBmp);
				}
				if(faceBmp!=null){
					changeBmp(faceBmp);
				}
				if(clothesBmp!=null){
					changeBmp(clothesBmp);
				}
				if(lianBmp!=null){
					changeBmp(lianBmp);
				}
				if(eyebrowBmp!=null){
					changeBmp(eyebrowBmp);
				}
				if(saihongBmp!=null){
					changeBmp(saihongBmp);
				}
				if(huziBmp!=null){
					changeBmp(huziBmp);
				}
				
			}else if (CurrentStage == ConstValue.Stage.Scene&&mBmps[0].isFocus()) {
				if(hairBmp!=null){
					changeBmp(hairBmp);
				}
				if(faceBmp!=null){
					changeBmp(faceBmp);
				}
				if(clothesBmp!=null){
					changeBmp(clothesBmp);
				}
				if(lianBmp!=null){
					changeBmp(lianBmp);
				}
				if(eyebrowBmp!=null){
					changeBmp(eyebrowBmp);
				}
				if(saihongBmp!=null){
					changeBmp(saihongBmp);
				}
				if(huziBmp!=null){
					changeBmp(huziBmp);
				}
				
				
				
			}else if (CurrentStage == ConstValue.Stage.Body&&mBmps[0].isFocus()) {
				
			}else if (CurrentStage == ConstValue.Stage.Body&&mBmps[1].isFocus()) {
				//头部移动
				if(eyebrowBmp!=null){
					eyebrowBmp.setPreX(eyebrowBmp.getXY(1)+tempBitmap.getXY(1)-preX);
					eyebrowBmp.setPreY(eyebrowBmp.getXY(2)+tempBitmap.getXY(2)-preY);
				}
				if(hairBmp!=null){
					hairBmp.setPreX(hairBmp.getXY(1)+tempBitmap.getXY(1)-preX);
					hairBmp.setPreY(hairBmp.getXY(2)+tempBitmap.getXY(2)-preY);
				}
				if(saihongBmp!=null){
					saihongBmp.setPreX(saihongBmp.getXY(1)+tempBitmap.getXY(1)-preX);
					saihongBmp.setPreY(saihongBmp.getXY(2)+tempBitmap.getXY(2)-preY);
				}
				if(huziBmp!=null){
					huziBmp.setPreX(huziBmp.getXY(1)+tempBitmap.getXY(1)-preX);
					huziBmp.setPreY(huziBmp.getXY(2)+tempBitmap.getXY(2)-preY);
				}
				if(faceBmp!=null){
					faceBmp.setPreX(faceBmp.getXY(1)+tempBitmap.getXY(1)-preX);
					faceBmp.setPreY(faceBmp.getXY(2)+tempBitmap.getXY(2)-preY);
				}
				if(lianBmp!=null){
					lianBmp.setPreX(lianBmp.getXY(1)+tempBitmap.getXY(1)-preX);
					lianBmp.setPreY(lianBmp.getXY(2)+tempBitmap.getXY(2)-preY);
				}
				
				
			}else if (CurrentStage == ConstValue.Stage.Hair&&mBmps[1].isFocus()) {
				if(faceBmp!=null){
					
				}else{
				}
			}else if (CurrentStage == ConstValue.Stage.Face&&mBmps[1].isFocus()) {
				System.out.println("face选中");
				if(faceBmp!=null){
					
				}
				
				
				
			}
			

			CX = 0f;
			CY = 0f;
			Begin = false;
		}
		invalidate();
		return true;
	}

	private void twoFinger(MotionEvent event,Bmp bmp) {
		if(bmp!=null){
			System.out.println("fuck!!!!!!!!!!!!");
			rotalP = rotalPoint(new float[] { event.getX(0), event.getY(0) },
					bmp.getXY(1), bmp.getXY(2),
					bmp.width / 2, bmp.height / 2,
					bmp.matrix);
			rotalP_2 = rotalPoint(new float[] { event.getX(1), event.getY(1) },
					bmp.getXY(1), bmp.getXY(2),
					bmp.width / 2, bmp.height / 2,
					bmp.matrix);
			
			if (bmp.isCanChange()) {
				if (bool) {
					preLength = spacing(event);
					preCos = cos(event);
					bool = false;
				}
				// 获取角度和长度
				length = spacing(event);
				cos = cos(event);
				// 放大和缩小
				if (length - preLength != 0
						&& !bmp.isOnlyCanTranslation()) {
					float newWidth, newHeight, oldWidth;// w1,h1为图片的宽和高w,h为实时的图片宽高
					oldWidth = bmp.width;
					newWidth = bmp.width
							* (1.0f + (length - preLength) / length);
					newHeight = bmp.height
							* (1.0f + (length - preLength) / length);

					if (newWidth > oldWidth) {
						// 放大限制条件
						if (newWidth < cjWidth && newHeight < cjHeight) {
							bmp.width = newWidth;
							bmp.height = newHeight;
						}
					}
					if (newWidth < oldWidth) {
						// 缩小限制条件
						if (CurrentStage == ConstValue.Stage.Face) {
							if (bmp.getImgId() == 1
									&& (newWidth > cjWidth / 20 && newHeight > cjHeight / 20)) {
								bmp.width = newWidth;
								bmp.height = newHeight;
							}
							if (bmp.getImgId() != 1
									&& (newWidth > cjWidth / 120 && newHeight > cjHeight / 120)) {
								bmp.width = newWidth;
								bmp.height = newHeight;
							}
						} else if ((newWidth > cjWidth / 20 && newHeight > cjHeight / 20)) {
							bmp.width = newWidth;
							bmp.height = newHeight;
						}

					}

					// tempBitmap.width *= (1.0f + (length - preLength) /
					// length);
					// tempBitmap.height *= (1.0f + (length - preLength) /
					// length);

					// 再次加载一遍，保证基础位置不变，并取消光圈
					bmp.cancelHighLight();
					scale(bmp.width / 2, bmp.height / 2,
							bmp.getXY(1), bmp.getXY(2),
							bmp.matrix);
					
					
				}

				// 旋转
				if (Math.abs(cos) > imgCount - 1 && Math.abs(cos) < 177
						&& Math.abs(cos - preCos) < 15
						&& !bmp.isOnlyCanTranslation()) {
					// 旋转
					bmp.matrix.postRotate(cos - preCos);
					// 保存角度
					bmp.setRotateSize(bmp.getRotateSize() + cos
							- preCos);
					this.getT(bmp.width / 2, bmp.height / 2,
							bmp.getXY(1), bmp.getXY(2),
							bmp.matrix);
				}
				preCos = cos;
				preLength = length;
				this.getT(bmp.width / 2, bmp.height / 2,
						bmp.getXY(1), bmp.getXY(2),
						bmp.matrix);
			}
		}
		

	}
	
	
	
	private void changeBmp(Bmp bmp) {
		bmp.setPreX(bmp.getXY(1)+tempBitmap.getXY(1)-preX);
		bmp.setPreY(bmp.getXY(2)+tempBitmap.getXY(2)-preY);
		float scale=tempBitmap.getWidth()/preWidth;
		float preWidth=bmp.getWidth();
		float preHeight=bmp.getHeight();
		bmp.width=bmp.getWidth()*scale;
		bmp.height=bmp.getHeight()*scale;
		bmp.setRotateSize(bmp.getRotateSize()+tempBitmap.getRotateSize()-preRotate);
		bmp.cancelHighLight();
//		float moveX=(bmp.getWidth()-preWidth)/2;
//		float moveY=(bmp.getHeight()-preHeight)/2;
//		bmp.setPreX(bmp.getXY(1)-moveX);
//		bmp.setPreY(bmp.getXY(2)-moveY);
		
	}

	/**
	 * 清空缓存
	 */
	public static void clearBuffer() {
		if (saveHeadBmps != null) {
			for (Bmp mBmp : saveHeadBmps) {
				if (mBmp != null) {
					mBmp.recycleMap();
				}
			}
			saveHeadBmps = null;
		}
		if (saveHairBmps != null) {
			for (Bmp mBmp : saveHairBmps) {
				if (mBmp != null) {
					mBmp.recycleMap();
				}
			}
			saveHairBmps = null;
		}
		if (saveBodyBmps != null) {
			for (Bmp mBmp : saveBodyBmps) {
				if (mBmp != null) {
					mBmp.recycleMap();
				}
			}
			saveBodyBmps = null;
		}
		if (hairBmp != null) {
			hairBmp.recycleMap();
			hairBmp = null;
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
		if (faceBmp != null) {
			faceBmp.recycleMap();
			faceBmp = null;
		}
		if (headBmp != null) {
			headBmp.recycleMap();
			headBmp = null;
		}
		if (bodyBmp != null) {
			bodyBmp.recycleMap();
			bodyBmp = null;
		}
		if (saihongBmp != null) {
			saihongBmp.recycleMap();
			saihongBmp = null;
		}
		if (eyebrowBmp != null) {
			eyebrowBmp.recycleMap();
			eyebrowBmp = null;
		}
		if (huziBmp != null) {
			huziBmp.recycleMap();
			huziBmp = null;
		}
	
		
		
	}

	/**
	 * 按照参数的矩形保存saveBitmap内的图片，使用之前请保证全局变量saveBitmap已经成功赋值。
	 * 
	 * @param mRectangleManager
	 * @return
	 */
	protected Bmp getBodyBmpForScene(RectangleManager mRectangleManager) {
		// 剪切图片,控制剪切范围，在图片内部
		float x = (mRectangleManager.getPreX() - mRectangleManager.getWidth() / 2);
		float width = mRectangleManager.getWidth();
		// TODO 保存后会向右平移
		if (x < 0) {
			mRectangleManager.setWidth(mRectangleManager.getWidth() + x);// x为负数
			mRectangleManager.setPreX(mRectangleManager.getPreX() - x);
			x = 0;
		}
		if (x + width > saveBitmap.getWidth()) {
			mRectangleManager.setWidth(mRectangleManager.getWidth()
					- (x + width - saveBitmap.getWidth()));
			mRectangleManager.setPreX(saveBitmap.getWidth()
					- mRectangleManager.getWidth() / 2);
			width = saveBitmap.getWidth() - x;
		}

		float y = (mRectangleManager.getPreY() - mRectangleManager.getHeight() / 2);
		float heigt = mRectangleManager.getHeight();
		// TODO 保存后会向下平移
		if (y < 0) {
			mRectangleManager.setHeight(mRectangleManager.getHeight() + y);// y为负数
			mRectangleManager.setPreY(mRectangleManager.getPreY() - y);
			y = 0;
		}
		if (y + heigt > saveBitmap.getHeight()) {
			mRectangleManager.setHeight(mRectangleManager.getHeight()
					- (y + heigt - saveBitmap.getHeight()));
			mRectangleManager.setPreY(saveBitmap.getHeight()
					- mRectangleManager.getHeight() / 2);
			heigt = saveBitmap.getHeight() - y;
		}

		Bitmap newSaveImage = Bitmap.createBitmap(saveBitmap, (int) x, (int) y,
				(int) width, (int) heigt);
		// 计算场景中得bmp
		Bmp bmpRe = new Bmp(newSaveImage, 0, mRectangleManager.getPreX(),
				mRectangleManager.getPreY(), true, false, false);
		// 尝试保存旋转方向
		/*
		 * if (toBmp != null) { bmpRe.matrix = toBmp.matrix; }
		 */
		return bmpRe;
	}
}
