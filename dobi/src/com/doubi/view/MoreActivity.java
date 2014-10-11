package com.doubi.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.exception.ExitAppUtils;
import com.doubi.logic.ImageManager;
import com.doubi.logic.LogicMore;
import com.doubi.logic.drawView.BodyDrawView;
import com.doubi.logic.drawView.HairDrawView;
import com.doubi.logic.drawView.HeadDrawView;
import com.doubi.logic.drawView.MoreDrawViewBase;
import com.doubi.logic.drawView.MoreSceneDrawView;
import com.doubi.logic.drawView.PropDrawView;
import com.doubi.logic.drawView.SceneDrawView;
import com.doubi.logic.drawView.SingleDrawViewBase;
import com.doubi.logic.svgResolve.SVG;
import com.doubi.view.adapter.MoreAdapter;
import com.doubi.view.adapter.SingleAdapter;
import com.doubi.view.adapter.item.MapItem;
import com.doubi.view.adapter.item.MoreFaceItem;
import com.umeng.analytics.MobclickAgent;
import com.umeng.common.message.Log;


import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;


@SuppressLint("HandlerLeak")
public class MoreActivity extends Activity {

	private ImageManager mImageManager;
	private LogicMore mLogicMore;
	private Handler handler;// 接收道具图片坐标的Handler

	private MoreSceneDrawView mMoreSceneDrawView;
	// private MorePropDrawView mMorePropDrawView;

	private boolean hasMeasured = false;// 确保只执行一次
	private LinearLayout linear;// 照相机弹出框
	private ImageView btnCamera;// 弹出框里的相机按钮
	private ImageView btnPhoto;// 弹出框里的相册按钮

	private ImageButton btnScene;
	private ImageButton btnProp;
	private ImageButton btnPet;
	private ImageButton btnText;

	private ListView sceneListView, propListView, petListView, bubbleListView;

	private ImageButton propDelete;
	private int propId;

	private LinearLayout unitScene, unitProp, unitPet, unitText;// 化妆，装扮
	private Animation translateLeft, translateRight, translateLeft2,
			translateRight2;// 动画
	
	
	// 首先在您的Activity中添加如下成员变量
		final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
		//com.umeng.socialize.controller.UMSocialService

	// 自定义气泡图形
	private Bitmap selfBubble;
	/**
	 * 场景按钮点击标志true：向上滚动
	 */
	private boolean flagScene = true;
	/**
	 * 道具按钮点击标志true：向上滚动
	 */
	private boolean flagProp = true, flagPet = true, flagText = true;
	// private int btnFlag = 0;// 按钮点击标志0为化妆按钮1为装扮按钮2为场景按钮3为道具按钮

	/**
	 * 装扮控件尺寸
	 */
	private int stageWidth = 0;
	private int stageHeight = 0;

	/**
	 * 动画控件开始前的Y坐标
	 */
	private float unitLeft = 0f, unitLeft1 = 0f;
	private int preX = 0, preX1 = 0;

	/**
	 * 保存处于选择哪种道具状态，1：道具，2：宠物，3：气泡文字
	 */
	private int PropStage;
	private List<Bitmap> mData;
	private List<MapItem> sceneData;

	// 分段加载变量
	private String filepath;
	private int pageCount;// 总页数
	private int pageSize = 10;
	private int currentPage = 1;
	private int type;
	private View CC;
	private int index;
	/**
	 * 场景图片所在地址
	 */
	public static String scenePaht = "";

	@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		setContentView(R.layout.activity_more);
		Log.d("myLog", "onCreate()");

