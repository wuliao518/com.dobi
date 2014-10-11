package com.doubi.logic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.dobi.R;
import com.doubi.logic.drawView.MoreSceneDrawView;
import com.doubi.view.adapter.item.MoreFaceItem;

public class LogicMore {
	@SuppressWarnings("unused")
	private boolean clickFlag = true;
	private int id;

	/**
	 * 脸部显示按钮
	 * 
	 * @param mSceneDrawView
	 * @param context
	 * @param linear
	 */
	@SuppressLint("NewApi")
	public void creatBtnToFace(final MoreSceneDrawView mSceneDrawView,
			Activity context, final LinearLayout linear) {
		FrameLayout view = (FrameLayout) context
				.findViewById(R.id.drawViewFrameLayout);
		// 红点的
		for (final MoreFaceItem mMoreFaceItem : mSceneDrawView
				.getMoreFaceItems()) {
			int[] c = mMoreFaceItem.getLocation();
			final int[] d = c;

			// 设置按钮
			ImageButton head = new ImageButton(context);
			head.setBackground(context.getResources().getDrawable(
					R.drawable.button_face));
			// 脸存在则不显示按钮
			if (mMoreFaceItem.getmBitmap() == null) {
				head.setBackgroundResource(R.drawable.shoot);
			}

			final int resourcesWidth = Integer.parseInt(context.getResources()
					.getString(R.string.morePhotoWidth));
			final int Width = Integer.parseInt(context.getResources()
					.getString(R.string.morePhotoLinearWidth));
			Log.v("myLog", "width:" + resourcesWidth);
			head.setMaxWidth(resourcesWidth);
			head.setMaxHeight(resourcesWidth);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
			params.height = resourcesWidth;
			params.width = resourcesWidth;
			params.setMargins(c[0] - params.width / 2,
					c[1] - params.height / 2, 0, 0);
			head.setLayoutParams(params);
			head.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v.getId() != id) {
						clickFlag = false;
						LayoutParams params2 = new LayoutParams(
								(int) (Width * 2.5f), Width);
						params2.setMargins(d[0] - params2.width / 2, d[1]
								+ params2.height / 2, 0, 0);
						linear.setLayoutParams(params2);
						linear.setTag(mMoreFaceItem.getIndex());
						linear.setVisibility(View.VISIBLE);
					} else if (clickFlag = false) {
						clickFlag = true;
						linear.setVisibility(View.GONE);
					} else if (clickFlag = true) {
						LayoutParams params2 = new LayoutParams(
								(int) (Width * 2.5f), Width);
						params2.setMargins(d[0] - params2.width / 2, d[1]
								+ params2.height / 2, 0, 0);
						linear.setLayoutParams(params2);
						linear.setVisibility(View.VISIBLE);
						clickFlag = false;
					}
					id = v.getId();
					mMoreFaceItem.setHangest(true);
					mSceneDrawView.selectMap(mSceneDrawView.mBmps[mMoreFaceItem.getIndex()*2]);
				}

			});
			mMoreFaceItem.setmButton(head);
			view.addView(head);
		}
	}
}
