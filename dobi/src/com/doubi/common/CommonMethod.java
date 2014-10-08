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
 * @author Administrator ��ͬ����
 */
public class CommonMethod {

	/**
	 * ��Ļ�ܶ�
	 */
	private static float density = 0;

	/**
	 * �������ͣ�0�����˰��� ��1�����˰���
	 */
	private static int sceneType = 0;

	/**
	 * ��ȡ��Ļ���
	 * 
	 * @param activity
	 * @return
	 */
	// ���ȼ�����ʾDialog
	private static Dialog note;
	
	private static SharedPreferences mySharedPreferences;

	public static float GetDensity(Activity activity) {
		if (density == 0) {
			// ��ȡ��Ļ
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
	 * ��ȡ�������ͣ�
	 * 
	 * @return 0�����˰��� ��1�����˰���,2:���ν�����˰���
	 */
	public static int GetSingleOrMore() {
		return sceneType;
	}

	/**
	 * ���ó�������
	 * 
	 * @param i
	 *            0�����˰��� ��1�����˰���
	 */
	public static void SetSingleOrMore(int i) {
		sceneType = i;
	}

	/**
	 * ��ȡ����ǰͼƬ��߱���
	 * 
	 * @return
	 */
	public static double GetFaceForClipScale() {
		return (double) 480 / (double) 601;
	}

	/**
	 * Toast��ʾ
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
	 * ���ȼ�����ʾ��
	 */
	@SuppressLint("InflateParams")
	public static void ShowMyDialog(Activity mActivity) {
		if (IsDialogShowing()) {
			return;
		}

		// �������ײ�ͼ��
		ImageView progress;
		// ������ת��ͼ��
		ImageView fresh;

		// ��Ⱦ���֣���ȡ��Ӧ�ؼ�
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		View view = inflater.inflate(R.layout.dialog_prompt, null);
		progress = (ImageView) view.findViewById(R.id.progressBackground);
		fresh = (ImageView) view.findViewById(R.id.progressFresh);
		// ���ü��ؽ���������
		Animation animation = AnimationUtils.loadAnimation(mActivity,
				R.anim.dialog_progress);
		fresh.startAnimation(animation);
		// ��ȡprogress�ؼ��Ŀ��
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		progress.measure(w, h);
		int height = progress.getMeasuredHeight();
		int width = progress.getMeasuredWidth();
		// �½�Dialog
		note = new Dialog((Context)mActivity, R.style.Translucent_NoTitle);
		// note.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutParams params = new LayoutParams(width, height);
		// ���öԻ����С�������ã�
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
	 * Dialog �Ƿ�������ʾ
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
	 * ��ȡSharepreference��ֵ
	 * 
	 * @param context
	 * @param key
	 * @return ���δ����ֵ����-1
	 * 
	 */
	public static int GetSharepreferenceValue(Context context,
			ConstValue.SharepreferenceKey key) {
		if (mySharedPreferences == null) {
			mySharedPreferences = context.getSharedPreferences(ConstValue.DOBI,
					Context.MODE_PRIVATE); // ˽������
		}

		int result = mySharedPreferences.getInt(key.toString(), -1);

		return result;

	}

	/**
	 * ����Sharepreference��ֵ
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void SetSharepreferenceValue(Context context,
			ConstValue.SharepreferenceKey key, int value) {
		if (mySharedPreferences == null) {
			mySharedPreferences = context.getSharedPreferences(ConstValue.DOBI,
					Context.MODE_PRIVATE); // ˽������
		}
		Editor editor = mySharedPreferences.edit();// ��ȡ�༭��
		editor.putInt(key.toString(), value);
		editor.commit();
	}

}
