package com.doubi.common;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageLoader {
	private LruCache<String,Bitmap> mLruCache;

	   
   @SuppressLint("NewApi")
   public ImageLoader(){
	   int cacheSize=(int) (Runtime.getRuntime().maxMemory()/4);
	   mLruCache=new LruCache<String, Bitmap>(cacheSize) {
	       protected int sizeOf(String key, Bitmap bitmap) {
	           return bitmap.getHeight()*bitmap.getRowBytes();
	       }
	   };
   }
   @SuppressLint("NewApi")
   public void addBitmapToMemoryCache(String key, Bitmap bitmap) {  
       if (getBitmapFromMemoryCache(key) == null) {
    	   mLruCache.put(key, bitmap);  
       }  
   }
   @SuppressLint("NewApi")
   public Bitmap getBitmapFromMemoryCache(String key) {  
	   return mLruCache.get(key);
       
   }

}
