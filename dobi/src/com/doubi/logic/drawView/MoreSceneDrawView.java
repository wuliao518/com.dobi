package com.doubi.logic.drawView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

import com.dobi.R;
import com.doubi.common.ConstValue;
import com.doubi.logic.svgResolve.SVG;
import com.doubi.logic.svgResolve.SVGParser;
import com.doubi.view.adapter.item.MoreFaceItem;

/**
 * �Զ���ؼ��������ؼ�
 * 
 * @author YHZ
 */

@SuppressLint("FloatMath")
public class MoreSceneDrawView extends MoreDrawViewBase {
	private String mScenePath;

	public MoreSceneDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		baseImgCount = 0;
		// ���峡����Ҫ��ʾͼƬ������
		imgCount = baseImgCount + PROP_COUNT;
	}

	/**
	 * ��ʼ��
	 * 
	 * @param activity
	 * @param mBitmap
	 *            ��Activity��ͼƬ�ؼ��ϵ�ͼƬ��Դ
	 * @param cj_width
	 * @param cj_height
	 * @param harimap
	 * @param bodymap
	 */
	public void Inteligense(Activity activity, String scenePath, int cj_width,
			int cj_height) {
		mScenePath = scenePath;
		File sceneFile = new File(mScenePath + "/" + 0 + "jpg");
		Bitmap bitmapBj = mImageManager.getBitmapFromFile(sceneFile);
		int bjWidth = bitmapBj.getWidth();
		int bjHeight = bitmapBj.getHeight();
		// �������
		if (activity != null) {
			super.Inteligense(activity, bitmapBj, cj_width, cj_height);
		} else {
			if(sceneBitmap!=null){
				sceneBitmap.recycle();
			}
			sceneBitmap = Bitmap.createScaledBitmap(bitmapBj, cjHeight
					* bitmapBj.getWidth() / bitmapBj.getHeight(), cjHeight,
					false);
		}
		// ��ȡͷ������
		sceneFile = new File(mScenePath);
		File files[] = sceneFile.listFiles();
		creatMoreFaceItemList(files.length - 1);
		// ������Ҫ��ʾ�������������¶���ͼ������
		baseImgCount = moreFaceItems.size() * 2;
		// ���峡����Ҫ��ʾͼƬ������
		imgCount = baseImgCount + PROP_COUNT + 1;
		// ���峡����Ҫ��ʾͼƬ������
		mBmps = new Bmp[imgCount];
		// ��ͷ�����ͼƬ����ͼ��
		for (MoreFaceItem mMoreFaceItem : moreFaceItems) {
			int index = mMoreFaceItem.getIndex();
			File faceBackFile = new File(mScenePath + "/" + (index + 1)
					+ "png");
			if (faceBackFile.exists()) {
				Bitmap faceBackBitmap = mImageManager
						.getBitmapFromFile(faceBackFile);
				faceBackBitmap = Bitmap.createScaledBitmap(
						faceBackBitmap,
						faceBackBitmap.getWidth() * cjHeight
								* bitmapBj.getWidth() / bitmapBj.getHeight()
								/ bjWidth, faceBackBitmap.getHeight()
								* cjHeight / bjHeight, false);
				mBmps[index * 2 + 1] = new Bmp(faceBackBitmap, -1,
						(float) mMoreFaceItem.getLocation()[0],
						(float) mMoreFaceItem.getLocation()[1], false, false,
						false);
			}
		}
		updateFaces(true);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	/**
	 * �л�����
	 * 
	 * @param mBitmap
	 */
	public void updateScene(String scenePath) {
		// �л�������յ���
		propBmps = new Bmp[PROP_COUNT];
		this.Inteligense(null, scenePath, 0, 0);
	}

	/**
	 * ���ݱ���ͼƬ����ͷ���б�
	 * 
	 * @param count
	 *            ͷ������
	 */
	private void creatMoreFaceItemList(int count) {
		List<MoreFaceItem> listMoreFaceItem = new ArrayList<MoreFaceItem>();
		// List<int[]> list = new ArrayList<int[]>(); TODO for test
		List<int[]> list = mImageManager.getRed(sceneBitmap, count);
		
		Log.i("jiang","list.size()"+list.size());
		Log.i("jiang","count"+count);
		for (int i = 0; i < list.size(); i++) {
			MoreFaceItem mMoreFaceItem = new MoreFaceItem();
			mMoreFaceItem.setIndex(i);
			mMoreFaceItem.setLocation(list.get(i));
			mMoreFaceItem.setHangest(true);
			listMoreFaceItem.add(mMoreFaceItem);
		}
		Log.i("jiang", "jiangjiangjinag");
		this.setMoreFaceItems(listMoreFaceItem);
	}

	/**
	 * ��������ͼƬ
	 * 
	 * @param isChangeScene
	 *            �Ƿ����ڸ�������
	 */
	@SuppressLint("NewApi")
	public void updateFaces(boolean isChangeScene) {
		//Log.i("jiang", moreFaceItems.size()+"fuck");
		if (moreFaceItems != null) {
			Log.i("jiang","fuck222");
			if (moreFaceItems.size() > 0) {
				for (int i = 0; i < moreFaceItems.size(); i++) {
					if (moreFaceItems.get(i).isHangest()) {
						// Ĭ��ʹ���Ѿ��Ĺ�����Ƭ
						Bitmap faceBitmap = BitmapFactory
								.decodeFile(Environment
										.getExternalStorageDirectory()
										+ ConstValue.ROOT_PATH
										+ ConstValue.MORE_CLIP_FACE
										+ ConstValue.ImgName.morePhotoClip
												.toString() + i + "jpg");
						moreFaceItems.get(i).setmBitmap(faceBitmap);
						Log.i("jiang","fuck333");
						if (faceBitmap != null) {
							// ��һ�ν���ĳɹ�����
							InputStream mInputStream = getResources()
									.openRawResource(R.raw.default_face);
							SVG mSVG = SVGParser
									.getSVGFromResource(mInputStream);
							faceBitmap = mImageManager.ClipBitmapOnSVG(
									faceBitmap, mSVG);
							
							// ���ȿ�����ʾ����
							float expSize = 7f;
							faceBitmap = Bitmap
									.createScaledBitmap(
											faceBitmap,
											(int) (cjWidth * expSize / 100),
											(int) (cjWidth * expSize / 100
													* faceBitmap.getHeight() / faceBitmap
													.getWidth()), true);
							int face_x = moreFaceItems.get(i).getLocation()[0];
							int face_y = moreFaceItems.get(i).getLocation()[1]
									+ cjHeight * 2 / 100;
							mBmps[i * 2] = new Bmp(faceBitmap, i * 2, face_x,face_y, true, true, false);
							// �����������������
							if (!isChangeScene) {
								this.intBmp(i * 2);
							}
							ImageButton imgBtn = moreFaceItems.get(i)
									.getmButton();
							if (imgBtn != null) {
								imgBtn.setBackground(getResources()
										.getDrawable(R.drawable.button_face));
							}
						}
						moreFaceItems.get(i).setHangest(false);
					}
				}
			}
			// ����ͷ����Ϣ
			headBmps = mBmps;

			if (isChangeScene) {
				if (propBmps != null && propBmps.length > 0) {
					for (int i = 0; i < propBmps.length; i++) {
						mBmps[i + baseImgCount] = propBmps[i];
						// ��������id����֤���򣬽�ͼ������
						if (mBmps[i + baseImgCount] != null) {
							mBmps[i + baseImgCount].setImgId(i + baseImgCount);
						}
					}
				}
				this.intAllBmps();
			}

			this.invalidate();
		}
	}

	/**
	 * ���ӵ���
	 * 
	 * @param mBitmap
	 */
	public void addProp(Bitmap mBitmap) {
		if (propBmps == null) {
			propBmps = new Bmp[PROP_COUNT];
		}
		for (int i = 0; i < PROP_COUNT; i++) {
			if (propBmps[i] == null) {
				propBmps[i] = new Bmp(mBitmap, baseImgCount + i + 1,
						(new Random()).nextInt(cjWidth - 1) + 1,
						(new Random()).nextInt(cjHeight - 1) + 1, true, false,
						false);
				mBmps[i + baseImgCount] = propBmps[i];
				this.intBmp(i + baseImgCount);
				break;
			}
		}
		this.invalidate();
	}

	/**
	 * ɾ������
	 */
	public void delProp(int id) {
		if (propBmps[id - baseImgCount] != null) {
			propBmps[id - baseImgCount] = null;
			mBmps[id] = propBmps[id - baseImgCount];
		}
		this.invalidate();
	}

	/**
	 * ��ȡ�����༯��
	 * 
	 * @return
	 */
	public static List<MoreFaceItem> GetMoreFaceItems() {
		return moreFaceItems;
	}
}
