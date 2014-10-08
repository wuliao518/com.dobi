package com.doubi.frist.date;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;

import com.dobi.R;
import com.doubi.frist.activity_main.MainActivity;

//Create a desktop shortcut（创建桌面快捷方式的工具）
public class CreateShut {
	public CreateShut(Activity activity) {
		// intent,Implicit jump, to create a desktop
		// shortcut（intent进行隐式跳转,到桌面创建快捷方式）
		Intent addIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// Not allowed to rebuild(不允许重建)
		addIntent.putExtra("duplicate", false);
		// Get the name of the application (得到应用的名称)
		String title = activity.getResources().getString(R.string.app_name);
		// 将应用的图标设置为Parceable类型
		Parcelable icon = Intent.ShortcutIconResource.fromContext(activity,
				R.drawable.ic_launcher);
		// 点击图标之后的意图操作
		Intent myIntent = new Intent(activity, MainActivity.class);
		// 设置快捷方式的名称
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		// 设置快捷方式的图标
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		// 设置快捷方式的意图
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);
		// 发送广播
		activity.sendBroadcast(addIntent);
	}
}
