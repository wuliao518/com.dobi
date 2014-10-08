package com.doubi.view.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.dobi.R;
import com.doubi.common.ConstValue;
import com.doubi.common.ImageLoader;
import com.doubi.common.ConstValue.Stage;
import com.doubi.logic.ImageManager;
import com.doubi.logic.drawView.BodyDrawView;
import com.doubi.logic.drawView.HairDrawView;
import com.doubi.logic.drawView.HeadDrawView;
import com.doubi.logic.drawView.PropDrawView;
import com.doubi.logic.drawView.SceneDrawView;
import com.doubi.logic.drawView.SingleDrawViewBase;
import com.doubi.logic.svgResolve.SVG;
import com.doubi.logic.svgResolve.SVGParser;
import com.doubi.logic.update.MyDialog;
import com.doubi.view.adapter.item.MapItem;

public class SingleAdapter extends BaseAdapter implements OnScrollListener{
	private ImageManager mImageManager;
	private ImageLoader mImageLoader;
	private List<MapItem> data;
	private List<SVG> faceData;
	private LayoutInflater listContainer; // 视图容器
	private HeadDrawView mHeadDrawView;
	private HairDrawView mHairDrawView;
	private BodyDrawView mBodyDrawView;
	private SceneDrawView mSceneDrawView;
	private PropDrawView mPropDrawView;
	private ConstValue.Stage currentStage;
	private SingleDrawViewBase drawView;
	private Activity mActivity;
	private String word = "";// 气泡文字
	private EditText edit;
	private Button btnCancel;// 气泡文字输入框取消按钮
	private MyDialog myDialog;// 气泡文字输入框Dialog
	/**
	 * 操作类型：1 脸型，2眉毛，3腮红，4胡子
	 */
	private int mType;
	private int pop = 0;// 气泡文字标识
	private GridView mGridView;

	/**
	 * 
	 * @param activity
	 * @param data
	 * @param headDrawView
	 * @param type
	 *            操作类型：1 脸型，2眉毛，3腮红，4胡子
	 */
	public SingleAdapter(Activity activity, List<MapItem> data,
			SingleDrawViewBase drawView, int type, ConstValue.Stage CurrentStage,GridView gridView) {
		this.data = data;
		this.setInfo(activity, drawView, type, CurrentStage,gridView);
	}

	public SingleAdapter(Activity activity, List<MapItem> data,
			SingleDrawViewBase drawView, int type,
			ConstValue.Stage CurrentStage, int pop,GridView gridView) {
		this.data = data;
		this.setInfo(activity, drawView, type, CurrentStage,gridView);
		this.pop = pop;
	}

	public SingleAdapter(Activity activity, SingleDrawViewBase drawView,
			int type, ConstValue.Stage CurrentStage, List<SVG> data,GridView gridView) {
		this.faceData = data;
		this.setInfo(activity, drawView, type, CurrentStage,gridView);
	}

	private void setInfo(Activity activity, SingleDrawViewBase drawView,
			int type, ConstValue.Stage CurrentStage,GridView gridView) {
		mType = type;
		this.drawView = drawView;
		this.mGridView = gridView;
		this.currentStage = CurrentStage;
		listContainer = LayoutInflater.from(activity); // 创建视图容器并设置上下文
		/*
		 * if (currentStage == ConstValue.Stage.Face) { mHeadDrawView =
		 * (HeadDrawView) drawView; } if (currentStage == ConstValue.Stage.Body)
		 * { mBodyDrawView = (BodyDrawView) drawView; }
		 */
		mActivity = activity;
		mImageManager = new ImageManager();
		mImageLoader=new ImageLoader();
		mGridView.setOnScrollListener(this);
	}

	@Override
	public int getCount() {
		if (data == null) {
			if (faceData == null) {
				return 0;
			}
			return faceData.size();
		} else {
			return data.size();
		}

	}

