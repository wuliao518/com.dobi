package com.doubi.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.dobi.R;

/**
 * 
 * @author Administrator 共同方法
 */
public class CommonMethod {

	/**
	 * 屏幕密度
	 */
	private static float density = 0;

	/**
	 * 场景类型，0：单人扮演 ，1：多人扮演
	 */
	private static int sceneType = 0;

	/**
	 * 获取屏幕宽度
	 * 
	 * @param activity
	 * @return
	 */
	// 进度加载提示Dialog
	private static Dialog note;
	
	private static SharedPreferences mySharedPreferences;

	public static float GetDensity(Activity activity) {
		if (density == 0) {
			// 获取屏幕
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			density = dm.density;
		}
		return density;
	}
	
	public static int getHeight(Activity activity){
		return activity.getWindowManager().getDefaultDisplay().getHeight();
	}
	
	public static int getWidth(Activity activity){
		return activity.getWindowManager().getDefaultDisplay().getWidth();
	}
	/**
	 * 获取场景类型，
	 * 
	 * @return 0：单人扮演 ，1：多人扮演,2:初次进入多人扮演
	 */
	public static int GetSingleOrMore() {
		return sceneType;
	}

	/**
	 * 设置场景类型
	 * 
	 * @param i
	 *            0：单人扮演 ，1：多人扮演
	 */
	public static void SetSingleOrMore(int i) {
		sceneType = i;
	}

	/**
	 * 获取剪切前图片宽高比例
	 * 
	 * @return
	 */
	public static double GetFaceForClipScale() {
		return (double) 480 / (double) 601;
	}

	/**
	 * Toast提示
	 */
	@SuppressLint("InflateParams")
	public static void ShowMyToast(Activity mActivity, String massage) {
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		View view = inflater.inflate(R.layout.toast_view, null);
		TextView text = (TextView) view.findViewById(R.id.toast);
		text.setText(massage);
		Toast toast = Toast.makeText(mActivity, massage, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, (int)GetDensity(mActivity)*65);
		toast.setView(view);

		toast.show();
	}

	/**
	 * 进度加载提示框
	 */
	@SuppressLint("InflateParams")
	public static void ShowMyDialog(Activity mActivity) {
		if (IsDialogShowing()) {
			return;
		}

		// 进度条底层图案
		ImageView progress;
		// 进行旋转的图案
		ImageView fresh;

		// 渲染布局，获取相应控件
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		View view = inflater.inflate(R.layout.dialog_prompt, null);
		progress = (ImageView) view.findViewById(R.id.progressBackground);
		fresh = (ImageView) view.findViewById(R.id.progressFresh);
		// 设置加载进度条动画
		Animation animation = AnimationUtils.loadAnimation(mActivity,
				R.anim.dialog_progress);
		fresh.startAnimation(animation);
		// 获取progress控件的宽高
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		progress.measure(w, h);
		int height = progress.getMeasuredHeight();
		int width = progress.getMeasuredWidth();
		// 新建Dialog
		note = new Dialog((Context)mActivity, R.style.Translucent_NoTitle);
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
		note.setCancelable(false);
		note.show();
	}

	public static void CloseDialog() {
		if (note != null) {
			note.dismiss();
		}
	}

	/**
	 * Dialog 是否正在显示
	 * 
	 * @return
	 */
	public static boolean IsDialogShowing() {
		if (note == null) {
			return false;
		} else {
			return note.isShowing();
		}
	}

	/**
	 * 获取Sharepreference的值
	 * 
	 * @param context
	 * @param key
	 * @return 如果未储存值返回-1
	 * 
	 */
	public static int GetSharepreferenceValue(Context context,
			ConstValue.SharepreferenceKey key) {
		if (mySharedPreferences == null) {
			mySharedPreferences = context.getSharedPreferences(ConstValue.DOBI,
					Context.MODE_PRIVATE); // 私有数据
		}

		int result = mySharedPreferences.getInt(key.toString(), -1);

		return result;

	}

	/**
	 * 设置Sharepreference的值
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void SetSharepreferenceValue(Context context,
			ConstValue.SharepreferenceKey key, int value) {
		if (mySharedPreferences == null) {
			mySharedPreferences = context.getSharedPreferences(ConstValue.DOBI,
					Context.MODE_PRIVATE); // 私有数据
		}
		Editor editor = mySharedPreferences.edit();// 获取编辑器
		editor.putInt(key.toString(), value);
		editor.commit();
	}

}
