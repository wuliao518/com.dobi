package com.doubi.view.adapter;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.dobi.R;
import com.doubi.common.ConstValue;
import com.doubi.common.ConstValue.Stage;
import com.doubi.logic.ImageManager;
import com.doubi.logic.drawView.MoreDrawViewBase;
import com.doubi.logic.drawView.MoreSceneDrawView;
import com.doubi.logic.update.MyDialog;
import com.doubi.view.MoreActivity;
import com.doubi.view.adapter.item.MoreFaceItem;
import com.doubi.view.adapter.item.MapItem;

public class MoreAdapter extends BaseAdapter {
	private Bitmap mBitmap;// 缩放后的图片
	private Bitmap InitiallyBitmap; // 处理过后的原始图片
	protected Bitmap face_item; // 脸部
	// 道具用
	private List<Bitmap> data;
	// 场景用
	private List<MapItem> sceneData;
	private LayoutInflater listContainer; // 视图容器
	private MoreSceneDrawView mSceneDrawView;
	private ConstValue.Stage currentStage;
	private MoreDrawViewBase drawView;
	/**
	 * 操作类型：1 脸型，2眉毛，3腮红，4胡子
	 */
	private int mType;
	private Activity mActivity;
	private String word;
	private LinearLayout linear;// 照相机弹出框
	private ImageManager mImageManager;
	private int pop = 0;// 气泡文字标识
	private EditText edit;
	private Button btnCancel;// 气泡文字输入框取消按钮
	private MyDialog myDialog;// 气泡文字输入框Dialog

	/**
	 * 
	 * @param context
	 * @param data
	 * @param headDrawView
	 * @param type
	 *            操作类型：1 脸型，2眉毛，3腮红，4胡子
	 */
	public MoreAdapter(Activity context, List<Bitmap> data,
			List<MapItem> sceneData, MoreDrawViewBase drawView, int type,
			ConstValue.Stage CurrentStage) {
		this.mActivity = context;
		mType = type;
		this.data = data;
		this.drawView = drawView;
		this.currentStage = CurrentStage;
		listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.sceneData = sceneData;
		mImageManager = new ImageManager();
		/*
		 * if (currentStage == ConstValue.Stage.Face) { mHeadDrawView =
		 * (HeadDrawView) drawView; } if (currentStage == ConstValue.Stage.Body)
		 * { mBodyDrawView = (BodyDrawView) drawView; }
		 */

	}

	public MoreAdapter(Activity context, List<Bitmap> data,
			List<MapItem> sceneData, MoreDrawViewBase drawView, int type,
			ConstValue.Stage CurrentStage, int pop) {
		this.mActivity = context;
		mType = type;
		this.data = data;
		this.drawView = drawView;
		this.currentStage = CurrentStage;
		listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.sceneData = sceneData;
		mImageManager = new ImageManager();
		this.pop = pop;
	}

