package com.doubi.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;

import cn.sharesdk.onekeyshare.OnekeyShare;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.exception.ExitAppUtils;
import com.doubi.logic.ImageManager;
import com.doubi.logic.drawView.BodyDrawView;
import com.doubi.logic.drawView.HairDrawView;
import com.doubi.logic.drawView.HeadDrawView;
import com.doubi.logic.drawView.PropDrawView;
import com.doubi.logic.drawView.SceneDrawView;
import com.doubi.logic.drawView.SingleDrawViewBase;
import com.doubi.logic.svgResolve.SVG;
import com.doubi.view.adapter.SingleAdapter;
import com.doubi.view.adapter.item.MapItem;
import com.umeng.analytics.MobclickAgent;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class SingleActivity extends Activity {

	/**
	 * GridView 的高度
	 */
	private final int GRIDVIEW_WIDTH = 60;

	private HeadDrawView mHeadDrawView;
	private HairDrawView mHairDrawView;
	private BodyDrawView mBodyDrawView;
	private SceneDrawView mSceneDrawView;
	private PropDrawView mPropDrawView;
	private boolean hasMeasured = false;// 确保只执行一次
	private Bitmap bitmapBj;// 场景

	private ImageButton btnHZ;
	private ImageButton btnZB;
	private ImageButton btnScene;
	private ImageButton btnProp;
	private ImageButton btnFace, btnBrow, btnMoustache, btnBlusher;// 化妆中的按钮
	private ImageButton btnHair, btnOldHair, btnRX, btnXR;// 装扮中的按钮
	private ImageButton btnGM, btnYY, btnYD;// 场景中的按钮
	private ImageButton btnQP, btnCW, btnDJ;// 道具中的按钮

	private LinearLayout unitHead, unitDressing, unitScene, unitProp;// 化妆，装扮

	private Animation translateUp, translateDown;// 动画
	/**
	 * 化妆按钮点击标志 true:向上滚动
	 */
	private boolean flagHead = true;
	/**
	 * 装扮按钮点击标志 true:向上滚动
	 */
	private boolean flagDressing = true;
	/**
	 * 场景按钮点击标志true：向上滚动
	 */
	private boolean flagScene = true;
	/**
	 * 道具按钮点击标志true：向上滚动
	 */
	private boolean flagProp = true;

	/**
	 * 装扮控件尺寸
	 */
	private int stageWidth = 0;
	private int stageHeight = 0;
	private GridView propGridView;
	private GridView sceneGridView;

	private GridView faceGridView;
	private GridView dressingGridView;

	private Handler handler;// 接收道具图片坐标的Handler
	private ImageButton propDelete;
	private int propId;

	/**
	 * 动画控件开始前的Y坐标
	 */
	private float unitTop = 0f;
	private int preY = 0;
	/**
	 * 取消选择用的图片
	 */
	private Bitmap cancelSelectMap;
	// 自定义气泡图形
	private Bitmap selfBubble;
	private ImageManager mImageManager;
	private HorizontalScrollView mScrollView, mPropScrollView,
			mDressScrollView, mSceneScollView;

	private List<MapItem> mData;
	// 分段加载变量
	private String filepath;
	private int pageCount;// 总页数
	private int facePageSize = 20;
	private int pageSize = 10;
	private int currentPage = 1;
	private int type;
	private boolean isMovePre = false;// 是否在向前移动

	@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ShareSDK.initSDK(this);
		setContentView(R.layout.activity_single);
		ExitAppUtils.getInstance().addActivity(this);
		propDelete = (ImageButton) findViewById(R.id.propDelete);

		propDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPropDrawView.delProp(propId);
				propDelete.setVisibility(View.GONE);
			}
		});
		//用于删除场景
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int position[] = (int[]) msg.obj;
				propId = msg.what;
				LayoutParams params = new LayoutParams(0, 0);
				params.height = Integer.parseInt(SingleActivity.this
						.getResources().getString(R.string.deleteBtnHeight));
				params.width = Integer.parseInt(SingleActivity.this
						.getResources().getString(R.string.deleteBtnWidth));
				params.setMargins(position[0] - params.width / 2, position[1]
						+ params.height / 2, 0, 0);
				propDelete.setLayoutParams(params);
				propDelete.setVisibility(View.VISIBLE);
			}
		};

		mImageManager = new ImageManager();
		SingleDrawViewBase.CurrentStage = ConstValue.Stage.Face;
		// 从资源文件中得到图片
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inSampleSize=2;
		bitmapBj = BitmapFactory.decodeResource(getResources(), R.drawable.cj,options);
		mHeadDrawView = (HeadDrawView) this.findViewById(R.id.mHeadDrawView);
		mHairDrawView = (HairDrawView) this.findViewById(R.id.mHairDrawView);
		mBodyDrawView = (BodyDrawView) this.findViewById(R.id.mBodyDrawView);
		mSceneDrawView = (SceneDrawView) this.findViewById(R.id.mSceneDrawView);
		mPropDrawView = (PropDrawView) this.findViewById(R.id.mPropDrawView);
		btnHZ = (ImageButton) this.findViewById(R.id.btnHZ);
		btnZB = (ImageButton) this.findViewById(R.id.btnPropMore);
		btnScene = (ImageButton) this.findViewById(R.id.btnScene);
		btnProp = (ImageButton) this.findViewById(R.id.btnProp);
		
		
		
		
		btnFace = (ImageButton) findViewById(R.id.btnFace);
		btnBrow = (ImageButton) findViewById(R.id.btnBrow);
		btnMoustache = (ImageButton) findViewById(R.id.btnMoustache);
		btnBlusher = (ImageButton) findViewById(R.id.btnBlusher);

		btnHair = (ImageButton) findViewById(R.id.btnHair);
		btnOldHair = (ImageButton) findViewById(R.id.btnOldHair);
		btnRX = (ImageButton) findViewById(R.id.btnRX);
		btnXR = (ImageButton) findViewById(R.id.btnXR);

		btnGM = (ImageButton) findViewById(R.id.btnGM);
		btnYY = (ImageButton) findViewById(R.id.btnYY);
		btnYD = (ImageButton) findViewById(R.id.btnYD);

		btnQP = (ImageButton) findViewById(R.id.btnQP);
		btnCW = (ImageButton) findViewById(R.id.btnCW);
		btnDJ = (ImageButton) findViewById(R.id.btnDJ);

		// DrowView 控件监听
		addEventForDrowView();

		// 从drawable中获取删除按钮的图片资源
		InputStream is = getResources()
				.openRawResource(R.drawable.cancleselect);
		cancelSelectMap = BitmapFactory.decodeStream(is);
		InputStream it = getResources().openRawResource(R.drawable.bubbletext);
		selfBubble = BitmapFactory.decodeStream(it);
		unitHead = (LinearLayout) this.findViewById(R.id.LinearBottom);
		unitDressing = (LinearLayout) this.findViewById(R.id.singleDressing);
		unitScene = (LinearLayout) this.findViewById(R.id.singleScene);
		unitProp = (LinearLayout) this.findViewById(R.id.prop);

		intAnimation();

		faceGridView = (GridView) findViewById(R.id.faceGridView);
		dressingGridView = (GridView) findViewById(R.id.dressingGridView);
		sceneGridView = (GridView) findViewById(R.id.sceneGridView);
		propGridView = (GridView) findViewById(R.id.propGridView);

		// 如果没有头像可使用，需要拍头像
		if (!mImageManager.loadImg()) {
			Intent intent = new Intent(this, HomeActivity.class);
			this.startActivity(intent);
			this.finish();
		} else {
			// 创造获取控件高宽的有效时机
			ViewTreeObserver vto = mHairDrawView.getViewTreeObserver();
			vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				public boolean onPreDraw() {
					if (hasMeasured == false) {
						// 化妆画面
						stageWidth = mHeadDrawView.getMeasuredWidth();// 获取到宽度和高度后
						stageHeight = mHeadDrawView.getMeasuredHeight();
						// 初始化默认头饰、衣服
						invisibilityDrawview();
						mHeadDrawView.setVisibility(View.VISIBLE);
						// 启动化妆画面
						mHeadDrawView.Inteligense(SingleActivity.this,
								bitmapBj, stageWidth, stageHeight);
						SingleDrawViewBase.CurrentStage = ConstValue.Stage.Face;
						hasMeasured = true;
					}

					return true;
				}
			});
		}

		mScrollView = (HorizontalScrollView) findViewById(R.id.scrollImageView);
		mPropScrollView = (HorizontalScrollView) findViewById(R.id.propScrollView);
		mDressScrollView = (HorizontalScrollView) findViewById(R.id.dressScrollView);
		mSceneScollView = (HorizontalScrollView) findViewById(R.id.sceneScrollView);
		mScrollView.setOnTouchListener(new TouchListenerImpl());
		mPropScrollView.setOnTouchListener(new TouchListenerImpl());
		mDressScrollView.setOnTouchListener(new TouchListenerImpl());
		mSceneScollView.setOnTouchListener(new TouchListenerImpl());
		// 预留，保留锁屏后游戏信息不消失
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
	}
	
	/**
	 * 对单人扮演的化妆HorizontalScrollView进行监听
	 * 
	 *
	 */

	private class TouchListenerImpl implements OnTouchListener {
		private int preCount = 0;
		private float Xpre = 0;

		@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:

				break;
			case MotionEvent.ACTION_MOVE:
				float X = motionEvent.getX();
				int scrollX = view.getScrollX();
				int width = view.getWidth();
				int scrollViewMeasuredWidth = 0;
				if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Face) {
					scrollViewMeasuredWidth = mScrollView.getChildAt(0)
							.getMeasuredWidth();
				} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Hair
						|| SingleDrawViewBase.CurrentStage == ConstValue.Stage.Body) {
					scrollViewMeasuredWidth = mDressScrollView.getChildAt(0)
							.getMeasuredWidth();
				} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {
					scrollViewMeasuredWidth = mSceneScollView.getChildAt(0)
							.getMeasuredWidth();
				} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
					scrollViewMeasuredWidth = mPropScrollView.getChildAt(0)
							.getMeasuredWidth();
				}

				if (Xpre != 0 && X != 0 && !filepath.isEmpty()
						&& !CommonMethod.IsDialogShowing()) {
					// 向后划
					if (Xpre < X && scrollX == 0 && currentPage > 1) {
						preCount++;
						if (preCount >= 3) {
							isMovePre = true;
							preCount = 0;
						} else {
							isMovePre = false;
						}
						
					}
					if (Xpre < X && isMovePre && scrollX == 0
							&& currentPage > 1) {
						preCount = 0;
						currentPage = currentPage <= 1 ? 1 : currentPage - 1;
						new processImageTask(SingleActivity.this.type)
								.execute();
						X = 0;
						Xpre = 0;
					}
					// 向前划
					if (Xpre > X
							&& (scrollX + width) == scrollViewMeasuredWidth) {
						if (currentPage < pageCount) {
							preCount = 0;
							currentPage = currentPage >= pageCount ? pageCount
									: currentPage + 1;
							new processImageTask(SingleActivity.this.type)
									.execute();
						}
						X = 0;
						Xpre = 0;
					}
				}

				Xpre = X;
				break;
			case MotionEvent.ACTION_UP:
				X = 0;
				Xpre = 0;
				break;
			default:
				break;
			}
			return false;
		}
	};

	@SuppressLint("ClickableViewAccessibility")
	private void addEventForDrowView() {
		mHeadDrawView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mHeadDrawView.CancelAllSelect();
					/*
					 * if (!flagHead) { cancelAllCircel();
					 * unitHead.setAnimation(translateDown);
					 * unitHead.startAnimation(translateDown); }
					 */
					break;
				}
				return false;
			}
		});
		mHairDrawView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mHairDrawView.CancelAllSelect();
					/*
					 * if (!flagDressing) { cancelAllCircel();
					 * unitDressing.setAnimation(translateDown);
					 * unitDressing.startAnimation(translateDown); }
					 */
					break;
				}
				return false;
			}
		});
		mBodyDrawView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mBodyDrawView.CancelAllSelect();
					/*
					 * if (!flagDressing) { cancelAllCircel();
					 * unitHead.setAnimation(translateDown);
					 * unitHead.startAnimation(translateDown); }
					 */
					break;
				}
				return false;
			}
		});
		mSceneDrawView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mSceneDrawView.CancelAllSelect();
					/*
					 * if (!flagScene) { cancelAllCircel();
					 * unitScene.setAnimation(translateDown);
					 * unitScene.startAnimation(translateDown); }
					 */
					break;
				}
				return false;
			}
		});

		mPropDrawView.setHandler(handler);
		mPropDrawView.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mPropDrawView.CancelAllSelect();
					propDelete.setVisibility(View.GONE);
					/*
					 * if (!flagProp) { cancelAllCircel();
					 * unitProp.setAnimation(translateDown);
					 * unitProp.startAnimation(translateDown); }
					 */
					break;
				}
				return false;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		// 友盟统计
		MobclickAgent.onResume(this);
	}
	@Override
	public void onPause() {
		super.onPause();
		// 友盟统计
		MobclickAgent.onPause(this);
	}

	/**
	 * 设置最底下四个控件是否可用
	 * 
	 * @param enable
	 */
	private void setButtonsEnable(boolean enable) {
		btnHZ.setEnabled(enable);
		btnZB.setEnabled(enable);
		btnScene.setEnabled(enable);
		btnProp.setEnabled(enable);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	/**
	 * 初始化动画
	 */
	@SuppressLint("NewApi")
	private void intAnimation() {
		translateUp = AnimationUtils.loadAnimation(this, R.anim.translate_up);

		translateUp.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				setButtonsEnable(false);
				initialiseUnitTop();
				initPosition();
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				setButtonsEnable(true);
				LayoutParams params = new LayoutParams(0, 0);
				params.setMargins(0, preY, 0, 0);
				// 清除动画
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Face)) {
					params.height = unitHead.getHeight();
					params.width = unitHead.getWidth();
					unitHead.setLayoutParams(params);
					unitHead.clearAnimation();
				}
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Hair)
						|| SingleDrawViewBase.CurrentStage
								.equals(ConstValue.Stage.Body)) {
					params.height = unitDressing.getHeight();
					params.width = unitDressing.getWidth();
					unitDressing.setLayoutParams(params);
					unitDressing.clearAnimation();
				}
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Scene)) {
					params.height = unitScene.getHeight();
					params.width = unitScene.getWidth();
					unitScene.setLayoutParams(params);
					unitScene.clearAnimation();
				}
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Prop)) {
					params.height = unitProp.getHeight();
					params.width = unitProp.getWidth();
					unitProp.setLayoutParams(params);
					unitProp.clearAnimation();
				}

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		translateDown = AnimationUtils.loadAnimation(this,
				R.anim.translate_down);

		translateDown.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				setButtonsEnable(false);
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				setButtonsEnable(true);

				LayoutParams params = new LayoutParams(0, 0);
				params.setMargins(0, (int) unitTop, 0, 0);
				// 清除动画
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Face)) {
					params.height = unitHead.getHeight();
					params.width = unitHead.getWidth();
					unitHead.setVisibility(View.GONE);
					unitHead.setLayoutParams(params);
					unitHead.clearAnimation();
				}
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Hair)
						|| SingleDrawViewBase.CurrentStage
								.equals(ConstValue.Stage.Body)) {
					params.height = unitDressing.getHeight();
					params.width = unitDressing.getWidth();
					unitDressing.setVisibility(View.GONE);
					unitDressing.setLayoutParams(params);
					unitDressing.clearAnimation();
				}
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Scene)) {
					params.height = unitScene.getHeight();
					params.width = unitScene.getWidth();
					unitScene.setVisibility(View.GONE);
					unitScene.setLayoutParams(params);
					unitScene.clearAnimation();
				}
				if (SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Prop)) {
					params.height = unitProp.getHeight();
					params.width = unitProp.getWidth();
					unitProp.setVisibility(View.GONE);
					unitProp.setLayoutParams(params);
					unitProp.clearAnimation();
				}

				flagHead = true;
				flagDressing = true;
				flagScene = true;
				flagProp = true;

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
	}

	/**
	 * 记录按钮最初高度
	 */
	@SuppressLint("NewApi")
	private void initialiseUnitTop() {
		if (unitTop == 0) {
			if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Face)) {
				unitTop = unitHead.getY();
			}
			if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Hair)) {
				unitTop = unitDressing.getY();
			}
			if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Scene)) {
				unitTop = unitScene.getY();
			}
			if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Prop)) {
				unitTop = unitProp.getY();
			}

		}

		if (preY == 0) {
			RelativeLayout btnRelative = (RelativeLayout) SingleActivity.this
					.findViewById(R.id.btnRelative);
			// Y坐标
			preY = (int) (unitTop - btnRelative.getHeight());
		}
	}

	/**
	 * 初始化底部控件的默认位置，当点击按钮时有控件处于弹出状态时调用
	 */
	private void initPosition() {
		if (unitTop != 0) {
			LayoutParams params1 = new LayoutParams(0, 0);
			params1.height = unitHead.getHeight();
			params1.width = unitHead.getWidth();
			params1.setMargins(0, (int) unitTop, 0, 0);
			unitHead.setLayoutParams(params1);
			unitDressing.setLayoutParams(params1);
			unitScene.setLayoutParams(params1);
			unitProp.setLayoutParams(params1);
		}

		// 隐藏相应控件
		unitHead.setVisibility(View.GONE);
		unitDressing.setVisibility(View.GONE);
		unitScene.setVisibility(View.GONE);
		unitProp.setVisibility(View.GONE);

		flagHead = true;
		flagDressing = true;
		flagScene = true;
		flagProp = true;

		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Face)) {
			unitHead.setVisibility(View.VISIBLE);
			flagHead = false;
		}
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Hair)
				|| SingleDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Body)) {
			unitDressing.setVisibility(View.VISIBLE);
			flagDressing = false;
		}
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Scene)) {
			unitScene.setVisibility(View.VISIBLE);
			flagScene = false;
		}
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Prop)) {
			unitProp.setVisibility(View.VISIBLE);
			flagProp = false;
		}
	}

	/**
	 * 隐藏所有画板
	 */
	private void invisibilityDrawview() {
		mHeadDrawView.setVisibility(View.GONE);
		mHairDrawView.setVisibility(View.GONE);
		mBodyDrawView.setVisibility(View.GONE);
		mSceneDrawView.setVisibility(View.GONE);
		mPropDrawView.setVisibility(View.GONE);
	}

	/**
	 * 重新拍照
	 * 
	 * @param v
	 */
	public void btnPhotoOnclick(View v) {
		// 清空缓存，保证重新加载头像
		//SingleDrawViewBase.clearBuffer();
		startPhoto();
	}

	/**
	 * 开始拍照
	 */
	private void startPhoto() {
		
		
		
		
		final Dialog note;
		RelativeLayout relativeLayout;
		// 渲染布局，获取相应控件
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		View view = inflater.inflate(R.layout.window_pop, null);
		ImageButton one=(ImageButton)view.findViewById(R.id.xiangji);
		ImageButton two=(ImageButton)view.findViewById(R.id.xiangce);
		relativeLayout=(RelativeLayout) view.findViewById(R.id.rl_layout);
		// 获取progress控件的宽高
		int height = (int) (CommonMethod.GetDensity(SingleActivity.this)*180+0.5);
		int width = (int) (CommonMethod.GetDensity(SingleActivity.this)*200+0.5);
		// 新建Dialog
		note = new Dialog(this, R.style.Translucent_NoTitle);
		// note.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutParams params = new LayoutParams(width, height);
		// 设置对话框大小（不好用）
		WindowManager.LayoutParams params1 = note.getWindow().getAttributes();
		params1.width = width;
		params1.height = height;
		params1.x = 0;
		params1.y = 0;
		note.getWindow().setAttributes(params1);
		note.addContentView(view, params);
		note.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				
			}
		});
		note.show();
		one.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				note.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri mOutPutFileUri;
				//文件夹doubi
				String path = Environment.getExternalStorageDirectory().toString()+"/doubi";
				File path1 = new File(path);
				if(!path1.exists()){
					path1.mkdirs();
				}
				File file = new File(path1,"photo"+"jpg");
				mOutPutFileUri = Uri.fromFile(file);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
				startActivityForResult(intent, 0);
			}
		});
		two.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				note.dismiss();
				Intent intent = new Intent();
				/* 开启Pictures画面Type设定为image */
				intent.setType("image/*");
				/* 使用Intent.ACTION_GET_CONTENT这个Action */
				intent.setAction(Intent.ACTION_GET_CONTENT);
				/* 取得相片后返回本画面 */
				startActivityForResult(intent, 1);
			}
		});

	} 
	
		
		
