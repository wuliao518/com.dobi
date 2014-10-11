package com.doubi.logic.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dobi.R;
import com.doubi.common.CommonMethod;
import com.doubi.common.ConstValue;
import com.doubi.frist.activity_main.MainActivity;
import com.doubi.frist.date.NetManager;

@SuppressLint({ "HandlerLeak", "InflateParams" })
public class UpdateAPK {

	private int imageVersion=0;// 当前素材版本号
	private int imageVersionWeb = 0;// 网络获取素材版本号
	private int zipVersion;// 用于解压文件存储的临时版本号
	private Activity mActivity;// 用于获取当前运行的mActivity
	private MainActivity mainActivity;
	// 更新的提示信息
	private String updateMsg = R.string.find + "";
	// 返回的安装包Url
	private String apkUrl = "";
	// 素材包的Url
	private String imageUrl = "";
	// 图片素材下载url(不全)
	private String imageDownload = "";
	// 下载提示Dialog
	private MyDialog noticeDialog;
	// 下载进行时Dialog
	private MyDialog downloadDialog;
	// apk及素材包下载安装路径
	private static final String savePath = Environment
			.getExternalStorageDirectory() + ConstValue.ROOT_PATH;
	private static final String saveFileName = savePath + "doubi.apk";

	// 进度条
	private ProgressBar mProgress;
	private ProgressBar seekBar;
	// private TextView text;

	// 下载状态标志位
	private static final int DOWN_UPDATE = 1;

	private static final int DOWN_OVER = 2;

	// 进度条进度
	private int progress;

	// 当前进度条进度
	private int number = 0;

	// 下载线程
	private Thread downLoadThread;
	// 文件解压Handler
	public Handler readZipHandler;
	public Handler readingHandler;
	public Handler promptHandler;
	private String file;// 解压文件路径
	
	private NetManager netManager;

	// 拦截标志位true:拦截状态 false：未拦截状态
	private boolean interceptFlag = false;

	// APK进度条设置，更新进度百分比，下载完成后自动安装程序