	@Override
	public int getCount() {
		if (sceneData != null) {
			return sceneData.size();
		} else {
			return data.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (sceneData != null) {
			return data.size();
		} else {
			return data.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(R.layout.list_item, null);
		}

		ImageButton imgBtnFace = (ImageButton) convertView
				.findViewById(R.id.itemImgbtn);

		if (currentStage == ConstValue.Stage.Prop && data != null
				&& data.size() > 0) {

			// final Bitmap mBitmap = data.get(position);
			mBitmap = data.get(position);
			InitiallyBitmap = data.get(position);

		}
		if (currentStage == ConstValue.Stage.Scene && sceneData != null
				&& sceneData.size() > 0) {
			File file = new File(sceneData.get(position).getImgPath() + "/" + 0
					+ "jpg");
			mBitmap = mImageManager.getBitmapFromFile(file, 150);
			// InitiallyBitmap = mImageManager.getBitmapFromFile(file); 不需要保存
		}
		/**
		 * 调整大小
		 */
		if (mBitmap != null) {
			float bitHeight = mBitmap.getHeight();
			float bitWidth = mBitmap.getWidth();
			float btnHeight = 80;
			float btnWidth = 80;
			float xWidth = 0, yHeigth = 0;

			// 气泡文字的扩大比例
			float sizeTwo = Float.parseFloat((mActivity.getResources()
					.getString(R.string.bubbleMoreSize))) / 100;
			// 正常图片的扩大比例
			float size = Float.parseFloat((mActivity.getResources()
					.getString(R.string.propMoreSize))) / 10;
			if (currentStage == Stage.Prop) {
				if (mType == 3) {// 气泡文字
					if (bitHeight < bitWidth) {
						yHeigth = (bitHeight * btnWidth / bitWidth) * sizeTwo;
						xWidth = btnWidth * sizeTwo;
					} else if (bitHeight == bitWidth) {
						yHeigth = (bitHeight * btnWidth / bitWidth) * sizeTwo;
						xWidth = btnHeight * sizeTwo;
					} else if (bitHeight > bitWidth) {
						xWidth = (bitWidth * btnHeight / bitHeight) * sizeTwo;
						yHeigth = btnWidth * sizeTwo;
					}
				} else if (bitHeight < bitWidth) {
					yHeigth = (bitHeight * btnWidth / bitWidth) * size;
					xWidth = btnHeight * size;
				} else if (bitHeight == bitWidth) {
					yHeigth = (bitHeight * btnWidth / bitWidth) * size;
					xWidth = btnHeight * size;
				} else if (bitHeight > bitWidth) {
					xWidth = (bitWidth * btnHeight / bitHeight) * size;
					yHeigth = btnWidth * size;
				}
			} else {
				if (bitHeight < bitWidth) {
					yHeigth = (bitHeight * btnWidth / bitWidth) * size;
					xWidth = btnWidth * size;
				} else {
					xWidth = (bitWidth * btnHeight / bitHeight) * size;
					yHeigth = btnHeight * size;
				}
			}
			mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) xWidth,
					(int) yHeigth, true);

			imgBtnFace.setImageBitmap(mBitmap);
			// 创建GridView子项点击监听
			BtnOnClickListener btnListener = new BtnOnClickListener(drawView,
					position, InitiallyBitmap, currentStage, mType,
					sceneData != null ? sceneData.get(position) : null);
			imgBtnFace.setOnClickListener(btnListener);
		}
		return convertView;
	}

	/**
	 * GridView的子项点击监听
	 * 
	 * @author Administrator
	 *
	 */
	public class BtnOnClickListener implements OnClickListener {
		private MoreDrawViewBase drawView;
		private int position;
		private Bitmap bitMap;
		private ConstValue.Stage CurrentStage;
		private int mType;
		private MapItem mSceneItem;

		public BtnOnClickListener(MoreDrawViewBase drawView, int position,
				Bitmap bitMap, ConstValue.Stage CurrentStage, int mType,
				MapItem mSceneItem) {
			this.drawView = drawView;
			this.position = position;
			this.bitMap = bitMap;
			this.CurrentStage = CurrentStage;
			this.mType = mType;
			this.mSceneItem = mSceneItem;
			// 延迟加载图片，降低内存使用
			if (mSceneItem != null) {
				this.mSceneItem.setBitmap(bitMap);
			}
		}

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (CurrentStage == ConstValue.Stage.Scene) {
				linear = (LinearLayout) mActivity
						.findViewById(R.id.cameraWidget);
				linear.setVisibility(View.GONE);
				mSceneDrawView = (MoreSceneDrawView) drawView;
				mSceneDrawView.updateScene(mSceneItem.getImgPath());
				mSceneDrawView.updateFaces(true);
				MoreActivity.scenePaht=mSceneItem.getImgPath();
				FrameLayout view = (FrameLayout) mActivity
						.findViewById(R.id.drawViewFrameLayout);

				int count = view.getChildCount();
				if(count>=2){
					view.removeViews(1, count-1);
				}
				for (final MoreFaceItem mMoreFaceItem : mSceneDrawView
						.getMoreFaceItems()) {
					int[] c = mMoreFaceItem.getLocation();
					final int[] d = c;
					ImageButton head = new ImageButton(mActivity);
					head.setBackground(mActivity.getResources().getDrawable(
							R.drawable.button_face));
					// 脸存在则不显示按钮
					if (mMoreFaceItem.getmBitmap() == null) {
						head.setBackgroundResource(R.drawable.shoot);
					}

					final int resourcesWidth = Integer.parseInt(mActivity
							.getResources().getString(R.string.morePhotoWidth));

					head.setMaxWidth(resourcesWidth);
					head.setMaxHeight(resourcesWidth);
					FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
							0, 0);
					params.height = resourcesWidth;
					params.width = resourcesWidth;
					params.setMargins(c[0] - params.width / 2, c[1]
							- params.height / 2, 0, 0);
					head.setLayoutParams(params);
					head.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							int resourceWidth = Integer.parseInt(mActivity
									.getResources().getString(
											R.string.morePhotoLinearWidth));

							LayoutParams params2 = new LayoutParams(
									(int) (resourceWidth * 2.5f), resourceWidth);
							params2.setMargins(d[0] - params2.width / 2, d[1]
									+ params2.height / 2, 0, 0);
							linear.setTag(mMoreFaceItem.getIndex());
							linear.setLayoutParams(params2);
							linear.setVisibility(View.VISIBLE);
							mMoreFaceItem.setHangest(true);
							mSceneDrawView.selectMap(mSceneDrawView.mBmps[mMoreFaceItem.getIndex()*2]);
						}
					});
					mMoreFaceItem.setmButton(head);
					view.addView(head);
				}

			} else if (CurrentStage == ConstValue.Stage.Prop) {
				mSceneDrawView = (MoreSceneDrawView) drawView;
				// if (position == 0) {
				// mPropDrawView.delPic();
				// } else {
				if (mType == 3 && position == 0) {
					if (pop == 1) {
						/*
						 * AlertDialog.Builder builder = new Builder(mActivity);
						 * builder.setTitle(mActivity.getResources().getString(
						 * R.string.custom_bubble));// 自定义气泡文字 edit = new
						 * EditText(mActivity); builder.setView(edit); //
						 * 按返回键不能退出 builder.setCancelable(false);
						 * builder.setPositiveButton(R.string.yes, new
						 * DialogInterface.OnClickListener() {
						 * 
						 * @Override public void onClick(DialogInterface dialog,
						 * int which) { word = edit.getText().toString();
						 * ImageManager mImageManager = new ImageManager();
						 * mImageManager.setmActivity(mActivity); Bitmap bit =
						 * null; bit = mImageManager.setTextToBitmap( bitMap,
						 * word); mSceneDrawView.addProp(bit); } });
						 * noticeDialog = builder.create(); noticeDialog.show();
						 */
						LayoutInflater inflater = LayoutInflater
								.from(mActivity);
						View view = inflater.inflate(R.layout.dialog_bubble,
								null);
						myDialog = new MyDialog(mActivity, view,
								R.style.Self_Dialog);
						edit = (EditText) view
								.findViewById(R.id.dialog_edittext);
						btnCancel = (Button) view
								.findViewById(R.id.dialog_button);
						/******************************************************************/
						WindowManager m = mActivity.getWindowManager();
						Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
						android.view.WindowManager.LayoutParams p = myDialog
								.getWindow().getAttributes(); // 获取对话框当前的参数值
						// p.height = (int) (d.getHeight() * 0.2); //高度设置为屏幕的0.6
						p.height = LayoutParams.WRAP_CONTENT;
						p.width = (int) (d.getWidth() * 1); // 宽度设置为屏幕的0.95
						myDialog.getWindow().setAttributes(p);
						/*****************************************************************/
						btnCancel.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								word = edit.getText().toString();
								Bitmap bit = null;
								bit = mImageManager.setTextToBitmap(bitMap,
										word);
								mSceneDrawView.addProp(bit);
								myDialog.dismiss();
							}
						});
						// 限制dialog位置
						WindowManager.LayoutParams params = myDialog
								.getWindow().getAttributes();
						myDialog.getWindow().setGravity(Gravity.BOTTOM);

						myDialog.setContentView(view);
						myDialog.setCancelable(true);
						myDialog.show();
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								((InputMethodManager) mActivity
										.getSystemService(mActivity.INPUT_METHOD_SERVICE))
										.toggleSoftInput(
												0,
												InputMethodManager.HIDE_NOT_ALWAYS);
							}
						}, 100);
					} else {
						mSceneDrawView.addProp(bitMap);
					}
				} else {
					mSceneDrawView.addProp(bitMap);
				}
				// }
			}

		}
	}
	

}
