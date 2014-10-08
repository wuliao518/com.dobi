package com.doubi.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import android.os.Environment;

import com.doubi.common.ConstValue;

public class Intelegence {
	/**
	 * 检查并创建图片存放目录创建目录
	 */
	public void CheckAndCreatRoot() {
		String root = Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH;
		File fileFolder = new File(root);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.HAIR_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();

		}

		fileFolder = new File(root + ConstValue.DEFAULT_HAIR_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.EYEBROWS_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.BEARD_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.BLUSHER_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.FACE_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.CLOTHES_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.DEFAULT_CLOTHES_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.SCENE_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.SCENE_PATH
				+ ConstValue.WOODEN_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.SCENE_PATH
				+ ConstValue.TRAVE_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.SCENE_PATH
				+ ConstValue.MOVE_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.PROP_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.PROP_PATH
				+ ConstValue.BUBBLE_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.PROP_PATH + ConstValue.PET_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.PROP_PATH
				+ ConstValue.PROPS_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.HAIR_PATH
				+ ConstValue.NEWHAIR_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.HAIR_PATH
				+ ConstValue.OLDHAIR_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.CLOTHES_PATH
				+ ConstValue.ARMORED_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.CLOTHES_PATH
				+ ConstValue.OPERA_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.CLOTHES_PATH
				+ ConstValue.GOLDEN_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.CLOTHES_PATH
				+ ConstValue.YOUNGTH_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH
				+ ConstValue.CLOUD_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH
				+ ConstValue.HAPPY_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH
				+ ConstValue.FRIEND_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH
				+ ConstValue.OPERA_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH
				+ ConstValue.WORLD_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH
				+ ConstValue.THREE_PATH);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileFolder = new File(root + ConstValue.MORE_SCENE_PATH
				+ ConstValue.MORE_SCENE_DEFAULT);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}
		fileFolder = new File(root + ConstValue.MORE_CLIP_FACE);
		if (!fileFolder.exists()) {
			fileFolder.mkdir();
		}

		fileExists();

	}

	// 判断文件是否存在
	private void fileExists() {
		BufferedWriter fileWriter = null;
		File versionFile = new File(Environment.getExternalStorageDirectory()
				+ ConstValue.ROOT_PATH + "Version.txt");
		if (!versionFile.exists()) {
			try {
				versionFile.createNewFile();
				fileWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(versionFile, true), "UTF-8"));
				fileWriter.append("0");
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
	}
}
