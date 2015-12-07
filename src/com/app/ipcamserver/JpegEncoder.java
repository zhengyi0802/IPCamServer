package com.app.ipcamserver;

import java.io.ByteArrayOutputStream;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

public class JpegEncoder {
	private final static String TAG = "JpegEncoder";

	private int mWidth;
	private int mHeight;
	private YuvImage mImage;
	
	public JpegEncoder(int width, int height) {
		mWidth = width;
		mHeight = height;
	}
	
	public int offerEncoder(byte[] input, byte[] output) {
		int size = 0;
		int quality = 70;
		
		//ColorSpaceManager.NV21toYUV420Planar(input, yuv420, mWidth, mHeight);  
		mImage = new YuvImage(input, ImageFormat.NV21, mWidth, mHeight, null);
		Rect mRect = new Rect(0, 0, mWidth-1, mHeight-1);
		ByteArrayOutputStream outData = new ByteArrayOutputStream();		
		mImage.compressToJpeg(mRect, quality, outData);
		byte[] outdata = outData.toByteArray();
		if(output == null) {
			output = new byte[outdata.length];
		}
		System.arraycopy(outdata, 0, output, 0, outdata.length);
		size = outData.size();
		return size;
	}
	
	public void close() {
		mImage = null;
		mWidth = 0;
		mHeight = 0;
	}

	
}
