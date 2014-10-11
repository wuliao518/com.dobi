package com.doubi.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.doubi.common.ConstValue;
import com.doubi.common.ImageLoader;
import com.doubi.logic.ImageFilter.IImageFilter;
import com.doubi.logic.ImageFilter.Image;
import com.doubi.logic.ImageFilter.SoftGlowFilter;
import com.doubi.logic.drawView.MoreSceneDrawView;
import com.doubi.logic.svgResolve.SVG;
import com.doubi.logic.svgResolve.SVGParser;
import com.doubi.view.adapter.item.MapItem;
import com.doubi.view.adapter.item.MoreFaceItem;

/**
 * 图片存取
 * 
 * @author Administrator
 *
 */
@SuppressLint({ "SdCardPath", "DefaultLocale" })
public class ImageManager {

	private String extJPG = "jpg";
	private String extPNG = "png";
	private ImageLoader mImageLoader;
	public ImageManager(){
		mImageLoader=new ImageLoader();
	}
	/**
	 * 加载单人扮演拍照图片
	 * 
	 * @return Bitmap
	 */
	public boolean loadImg() {
		String filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH
				+ ConstValue.ImgName.singlePhotoClip.toString() +this.extJPG;
		File file = new File(filepath);

		return file.exists();
	}

	/**
	 * 加载多人扮演拍照图片
	 * 
	 * @return Bitmap
	 */
	public boolean loadImgForMore() {
		String filepath = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + ConstValue.MORE_CLIP_FACE
				+ "photo"+ this.extJPG;
		File file = new File(filepath);

		return file.exists();
	}

	/**
	 * 根据路径获取扩展名为png或jpg的Bitmap
	 * 
	 * @param file
	 * @return
	 */
	public Bitmap getBitmapFromPath(String path) {
		File file = new File(path);
		Bitmap bm = null;
		if (file != null && file.exists()) {
			bm = getBitmapFromFile(file, 0);
		}
		return bm;
	}

	/**
	 * 根据File 获取扩展名为png或jpg的Bitmap
	 * 
	 * @param file
	 * @return
	 */
	public Bitmap getBitmapFromFile(File file) {
		Bitmap bm = getBitmapFromFile(file, 0);
		return bm;
	}

	/**
	 * 根据File 获取扩展名为png或jpg的Bitmap
	 * 增加缓存
	 * 
	 * @param file
	 * @param maxWidth
	 *            最大宽度，为0代表不限制
	 * @return
	 */
	public Bitmap getBitmapFromFile(File file, int maxWidth) {
		Bitmap bm = mImageLoader.getBitmapFromMemoryCache(file.getAbsolutePath());
		if(bm!=null){
			return bm;
		}
		//if (file.exists()) {
			String ext =getExtensionName(file.getPath());
			//if (ext.equals(this.extPNG) || ext.equals(this.extJPG)) {
				try {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;// 只取尺寸
					bm = BitmapFactory.decodeFile(file.getPath(), options);
					int newWidth = 1000;
					if (maxWidth != 0) {
						maxWidth = maxWidth > options.outWidth ? options.outWidth
								: maxWidth;
						newWidth = maxWidth;
					} else {
						newWidth = newWidth > options.outWidth ? options.outWidth
								: newWidth;
					}

					options.inJustDecodeBounds = false;
					options.inPurgeable = true;// 使图片不抖动
					options.inInputShareable = true;// 获取资源

					if (newWidth < options.outWidth) {
						int scale = options.outWidth / newWidth;
						options.inJustDecodeBounds = false;
						options.inSampleSize = scale;
					}

					bm = BitmapFactory.decodeFile(file.getPath(), options);
					//加入缓存
					mImageLoader.addBitmapToMemoryCache(file.getAbsolutePath(), bm);

				} catch (Throwable ex) {
					// TODO 内存溢出提示
					Log.v("ImageManager", "图片加载失败：" + file.getPath());
					// mToast.show();
				}
			//}
		//}

		return bm;
	}

