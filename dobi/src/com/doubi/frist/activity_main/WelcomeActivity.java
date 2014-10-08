package com.doubi.frist.activity_main;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dobi.R;
import com.doubi.exception.ExitAppUtils;
import com.doubi.view.HomeActivity;
import com.umeng.analytics.MobclickAgent;

//��һ�����е�����ҳ����
public class WelcomeActivity extends Activity implements OnPageChangeListener,
		OnClickListener {

	private Context context;
	private ViewPager viewPager;
	private PagerAdapter pagerAdapter;
	private Button startButton;
	private LinearLayout indicatorLayout;
	private ArrayList<View> views;
	private ImageView[] indicators = null;
	private int[] images;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);

		context = this;
		// ���������ݷ�ʽ
		new com.doubi.frist.date.CreateShut(this);
		// ��������ͼƬ
		// ������������ͼƬ ָʾ����page�Զ����
		images = new int[] { R.drawable.welcome_02, R.drawable.welcome_03,
				R.drawable.welcome_04};
		initView();

	}

	// ��ʼ����ͼ
	private void initView() {
		// ʵ������ͼ�ؼ�
		viewPager = (ViewPager) findViewById(R.id.viewpage);
		startButton = (Button) findViewById(R.id.start_Button);
		startButton.setOnClickListener(this);
		indicatorLayout = (LinearLayout) findViewById(R.id.indicator);
		views = new ArrayList<View>();
		indicators = new ImageView[images.length]; // ����ָʾ�������С
		for (int i = 0; i < images.length; i++) {
			// ѭ������ͼƬ
			ImageView imageView = new ImageView(context);
			imageView.setBackgroundResource(images[i]);
			views.add(imageView);
			// ѭ������ָʾ��
			indicators[i] = new ImageView(context);
			indicators[i].setPadding(8, 8, 8, 8);
			indicators[i].setBackgroundResource(R.drawable.indicators_default);
			if (i == 0) {
				indicators[i].setBackgroundResource(R.drawable.indicators_now);
			}
			indicatorLayout.addView(indicators[i]);
		}
		pagerAdapter = new com.doubi.frist.adapter.BasePagerAdapter(views);
		viewPager.setAdapter(pagerAdapter); // ����������
		viewPager.setOnPageChangeListener(this);
	}

	// ��ť�ĵ���¼�
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.start_Button) {
			SharedPreferences shared = new com.doubi.frist.date.SharedConfig(
					this).GetConfig();
			Editor editor = shared.edit();
			editor.putBoolean("First", false);
			editor.commit();

			startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			this.finish();
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	// ����viewpage
	@Override
	public void onPageSelected(int arg0) {
		// ��ʾ���һ��ͼƬʱ��ʾ��ť
		if (arg0 == indicators.length - 1) {
			startButton.setVisibility(View.VISIBLE);
		} else {
			startButton.setVisibility(View.INVISIBLE);
		}
		// ����ָʾ��ͼƬ
		for (int i = 0; i < indicators.length; i++) {
			indicators[arg0].setBackgroundResource(R.drawable.indicators_now);
			if (arg0 != i) {
				indicators[i]
						.setBackgroundResource(R.drawable.indicators_default);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// ����ͳ��
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// ����ͳ��
		MobclickAgent.onPause(this);
	}
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}
}
