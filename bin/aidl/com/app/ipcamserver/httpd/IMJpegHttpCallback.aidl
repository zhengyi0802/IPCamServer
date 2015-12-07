package com.app.ipcamserver.httpd;

interface IMJpegHttpCallback {
	void  read(in byte[] buffer);
}