	/**
	 * 文件操作 获取文件扩展名
	 * 
	 * @param filename
	 * @return
	 */
	public String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}

	/**
	 * 文件操作 获取不带扩展名的文件名
	 * 
	 * @param filename
	 * @return
	 */
	public String getFileNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	/**
	 * 将拍下来的照片存放在SD卡中
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void saveToSDCard(byte[] data) throws IOException {
		FileOutputStream outputStream = creatFile(
				ConstValue.ImgName.photo.toString(), this.extJPG, "");
		outputStream.write(data); // 写入sd卡中
		outputStream.close(); // 关闭输出流
	}

	/**
	 * 将拍下来的照片存放在SD卡中
	 * 
	 * @param bitmap
	 *            需要保存的图片
	 * @param imgName
	 *            保存的文件名
	 * @throws IOException
	 */
	public void saveToSDCard(Bitmap bitmap, ConstValue.ImgName imgName)
			throws IOException {
		if (imgName == ConstValue.ImgName.photo
				|| imgName == ConstValue.ImgName.singlePhotoClip
				|| imgName == ConstValue.ImgName.resultImg) {
			this.saveToSDCard("", bitmap, imgName.toString(),
					Bitmap.CompressFormat.JPEG);
		} else if (imgName == ConstValue.ImgName.morePhotoClip) {

			// 获取正在给哪张脸拍照
			int index = 0;
			List<MoreFaceItem> list = MoreSceneDrawView.GetMoreFaceItems();
			if (list != null && list.size() != 0) {
				for (MoreFaceItem mMoreFaceItem : list) {
					if (mMoreFaceItem.isHangest()) {
						index = mMoreFaceItem.getIndex();
						break;
					}
				}
			}

			this.saveToSDCard(ConstValue.MORE_CLIP_FACE, bitmap,
					imgName.toString() + index, Bitmap.CompressFormat.JPEG);

		} else {
			this.saveToSDCard("", bitmap, imgName.toString(),
					Bitmap.CompressFormat.PNG);
		}
	}

	/**
	 * 保存图片到某个目录
	 * 
	 * @param path
	 *            dobi跟目录下的子目录，保存在根目录情况输入空
	 * @param bitmap
	 *            要保存的图片
	 * @param imgName
	 *            图片名称
	 * @param format
	 *            图片类型
	 * @throws IOException
	 */
	public void saveToSDCard(String path, Bitmap bitmap, String imgName,
			Bitmap.CompressFormat format) throws IOException {
		FileOutputStream outputStream = null;
		if (format.equals(Bitmap.CompressFormat.JPEG))
			outputStream = creatFile(imgName, this.extJPG, path);
		else if (format.equals(Bitmap.CompressFormat.PNG))
			outputStream = creatFile(imgName, this.extPNG, path);
		bitmap.compress(format, 100, outputStream);
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * 将改好的图片保存到本地相册
	 */

	public Uri saveToAlbum(Activity singleActivity, Bitmap bitmap)
			throws IOException {
		ContentValues values = new ContentValues(8);
		String newname = DateFormat.format("doubi-" + "yyyy-MM-dd kk.mm.ss",
				System.currentTimeMillis()).toString();
		values.put(MediaStore.Images.Media.TITLE, newname);// 名称
		values.put(MediaStore.Images.Media.DISPLAY_NAME, newname);
		values.put(MediaStore.Images.Media.DESCRIPTION, "");// 描述
		values.put(MediaStore.Images.Media.DATE_TAKEN,
				System.currentTimeMillis());// 图像的拍摄时间，显示时根据这个排序
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");// 默认为jpg格式
		values.put(MediaStore.Images.Media.ORIENTATION, 0);//

		final String CAMERA_IMAGE_BUCKET_NAME = "/sdcard/dcim/camera";
		final String CAMERA_IMAGE_BUCKET_ID = String
				.valueOf(CAMERA_IMAGE_BUCKET_NAME.hashCode());
		File parentFile = new File(CAMERA_IMAGE_BUCKET_NAME);
		String name = parentFile.getName().toLowerCase();

		values.put(Images.ImageColumns.BUCKET_ID, CAMERA_IMAGE_BUCKET_ID);// id
		values.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);

		// 先得到新的URI
		Uri uri = singleActivity.getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		String path=getFilePathByContentResolver(singleActivity,uri);
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri newUri=Uri.fromFile(new File(path));
		intent.setData(newUri);     
		singleActivity.sendBroadcast(intent);  
		try {
			// 写入数据
			OutputStream outStream = singleActivity.getContentResolver()
					.openOutputStream(uri);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.close();
			bitmap.recycle();
		} catch (Exception e) {
			// Log.e(TAG, "exception while writing image", e);
		}

		return uri;
	}

	/**
	 * 创建保存位置文件流
	 * 
	 * @param imgName
	 *            枚举中的文件名
	 * @param exp
	 *            扩展名
	 * @param childFoder
	 *            根目录之后的子目录
	 * @return
	 * @throws FileNotFoundException
	 */
	private FileOutputStream creatFile(String imgName, String exp,
			String childFoder) throws FileNotFoundException {
		String filename = imgName.toString();
		filename += exp;
		File fileFolder = new File(Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + childFoder);
		File jpgFile = new File(fileFolder, filename);
		FileOutputStream outputStream;

		outputStream = new FileOutputStream(jpgFile);
		// 文件输出流

		return outputStream;
	}

	/**
	 * 以宽为基准宽高同比例放缩
	 * 
	 * @param bitMap
	 * @param xSize
	 *            新图片宽度
	 * @return
	 */
	public Bitmap getNewSizeMap(Bitmap bitMap, int xSize) {
		int width = bitMap.getWidth();
		int height = bitMap.getHeight();
		if (xSize > 0) {
			int newHeight, newWidth;

			// 设置想要的大小
			newWidth = xSize;
			newHeight = newWidth * height / width;

			// 计算缩放比例
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;

			// 取得想要缩放的matrix参数
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			// 得到新的图片
			bitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix,
					true);

		}
		return bitMap;
	}

	/**
	 * 获取文件夹内文件和文件夹数量
	 * 
	 * @param filepath
	 *            文件夹路径
	 * @return
	 */
	public int GetFileCount(String filepath) {
		File directory = new File(filepath);
		File files[] = directory.listFiles();
		return files.length;
	}

	/**
	 * 获取文件夹内图片集合
	 * 
	 * @param filepath
	 *            文件夹路径
	 * @return
	 */
	public List<Bitmap> GetAllBitmaps(String filepath) {
		List<Bitmap> mapList = new ArrayList<Bitmap>();

		File directory = new File(filepath);
		File files[] = directory.listFiles();
		if (files != null) {
			for (File f : files) {
				if (!f.isDirectory()) {
					mapList.add(getBitmapFromFile(f));
				}
			}
		}

		return mapList;
	}

	/**
	 * 分段获取数据
	 * 
	 * @param filepath
	 *            文件夹地址
	 * 
	 * @param pageIndex
	 *            第几页
	 * @param pageSize
	 *            每页显示多少
	 * @param ext
	 *            扩展名
	 * @param size
	 *            缩小到的尺寸 ，0代表不放缩
	 * @return
	 */
	public List<MapItem> GetCurrentDatas(String filepath, int pageIndex,
			int pageSize, String ext, int size) {
		List<MapItem> mapList = new ArrayList<MapItem>();
		String tmpFilepath;
		for (int i = 0; i < pageSize; i++) {

			tmpFilepath = filepath + ((pageIndex - 1) * pageSize + 1 + i) + ext;
			File directory = new File(tmpFilepath);
			if (directory != null && !directory.isDirectory()
					&& directory.exists()) {
				Bitmap mBitmap = getBitmapFromFile(directory, size);
				MapItem mMapItem = new MapItem();
				mMapItem.setBitmap(mBitmap);
				mMapItem.setImgPath(tmpFilepath);
				mapList.add(mMapItem);
			}
		}
		return mapList;
	}

	/**
	 * 获取文件夹内图片集合
	 * 
	 * @param filepath
	 *            文件夹路径
	 * @return
	 */
	public List<SVG> GetAllSVGs(String filepath) {
		List<SVG> mapList = new ArrayList<SVG>();

		File directory = new File(filepath);
		File files[] = directory.listFiles();
		if (files != null) {
			for (File f : files) {
				if (!f.isDirectory()) {
					InputStream mInputStream = null;
					try {
						mInputStream = new FileInputStream(f);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					SVG mSVG = SVGParser.getSVGFromResource(mInputStream);
					mapList.add(mSVG);
				}
			}
		}

		return mapList;
	}

	/**
	 * 获取文件夹内图片集合
	 * 
	 * @param filepath
	 *            文件夹路径
	 * @return
	 */
	public List<String> GetAllFilePaths(String filepath) {
		List<String> mapList = new ArrayList<String>();

		File directory = new File(filepath);
		File files[] = directory.listFiles();
		if (files != null) {
			for (File f : files) {
				if (!f.isDirectory()) {
					mapList.add(f.getPath());
				}
			}
		}

		return mapList;
	}

	/**
	 * 获取文件夹内的文件夹列表
	 * 
	 * @param filepath
	 * @return
	 */
	public List<String> getCurrentFoders(String filepath, int pageIndex,
			int pageSize) {
		List<String> strList = new ArrayList<String>();
		String tmpFilepath;
		for (int i = 0; i < pageSize; i++) {

			tmpFilepath = filepath + ((pageIndex - 1) * pageSize + 1 + i);
			File directory = new File(tmpFilepath);
			if (directory != null && directory.isDirectory()
					&& directory.exists()) {
				strList.add(directory.getPath());
			}
		}

		return strList;
	}

	/**
	 * 获取文件夹内的文件夹列表
	 * 
	 * @param filepath
	 * @return
	 */
	public List<String> getAllFoders(String filepath) {
		File directory = new File(filepath);
		List<String> strList = new ArrayList<String>();
		File files[] = directory.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					strList.add(f.getPath());
				}
			}
		}

		return strList;
	}

	/**
	 * 根据path绘画bitmap白色虚线图
	 * 
	 * @param path
	 * @param w
	 * @param h
	 * @return
	 */
	public Bitmap getBitmapFromPath(Path path, int w, int h) {
		Bitmap mBitmap = Bitmap.createBitmap(w, h, ConstValue.MY_CONFIG_4444);
		Canvas saveToPhoneCanvas = new Canvas(mBitmap);
		Paint mPaint = new Paint();
		mPaint.setColor(Color.BLACK);// 设置画笔颜色
		mPaint.setStyle(Paint.Style.STROKE);// 设置空心
		mPaint.setStrokeWidth(3f);// 设置线的粗细
		mPaint.setAntiAlias(true);// 消除锯齿
		PathEffect effects = new DashPathEffect(new float[] { 15, 15, 15, 15 },
				1);// 设置虚线
		mPaint.setPathEffect(effects);
		saveToPhoneCanvas.setDrawFilter(new PaintFlagsDrawFilter(0,
				Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));// 消除锯齿
		saveToPhoneCanvas.drawPath(path, mPaint);
		return mBitmap;
	}

	/**
	 * 矩形处理类
	 */
	public class RectangleManager {
		/**
		 * 四个顶点坐标[x,y]
		 */
		private float[] d1 = { 0, 0 }, d2 = { 0, 0 };
		private float[] d3 = { 0, 0 }, d4 = { 0, 0 };

		/**
		 * 包含四个顶点的数组
		 */
		private float[][] apex = { d1, d2, d3, d4 };

		/**
		 * 中心点X坐标
		 */
		private float preX;
		/**
		 * 中心点Y坐标
		 */
		private float preY;
		/**
		 * 矩形宽度
		 */
		private float width;
		/**
		 * 矩形高度
		 */
		private float height;
		/**
		 * 旋转角度
		 */
		private float cos;

		/**
		 * 初始化
		 * 
		 * @param mpreX
		 *            中心点X坐标
		 * @param mpreY
		 *            中心点Y坐标
		 * @param mwidth
		 *            矩形宽度
		 * @param mheight
		 *            矩形高度
		 * @param mcos
		 *            旋转角度
		 */
		public RectangleManager(float mpreX, float mpreY, float mwidth,
				float mheight, float mcos) {
			preX = mpreX;
			preY = mpreY;
			width = mwidth;
			height = mheight;
			cos = mcos;

		}

		/**
		 * 获取四个顶点[x,y]
		 * 
		 * @return
		 */
		public float[][] geTapex() {
			// 左上x,y
			d1[0] = preX - width / 2;
			d1[1] = preY - height / 2;
			// 右上x,y
			d2[0] = preX + width / 2;
			d2[1] = preY - height / 2;
			// 右下x,y
			d3[0] = preX + width / 2;
			d3[1] = preY + height / 2;
			// 坐下x,y
			d4[0] = preX - width / 2;
			d4[1] = preY + height / 2;

			d1 = getRevolve(d1);
			d2 = getRevolve(d2);
			d3 = getRevolve(d3);
			d4 = getRevolve(d4);

			return apex;
		}

		/**
		 * 获取旋转后的坐标
		 * 
		 * @param d
		 * @return
		 */
		private float[] getRevolve(float[] d) {
			float[] n = { 0, 0 };
			if (cos > 0) {
				n[0] = (float) Math.sin(d[0]);
				n[1] = (float) Math.cos(d[1]);
			} else {
				n[0] = (float) Math.cos(d[0]);
				n[1] = (float) Math.sin(d[1]);
			}
			return n;
		}

		public float getPreX() {
			return preX;
		}

		public void setPreX(float preX) {
			this.preX = preX;
		}

		public float getPreY() {
			return preY;
		}

		public void setPreY(float preY) {
			this.preY = preY;
		}

		public float getWidth() {
			return width;
		}

		public void setWidth(float width) {
			this.width = width;
		}

		public float getHeight() {
			return height;
		}

		public void setHeight(float height) {
			this.height = height;
		}

		public float getCos() {
			return cos;
		}

		public void setCos(float cos) {
			this.cos = cos;
		}

	}

	/**
	 * 输入多个矩形，根据所有矩形位置创建能将所有矩形涵盖其中的竖直新矩形
	 * 
	 * @param mRectangleManager
	 * @return
	 */
	public RectangleManager getNewRectangle(
			RectangleManager... mRectangleManagers) {
		RectangleManager result = null;

		List<float[]> apexs = new ArrayList<float[]>();
		// 将所有点放在集合中
		for (RectangleManager mRectangleManager : mRectangleManagers) {
			if (mRectangleManager != null) {
				// 获取四个顶点
				float[][] apexF = mRectangleManager.geTapex();
				// 将每个顶点放在集合中
				for (float[] f : apexF) {
					apexs.add(f);
				}
			}
		}

		// 找出所有点中最外围的点
		float minX = apexs.get(0)[0], minY = apexs.get(0)[1], maxX = 0, maxY = 0;
		for (float[] apex : apexs) {
			minX = minX < apex[0] ? minX : apex[0];
			minY = minY < apex[1] ? minY : apex[1];
			maxX = maxX < apex[0] ? apex[0] : maxX;
			maxY = maxY < apex[1] ? apex[1] : maxY;
		}

		// 根据最外围的点坐标创建矩形
		result = new RectangleManager((maxX + minX) / 2, (maxY + minY) / 2,
				maxX - minX, maxY - minY, 0);
		return result;
	}

	/**
	 * 将文字写进bitmap
	 * 
	 * @param mBitmap
	 * @param text
	 * @return
	 */
	public Bitmap setTextToBitmap(Bitmap mBitmap, String text) {
		String w = text;
		int height = mBitmap.getHeight();
		int width = mBitmap.getWidth();
		int len = text.getBytes().length;// 文字长度s
		int count = 0;// 行数
		// 每行的字节数
		int firstLen = 0;
		int secondLen = 0;
		int thirdLen = 0;
		// 每行的字符数
		int firstCount = 0;
		int secondCount = 0;
		int thirdCount = 0;
		// int line = ((width * 3) / 4) * 5 / height;// 每行文字数
		for (int i = 0; i < text.length(); i++) {
			// String word=text.substring(i,i+1);
			char word = text.charAt(i);
			if ((firstLen * height) / 10 < width) {
				count = 1;
				// int extent=word.getBytes().length;
				if ((word >= 0x0000 && word <= 0x00FF)) {
					if (word >= 'a' && word <= 'z' || word == '1') {
						firstLen++;
					} else {
						firstLen = firstLen + 2;
					}
				} else {
					firstLen = firstLen + 2;
				}
				firstCount++;
			} else if ((secondLen * height) / 10 < width) {
				count = 2;
				// int extent=word.getBytes().length;
				if ((word >= 0x0000 && word <= 0x00FF)) {
					if (word >= 'a' && word <= 'z' || word == '1') {
						secondLen++;
					} else {
						secondLen = secondLen + 2;
					}
				} else {
					secondLen = secondLen + 2;
				}
				secondCount++;
			} else if ((thirdLen * height) / 10 < width) {
				count = 3;
				// int extent=word.getBytes().length;
				if ((word >= 0x0000 && word <= 0x00FF)) {
					if (word >= 'a' && word <= 'z' || word == '1') {
						thirdLen++;
					} else {
						thirdLen = thirdLen + 2;
					}
				} else {
					thirdLen = thirdLen + 2;
				}
				thirdCount++;
			} else {
				count = 4;
				break;
			}
		}

		/*
		 * int line = 50;// (20 * width) / (4 * height) + 1; if (len > 0 && len
		 * <= line) { count = 1; } else if (len > line && len <= line * 2) {
		 * count = 2; } else if (len > line * 2 && len <= line * 3) { count = 3;
		 * } else if (len > line * 3) { count = 4; }
		 */
		String[] str = new String[3];

		// String a = text;
		// a = a + 1;

		Bitmap resultBitmap = Bitmap.createBitmap(mBitmap.getWidth(),
				mBitmap.getHeight(), ConstValue.MY_CONFIG_8888);

		Canvas cv = new Canvas(resultBitmap);
		cv.drawBitmap(mBitmap, 0, 0, null);
		Paint p = new Paint();
		String familyName = "sans";
		Typeface font = Typeface.create(familyName, Typeface.NORMAL);
		p.setColor(Color.BLACK);
		p.setStyle(Style.FILL); 
		p.setStrokeWidth(1.0f);
		p.setTextSize(height / 9);
		// 将文字写入图片
		switch (count) {
		case 1:
			str[0] = w.substring(0);
			cv.drawText(str[0], paddingLeft(width, height, firstLen),
					height * 1 / 2, p);
			break;
		case 2:
			str[0] = w.substring(0, firstCount);
			str[1] = w.substring(firstCount);
			cv.drawText(str[0], paddingLeft(width, height, firstLen),
					height * 4 / 9, p);
			cv.drawText(str[1], paddingLeft(width, height, secondLen),
					height * 5 / 9, p);
			break;
		case 3:
			str[0] = w.substring(0, firstCount);
			str[1] = w.substring(firstCount, secondCount + firstCount);
			str[2] = w.substring(secondCount + firstCount);
			cv.drawText(str[0], paddingLeft(width, height, firstLen),
					height * 3 / 9, p);
			cv.drawText(str[1], paddingLeft(width, height, secondLen),
					height * 1 / 2, p);
			cv.drawText(str[2], paddingLeft(width, height, thirdLen),
					height * 6 / 9, p);
			break;
		case 4:
			str[0] = w.substring(0, firstCount);
			str[1] = w.substring(firstCount, secondCount + firstCount);
			str[2] = w.substring(secondCount + firstCount + 2, secondCount
					+ firstCount + thirdCount)
					+ "...";
			cv.drawText(str[0], paddingLeft(width, height, firstLen),
					height * 3 / 9, p);
			cv.drawText(str[1], paddingLeft(width, height, secondLen),
					height * 1 / 2, p);
			cv.drawText(str[2], width / 5, height * 6 / 9, p);
			break;

		}
		// cv.drawText(text, width / 8, height / 2, p);

		return resultBitmap;
	}

	/**
	 * 将手机中原图按一定比例截出
	 * 
	 * @param mBitmap
	 * @param mSVG
	 * @return
	 */
	public Bitmap ClipBitmapOnSVG(Bitmap mBitmap, SVG mSVG) {
		int w = mBitmap.getWidth();
		Path mPath = mSVG.getPath();
		mBitmap = this.getNewSizeMap(mBitmap, mSVG.getPicture().getWidth());
		mBitmap = this.ClipBitmapOnPath(mBitmap, mPath);
		mBitmap = this.getNewSizeMap(mBitmap, w);
		return mBitmap;
	}

	/**
	 * 把图片按着某种路径切割，剩余部分为透明
	 * 
	 * @param canvasBitmap
	 * @param mPath
	 * @return
	 */
	public Bitmap ClipBitmapOnPath(Bitmap canvasBitmap, Path mPath) {
		Bitmap roundConcerImage = null;
		// 创建一个和原始图片一样大小位图
		roundConcerImage = Bitmap.createBitmap(canvasBitmap.getWidth(),
				canvasBitmap.getHeight(), ConstValue.MY_CONFIG_8888);
		// 创建带有位图roundConcerImage的画布
		Canvas mCanvas = new Canvas(roundConcerImage);
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));// 消除锯齿
		mCanvas.clipPath(mPath, Region.Op.REPLACE);// 设置切割方式
		mCanvas.drawBitmap(canvasBitmap, 0, 0, null);

		// 压缩图片
		int newWidth = ConstValue.FACE_SIZE[0];
		int newHeight = newWidth * roundConcerImage.getHeight()
				/ roundConcerImage.getWidth();
		roundConcerImage = Bitmap.createScaledBitmap(roundConcerImage,
				newWidth, newHeight, false);

		if (canvasBitmap != roundConcerImage) {
			canvasBitmap.recycle();
		}
		return roundConcerImage;
	}

	/**
	 * 获取图片上5*5红点的坐标集合
	 * 
	 * @param bitmap
	 * @param count
	 *            头像数量
	 * @return
	 */
	public List<int[]> getRed(Bitmap bitmap, int count) {
		List<int[]> list = new ArrayList<int[]>();
		int flag = 0;
		int red = 210, green = 50, blue = 50;
		for (int i = 6; i < bitmap.getWidth() - 3; i++) {
			for (int j = 6; j < bitmap.getHeight() - 3; j++) {
				int l = bitmap.getPixel(i, j);// 获取像素点上的agrb
				int r = (l & 0x00ff0000) >> 16;// 取高两位(R)
				int g = (l & 0x0000ff00) >> 8; // (G)
				int b = (l & 0x000000ff);// 取低两位(B)
				
				if (r >= red && g < green && b < blue) {
						int l3 = bitmap.getPixel(i-5, j-5);// 获取像素点上的agrb
						int r3 = (l3 & 0x00ff0000) >> 16;// 取高两位(R)
						int g3 = (l3 & 0x0000ff00) >> 8; // (G)
						int b3 = (l3 & 0x000000ff);// 取低两位(B)
					
					for (int m = i; m < i + 3; m++) {
						for (int n = j; n < j + 3; n++) {
							int l2 = bitmap.getPixel(m, n);
							int r2 = (l2 & 0x00ff0000) >> 16; // 取高两
							int g2 = (l2 & 0x0000ff00) >> 8; // (G)
							int b2 = (l2 & 0x000000ff);// 取低两位(B)
							if (r2 >= red && g2 < green && b2 < blue && r3>=red&&g3>red&&b3>red) {
								flag++;
							} else {
								break;
							}
						}
					}
					if (flag == 9) {
						int k[] = { i + 1, j + 1 };
						list.add(k);
						if (list.size() == count) {
							return list;
						}
						i = i + 10;
					}
					flag = 0;

				}
			}
			if (list.size() == count) {
				break;
			}
		}
		return list;
	}

	/**
	 * 分等级处理图像，共分三级
	 * 
	 * @param mBitmap
	 * @param filterLevel
	 *            0:弱 , 1:中, 2:强, 3:不变
	 * @param speed
	 *            渲染速度（1~4），数字越大速度越快，质量越低
	 * @return
	 */
	public Bitmap LoadBitmapFilter(Bitmap mBitmap, int filterLevel, float speed) {
		mBitmap = this.getNewSizeMap(mBitmap,
				(int) (mBitmap.getWidth() / speed));
		// ==============色相、饱和度、亮度处理===============
		// f 表示亮度比例，取值小于1，表示亮度减弱，否则亮度增强
		float mHueValue = 1;
		// saturation 饱和度值，最小可设为0，此时对应的是灰度图(也就是俗话的“黑白图”)，
		// 为1表示饱和度不变，设置大于1，就显示过饱和
		float mSaturationValue = 1;
		// hueColor就是色轮旋转的角度,正值表示顺时针旋转，负值表示逆时针旋转
		// 这里相当于改变的是全图的色相
		float mLightnessValue = 0;

		// =============================我的美白===================================

		switch (filterLevel) {
		case 0:
			mHueValue = 1.0f;
			mSaturationValue = 1.0f;
			mLightnessValue = 0f;
			break;
		case 1:
			mHueValue = 1.025f;
			mSaturationValue = 1.0f;
			mLightnessValue = 0f;
			break;
		case 2:
			mHueValue = 1.05f;
			mSaturationValue = 1.0f;
			mLightnessValue = 0f;
			break;
		case 3:
			mHueValue = 1.0f;
			mSaturationValue = 1.0f;
			mLightnessValue = 0f;
			break;

		}

		// 色相
		ColorMatrix mHueMatrix = new ColorMatrix();
		mHueMatrix.reset();
		mHueMatrix.setScale(mHueValue, mHueValue, mHueValue, 1);
		// 饱和度
		ColorMatrix mSaturationMatrix = new ColorMatrix();
		mSaturationMatrix.reset();
		mSaturationMatrix.setSaturation(mSaturationValue);
		// 亮度
		ColorMatrix mLightnessMatrix = new ColorMatrix();
		mLightnessMatrix.reset(); // 设为默认值
		mLightnessMatrix.setRotate(0, mLightnessValue); // 控制让红色区在色轮上旋转hueColor葛角度
		mLightnessMatrix.setRotate(1, mLightnessValue); // 控制让绿红色区在色轮上旋转hueColor葛角度
		mLightnessMatrix.setRotate(2, mLightnessValue); // 控制让蓝色区在色轮上旋转hueColor葛角度

		ColorMatrix mAllMatrix = new ColorMatrix();
		mAllMatrix.reset();
		mAllMatrix.postConcat(mHueMatrix);
		mAllMatrix.postConcat(mSaturationMatrix); // 效果叠加
		mAllMatrix.postConcat(mLightnessMatrix); // 效果叠加

		Paint paint = new Paint(); // 新建paint
		paint.setAntiAlias(true); // 设置抗锯齿,也即是边缘做平滑处理
		paint.setColorFilter(new ColorMatrixColorFilter(mAllMatrix));// 设置颜色变换效果
		Bitmap bmp = Bitmap.createBitmap(mBitmap.getWidth(),
				mBitmap.getHeight(), ConstValue.MY_CONFIG_8888);
		// 创建一个相同尺寸的可变的位图区,用于绘制调色后的图片
		Canvas canvas = new Canvas(bmp); // 得到画笔对象
		canvas.drawBitmap(mBitmap, 0, 0, paint); // 将颜色变化后的图片输出到新创建的位图区
		// 返回新的位图，也即调色处理后的图片
		mBitmap = bmp;

		// =============亮化，柔光处理==需要时间较长===========
		IImageFilter filter = null;
		switch (filterLevel) {
		case 0:
			filter = new SoftGlowFilter(4, -0.6f, 0f);
			break;
		case 1:
			// filter = new SoftGlowFilter(6, -0.3f, 0.02f);
			filter = new SoftGlowFilter(3, -0.5f, 0f);
			break;
		case 2:
			// filter = new SoftGlowFilter(7, -0.2f, 0.05f);
			filter = new SoftGlowFilter(3, -0.4f, 0.02f);
			break;
		case 3:
			filter = new SoftGlowFilter(0, -1.0f, 0f);
			break;
		default:
			break;
		}
		Image img = null;
		try {
			img = new Image(mBitmap);
			if (filter != null) {
				img = filter.process(img);
				img.copyPixelsFromBuffer();
			}
			mBitmap = img.getImage();

		} catch (Exception e) {
			if (img != null && img.destImage.isRecycled()) {
				img.destImage.recycle();
				img.destImage = null;
				System.gc(); // 提醒系统及时回收
			}
		} finally {
			if (img != null && img.image.isRecycled()) {
				img.image.recycle();
				img.image = null;
				System.gc(); // 提醒系统及时回收
			}
		}

		// =============================================
		mBitmap = this.getNewSizeMap(mBitmap,
				(int) (mBitmap.getWidth() * speed));
		return mBitmap;
	}

	/**
	 * 读取图片的旋转的角度
	 *
	 * @param path
	 *            图片绝对路径
	 * @return 图片的旋转角度
	 */
	public int GetBitmapDegree(String path) {
		int degree = 0;
		try {
			// 从指定路径下读取图片，并获取其EXIF信息
			ExifInterface exifInterface = new ExifInterface(path);
			// 获取图片的旋转信息
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 将图片按照某个角度进行旋转
	 *
	 * @param bm
	 *            需要旋转的图片
	 * @param degree
	 *            旋转角度
	 * @return 旋转后的图片
	 */
	public Bitmap getNewDegreeMap(Bitmap bm, int degree) {
		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
					matrix, true);
		} catch (OutOfMemoryError e) {
		}
		return bm;
	}

	/**
	 * byte 类型转换为Bitmap
	 * 
	 * @param b
	 * @return
	 */
	public Bitmap BytesToBimap(byte[] b,Context context) {
		if (b.length != 0) {
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inJustDecodeBounds=true;
			Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length,options);
			int scale=1;
			float bitWidth=options.outWidth;
			float bitHeight=options.outHeight;
			WindowManager wm=(WindowManager) context.getSystemService("window");
			Display display=wm.getDefaultDisplay();
			float width=display.getWidth();
			float height=display.getHeight();
			float scaleX=(float)bitWidth/width;
			float scaleY=(float)bitHeight/height;
			scale=(int) Math.max(scaleX,scaleY);
			if(scale>1){
				options.inJustDecodeBounds=false;
				options.inSampleSize=scale;
				bitmap = BitmapFactory.decodeByteArray(b, 0, b.length,options);
			}else{
				options.inJustDecodeBounds=false;
				options.inSampleSize=1;
				bitmap = BitmapFactory.decodeByteArray(b, 0, b.length,options);
			}
			return bitmap;
		} else {
			return null;
		}
	}

	/**
	 * 计算位置显示位置
	 * 
	 * @param width图片的宽
	 * @param height图片的高
	 * @param count每行的字节数
	 * @return
	 */
	public float paddingLeft(int width, int height, int count) {
		float distance = 0f;
		distance = (float) ((width - (height / 16) * count) / 2);
		return distance;
	}
	private  String getFilePathByContentResolver(Context context, Uri uri) {  
        if (null == uri) {  
            return null;  
        }  
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);  
        String filePath  = null;  
        if (null == c) {  
            throw new IllegalArgumentException(  
                    "Query on " + uri + " returns null result.");  
        }  
        try {  
            if ((c.getCount() != 1) || !c.moveToFirst()) {  
            } else {  
                filePath = c.getString(  
                        c.getColumnIndexOrThrow(MediaColumns.DATA));  
            }  
        } finally {  
            c.close();  
        }  
        return filePath;  
    } 
}
