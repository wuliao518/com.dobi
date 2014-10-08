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
	/** �û��ֶ������½�����ť **/
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
		// ����ͳ��
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// ����ͳ��
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