//		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		Uri mOutPutFileUri;
//		//文件夹doubi
//		String path = Environment.getExternalStorageDirectory().toString()+"/doubi";
//		File path1 = new File(path);
//		if(!path1.exists()){
//			path1.mkdirs();
//		}
//		File file = new File(path1,"photo"+".jpg");
//		mOutPutFileUri = Uri.fromFile(file);
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
//		SingleActivity.this.startActivityForResult(intent, 1);

	/**
	 * 返回主页
	 * 
	 * @param v
	 */
	public void btnMainOnclick(View v) {
		// 退出画面，清空缓存，释放内存空间
		if (mData != null) {
			for (int i = 0; i < mData.size(); i++) {
				if (mData.get(i) != null) {
					mData.get(i).getBitmap().recycle();
				}
			}
		}
		Intent intent = new Intent(this, HomeActivity.class);
		this.startActivity(intent);
		this.finish();
	}

	/**
	 * 保存按钮
	 * 
	 * @param v
	 * 
	 * @throws IOException
	 */
	public void btnSaveOnclick(View v) {
		String msg = SingleActivity.this.getResources().getString(
				R.string.save_album);

		try {
			Bitmap bitmap=this.getSaveMap();
			mImageManager.saveToAlbum(SingleActivity.this, bitmap);
			CommonMethod.ShowMyToast(SingleActivity.this, msg);
			if(bitmap!=null){
				bitmap.recycle();
			}
			//Toast.makeText(getApplicationContext(), msg, 0).show();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private Bitmap getSaveMap() {
		Bitmap doubiBitmap = null;// 图片名
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Face)) {
			doubiBitmap = mHeadDrawView.getCurrentPic();
		}
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Hair)) {
			doubiBitmap = mHairDrawView.getCurrentPic();
		}
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Body)) {
			doubiBitmap = mBodyDrawView.getCurrentPic();
		}
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Scene)) {
			doubiBitmap = mSceneDrawView.getCurrentPic();
		}
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Prop)) {
			doubiBitmap = mPropDrawView.getCurrentPic();
		}
		return doubiBitmap;
	}

	/**
	 * 点击分享按钮
	 * 
	 * @throws IOException
	 */
	public void btnShareOnclick(View v) throws IOException {
		Bitmap tempBitmap = this.getSaveMap();
		mImageManager.saveToSDCard(tempBitmap, ConstValue.ImgName.resultImg);
		showShare();

	}

	// ===============================化妆=======start======================================
	/**
	 * 化妆
	 * 
	 * @param v
	 */
	public void btnHZOnclick(View v) {
		//
		selectedColor();
		statusChangeColor();
		bottomTextColor();
		//btnHZ.setTextColor(Color.WHITE);
		btnHZ.setImageDrawable(getResources().getDrawable(R.drawable.huazhuang_bk));
		if (SingleDrawViewBase.CurrentStage != ConstValue.Stage.Face) {
			cancelAllCircel();
			invisibilityDrawview();
			mHeadDrawView.setVisibility(View.VISIBLE);
			mHeadDrawView.Inteligense(SingleActivity.this, bitmapBj,
					stageWidth, stageHeight);
			SingleDrawViewBase.CurrentStage = ConstValue.Stage.Face;
		}
		// 动画
		if (flagHead) {
			unitHead.setVisibility(View.VISIBLE);
			// 开始动画
			unitHead.setAnimation(translateUp);
			unitHead.startAnimation(translateUp);
			loadFaceToGrid();
		} else {
			unitHead.setAnimation(translateDown);
			unitHead.startAnimation(translateDown);
			cancelAllCircel();
		}

		mHeadDrawView.selectWidget(1);
	}

	/**
	 * 加载脸型数据列表
	 */
	private void loadFaceToGrid() {
		statusChangeColor();
		btnFace.setImageDrawable(getResources().getDrawable(R.drawable.face_bk));
		centerTextColor();
		btnFace.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.FACE_PATH;
		this.pageCount = (this.mImageManager.GetFileCount(filepath)
				+ facePageSize - 1)
				/ facePageSize;
		new processImageTask(1).execute();
	}

	/**
	 * 加载脸型
	 * 
	 * @param v
	 */
	public void btnFaceOnClick(View v) {
		//取消线圈
		cancelAllCircel();
		loadFaceToGrid();
		statusChangeColor();
		btnFace.setImageDrawable(getResources().getDrawable(R.drawable.face_bk));
		centerTextColor();
		btnFace.setBackgroundColor(getResources().getColor(R.color.center));
		mHeadDrawView.selectWidget(1);
	}

	/**
	 * 点击眉毛按钮
	 * 
	 * @param v
	 */
	public void btnBrowOnClick(View v) {
		cancelAllCircel();
		statusChangeColor();
		btnBrow.setImageDrawable(getResources().getDrawable(R.drawable.meimao_bk));
		centerTextColor();
		btnBrow.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.EYEBROWS_PATH;
		this.pageCount = (this.mImageManager.GetFileCount(filepath)
				+ facePageSize - 1)
				/ facePageSize;

		new processImageTask(2).execute();
		mHeadDrawView.selectWidget(2);
	}

	/**
	 * 点击腮红按钮
	 * 
	 * @param v
	 */
	public void btnBlusherOnClick(View v) {
		cancelAllCircel();
		statusChangeColor();
		btnBlusher.setImageDrawable(getResources().getDrawable(R.drawable.saihong_bk));
		centerTextColor();
		btnBlusher.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.BLUSHER_PATH;
		this.pageCount = (this.mImageManager.GetFileCount(filepath)
				+ facePageSize - 1)
				/ facePageSize;

		new processImageTask(3).execute();
		mHeadDrawView.selectWidget(3);
	}

	/**
	 * 点击胡子按钮
	 * 
	 * @param v
	 */
	public void btnMoustacheOnClick(View v) {
		cancelAllCircel();
		statusChangeColor();
		btnMoustache.setImageDrawable(getResources().getDrawable(R.drawable.huzi_bk));
		centerTextColor();
		btnMoustache.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.BEARD_PATH;
		this.pageCount = (this.mImageManager.GetFileCount(filepath)
				+ facePageSize - 1)
				/ facePageSize;

		new processImageTask(4).execute();
		mHeadDrawView.selectWidget(4);
	}

	/**
	 * 设置Gridview属性保证横向滚动
	 * 
	 * @param gridView
	 * @param size
	 */
	private void setGridViewScroll(GridView gridView, int size) {
		float gridViewSize = Float.parseFloat(this.getResources().getString(
				R.string.gridViewSize));
		int allWidth;
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Face)) {
			allWidth = (int) (this.GRIDVIEW_WIDTH * size
					* CommonMethod.GetDensity(this) + size * gridViewSize);
		} else {
			allWidth = (int) (this.GRIDVIEW_WIDTH * size
					* CommonMethod.GetDensity(this) + size * 12);
		}

		int itemWidth = (int) (this.GRIDVIEW_WIDTH * CommonMethod
				.GetDensity(this));

		@SuppressWarnings("deprecation")
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				allWidth, LinearLayout.LayoutParams.FILL_PARENT);
		gridView.setLayoutParams(params);
		gridView.setColumnWidth(itemWidth);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setNumColumns(size);
	}

	// ===============================化妆=======end======================================
	// ===============================装扮=======start====================================

	/**
	 * 装扮按钮，进入发饰状态
	 * 
	 * @param v
	 */
	public void btnZBOnclick(View v) {
		selectedColor();
		statusChangeColor();
		btnZB.setImageDrawable(getResources().getDrawable(R.drawable.zhuangban_bk));
		bottomTextColor();
		if (SingleDrawViewBase.CurrentStage != ConstValue.Stage.Hair
				&& SingleDrawViewBase.CurrentStage != ConstValue.Stage.Body) {
			cancelAllCircel();
			enterHairStage();
			mHairDrawView.selectWidget(2);
		}
		// 判断按键状态，执行动画效果
		if (flagDressing) {
			unitDressing.setVisibility(View.VISIBLE);
			unitDressing.setAnimation(translateUp);
			unitDressing.startAnimation(translateUp);
			loadHairToGrid();
		} else {
			cancelAllCircel();
			unitDressing.setAnimation(translateDown);
			unitDressing.startAnimation(translateDown);
		}
	}

	/**
	 * 加载装扮数据列表
	 */
	private void loadHairToGrid() {
		statusChangeColor();
		btnHair.setImageResource(R.drawable.xiandai_bk);
		centerTextColor();
		btnHair.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.HAIR_PATH
				+ ConstValue.NEWHAIR_PATH;
		updatePageCount(filepath);
		new processImageTask(2).execute();
	}

	/**
	 * 进入发饰步骤
	 */
	private void enterHairStage() {
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Face)) {
			mHeadDrawView.saveFaceBitmap();
			
		}
		SingleDrawViewBase.CurrentStage = ConstValue.Stage.Hair;

		invisibilityDrawview();
		mHairDrawView.setVisibility(View.VISIBLE);

		mHairDrawView.Inteligense(SingleActivity.this, bitmapBj, stageWidth,
				stageHeight);
		mHairDrawView.invalidate();
	}

	/**
	 * 点击现代发饰按钮
	 * 
	 * @param v
	 */
	public void btnHairOnClick(View v) {
		statusChangeColor();
		btnHair.setImageResource(R.drawable.xiandai_bk);
		centerTextColor();
		btnHair.setBackgroundColor(getResources().getColor(R.color.center));
		if (SingleDrawViewBase.CurrentStage != ConstValue.Stage.Hair) {
			cancelAllCircel();
			enterHairStage();
			mHairDrawView.selectWidget(2);
		}
		// 绑定适配器
		loadHairToGrid();
	}

	/**
	 * 点击古代发饰按钮
	 * 
	 * @param v
	 */
	public void btnOldHairOnClick(View v) {
		statusChangeColor();
		btnOldHair.setImageResource(R.drawable.gudai_bk);
		centerTextColor();
		btnOldHair.setBackgroundColor(getResources().getColor(R.color.center));
		if (SingleDrawViewBase.CurrentStage != ConstValue.Stage.Hair) {
			cancelAllCircel();
			enterHairStage();
			mHairDrawView.selectWidget(2);
		}
		// 绑定适配器
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.HAIR_PATH
				+ ConstValue.OLDHAIR_PATH;
		updatePageCount(filepath);
		new processImageTask(2).execute();
	}

	/**
	 * 点击装甲骑兵按钮
	 * 
	 * @param v
	 */
	public void btnZJOnClick(View v) {
		cancelAllCircel();
		toBodyStage();
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.CLOTHES_PATH
				+ ConstValue.ARMORED_PATH;
		updatePageCount(filepath);
		new processImageTask(0).execute();
		mHairDrawView.selectWidget(2);
	}

	/**
	 * 点击戏如人生按钮
	 * 
	 * @param v
	 */
	public void btnXROnClick(View v) {
		cancelAllCircel();
		statusChangeColor();
		btnXR.setImageResource(R.drawable.rensheng_bk);
		centerTextColor();
		btnXR.setBackgroundColor(getResources().getColor(R.color.center));
		toBodyStage();
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.CLOTHES_PATH
				+ ConstValue.OPERA_PATH;
		updatePageCount(filepath);
		new processImageTask(0).execute();
		mBodyDrawView.selectWidget(0);
	}

	/**
	 * 点击流金岁月按钮
	 * 
	 * @param v
	 */
	public void btnLJOnClick(View v) {
		cancelAllCircel();
		toBodyStage();
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.CLOTHES_PATH
				+ ConstValue.GOLDEN_PATH;
		updatePageCount(filepath);
		new processImageTask(0).execute();
		mBodyDrawView.selectWidget(0);
	}

	/**
	 * 点击热血青春按钮
	 * 
	 * @param v
	 */
	public void btnRXOnClick(View v) {
		cancelAllCircel();
		statusChangeColor();
		btnRX.setImageResource(R.drawable.rexue_bk);
		centerTextColor();
		btnRX.setBackgroundColor(getResources().getColor(R.color.center));
		toBodyStage();
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.CLOTHES_PATH
				+ ConstValue.YOUNGTH_PATH;
		updatePageCount(filepath);
		new processImageTask(0).execute();
		mBodyDrawView.selectWidget(0);
	}

	/**
	 * 进入选择衣服步骤
	 */
	private void toBodyStage() {
		cancelAllCircel();
		invisibilityDrawview();
		mBodyDrawView.setVisibility(View.VISIBLE);
		if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Hair)) {
			mHairDrawView.saveHeadBitmap();
		}
		mBodyDrawView.Inteligense(SingleActivity.this, bitmapBj, stageWidth,
				stageHeight);
		SingleDrawViewBase.CurrentStage = ConstValue.Stage.Body;
		mBodyDrawView.invalidate();
	}

	// ===============================装扮=======end======================================
	// ===============================场景=======start====================================
	/**
	 * 场景按钮，进入古木桌系列
	 */
	public void btnSceneOnclick(View v) {
		selectedColor();
		statusChangeColor();
		btnScene.setImageDrawable(getResources().getDrawable(R.drawable.changjing_bk));
		bottomTextColor();
		if (SingleDrawViewBase.CurrentStage != ConstValue.Stage.Scene) {
			cancelAllCircel();
			if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Body)) {
				mBodyDrawView.saveBitmap();
			} else if (SingleDrawViewBase.CurrentStage
					.equals(ConstValue.Stage.Hair)) {
				// mHairDrawView.saveHeadBitmap();
				mHairDrawView.saveBodyBitmap();
			} else if (SingleDrawViewBase.CurrentStage
					.equals(ConstValue.Stage.Face)) {
				mHeadDrawView.saveFaceBitmap();
				mHeadDrawView.saveBodyBitmap();
			}

			mSceneDrawView.Inteligense(SingleActivity.this, bitmapBj,
					stageWidth, stageHeight);
			mSceneDrawView.invalidate();
			SingleDrawViewBase.CurrentStage = ConstValue.Stage.Scene;

			invisibilityDrawview();
			mSceneDrawView.setVisibility(View.VISIBLE);
			mSceneDrawView.selectWidget(0);
		}
		if (flagScene) {
			unitScene.setVisibility(View.VISIBLE);
			unitScene.setAnimation(translateUp);
			unitScene.startAnimation(translateUp);
			loadWoodenToGrid();
		} else {
			cancelAllCircel();
			unitScene.setAnimation(translateDown);
			unitScene.startAnimation(translateDown);
		}
	}

	private void loadWoodenToGrid() {
		statusChangeColor();
		btnGM.setImageResource(R.drawable.gumu_bk);
		centerTextColor();
		btnGM.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.SCENE_PATH
				+ ConstValue.WOODEN_PATH;
		updatePageCount(filepath);
		new processImageTask(3).execute();
	}

	/**
	 * 古木桌
	 * 
	 * @param v
	 */
	public void btnGMOnClick(View v) {
		statusChangeColor();
		btnGM.setImageResource(R.drawable.gumu_bk);
		centerTextColor();
		//btnGM.setTextColor(Color.WHITE);
		loadWoodenToGrid();

	}

	/**
	 * 点击云游四海按钮
	 * 
	 * @param v
	 */
	public void btnYYOnClick(View v) {
		statusChangeColor();
		btnYY.setImageResource(R.drawable.yunyou_bk);
		centerTextColor();
		btnYY.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.SCENE_PATH
				+ ConstValue.TRAVE_PATH;
		updatePageCount(filepath);

		new processImageTask(1).execute();
	}

	/**
	 * 点击移动照相馆按钮
	 * 
	 * @param v
	 */
	public void btnYDOnClick(View v) {
		statusChangeColor();
		btnYD.setImageResource(R.drawable.yidong_bk);
		centerTextColor();
		btnYD.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.SCENE_PATH
				+ ConstValue.MOVE_PATH;
		updatePageCount(filepath);

		new processImageTask(2).execute();
	}

	// ===============================场景=======end======================================
	// ===============================道具=======start====================================
	/**
	 * 道具按钮，进入气泡文字
	 */
	public void btnPropOnclick(View v) {
		//取消选中效果
		selectedColor();
		//设置状态背景
		statusChangeColor();
		//设置选中效果
		btnProp.setImageDrawable(getResources().getDrawable(R.drawable.prop_bk));
		bottomTextColor();
		//btnProp.setTextColor(Color.WHITE);

		if (SingleDrawViewBase.CurrentStage != ConstValue.Stage.Prop) {
			cancelAllCircel();
			if (SingleDrawViewBase.CurrentStage.equals(ConstValue.Stage.Body)) {
				mBodyDrawView.saveBitmap();

			} else if (SingleDrawViewBase.CurrentStage
					.equals(ConstValue.Stage.Hair)) {
				// mHairDrawView.saveHeadBitmap();
				mHairDrawView.saveBodyBitmap();

			} else if (SingleDrawViewBase.CurrentStage
					.equals(ConstValue.Stage.Face)) {
				mHeadDrawView.saveFaceBitmap();
				mHeadDrawView.saveBodyBitmap();
			} else if (SingleDrawViewBase.CurrentStage
					.equals(ConstValue.Stage.Scene)) {
				mSceneDrawView.saveBodyBitmap();
			}

			mPropDrawView.Inteligense(SingleActivity.this, bitmapBj,
					stageWidth, stageHeight);
			mPropDrawView.invalidate();

			SingleDrawViewBase.CurrentStage = ConstValue.Stage.Prop;

			invisibilityDrawview();

			mPropDrawView.setVisibility(View.VISIBLE);

		}

		if (flagProp) {
			unitProp.setVisibility(View.VISIBLE);
			unitProp.setAnimation(translateUp);
			unitProp.startAnimation(translateUp);
			loadBubbleToGrid();
		} else {
			cancelAllCircel();
			unitProp.setAnimation(translateDown);
			unitProp.startAnimation(translateDown);
		}
	}

	private void loadBubbleToGrid() {
		statusChangeColor();
		btnQP.setImageResource(R.drawable.qipao_bk);
		centerTextColor();
		btnQP.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.PROP_PATH
				+ ConstValue.BUBBLE_PATH;
		updatePageCount(filepath);
		// 传递1代表气泡文字
		new processImageTask(1).execute();
	}

	/**
	 * 气泡文字
	 * 
	 * @param v
	 */
	public void btnQPOnClick(View v) {
		statusChangeColor();
		btnQP.setImageResource(R.drawable.qipao_bk);
		centerTextColor();
		btnQP.setBackgroundColor(getResources().getColor(R.color.center));
		loadBubbleToGrid();

	}

	/**
	 * 点击宠物系列按钮
	 * 
	 * @param v
	 */
	public void btnCWOnClick(View v) {
		statusChangeColor();
		btnCW.setImageResource(R.drawable.pet_bk);
		centerTextColor();
		btnCW.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.PROP_PATH
				+ ConstValue.PET_PATH;
		updatePageCount(filepath);

		new processImageTask(2).execute();
	}

	/**
	 * 点击道具系列按钮
	 * 
	 * @param v
	 */
	public void btnDJOnClick(View v) {
		statusChangeColor();
		btnDJ.setImageResource(R.drawable.daoju_bk);
		centerTextColor();
		btnDJ.setBackgroundColor(getResources().getColor(R.color.center));
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.PROP_PATH
				+ ConstValue.PROPS_PATH;
		updatePageCount(filepath);

		new processImageTask(3).execute();
	}

	// ===============================道具=======end======================================
	// =============================背景色设置=====star====================================
	/**
	 * 取消选中效果
	 */
	private void selectedColor() {
		btnHZ.setImageDrawable(getResources().getDrawable(R.drawable.huazhuang));
		btnZB.setImageDrawable(getResources().getDrawable(R.drawable.zhuangban));
		btnScene.setImageDrawable(getResources().getDrawable(R.drawable.changjing));
		btnProp.setImageDrawable(getResources().getDrawable(R.drawable.prop));
	}

	private void bottomTextColor() {
//		btnHZ.setTextColor(Color.BLACK);
//		btnZB.setTextColor(Color.BLACK);
//		btnScene.setTextColor(Color.BLACK);
//		btnProp.setTextColor(Color.BLACK);
	}
	//
	private void statusChangeColor() {
		// private Button btnHair, btnOldHair, btnRX, btnXR;// 装扮中的按钮

		btnFace.setImageDrawable(getResources().getDrawable(R.drawable.face));
		btnBrow.setImageDrawable(getResources().getDrawable(R.drawable.meimao));
		btnMoustache.setImageDrawable(getResources().getDrawable(R.drawable.huzi));
		btnBlusher.setImageDrawable(getResources().getDrawable(R.drawable.saihong));
		
		btnHair.setImageDrawable(getResources().getDrawable(R.drawable.xiandai));
		btnOldHair.setImageDrawable(getResources().getDrawable(R.drawable.gudai));
		btnRX.setImageDrawable(getResources().getDrawable(R.drawable.rexue));
		btnXR.setImageDrawable(getResources().getDrawable(R.drawable.rensheng));
		
		btnGM.setImageDrawable(getResources().getDrawable(R.drawable.gumu));
		btnYY.setImageDrawable(getResources().getDrawable(R.drawable.yunyou));
		btnYD.setImageDrawable(getResources().getDrawable(R.drawable.yidong));
		
		btnQP.setImageDrawable(getResources().getDrawable(R.drawable.qipao));
		btnCW.setImageDrawable(getResources().getDrawable(R.drawable.pet));
		btnDJ.setImageDrawable(getResources().getDrawable(R.drawable.daoju));
	}

	private void centerTextColor() {
		btnFace.setBackgroundColor(getResources().getColor(R.color.top));
		btnBrow.setBackgroundColor(getResources().getColor(R.color.top));
		btnMoustache.setBackgroundColor(getResources().getColor(R.color.top));
		btnBlusher.setBackgroundColor(getResources().getColor(R.color.top));
		
		btnHair.setBackgroundColor(getResources().getColor(R.color.top));
		btnOldHair.setBackgroundColor(getResources().getColor(R.color.top));
		btnRX.setBackgroundColor(getResources().getColor(R.color.top));
		btnXR.setBackgroundColor(getResources().getColor(R.color.top));
		
		btnGM.setBackgroundColor(getResources().getColor(R.color.top));
		btnQP.setBackgroundColor(getResources().getColor(R.color.top));
		btnYY.setBackgroundColor(getResources().getColor(R.color.top));
		btnYD.setBackgroundColor(getResources().getColor(R.color.top));
		
		btnQP.setBackgroundColor(getResources().getColor(R.color.top));
		btnCW.setBackgroundColor(getResources().getColor(R.color.top));
		btnDJ.setBackgroundColor(getResources().getColor(R.color.top));
	}

	// =============================背景色设置=====end=====================================
	/**
	 * 更新页数
	 */
	private void updatePageCount(String filepath) {
		currentPage = 1;
		this.pageCount = (this.mImageManager.GetFileCount(filepath) + pageSize - 1)
				/ pageSize;
	}

	private class processImageTask extends AsyncTask<Void, Void, Void> {
		private List<SVG> mFaceData;
		// 绑定适配器
		private SingleAdapter mAdapter;

		/**
		 * 
		 * @param type
		 *            区分不同步骤下的不同文件夹
		 */
		public processImageTask(int type) {
			SingleActivity.this.type = type;
		}

		@SuppressLint("InflateParams")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			CommonMethod.ShowMyDialog(SingleActivity.this);
		}

		public Void doInBackground(Void... params) {

			mData = new ArrayList<MapItem>();

			if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Face) {
				if (SingleActivity.this.type == 1) { // 脸部
					mFaceData = mImageManager.GetAllSVGs(filepath);
				} else {
					mData = mImageManager.GetCurrentDatas(filepath,
							currentPage, facePageSize, "png", 50);
				}
			} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {// 场景
				mData = mImageManager.GetCurrentDatas(filepath, currentPage,
						pageSize, "jpg", 50);
			} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Prop
					&& SingleActivity.this.type == 1) {// 气泡文字
				mData = mImageManager.GetCurrentDatas(filepath, currentPage,
						pageSize, "png", 150);
			} else {
				mData = mImageManager.GetCurrentDatas(filepath, currentPage,
						pageSize, "png", 50);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			disPosAllGridview();
			if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Face) {
				if (SingleActivity.this.type == 1) {
					if (mFaceData != null && mFaceData.size() > 0) {
						mFaceData.add(0, null);
						mAdapter = new SingleAdapter(SingleActivity.this,
								mHeadDrawView, SingleActivity.this.type,
								SingleDrawViewBase.CurrentStage, mFaceData,faceGridView);
						faceGridView.setNumColumns(mData.size());
						SingleActivity.this.setGridViewScroll(faceGridView,
								mAdapter.getCount());
						faceGridView.setAdapter(mAdapter);
					}

				} else {
					if (mData != null && mData.size() > 0) {
						MapItem mMapItem = new MapItem();
						mMapItem.setBitmap(cancelSelectMap);
						mData.add(0, mMapItem);

						mAdapter = new SingleAdapter(SingleActivity.this,
								mData, mHeadDrawView, SingleActivity.this.type,
								SingleDrawViewBase.CurrentStage,faceGridView);
						faceGridView.setNumColumns(mData.size());
						SingleActivity.this.setGridViewScroll(faceGridView,
								mAdapter.getCount());
						faceGridView.setAdapter(mAdapter);
					}
				}
			}
			// ================================================================================================
			if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Hair) {

				if (mData != null && mData.size() > 0) {
					mAdapter = new SingleAdapter(SingleActivity.this, mData,
							mHairDrawView, SingleActivity.this.type,
							SingleDrawViewBase.CurrentStage,dressingGridView);
					dressingGridView.setNumColumns(mData.size());
					SingleActivity.this.setGridViewScroll(dressingGridView,
							mAdapter.getCount());
					dressingGridView.setAdapter(mAdapter);
				}

			}
			if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Body) {

				if (mData != null && mData.size() > 0) {
					mAdapter = new SingleAdapter(SingleActivity.this, mData,
							mBodyDrawView, SingleActivity.this.type,
							SingleDrawViewBase.CurrentStage,dressingGridView);
					dressingGridView.setNumColumns(mData.size());
					SingleActivity.this.setGridViewScroll(dressingGridView,
							mAdapter.getCount());
					dressingGridView.setAdapter(mAdapter);
				}

			}
			// =====================================================================================================
			if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {
				if (mData != null && mData.size() > 0) {
					mAdapter = new SingleAdapter(SingleActivity.this, mData,
							mSceneDrawView, SingleActivity.this.type,
							SingleDrawViewBase.CurrentStage,sceneGridView);
					sceneGridView.setNumColumns(mData.size());
					SingleActivity.this.setGridViewScroll(sceneGridView,
							mAdapter.getCount());
					sceneGridView.setAdapter(mAdapter);
				}
			}
			// ====================================================================================================
			if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
				if (mData != null && mData.size() > 0) {
					if (SingleActivity.this.type == 1) {
						MapItem mMapItem = new MapItem();
						mMapItem.setBitmap(selfBubble);
						mData.add(0, mMapItem);
					}
					mAdapter = new SingleAdapter(SingleActivity.this, mData,
							mPropDrawView, SingleActivity.this.type,
							SingleDrawViewBase.CurrentStage, type,propGridView);
					propGridView.setNumColumns(mData.size());
					SingleActivity.this.setGridViewScroll(propGridView,
							mAdapter.getCount());
					propGridView.setAdapter(mAdapter);
				}
			}
			// TODO
			// mData = null;
			mAdapter = null;

			CommonMethod.CloseDialog();

			if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Face) {
				mScrollView.setScrollX(0);
			} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Hair
					|| SingleDrawViewBase.CurrentStage == ConstValue.Stage.Body) {
				mDressScrollView.setScrollX(0);
			} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {
				mSceneScollView.setScrollX(0);
			} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
				mPropScrollView.setScrollX(0);
			}
		}

		private void disPosAllGridview() {
			faceGridView.setAdapter(null);
			dressingGridView.setAdapter(null);
			sceneGridView.setAdapter(null);
			propGridView.setAdapter(null);
		}

	}

	/**
	 * 取消所有的选中光圈，需要在更新场景之前调用
	 */
	private void cancelAllCircel() {
		if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Face) {
			mHeadDrawView.CancelAllSelect();
		} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Hair) {
			mHairDrawView.CancelAllSelect();
		} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Body) {
			mBodyDrawView.CancelAllSelect();
		} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {
			mSceneDrawView.CancelAllSelect();
		} else if (SingleDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
			mPropDrawView.CancelAllSelect();
		}
	}

	/**
	 * 菜单、返回键响应
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click(); // 调用双击退出函数
		}
		return false;
	}

	/**
	 * 双击退出函数
	 */
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			CommonMethod.ShowMyToast(SingleActivity.this,
					this.getString(R.string.drop));
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 3000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
			System.exit(0);
		}
	}

	private void showShare() {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		oks.setNotification(R.drawable.ic_launcher,
				getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(getString(R.string.share));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl("http://www.do-bi.cn");
		// text是分享文本，所有平台都需要这个字段
		oks.setText("来自 " + this.getResources().getString(R.string.app_name));
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath(Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH
				+ ConstValue.ImgName.resultImg.toString() + "jpg");
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl("http://www.do-bi.cn");
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("来自 " + this.getResources().getString(R.string.app_name));
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("http://www.do-bi.cn");

		// 启动分享GUI
		oks.show(this);

	}
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode==Activity.RESULT_CANCELED){
//			finish();
//			Intent intent = new Intent(this, SingleActivity.class);
//	     	intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
//	     			 ConstValue.ImgSourceType.front.toString());
//				 this.startActivity(intent);
		}else{
			switch (requestCode) {  
	         case 0:  
	        	 Intent intent1 = new Intent(this, ShowPicActivity.class);
	        	 intent1.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
	        			 ConstValue.ImgSourceType.front.toString());
	        	 intent1.putExtra("name", "single");
				 this.startActivity(intent1);
	             break;  
	         case 1:  
	        	Uri uri = data.getData();
	 			ContentResolver cr = this.getContentResolver();
	 			try {
	 				BitmapFactory.Options options=new BitmapFactory.Options();
	 				options.inJustDecodeBounds=true;
	 				Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri),null,options);
	 				int scale=1;
	 				float bitWidth=options.outWidth;
	 				float bitHeight=options.outHeight;
	 				WindowManager wm=(WindowManager) getSystemService("window");
	 				Display display=wm.getDefaultDisplay();
	 				float width=display.getWidth()*1.5f;
	 				float height=display.getHeight()*1.5f;
	 				float scaleX=(float)bitWidth/width;
	 				float scaleY=(float)bitHeight/height;
	 				scale=(int) Math.max(scaleX,scaleY);
	 				if(scale>1){
	 					options.inJustDecodeBounds=false;
	 					options.inSampleSize=scale;
	 					bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri),null,options);
	 				}else{
	 					options.inJustDecodeBounds=false;
	 					options.inSampleSize=1;
	 					bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri),null,options);
	 				}
	 				
	 				ImageManager mImageManager = new ImageManager();
	 				try {
	 					mImageManager
	 							.saveToSDCard(bitmap, ConstValue.ImgName.photo);
	 				} catch (IOException e) {
	 					bitmap.recycle();
	 					e.printStackTrace();
	 				}

	 				Intent intent = new Intent(this, ShowPicActivity.class);
	 				intent.putExtra("name", "single");
	 				intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
	 						ConstValue.ImgSourceType.select.toString());
	 				this.startActivity(intent);
	 				bitmap.recycle();

	 			} catch (FileNotFoundException e) {

	 			}
	             break;  
			}
			
		}
	}

}