	private Handler mHandler = new Handler() {

		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				// 设置滚动条百分比
				seekBar.setProgress(progress);
				break;
			case DOWN_OVER:
				installApk();
				break;
			default:
				break;
			}
		}
	};
	// 素材下载进度条动态设置
	private Handler imgHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				// 设置滚动条百分比
				seekBar.setProgress(number);
				break;
			case DOWN_OVER:
				mainActivity.into(true);
				CommonMethod.CloseDialog();
				break;
			default:
				break;
			}
		}
	};

	// 构造方法
	public UpdateAPK(Activity context) {
		this.mActivity = context;
		netManager=new NetManager(context);
		FileRead();
	}

	// 获取本地软件版本号
	int loadversion = 0;
	/**
	 * 检查当前更新信息
	 */
	public void checkUpdateInfo(MainActivity mainActivity) {
		// 获取包管理器，包含了安装在当前设备上的应用包的相关信息
		PackageManager nPackageManager = this.mActivity.getPackageManager();
		this.mainActivity=mainActivity;
		try {
			PackageInfo nPackageInfo = nPackageManager.getPackageInfo(
					this.mActivity.getPackageName(),
					PackageManager.GET_CONFIGURATIONS);
			// 获取现在app的版本号
			loadversion = nPackageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if(netManager.isOpenNetwork()||netManager.isOpenWifi()){
			GetJsonObject(loadversion);
		}else if(imageVersion!=0){
			mainActivity.into(true);
		}else if(imageVersion==0){
			mainActivity.into(false);
		}
	}
	/**
	 * 获取JSON数据
	 */
	private void GetJsonObject(final int loadversion) {
		final HttpClient client = new DefaultHttpClient();
		// StringBuilder 字符串变量（非线程安全）
		final StringBuilder builder = new StringBuilder();
		final HttpGet get = new HttpGet(ConstValue.VERSION_URL);
		// Task在另外的线程执行，不能直接在Task中更新UI，因此创建了Handler
		final Handler jsonHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String[] m = (String[]) msg.obj;
				showNoticeDialog(m[0], m[1], m[2]);
			}
		};
		// 图片素材Handler
		final Handler updateHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String m = (String) msg.obj;
				showNoticeDialog(m);
			}
		};
		// 下载apk文件
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpResponse response = client.execute(get);
					// response.getEntity从响应中获取消息实体
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					for (String s = reader.readLine(); s != null; s = reader
							.readLine()) {
						builder.append(s);
					}
					JSONObject jsonObject = null;
					jsonObject = new JSONObject(builder.toString());
					int version = 0;// 网络获取APK版本号
					String download = "";// 下载地址
					version = jsonObject.getInt("version");// 得到服务端的app版本号
					download = jsonObject.getString("downurl");
					imageDownload = jsonObject.getString("imageDownload");
					imageVersionWeb = jsonObject.getInt("imageversion");
					String ismust = jsonObject.getString("ismust");
					String capacity = jsonObject.getString("capacity");
					if ((loadversion < version)) {// 当前版本号小于服务器app版本号，需要更新
						Message mg = Message.obtain();// 获取Message对象
						mg.obj = new String[] { download, ismust, capacity };
						jsonHandler.sendMessage(mg);
					} else if (imageVersion < imageVersionWeb) {
						Message msg = Message.obtain();
						msg.obj = imageDownload;
						updateHandler.sendMessage(msg);
					} else {
						mainActivity.into(true);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

	/**
	 * 下载APK显示窗口 url:url地址 ismust:是否强制更新 capacity:文件大小
	 * 
	 * @param url
	 * @param ismust
	 * @param capacity
	 */
	private void showNoticeDialog(String url, final String ismust,
			String capacity) {
		final Handler updateHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String m = (String) msg.obj;
				showNoticeDialog(m);
			}
		};
		apkUrl = url;

		if (ismust.equals("1")) {
			updateMsg = mActivity.getString(R.string.version_low);
		}
		updateMsg += "\n" + mActivity.getString(R.string.flow) + capacity + "M";

		final LayoutInflater inflater = LayoutInflater.from(mActivity);
		View v = inflater.inflate(R.layout.dialog_updata, null);
		noticeDialog = new MyDialog(mActivity, 0, 0, v, R.style.Self_Dialog);
		/******************************************************************/
		WindowManager m = mActivity.getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
		LayoutParams p = noticeDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
		// p.height = (int) (d.getHeight() * 0.6); //高度设置为屏幕的0.6
		p.width = (int) (d.getWidth() * 0.80); // 宽度设置为屏幕的0.95
		noticeDialog.getWindow().setAttributes(p);
		/*****************************************************************/
		TextView title = (TextView) v.findViewById(R.id.dialogTitle);
		title.setText(mActivity.getResources().getString(
				R.string.dialog_updata_title));
		TextView text = (TextView) v.findViewById(R.id.dialogText);
		text.setText(mActivity.getResources().getString(
				R.string.version_low));
		Button ok = (Button) v.findViewById(R.id.dialogOk);
		Button cancel = (Button) v.findViewById(R.id.dialogCancel);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				noticeDialog.dismiss();
				showDownloadDialog();
			}

		});
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ismust.equals("1")) {
					mActivity.finish();
				}
				if (imageVersion < imageVersionWeb) {
					Message msg = Message.obtain();
					msg.obj = imageDownload;
					updateHandler.sendMessage(msg);
				} else {
					// Intent intent = new Intent(mActivity,
					// HomeActivity.class);
					// mActivity.startActivity(intent);
				}
				noticeDialog.dismiss();
			}
		});
		noticeDialog.setCancelable(false);
		noticeDialog.show();

	}

	/**
	 * 下载APK进行时的窗口
	 */
	private void showDownloadDialog() {
		noticeDialog.cancel();
		final Handler updateHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String m = (String) msg.obj;
				showNoticeDialog(m);
			}
		};

		final LayoutInflater inflater = LayoutInflater.from(mActivity);
		View v = inflater.inflate(R.layout.activity_progress, null);
		downloadDialog = new MyDialog(mActivity, 0, 0, v, R.style.Self_Dialog);
		TextView text = (TextView) v.findViewById(R.id.massage);
		text.setText(mActivity.getResources().getString(
				R.string.version_updateing));
		seekBar = (ProgressBar) v.findViewById(R.id.progress);
		downloadDialog.setContentView(v);

		/******************************************************************/
		WindowManager m = mActivity.getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
		LayoutParams p = downloadDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
		// p.height = (int) (d.getHeight() * 0.6); //高度设置为屏幕的0.6
		p.width = (int) (d.getWidth() * 0.80); // 宽度设置为屏幕的0.95
		downloadDialog.getWindow().setAttributes(p);
		/*****************************************************************/

		downloadDialog.setCancelable(false);
		downloadDialog.show();

		downloadApk();

	}

	/**
	 * 下载图片素材窗口
	 * 
	 * @param msg
	 */
	private void showNoticeDialog(String msg) {
		imageUrl = msg;
		if (imageVersion == 0) {
			ImageDownloadDialog();
		} else {
			
			final LayoutInflater inflater = LayoutInflater.from(mActivity);
			View v = inflater.inflate(R.layout.dialog_updata, null);
			noticeDialog = new MyDialog(mActivity, 0, 0, v, R.style.Self_Dialog);
			TextView title = (TextView) v.findViewById(R.id.dialogTitle);
			title.setText(mActivity.getResources().getString(
					R.string.dialog_updata_title));
			TextView text = (TextView) v.findViewById(R.id.dialogText);
			text.setText(mActivity.getResources().getString(
					R.string.dialog_updata_text));
			Button ok = (Button) v.findViewById(R.id.dialogOk);
			Button cancel = (Button) v.findViewById(R.id.dialogCancel);
			ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					noticeDialog.dismiss();
					ImageDownloadDialog();
				}

			});
			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mActivity.finish();

				}
			});
			noticeDialog.setCancelable(false);
			noticeDialog.show();
		}
	}

	/**
	 * 下载图片素材进行时窗口
	 */
	private void ImageDownloadDialog() {

		final LayoutInflater inflater = LayoutInflater.from(mActivity);
		View v = inflater.inflate(R.layout.activity_progress, null);
		downloadDialog = new MyDialog(mActivity, 0, 0, v, R.style.Self_Dialog);
		seekBar = (ProgressBar) v.findViewById(R.id.progress);
		downloadDialog.setContentView(v);
		/******************************************************************/
		WindowManager m = mActivity.getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
		LayoutParams p = downloadDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
		// p.height = (int) (d.getHeight() * 0.6); //高度设置为屏幕的0.6
		p.width = (int) (d.getWidth() * 0.80); // 宽度设置为屏幕的0.95
		downloadDialog.getWindow().setAttributes(p);
		/*****************************************************************/

		downloadDialog.setCancelable(false);
		downloadDialog.show();

		downImage();

	}

	private Runnable mdownApkRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				URL url = new URL(apkUrl);

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();

				File file = new File(savePath);

				if (!file.exists()) {
					file.mkdir();
				}
				String apkFile = saveFileName;
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);

				int count = 0;
				// 每次下载1024byte数据
				byte buf[] = new byte[1024];
				do {
					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread < 0) {
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);
				fos.close();
				is.close();
				downloadDialog.dismiss();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	};

	private Runnable mdownImageRunnable = new Runnable() {

		@Override
		public void run() {
			String imgurl = "";
			for (int i = imageVersion; i < imageVersionWeb; i++) {
				imgurl = imageUrl;
				imgurl = imgurl + (i + 1) + ".zip";
				// text.setText("素材包" + (i + 1) + ".zip下载中...");
				try {
					URL url = new URL(imgurl);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					int length = conn.getContentLength();
					InputStream is = conn.getInputStream();

					File file = new File(savePath);

					if (!file.exists()) {
						file.mkdir();
					}
					String apkFile = savePath + (i + 1) + ".zip";
					File ApkFile = new File(apkFile);
					FileOutputStream fos = new FileOutputStream(ApkFile);

					int count = 0;
					// 每次下载1024byte数据
					byte buf[] = new byte[1024];
					do {
						int numread = is.read(buf);
						count += numread;
						number = (int) (((float) count / length) * 100);
						imgHandler.sendEmptyMessage(DOWN_UPDATE);
						if (numread < 0) {
							imgHandler.sendEmptyMessage(DOWN_OVER);
							number = 0;
							break;
						}
						fos.write(buf, 0, numread);
					} while (!interceptFlag);

					fos.close();
					is.close();
					if (i >= imageVersion && i < imageVersionWeb - 1) {
						Message mg = Message.obtain();// 获取Message对象
						mg.what = 1;// 1代表下载进行
						mg.obj = i;
						readingHandler.sendMessage(mg);
					}
					if (i + 1 == imageVersionWeb) {
						Message mg = Message.obtain();// 获取Message对象
						mg.what = 2;// 2代表下载结束
						mg.obj = i;
						readingHandler.sendMessage(mg);

					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	};

	/**
	 * 下载apk
	 * 
	 * @param url
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 下载图片素材
	 * 
	 * @param url
	 */
	private void downImage() {

		readingHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int fileName = (Integer) msg.obj;
				String zipFile = savePath + (fileName + 1) + ".zip";
				switch (msg.what) {
				case 1:
					Message massage = new Message();// 获取Message对象
					massage.what = 3;// 3为下载进行时开启的解压
					massage.obj = zipFile;
					readZipHandler.sendMessage(massage);
					break;
				case 2:
					Message mass = new Message();// 获取Message对象
					mass.what = 4;// 4为下载结束时开启的解压即最后一次
					mass.obj = zipFile;
					readZipHandler.sendMessage(mass);

					Message ma = new Message();// 获取Message对象
					ma.what = 5;// 5代表弹出提示框解压进行时
					promptHandler.sendMessage(ma);
					break;
				}
			}
		};

		readZipHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				file = (String) msg.obj;
				switch (msg.what) {
				case 3:
					new Thread(new Runnable() {

						@Override
						public void run() {

							try {
								readByApacheZipFile(
										file,
										Environment
												.getExternalStorageDirectory()
												+ ConstValue.ROOT_PATH);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
					break;
				case 4:
					new Thread(new Runnable() {

						@Override
						public void run() {

							try {
								readByApacheZipFile(
										file,
										Environment
												.getExternalStorageDirectory()
												+ ConstValue.ROOT_PATH);

								Message massage = new Message();// 获取Message对象
								massage.what = 6;
								promptHandler.sendMessage(massage);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).start();
					break;
				}

			}
		};
		promptHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case 5:
					//CommonMethod.ShowMyDialog(mActivity);
					downloadDialog.dismiss();
					break;
				case 6:
					FileWrite();// 向文件中写入当前素材版本号
					CommonMethod.CloseDialog();
					break;
				}
			}
		};

		downLoadThread = new Thread(mdownImageRunnable);
		downLoadThread.start();
	}

	/**
	 * 安装APK
	 */
	private void installApk() {
		File apkfile = new File(saveFileName);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mActivity.startActivity(i);

	}

	/**
	 * 解压zip文件
	 * 
	 * @param archive压缩包路径
	 * @param decompressDir解压路径
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static void readByApacheZipFile(String archive, String decompressDir)
			throws IOException {
		BufferedInputStream bi;
		ZipFile compressFile = new ZipFile(archive);
		Enumeration e = compressFile.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze2 = (ZipEntry) e.nextElement();
			String entryName = ze2.getName();
			String path = decompressDir + "/" + entryName;
			if (ze2.isDirectory()) {
				System.out.print(R.string.establish_directory + entryName);// 正在创建解压目录-
				File decompressDirFile = new File(path);
				if (!decompressDirFile.exists()) {
					decompressDirFile.mkdirs();
				}
			} else {
				System.out.println(R.string.establish_file + entryName);// 正在创建解压文件-
				String fileDir = path.substring(1, path.lastIndexOf("/"));
				File fileDirFile = new File(fileDir);
				if (!fileDirFile.exists()) {
					fileDirFile.mkdirs();
				}	
				int index=entryName.lastIndexOf(".");
				if(index!=-1){
					StringBuffer sb=new StringBuffer();
					sb.append(entryName.substring(0, index));
					sb.append(entryName.substring(index+1, entryName.length()));
					entryName=sb.toString();
				}else{
					System.out.println("测试。。。"+entryName);
				}
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(decompressDir + "/" + entryName));
				
				bi = new BufferedInputStream(compressFile.getInputStream(ze2));
				byte[] readContent = new byte[1024];
				int readCount = bi.read(readContent);
				while (readCount != -1) {
					bos.write(readContent, 0, readCount);
					readCount = bi.read(readContent);
				}
				bos.close();
			}
		}
		compressFile.close();
		File file =new File(archive);
		if(file.exists()){
			file.delete();
		}
		
		
		
	}

	// 写文件的操作
	public void FileWrite() {

		BufferedWriter fileWriter = null;
		File versionFile = new File(savePath + "Version.txt");
		try {
			fileWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(versionFile), "UTF-8"));
			fileWriter.append(imageVersionWeb + "");
			// fileWriter.write(imageVersion + "");
			fileWriter.flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	// 读文件的操作
	public void FileRead() {
		BufferedReader reader = null;
		File fileReader = new File(savePath + "Version.txt");
		if(fileReader.exists()){
			String str = null;
			try {
				reader = new BufferedReader(new FileReader(fileReader));
				while ((str = reader.readLine()) != null) {
					imageVersion = Integer.parseInt(str);
					zipVersion = Integer.parseInt(str);
				}
				reader.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			imageVersion=0;
		}
		

	}

}
