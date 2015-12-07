package com.app.ipcamserver.httpd;

import com.app.ipcamserver.httpd.IMJpegHttpCallback;

interface IMJpegHttpService {
	void registerCallback(IMJpegHttpCallback callback);
	void unregisterCallback();
	void sendMedia(in byte[] buffer, in int size);
	boolean isStreaming();
	boolean isWriteable();
}
