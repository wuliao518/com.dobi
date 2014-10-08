package com.doubi.frist.activity_main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.exception.ExitAppUtils;
import com.umeng.analytics.MobclickAgent;

public class MaliangActivity extends Activity {
	private ImageButton btn_back;
	Intent intent;
	ProgressBar progressBar;
	WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_maliang);
		ini();

		// 设置返回
		View.OnClickListener view = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.btn_back:
					finish();
					break;
				default:
					break;
				}
			}
		};
		btn_back.setOnClickListener(view);
		
		
		
		

		//webView的控件
		webView = (WebView)findViewById(R.id.webView);
		
//		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		webView.getSettings().setJavaScriptEnabled(true);
//		webView.loadUrl("file:///android_asset/duxi.html");
			String language = getResources().getConfiguration().locale.getCountry();
	  
	        if (language != null  
	                && (language.equals("CN"))){
	        	webView.loadUrl("file:///android_asset/maliang_c.html");
	        }
	        else if(language !=null && ((language.equals("US")) || (language.equals("UK")))){
	        	webView.loadUrl("file:///android_asset/maliang_e.html");
	        }
		
	        
	        webView.setWebViewClient(new WebViewClient(){
				//网页加载开始时调用，显示加载提示旋转进度条
				            public void onPageStarted(WebView view, String url, Bitmap favicon) {
				                // TODO Auto-generated method stub
				                super.onPageStarted(view, url, favicon);
				                CommonMethod.ShowMyDialog(MaliangActivity.this);
//				                progressBar.setVisibility(android.view.View.VISIBLE);
				 //			    Toast.makeText(ElecHall.this, "onPageStarted", 2).show();
				            }

				//网页加载完成时调用，隐藏加载提示旋转进度条
				            @Override
				            public void onPageFinished(WebView view, String url) {
				                // TODO Auto-generated method stub
				                super.onPageFinished(view, url);
				                CommonMethod.CloseDialog();
	//			                progressBar.setVisibility(android.view.View.GONE);
				            }
				            //网页加载失败时调用，隐藏加载提示旋转进度条
				            @Override
				            public void onReceivedError(WebView view, int errorCode,
				                    String description, String failingUrl) {
				                // TODO Auto-generated method stub
				                super.onReceivedError(view, errorCode, description, failingUrl);
	//			                progressBar.setVisibility(android.view.View.GONE);
				            }
				            
				        });      
	}

	// 初始化
	private void ini() {
		// TODO Auto-generated method stub
		intent = new Intent();
		btn_back = (ImageButton) findViewById(R.id.btn_back);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.maliang, menu);
		return true;
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
	@Override
	protected void onDestroy() {
		ExitAppUtils.getInstance().delActivity(this);
		super.onDestroy();
	}
}
