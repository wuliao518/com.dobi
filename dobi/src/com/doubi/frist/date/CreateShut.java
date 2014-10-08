package com.doubi.frist.date;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;

import com.dobi.R;
import com.doubi.frist.activity_main.MainActivity;

//Create a desktop shortcut�����������ݷ�ʽ�Ĺ��ߣ�
public class CreateShut {
	public CreateShut(Activity activity) {
		// intent,Implicit jump, to create a desktop
		// shortcut��intent������ʽ��ת,�����洴����ݷ�ʽ��
		Intent addIntent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// Not allowed to rebuild(�������ؽ�)
		addIntent.putExtra("duplicate", false);
		// Get the name of the application (�õ�Ӧ�õ�����)
		String title = activity.getResources().getString(R.string.app_name);
		// ��Ӧ�õ�ͼ������ΪParceable����
		Parcelable icon = Intent.ShortcutIconResource.fromContext(activity,
				R.drawable.ic_launcher);
		// ���ͼ��֮�����ͼ����
		Intent myIntent = new Intent(activity, MainActivity.class);
		// ���ÿ�ݷ�ʽ������
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		// ���ÿ�ݷ�ʽ��ͼ��
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		// ���ÿ�ݷ�ʽ����ͼ
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);
		// ���͹㲥
		activity.sendBroadcast(addIntent);
	}
}
