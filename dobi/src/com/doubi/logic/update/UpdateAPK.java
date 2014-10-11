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

	private int imageVersion=0;// ��ǰ�زİ汾��
	private int imageVersionWeb = 0;// �����ȡ�زİ汾��
	private int zipVersion;// ���ڽ�ѹ�ļ��洢����ʱ�汾��
	private Activity mActivity;// ���ڻ�ȡ��ǰ���е�mActivity
	private MainActivity mainActivity;
	// ���µ���ʾ��Ϣ
	private String updateMsg = R.string.find + "";
	// ���صİ�װ��Url
	private String apkUrl = "";
	// �زİ���Url
	private String imageUrl = "";
	// ͼƬ�ز�����url(��ȫ)
	private String imageDownload = "";
	// ������ʾDialog
	private MyDialog noticeDialog;
	// ���ؽ���ʱDialog
	private MyDialog downloadDialog;
	// apk���زİ����ذ�װ·��
	private static final String savePath = Environment
			.getExternalStorageDirectory() + ConstValue.ROOT_PATH;
	private static final String saveFileName = savePath + "doubi.apk";

	// ������
	private ProgressBar mProgress;
	private ProgressBar seekBar;
	// private TextView text;

	// ����״̬��־λ
	private static final int DOWN_UPDATE = 1;

	private static final int DOWN_OVER = 2;

	// ����������
	private int progress;

	// ��ǰ����������
	private int number = 0;

	// �����߳�
	private Thread downLoadThread;
	// �ļ���ѹHandler
	public Handler readZipHandler;
	public Handler readingHandler;
	public Handler promptHandler;
	private String file;// ��ѹ�ļ�·��
	
	private NetManager netManager;

	// ���ر�־λtrue:����״̬ false��δ����״̬
	private boolean interceptFlag = false;

	// APK���������ã����½��Ȱٷֱȣ�������ɺ��Զ���װ����

	private Handler mHandler = new Handler() {

		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				// ���ù������ٷֱ�
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
	// �ز����ؽ�������̬����
	private Handler imgHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				// ���ù������ٷֱ�
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

	// ���췽��
	public UpdateAPK(Activity context) {
		this.mActivity = context;
		netManager=new NetManager(context);
		FileRead();
	}

	// ��ȡ��������汾��
	int loadversion = 0;
	/**
	 * ��鵱ǰ������Ϣ
	 */
	public void checkUpdateInfo(MainActivity mainActivity) {
		// ��ȡ���������������˰�װ�ڵ�ǰ�豸�ϵ�Ӧ�ð��������Ϣ
		PackageManager nPackageManager = this.mActivity.getPackageManager();
		this.mainActivity=mainActivity;
		try {
			PackageInfo nPackageInfo = nPackageManager.getPackageInfo(
					this.mActivity.getPackageName(),
					PackageManager.GET_CONFIGURATIONS);
			// ��ȡ����app�İ汾��
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
	 * ��ȡJSON����
	 */
	private void GetJsonObject(final int loadversion) {
		final HttpClient client = new DefaultHttpClient();
		// StringBuilder �ַ������������̰߳�ȫ��
		final StringBuilder builder = new StringBuilder();
		final HttpGet get = new HttpGet(ConstValue.VERSION_URL);
		// Task��������߳�ִ�У�����ֱ����Task�и���UI����˴�����Handler
		final Handler jsonHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String[] m = (String[]) msg.obj;
				showNoticeDialog(m[0], m[1], m[2]);
			}
		};
		// ͼƬ�ز�Handler
		final Handler updateHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String m = (String) msg.obj;
				showNoticeDialog(m);
			}
		};
		// ����apk�ļ�
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpResponse response = client.execute(get);
					// response.getEntity����Ӧ�л�ȡ��Ϣʵ��
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					for (String s = reader.readLine(); s != null; s = reader
							.readLine()) {
						builder.append(s);
					}
					JSONObject jsonObject = null;
					jsonObject = new JSONObject(builder.toString());
					int version = 0;// �����ȡAPK�汾��
					String download = "";// ���ص�ַ
					version = jsonObject.getInt("version");// �õ�����˵�app�汾��
					download = jsonObject.getString("downurl");
					imageDownload = jsonObject.getString("imageDownload");
					imageVersionWeb = jsonObject.getInt("imageversion");
					String ismust = jsonObject.getString("ismust");
					String capacity = jsonObject.getString("capacity");
					if ((loadversion < version)) {// ��ǰ�汾��С�ڷ�����app�汾�ţ���Ҫ����
						Message mg = Message.obtain();// ��ȡMessage����
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
	 * ����APK��ʾ���� url:url��ַ ismust:�Ƿ�ǿ�Ƹ��� capacity:�ļ���С
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
		Display d = m.getDefaultDisplay(); // Ϊ��ȡ��Ļ����
		LayoutParams p = noticeDialog.getWindow().getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
		// p.height = (int) (d.getHeight() * 0.6); //�߶�����Ϊ��Ļ��0.6
		p.width = (int) (d.getWidth() * 0.80); // �������Ϊ��Ļ��0.95
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
	 * ����APK����ʱ�Ĵ���
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
		Display d = m.getDefaultDisplay(); // Ϊ��ȡ��Ļ����
		LayoutParams p = downloadDialog.getWindow().getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
		// p.height = (int) (d.getHeight() * 0.6); //�߶�����Ϊ��Ļ��0.6
		p.width = (int) (d.getWidth() * 0.80); // �������Ϊ��Ļ��0.95
		downloadDialog.getWindow().setAttributes(p);
		/*****************************************************************/

		downloadDialog.setCancelable(false);
		downloadDialog.show();

		downloadApk();

	}

	/**
	 * ����ͼƬ�زĴ���
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
	 * ����ͼƬ�زĽ���ʱ����
	 */
	private void ImageDownloadDialog() {

		final LayoutInflater inflater = LayoutInflater.from(mActivity);
		View v = inflater.inflate(R.layout.activity_progress, null);
		downloadDialog = new MyDialog(mActivity, 0, 0, v, R.style.Self_Dialog);
		seekBar = (ProgressBar) v.findViewById(R.id.progress);
		downloadDialog.setContentView(v);
		/******************************************************************/
		WindowManager m = mActivity.getWindowManager();
		Display d = m.getDefaultDisplay(); // Ϊ��ȡ��Ļ����
		LayoutParams p = downloadDialog.getWindow().getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
		// p.height = (int) (d.getHeight() * 0.6); //�߶�����Ϊ��Ļ��0.6
		p.width = (int) (d.getWidth() * 0.80); // �������Ϊ��Ļ��0.95
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
				// ÿ������1024byte����
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
				// text.setText("�زİ�" + (i + 1) + ".zip������...");
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
					// ÿ������1024byte����
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
						Message mg = Message.obtain();// ��ȡMessage����
						mg.what = 1;// 1�������ؽ���
						mg.obj = i;
						readingHandler.sendMessage(mg);
					}
					if (i + 1 == imageVersionWeb) {
						Message mg = Message.obtain();// ��ȡMessage����
						mg.what = 2;// 2�������ؽ���
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
	 * ����apk
	 * 
	 * @param url
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * ����ͼƬ�ز�
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
					Message massage = new Message();// ��ȡMessage����
					massage.what = 3;// 3Ϊ���ؽ���ʱ�����Ľ�ѹ
					massage.obj = zipFile;
					readZipHandler.sendMessage(massage);
					break;
				case 2:
					Message mass = new Message();// ��ȡMessage����
					mass.what = 4;// 4Ϊ���ؽ���ʱ�����Ľ�ѹ�����һ��
					mass.obj = zipFile;
					readZipHandler.sendMessage(mass);

					Message ma = new Message();// ��ȡMessage����
					ma.what = 5;// 5��������ʾ���ѹ����ʱ
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

								Message massage = new Message();// ��ȡMessage����
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
					FileWrite();// ���ļ���д�뵱ǰ�زİ汾��
					CommonMethod.CloseDialog();
					break;
				}
			}
		};

		downLoadThread = new Thread(mdownImageRunnable);
		downLoadThread.start();
	}

	/**
	 * ��װAPK
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
	 * ��ѹzip�ļ�
	 * 
	 * @param archiveѹ����·��
	 * @param decompressDir��ѹ·��
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
				System.out.print(R.string.establish_directory + entryName);// ���ڴ�����ѹĿ¼-
				File decompressDirFile = new File(path);
				if (!decompressDirFile.exists()) {
					decompressDirFile.mkdirs();
				}
			} else {
				System.out.println(R.string.establish_file + entryName);// ���ڴ�����ѹ�ļ�-
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
					System.out.println("���ԡ�����"+entryName);
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

	// д�ļ��Ĳ���
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

	// ���ļ��Ĳ���
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