	@Override
	public Object getItem(int position) {
		if (data == null) {
			return faceData.get(position);
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

		Bitmap mBitmap = null;
		String InitiallyBitmapPath = null;// 处理过后的原始图片

		if (data == null) {
			if (faceData != null && faceData.size() > 0) {
				if (position != 0) {
					SVG s = faceData.get(position);
					Path path = s.getPath();
					mBitmap = mImageManager.getBitmapFromPath(path, s
							.getPicture().getWidth(), s.getPicture()
							.getHeight());
				}
			}
		} else {
			if (data != null && data.size() > 0) {
				mBitmap = data.get(position).getBitmap();
				InitiallyBitmapPath = data.get(position).getImgPath();// 处理过后的原始图片
			}
		}

		if (mBitmap != null) {
			float bitHeight = mBitmap.getHeight();
			float bitWidth = mBitmap.getWidth();
			float btnHeight = Float.parseFloat(mActivity.getResources()
					.getString(R.string.itemWidth));
			float btnWidth = Float.parseFloat(mActivity.getResources()
					.getString(R.string.itemWidth));

			float xWidth = 0, yHeigth = 0;

			// 适配不同分辨率手机，调整图片尺寸大小
			// 气泡文字的扩大比例
			float sizeTwo = Float.parseFloat((mActivity.getResources()
					.getString(R.string.bubbleSingleSize))) / 100;
			// 正常图片的扩大比例
			float size = Float.parseFloat((mActivity.getResources()
					.getString(R.string.propSingleSize))) / 10;
			float faceSize = Float.parseFloat(mActivity.getResources()
					.getString(R.string.faceSingleSize)) / 100;
			float cancelSize = Float.parseFloat(mActivity.getResources()
					.getString(R.string.cancelSize)) / 100;
			if (currentStage == Stage.Prop) {
				if (pop == 1) {
					
					imgBtnFace.setScaleType(ScaleType.MATRIX);
					LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
							LinearLayout.LayoutParams.WRAP_CONTENT);
					params.gravity=Gravity.CENTER_VERTICAL;
					imgBtnFace.setLayoutParams(params);
					if (bitHeight < bitWidth) {
						yHeigth = (bitHeight * btnWidth / bitWidth) * sizeTwo;
						xWidth = btnHeight * sizeTwo;
					} else if (bitHeight == bitWidth) {
						yHeigth = (bitHeight * btnWidth / bitWidth) * sizeTwo;
						xWidth = btnHeight * sizeTwo;
					} else if (bitHeight > bitWidth) {
						xWidth = (bitWidth * btnHeight / bitHeight) * sizeTwo;
						yHeigth = btnWidth * sizeTwo;
					}
				}

				else if (bitHeight < bitWidth) {
					yHeigth = (bitHeight * btnWidth / bitWidth) * size;
					xWidth = btnHeight * size;
				} else if (bitHeight == bitWidth) {
					yHeigth = (bitHeight * btnWidth / bitWidth) * size;
					xWidth = btnHeight * size;
				} else if (bitHeight > bitWidth) {
					xWidth = (bitWidth * btnHeight / bitHeight) * size;
					yHeigth = btnWidth * size;
				}
			}
			/**
			 * 调整取消评按钮大小
			 */
			else if (currentStage == Stage.Face && position == 0) {
				if (bitHeight < bitWidth) {
					yHeigth = bitHeight * btnWidth / bitWidth * cancelSize;
					xWidth = btnHeight * cancelSize;
				} else if (bitHeight == bitWidth) {
					yHeigth = bitHeight * btnWidth / bitWidth * cancelSize;
					xWidth = btnHeight * cancelSize;
				} else if (bitHeight > bitWidth) {
					xWidth = bitWidth * btnHeight / bitHeight * cancelSize;
					yHeigth = btnWidth * cancelSize;
				}

			}// 调整脸型大小

			else if (currentStage == Stage.Face) {
				if (mType == 1) {
					if (bitWidth < bitHeight) {
						xWidth = bitWidth * btnHeight / bitHeight * faceSize;
						yHeigth = btnHeight * faceSize;
					} else {
						yHeigth = bitHeight * xWidth / bitWidth * faceSize;
						xWidth = btnWidth * faceSize;
					}

				} else {
					if (bitHeight < bitWidth) {
						yHeigth = bitHeight * btnWidth / bitWidth;
						xWidth = btnWidth;
					} else {
						xWidth = bitWidth * btnHeight / bitHeight;
						yHeigth = btnHeight;
					}
				}

			} else {
				if (bitHeight < bitWidth) {
					yHeigth = bitHeight * btnWidth / bitWidth;
					xWidth = btnWidth;
				} else {
					xWidth = bitWidth * btnHeight / bitHeight;
					yHeigth = btnHeight;
				}
			}

			mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) xWidth,
					(int) yHeigth, false);
		}

		if (faceData != null && faceData.size() > 0 && position == 0) {
			InputStream is = mActivity.getResources().openRawResource(
					R.drawable.cancleselect);
			Bitmap cancelSelectMap = BitmapFactory.decodeStream(is);
			imgBtnFace.setImageBitmap(cancelSelectMap);
		} else {
			imgBtnFace.setImageBitmap(mBitmap);
		}

		// 创建GridView子项点击监听
		BtnOnClickListener btnListener = new BtnOnClickListener(drawView,
				position, InitiallyBitmapPath, currentStage, mType);
		imgBtnFace.setOnClickListener(btnListener);
		// mBitmap.recycle();
		return convertView;
	}

	/**
	 * GridView的子项点击监听
	 * 
	 * @author Administrator
	 *
	 */
	public class BtnOnClickListener implements OnClickListener {
		private SingleDrawViewBase drawView;
		private int position;
		private String bitMapPath;
		private ConstValue.Stage CurrentStage;
		private InputStream is;

		private int mType;

		public BtnOnClickListener(SingleDrawViewBase drawView, int position,
				String bitMapPath, ConstValue.Stage CurrentStage, int mType) {
			this.drawView = drawView;
			this.position = position;
			this.bitMapPath = bitMapPath;
			this.CurrentStage = CurrentStage;
			this.mType = mType;
		}

		@Override
		public void onClick(View v) {
			if (CurrentStage == ConstValue.Stage.Face) {
				mHeadDrawView = (HeadDrawView) drawView;
				switch (mType) {
				case 1:
					if (position == 0) {
						InputStream mInputStream = mHeadDrawView.getResources()
								.openRawResource(R.raw.default_face);
						SVG mSVG = SVGParser.getSVGFromResource(mInputStream);
						mHeadDrawView.setFace(mSVG);
					} else {
						mHeadDrawView.setFace(faceData.get(position));
					}
					break;
				case 2:
					if (position == 0) {
						// 取消选择
						mHeadDrawView.setEyebrows(null);
					} else {
						Bitmap bitMap = mImageManager
								.getBitmapFromPath(bitMapPath);
						mHeadDrawView.setEyebrows(bitMap);
					}

					break;
				case 3:
					if (position == 0) {
						// 取消选择
						mHeadDrawView.setBlusher(null);
					} else {
						Bitmap bitMap = mImageManager
								.getBitmapFromPath(bitMapPath);
						mHeadDrawView.setBlusher(bitMap);
					}

					break;
				case 4:
					if (position == 0) {
						// 取消选择
						mHeadDrawView.setBeard(null);
					} else {
						Bitmap bitMap = mImageManager
								.getBitmapFromPath(bitMapPath);
						mHeadDrawView.setBeard(bitMap);
					}

					break;
				}
			} else if (CurrentStage == ConstValue.Stage.Hair) {
				Bitmap bitMap = mImageManager
						.getBitmapFromPath(bitMapPath);
				mHairDrawView = (HairDrawView) drawView;
				mHairDrawView.updatePic(bitMap, mType);
				mHairDrawView.selectWidget(2);

			} else if (CurrentStage == ConstValue.Stage.Body) {
				Bitmap bitMap = mImageManager
						.getBitmapFromPath(bitMapPath);
				mBodyDrawView = (BodyDrawView) drawView;
				mBodyDrawView.updatePic(bitMap);
				mBodyDrawView.selectWidget(0);
			} else if (CurrentStage == ConstValue.Stage.Scene) {
				Bitmap bitMap = mImageManager
						.getBitmapFromPath(bitMapPath);
				mSceneDrawView = (SceneDrawView) drawView;
				mSceneDrawView.updatePic(bitMap);
				mSceneDrawView.selectWidget(0);
			} else if (CurrentStage == ConstValue.Stage.Prop) {
				mPropDrawView = (PropDrawView) drawView;

				switch (position) {
				// case 0:
				// mPropDrawView.delPic();
				// break;
				case 0:
					if (pop == 1) {
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
						LayoutParams p = myDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
						// p.height = (int) (d.getHeight() * 0.2); //高度设置为屏幕的0.6
						p.height = LayoutParams.WRAP_CONTENT;
						p.width = (int) (d.getWidth() * 1); // 宽度设置为屏幕的0.95
						myDialog.getWindow().setAttributes(p);
						/*****************************************************************/
						/*
						 * edit.setLayoutParams(new
						 * LayoutParams((int)(d.getWidth
						 * ()*0.8),LayoutParams.WRAP_CONTENT));
						 * btnCancel.setLayoutParams(new
						 * LayoutParams((int)(d.getWidth
						 * ()*0.2),LayoutParams.WRAP_CONTENT));
						 */
						btnCancel.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								word = edit.getText().toString();
								Bitmap bit = null;
								// 从文件路径中获取bitmap对象
//								Bitmap bitMap = mImageManager
//										.getBitmapFromPath(bitMapPath);
								
								Bitmap bitMap=data.get(position).getBitmap();
								bit = mImageManager.setTextToBitmap(bitMap,
										word);
								mPropDrawView.addPic(bit);
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
								// 调用软键盘
								((InputMethodManager) mActivity
										.getSystemService(mActivity.INPUT_METHOD_SERVICE))
										.toggleSoftInput(
												0,
												InputMethodManager.HIDE_NOT_ALWAYS);
							}
						}, 100);

						/*
						 * AlertDialog.Builder builder = new Builder(mActivity);
						 * builder.setTitle(mActivity.getResources().getString(
						 * R.string.custom_bubble));// 自定义气泡文字 edit = new
						 * EditText(mActivity); builder.setView(edit); //
						 * 按返回键不能退出 builder.setCancelable(false);
						 */
						/*
						 * builder.setPositiveButton(R.string.yes, new
						 * DialogInterface.OnClickListener() {
						 * 
						 * @Override public void onClick(DialogInterface dialog,
						 * int which) { word = edit.getText().toString();
						 * mImageManager.setmActivity(mActivity); Bitmap bit =
						 * null; bit = mImageManager.setTextToBitmap(bitMap,
						 * word); mPropDrawView.addPic(bit); } });
						 */
						// noticeDialog = builder.create();

					} else {
						Bitmap bitMap = mImageManager
								.getBitmapFromPath(bitMapPath);
						mPropDrawView.addPic(bitMap);
					}
					break;
				default:
					Bitmap bitMap = mImageManager
					.getBitmapFromPath(bitMapPath);
					mPropDrawView.addPic(bitMap);
				}
			}
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
	}
}
