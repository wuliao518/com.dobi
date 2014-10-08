package com.doubi.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.exception.ExitAppUtils;
import com.umeng.analytics.MobclickAgent;

public class UpdateActivity extends Activity {
	/** 用户手动检测更新交互按钮 **/
	private Button btnChecknew;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		setContentView(R.layout.activity_update);
		initview();
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

	private void initview() {
		btnChecknew = (Button) findViewById(R.id.wgupdate_btn_check_new);
		btnChecknew.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

			}
		});

	}

	public void OnNotice(String massage) {
		CommonMethod.ShowMyToast(UpdateActivity.this, massage);
	}
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}
}