		propDelete = (ImageButton) findViewById(R.id.propDelMore);
		propDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMoreSceneDrawView.delProp(propId);
				propDelete.setVisibility(View.GONE);
			}
		});
		handler = new Handler() {
			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				int position[] = (int[]) msg.obj;
				propId = msg.what;
				LayoutParams params = new LayoutParams(0, 0);
				params.height = Integer.parseInt(MoreActivity.this
						.getResources().getString(R.string.deleteBtnHeight));
				params.width = Integer.parseInt(MoreActivity.this
						.getResources().getString(R.string.deleteBtnWidth));
				params.setMargins(position[0] - params.width / 2, position[1]
						- params.height / 2, 0, 0);
				propDelete.setLayoutParams(params);
				propDelete.setVisibility(View.VISIBLE);
			}
		};
		mImageManager = new ImageManager();
		mLogicMore = new LogicMore();
		MoreDrawViewBase.CurrentStage = ConstValue.Stage.Scene;
		mMoreSceneDrawView = (MoreSceneDrawView) this
				.findViewById(R.id.mMoreSceneDrawView);
		// mMorePropDrawView = (MorePropDrawView) this
		// .findViewById(R.id.mMorePropDrawView);
		// 添加监听
		mMoreSceneDrawView.setHandler(handler);
		mMoreSceneDrawView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMoreSceneDrawView.CancelAllSelect();
					linear.setVisibility(View.GONE);
					propDelete.setVisibility(View.GONE);
					// 原点击屏幕浮动框下滑隐藏功能
					/*
					 * if (!flagScene) { unitScene.setAnimation(translateRight);
					 * unitScene.startAnimation(translateRight); } else if
					 * (!flagProp) { unitProp.setAnimation(translateRight2);
					 * unitProp.startAnimation(translateRight2); } else if
					 * (!flagPet) { unitPet.setAnimation(translateRight2);
					 * unitPet.startAnimation(translateRight2); } if (!flagText)
					 * { unitText.setAnimation(translateRight2);
					 * unitText.startAnimation(translateRight2); }
					 */
					break;
				case MotionEvent.ACTION_UP:
					linear.setVisibility(View.GONE);
					List<MoreFaceItem> list = mMoreSceneDrawView
							.getMoreFaceItems();
					for (MoreFaceItem mMoreFaceItem : list) {
						mMoreFaceItem.setHangest(false);
					}
					break;
				}
				return false;
			}
		});

		btnProp = (ImageButton) this.findViewById(R.id.btnPropMore);
		btnScene = (ImageButton) this.findViewById(R.id.btnScene);
		btnPet = (ImageButton) this.findViewById(R.id.btnPet);
		btnText = (ImageButton) this.findViewById(R.id.btnText);
		// 对listview进行监听处理
		sceneListView = (ListView) findViewById(R.id.sceneListView);
		propListView = (ListView) findViewById(R.id.propListView);
		petListView = (ListView) findViewById(R.id.propListView);
		bubbleListView = (ListView) findViewById(R.id.propListView);
		sceneListView.setOnScrollListener(new LvScrollEvent());
		propListView.setOnScrollListener(new LvScrollEvent());
		petListView.setOnScrollListener(new LvScrollEvent());
		bubbleListView.setOnScrollListener(new LvScrollEvent());
		
		if(scenePaht==""){
			// 初始化默认场景
			String imgRootPaht = Environment.getExternalStorageDirectory()
					+ ConstValue.ROOT_PATH + ConstValue.MORE_SCENE_PATH
					+ ConstValue.MORE_SCENE_DEFAULT;
			List<String> sceneList = mImageManager.getAllFoders(imgRootPaht);
			if (sceneList.size() > 0) {
				scenePaht = sceneList.get(0);
			}
		}
		unitScene = (LinearLayout) this.findViewById(R.id.moreScene);
		unitProp = (LinearLayout) this.findViewById(R.id.prop);
		intAnimation();
		linear = (LinearLayout) this.findViewById(R.id.cameraWidget);
		btnCamera = (ImageView) linear.findViewById(R.id.cameraButton);
		btnPhoto = (ImageView) linear.findViewById(R.id.photoButton);
		// 气泡文字的bitmap
		InputStream it = getResources().openRawResource(R.drawable.bubbletext);
		selfBubble = BitmapFactory.decodeStream(it);
		btnCamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				index=(Integer) linear.getTag();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri mOutPutFileUri;
				//文件夹doubi/moerClipFace
				String path = Environment.getExternalStorageDirectory().toString()+"/doubi/moerClipFace";
				File path1 = new File(path);
				if(!path1.exists()){
					path1.mkdirs();
				}
				File file = new File(path1,"photo"+"jpg");
				mOutPutFileUri = Uri.fromFile(file);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
				startActivityForResult(intent,0);
			}
		});
		btnPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				index=(Integer) linear.getTag();
				Intent intent = new Intent();
				/* 开启Pictures画面Type设定为image */
				intent.setType("image/*");
				/* 使用Intent.ACTION_GET_CONTENT这个Action */
				intent.setAction(Intent.ACTION_GET_CONTENT);
				/* 取得相片后返回本画面 */
				startActivityForResult(intent, 1);
				// MoreActivity.this.startActivity(intent);
			}
		});

		// 创造获取控件高宽的有效时机
		ViewTreeObserver vto = mMoreSceneDrawView.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				if (hasMeasured == false) {
					// 化妆画面
					stageWidth = mMoreSceneDrawView.getMeasuredWidth();// 获取到宽度和高度后
					stageHeight = mMoreSceneDrawView.getMeasuredHeight();
					// 初始化默认头饰、衣服
					invisibilityDrawview();
					mMoreSceneDrawView.setVisibility(View.VISIBLE);
					// 启动化妆画面
					mMoreSceneDrawView.Inteligense(MoreActivity.this,
							scenePaht, stageWidth, stageHeight);

					mLogicMore.creatBtnToFace(mMoreSceneDrawView,
							MoreActivity.this, linear);

					MoreDrawViewBase.CurrentStage = ConstValue.Stage.Scene;
					hasMeasured = true;
				}

				return true;
			}
		});

		// 手机密度和尺寸检测
		/*
		 * DisplayMetrics metric = new DisplayMetrics();
		 * getWindowManager().getDefaultDisplay().getMetrics(metric); int width
		 * = metric.widthPixels; // 屏幕宽度（像素） int height = metric.heightPixels;
		 * // 屏幕高度（像素） float density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		 * int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
		 */
	}

	/**
	 * listview监听处理调用
	 */
	private class LvScrollEvent implements OnScrollListener {
		private int getLastVisiblePosition = 0, lastVisiblePositionY = 0;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			switch (scrollState) {

			// 滚动之前,手还在屏幕上 记录滚动前的下标
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				break;

			// 滚动停止
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (MoreDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {

					// 滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						View v = (View) view
								.getChildAt(view.getChildCount() - 1);
						int[] location = new int[2];
						v.getLocationOnScreen(location);// 获取在整个屏幕内的绝对坐标
						int y = location[1];
						Log.e("x" + location[0], "y" + location[1]);
						if (view.getLastVisiblePosition() != getLastVisiblePosition
								&& lastVisiblePositionY != y
								&& currentPage < pageCount)// 第一次拖至底部
						{

							getLastVisiblePosition = view
									.getLastVisiblePosition();
							lastVisiblePositionY = y;
							return;
						} else if (view.getLastVisiblePosition() == getLastVisiblePosition
								&& lastVisiblePositionY == y
								&& currentPage < pageCount)// 第二次拖至底部
						{
							currentPage++;
							new processImageTask(MoreActivity.this.type)
									.execute();
						}
					} else if (view.getFirstVisiblePosition() == 0) {
						if (currentPage == 1) {
							currentPage = 1;
						} else {
							currentPage--;
							new processImageTask(MoreActivity.this.type)
									.execute();
						}

					}
					// 未滚动到底部，第二次拖至底部都初始化
					getLastVisiblePosition = 0;
					lastVisiblePositionY = 0;

				}

				if (MoreDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
					// 滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						View v = (View) view
								.getChildAt(view.getChildCount() - 1);
						int[] location = new int[2];
						v.getLocationOnScreen(location);// 获取在整个屏幕内的绝对坐标
						int y = location[1];
						Log.e("x" + location[0], "y" + location[1]);
						if (view.getLastVisiblePosition() != getLastVisiblePosition
								&& lastVisiblePositionY != y
								&& currentPage < pageCount)// 第一次拖至底部
						{

							getLastVisiblePosition = view
									.getLastVisiblePosition();
							lastVisiblePositionY = y;
							return;
						} else if (view.getLastVisiblePosition() == getLastVisiblePosition
								&& lastVisiblePositionY == y
								&& currentPage < pageCount)// 第二次拖至底部
						{

							currentPage++;
							new processImageTask(MoreActivity.this.type)
									.execute();
						}
					} else if (view.getFirstVisiblePosition() == 0) {
						if (currentPage == 1) {
							currentPage = 1;
						} else {
							currentPage--;
							new processImageTask(MoreActivity.this.type)
									.execute();
						}

					}
					// 未滚动到底部，第二次拖至底部都初始化
					getLastVisiblePosition = 0;
					lastVisiblePositionY = 0;
				}
				break;
			case OnScrollListener.SCROLL_STATE_FLING:

				break;
			}

		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		mMoreSceneDrawView.updateFaces(false);
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
		btnScene.setEnabled(enable);
		btnProp.setEnabled(enable);
		btnPet.setEnabled(enable);
		btnText.setEnabled(enable);
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

	/**
	 * 初始化动画
	 */
	@SuppressLint("NewApi")
	private void intAnimation() {
		translateLeft = AnimationUtils.loadAnimation(this,
				R.anim.translate_left);
		translateLeft2 = AnimationUtils.loadAnimation(this,
				R.anim.translate_left2);

		translateLeft.setAnimationListener(new Animation.AnimationListener() {
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
				params.setMargins(preX, 0, 0, 0);
				// 清除动画
				if (MoreDrawViewBase.CurrentStage
						.equals(ConstValue.Stage.Scene)) {
					params.height = unitScene.getHeight();
					params.width = unitScene.getWidth();
					unitScene.setLayoutParams(params);
					unitScene.clearAnimation();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		translateLeft2.setAnimationListener(new Animation.AnimationListener() {
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
				params.setMargins(preX1, 0, 0, 0);
				if (PropStage == 1) {
					params.height = unitProp.getHeight();
					params.width = unitProp.getWidth();
					unitProp.setLayoutParams(params);
					unitProp.clearAnimation();
				} else if (PropStage == 2) {
					params.height = unitPet.getHeight();
					params.width = unitPet.getWidth();
					unitPet.setLayoutParams(params);
					unitPet.clearAnimation();
				} else if (PropStage == 3) {
					params.height = unitText.getHeight();
					params.width = unitText.getWidth();
					unitText.setLayoutParams(params);
					unitText.clearAnimation();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		translateRight = AnimationUtils.loadAnimation(this,
				R.anim.translate_right);
		translateRight2 = AnimationUtils.loadAnimation(this,
				R.anim.translate_right2);

		translateRight.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				setButtonsEnable(false);
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				setButtonsEnable(true);
				LayoutParams params = new LayoutParams(0, 0);
				params.setMargins((int) unitLeft, 0, 0, 0);
				params.height = unitScene.getHeight();
				params.width = unitScene.getWidth();
				// 清除动画
				unitScene.setVisibility(View.GONE);
				unitScene.setLayoutParams(params);
				unitScene.clearAnimation();

				flagScene = true;
				flagProp = true;
				flagPet = true;
				flagText = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		translateRight2.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				setButtonsEnable(false);
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				setButtonsEnable(true);
				LayoutParams params = new LayoutParams(0, 0);
				params.setMargins((int) unitLeft1, 0, 0, 0);
				params.height = unitProp.getHeight();
				params.width = unitProp.getWidth();
				// 清除动画
				if (PropStage == 1) {
					unitProp.setLayoutParams(params);
					unitProp.clearAnimation();
					unitProp.setVisibility(View.GONE);
					flagProp = true;
				} else if (PropStage == 2) {
					unitPet.setLayoutParams(params);
					unitPet.clearAnimation();
					unitPet.setVisibility(View.GONE);
					flagPet = true;
				} else if (PropStage == 3) {
					unitText.setLayoutParams(params);
					unitText.clearAnimation();
					unitText.setVisibility(View.GONE);
					flagText = true;
				}

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
		RelativeLayout btnRelative = (RelativeLayout) this
				.findViewById(R.id.btnRelative);
		if (MoreDrawViewBase.CurrentStage.equals(ConstValue.Stage.Scene)) {
			unitLeft = unitScene.getX();
			// Y坐标
			preX = (int) (unitLeft - btnRelative.getWidth());
		}
		if (MoreDrawViewBase.CurrentStage.equals(ConstValue.Stage.Prop)) {
			if (unitLeft1 == 0) {
				unitLeft1 = unitProp.getX();
			}
			preX1 = (int) (unitLeft1 - btnRelative.getWidth());
		}
	}

	/**
	 * 初始化底部控件的默认位置，当点击按钮时有控件处于弹出状态时调用
	 */
	private void initPosition() {
		if (unitLeft != 0) {
			LayoutParams params = new LayoutParams(0, 0);
			params.height = unitScene.getHeight();
			params.width = unitScene.getWidth();
			params.setMargins((int) unitLeft, 0, 0, 0);
			unitScene.setLayoutParams(params);
		}
		if (unitLeft1 != 0) {
			LayoutParams params1 = new LayoutParams(0, 0);
			params1.setMargins((int) unitLeft1, 0, 0, 0);
			if (PropStage == 1) {
				params1.height = unitProp.getHeight();
				params1.width = unitProp.getWidth();
				unitProp.setLayoutParams(params1);
			} else if (PropStage == 2) {
				params1.height = unitPet.getHeight();
				params1.width = unitPet.getWidth();
				unitPet.setLayoutParams(params1);
			} else if (PropStage == 3) {
				params1.height = unitText.getHeight();
				params1.width = unitText.getWidth();
				unitText.setLayoutParams(params1);
			}
		}

		// 隐藏相应控件
		unitScene.setVisibility(View.GONE);
		unitProp.setVisibility(View.GONE);

		flagScene = true;
		flagProp = true;
		flagPet = true;
		flagText = true;

		if (MoreDrawViewBase.CurrentStage.equals(ConstValue.Stage.Scene)) {
			unitScene.setVisibility(View.VISIBLE);
			flagScene = false;
		}
		if (MoreDrawViewBase.CurrentStage.equals(ConstValue.Stage.Prop)) {
			if (PropStage == 1) {
				unitProp.setVisibility(View.VISIBLE);
				flagProp = false;
			} else if (PropStage == 2) {
				unitPet.setVisibility(View.VISIBLE);
				flagPet = false;
			} else if (PropStage == 3) {
				unitText.setVisibility(View.VISIBLE);
				flagText = false;
			}
		}
	}

	/**
	 * 隐藏所有画板
	 */
	private void invisibilityDrawview() {
		mMoreSceneDrawView.setVisibility(View.GONE);
	}

	/**
	 * 重新拍照
	 * 
	 * @param v
	 */
	public void btnPhotoOnclick(View v) {
//		List<MoreFaceItem> list = mMoreSceneDrawView.getMoreFaceItems();
//		boolean isFull = true;
//		for (MoreFaceItem mMoreFaceItem : list) {
//			if (mMoreFaceItem.getmBitmap() == null) {
//				mMoreFaceItem.setHangest(true);
//				isFull = false;
//				break;
//			}
//		}
//		if (isFull) {
//			list.get(0).setHangest(true);
//		}
//		Intent intent = new Intent(this, PhotoActivity.class);
//		this.startActivity(intent);
		final Dialog note;
		RelativeLayout relativeLayout;
		// 渲染布局，获取相应控件
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		View view = inflater.inflate(R.layout.window_pop, null);
		ImageButton one=(ImageButton)view.findViewById(R.id.xiangji);
		ImageButton two=(ImageButton)view.findViewById(R.id.xiangce);
		relativeLayout=(RelativeLayout) view.findViewById(R.id.rl_layout);
		// 获取progress控件的宽高
		int height = (int) (CommonMethod.GetDensity(MoreActivity.this)*180+0.5);
		int width = (int) (CommonMethod.GetDensity(MoreActivity.this)*200+0.5);
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
		note.show();
		one.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				note.dismiss();
				index=0;
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri mOutPutFileUri;
				//文件夹doubi/moerClipFace
				String path = Environment.getExternalStorageDirectory().toString()+"/doubi/moerClipFace";
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
				index=0;
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

	/**
	 * 返回主页
	 * 
	 * @param v
	 */
	public void btnMainOnclick(View v) {
		// 退出画面，清空缓存
		if (mData != null) {
			for (int i = 0; i < mData.size(); i++) {
				if (mData.get(i) != null) {
					mData.get(i).recycle();
				}
			}
		}
		if (sceneData != null) {
			for (int i = 0; i < sceneData.size(); i++) {
				if (sceneData.get(i) != null) {
					sceneData.get(i).disBitmap();
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
	 */
	public void btnSaveOnclick(View v) {

		String msg = MoreActivity.this.getResources().getString(
				R.string.save_album);// 已保存到相册
		try {
			mImageManager.saveToAlbum(MoreActivity.this, this.getSaveMap());
			CommonMethod.ShowMyToast(MoreActivity.this, msg);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private Bitmap getSaveMap() {

		Bitmap doubiBitmap = null;// 图片名
		doubiBitmap = mMoreSceneDrawView.getCurrentPic();
		return doubiBitmap;

	}

	/**
	 * 多人分享按钮
	 * 
	 * @throws IOException
	 */
	public void btnShareOnclick(View v) throws IOException {

		 Bitmap tempBitmap = this.getSaveMap();
		 mImageManager.saveToSDCard(tempBitmap,
		 ConstValue.ImgName.moreShareImg);
		 showShare();
	}

	// ===============================场景=======start====================================
	/**
	 * 场景按钮，进入闺蜜时代系列
	 */
	public void btnSceneOnclick(View v) {
		cancleAll();
		btnScene.setImageDrawable(getResources().getDrawable(R.drawable.changjing_bk));
		if (flagScene) {
			MoreDrawViewBase.CurrentStage = ConstValue.Stage.Scene;
			unitScene.setVisibility(View.VISIBLE);
			unitScene.setAnimation(translateLeft);
			unitScene.startAnimation(translateLeft);
			loadFriendToGrid();
		} else {
			mMoreSceneDrawView.CancelAllSelect();
			unitScene.setAnimation(translateRight);
			unitScene.startAnimation(translateRight);
		}
	}

	private void loadFriendToGrid() {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.MORE_SCENE_PATH
				+ ConstValue.FRIEND_PATH;
		updatePageCount(filepath);
		new processImageTask(3).execute();
	}

	/**
	 * 点击叱咤风云按钮
	 * 
	 * @param v
	 */
	public void btnCCOnClick(View v) {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.MORE_SCENE_PATH
				+ ConstValue.CLOUD_PATH;
		updatePageCount(filepath);
		new processImageTask(2, CC).execute();
	}

	/**
	 * 点击乐不思蜀按钮
	 * 
	 * @param v
	 */
	public void btnLBOnClick(View v) {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.MORE_SCENE_PATH
				+ ConstValue.HAPPY_PATH;
		updatePageCount(filepath);
		new processImageTask(2).execute();
	}

	/**
	 * 摩登时代
	 * 
	 * @param v
	 */
	public void btnGMOnClick(View v) {
		loadFriendToGrid();
	}

	/**
	 * 点击似水流年按钮
	 * 
	 * @param v
	 */
	public void btnXROnClick(View v) {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.MORE_SCENE_PATH
				+ ConstValue.OPERA_PATH;
		updatePageCount(filepath);
		new processImageTask(2).execute();
	}

	/**
	 * 点击天下无双按钮
	 * 
	 * @param v
	 */
	public void btnTXOnClick(View v) {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.MORE_SCENE_PATH
				+ ConstValue.WORLD_PATH;
		updatePageCount(filepath);
		new processImageTask(1).execute();
	}

	/**
	 * 点击纵横四海按钮
	 * 
	 * @param v
	 */
	public void btnSGOnClick(View v) {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.MORE_SCENE_PATH
				+ ConstValue.THREE_PATH;
		updatePageCount(filepath);
		new processImageTask(2).execute();
	}

	// ===============================场景=======end======================================
	// ===============================道具=======start====================================
	/**
	 * 道具按钮，进入道具设置
	 */
	public void btnPropMoreOnclick(View v) {
		cancleAll();
		btnProp.setImageDrawable(getResources().getDrawable(R.drawable.prop_bk));
		if (flagProp) {
			// if (MoreDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
			// mMorePropDrawView.Inteligense(MoreActivity.this, stageWidth,
			// stageHeight);
			// mMorePropDrawView.invalidate();
			//
			MoreDrawViewBase.CurrentStage = ConstValue.Stage.Prop;
			PropStage = 1;
			// invisibilityDrawview();
			// mMorePropDrawView.setVisibility(View.VISIBLE);
			// }
			unitProp.setVisibility(View.VISIBLE);
			unitProp.setAnimation(translateLeft2);
			unitProp.startAnimation(translateLeft2);
			loadPropToList();
		} else {
			unitProp.setAnimation(translateRight2);
			unitProp.startAnimation(translateRight2);
		}
	}
	
	public void btnDaojuOnclick(View v){
		loadPropToList();
	}
	private void loadPropToList() {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.PROP_PATH
				+ ConstValue.PROPS_PATH;
		updatePageCount(filepath);
		new processImageTask(1).execute();
	}

	/**
	 * 点击宠物系列按钮
	 * 
	 * @param v
	 */
	public void btnPetOnclick(View v) {
		cancleAll();
		//if (flagPet) {
			// mMorePropDrawView.Inteligense(MoreActivity.this, stageWidth,
			// stageHeight);
			// mMorePropDrawView.invalidate();
			//MoreDrawViewBase.CurrentStage = ConstValue.Stage.Prop;
			//PropStage = 2;
			// invisibilityDrawview();
			// mMorePropDrawView.setVisibility(View.VISIBLE);

			//unitPet.setVisibility(View.VISIBLE);
			//unitPet.setAnimation(translateLeft2);
			//unitPet.startAnimation(translateLeft2);
			loadPetToList();
		//} else {
			//unitPet.setAnimation(translateRight2);
			//unitPet.startAnimation(translateRight2);
		//}
	}

	private void loadPetToList() {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.PROP_PATH
				+ ConstValue.PET_PATH;
		updatePageCount(filepath);
		new processImageTask(2).execute();
	}

	/**
	 * 气泡文字
	 * 
	 * @param v
	 */
	public void btnTextOnclick(View v) {
		cancleAll();
//		if (flagText) {
//			// mMorePropDrawView.Inteligense(MoreActivity.this, stageWidth,
//			// stageHeight);
//			// mMorePropDrawView.invalidate();
//			MoreDrawViewBase.CurrentStage = ConstValue.Stage.Prop;
//			PropStage = 3;
//			// invisibilityDrawview();
//			// mMorePropDrawView.setVisibility(View.VISIBLE);
//
//			//unitText.setVisibility(View.VISIBLE);
//			//unitText.setAnimation(translateLeft2);
//			//unitText.startAnimation(translateLeft2);
			loadBubbleToList();
//		} else {
//			//unitText.setAnimation(translateRight2);
//			//unitText.startAnimation(translateRight2);
//		}
	}

	private void cancleAll() {
		btnScene.setImageDrawable(getResources().getDrawable(R.drawable.changjing));
		btnProp.setImageDrawable(getResources().getDrawable(R.drawable.prop));
	}

	private void loadBubbleToList() {
		filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.PROP_PATH
				+ ConstValue.BUBBLE_PATH;
		updatePageCount(filepath);
		new processImageTask(3).execute();
	}

	// ===============================道具=======end======================================

	/**
	 * 获取多人扮演场景类列表，图片属性此时为空
	 * 
	 * @param filepath
	 * @return
	 */
	private List<MapItem> getSceneItems(String filepath, int currentPage,
			int pageSize) {
		List<MapItem> sceneData = new ArrayList<MapItem>();
		List<String> paths = mImageManager.getCurrentFoders(filepath,
				currentPage, pageSize);
		for (String path : paths) {
			MapItem mSceneItem = new MapItem();
			mSceneItem.setImgPath(path);
			sceneData.add(mSceneItem);
		}
		return sceneData;
	}

	// for (String path : paths) {
	// SceneItem mSceneItem = new SceneItem();
	// mSceneItem.setImgPath(path);
	// sceneData.add(mSceneItem);
	// }
	// return sceneData;
	// }

	// ====================================================================================
	/**
	 * 更新页数
	 */
	private void updatePageCount(String filepath) {
		currentPage = 1;
		this.pageCount = (this.mImageManager.GetFileCount(filepath) + pageSize - 1)
				/ pageSize;
	}

	private class processImageTask extends AsyncTask<Void, Void, Void> {
		// 绑定适配器
		private MoreAdapter mAdapter;

		/**
		 * 
		 * @param type
		 *            0：普通，其它：区分不同步骤下的不同文件夹
		 */
		public processImageTask(int type, View view) {
			MoreActivity.this.type = type;
		}

		/**
		 * 
		 * @param type
		 *            区分不同步骤下的不同文件夹
		 */
		public processImageTask(int type) {
			MoreActivity.this.type = type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			CommonMethod.ShowMyDialog(MoreActivity.this);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!filepath.equals("")
					&& MoreDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {
				sceneData = MoreActivity.this.getSceneItems(filepath,
						currentPage, pageSize);
			}
			// =============================================================================

			else if (!filepath.equals("")
					&& MoreDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
				// mData = mImageManager.GetCurrentDatas(filepath, currentPage, pageSize, ".png",50);
				mData = mImageManager.GetAllBitmaps(filepath);
			}
			return null;
		}

		// =========================================================================

		// =====================================================================================================

		@Override
		protected void onPostExecute(Void result) {
			disPosAllGridview();

			if (MoreDrawViewBase.CurrentStage == ConstValue.Stage.Scene) {

				if (sceneData != null) {
					mAdapter = new MoreAdapter(MoreActivity.this, null,
							sceneData, mMoreSceneDrawView,
							MoreActivity.this.type,
							MoreDrawViewBase.CurrentStage);
				}
				sceneListView.setAdapter(mAdapter);
			}
			if (MoreDrawViewBase.CurrentStage == ConstValue.Stage.Prop) {
				if (MoreActivity.this.type == 1) {
					if (mData != null && mData.size() > 0) {
						mAdapter = new MoreAdapter(MoreActivity.this, mData,
								null, mMoreSceneDrawView,
								MoreActivity.this.type,
								MoreDrawViewBase.CurrentStage);
					}
					propListView.setAdapter(mAdapter);
				} else if (MoreActivity.this.type == 2) {// 宠物
					if (mData != null && mData.size() > 0) {
						mAdapter = new MoreAdapter(MoreActivity.this, mData,
								null, mMoreSceneDrawView,
								MoreActivity.this.type,
								MoreDrawViewBase.CurrentStage);
					}
					petListView.setAdapter(mAdapter);
				}

				else {// 气泡文字
					if (mData != null && mData.size() > 0) {
						mData.add(0, selfBubble);
						mAdapter = new MoreAdapter(MoreActivity.this, mData,
								null, mMoreSceneDrawView,
								MoreActivity.this.type,
								MoreDrawViewBase.CurrentStage, 1);
					}
					bubbleListView.setAdapter(mAdapter);
				}
			}

			// sceneData = null;
			// mData = null;
			mAdapter = null;
			CommonMethod.CloseDialog();
		}

		private void disPosAllGridview() {
			sceneListView.setAdapter(null);
			propListView.setAdapter(null);
			petListView.setAdapter(null);
			bubbleListView.setAdapter(null);
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
			CommonMethod.ShowMyToast(MoreActivity.this,
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode==Activity.RESULT_CANCELED){
			
		}else{
			switch (requestCode) {  
	         case 0:  
	        	 Intent intent1 = new Intent(this, ShowPicActivity.class);
	        	 intent1.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
	        			 ConstValue.ImgSourceType.select.toString());
	        	 intent1.putExtra(ConstValue.ExtruaKey.MoreFaceIndex.toString(),index);
	        	 
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
	 					mImageManager.saveToSDCard(ConstValue.MORE_CLIP_FACE, bitmap, "photo", Bitmap.CompressFormat.JPEG);
	 				} catch (IOException e) {
	 					bitmap.recycle();
	 					e.printStackTrace();
	 				}

	 				Intent intent = new Intent(this, ShowPicActivity.class);
	 				intent.putExtra(ConstValue.ExtruaKey.PhotoType.toString(),
	 						ConstValue.ImgSourceType.select.toString());
	 				intent.putExtra(ConstValue.ExtruaKey.MoreFaceIndex.toString(),index);
	 				this.startActivity(intent);
	 				bitmap.recycle();

	 			} catch (FileNotFoundException e) {

	 			}
	             break;  
			}
			
		}
	}

	private void showShare() {
		
		// 是否只有已登录用户才能打开分享选择页
        mController.openShare(MoreActivity.this, false);
		
		
	}
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}
}
