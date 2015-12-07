package com.app.ipcamserver.httpd;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.app.ipcamserver.httpd.NanoHTTPD.ClientHandler;
import com.app.ipcamserver.httpd.NanoHTTPD.IHTTPSession;
import com.app.ipcamserver.httpd.NanoHTTPD.Response;
import com.app.ipcamserver.httpd.NanoHTTPD.Response.IStatus;
import com.app.ipcamserver.httpd.NanoHTTPD.Response.Status;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class WebServer extends NanoHTTPD {
	private final static String TAG = "WebServer";
	
	private MJpegStream mStream;
	private Context mContext;
	private boolean isStreaming = false;
	
	private class SessionMap {
		private IHTTPSession session;
		private OutputStream outputStream;

		public SessionMap(IHTTPSession s) {
			session = s;
			outputStream = s.getOutputStream();
		}
		
		public OutputStream getOutputStream() {
			return outputStream;
		}
		
		public IHTTPSession getHTTPSession() {
			return session;
		}
	}
	
	private List<SessionMap> mSessionMap = new ArrayList<SessionMap>();
	
	public WebServer(Context context, String hostname, int port) throws IOException  {
		super(hostname, port);
		mContext = context;
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		Log.d(TAG, "serve uri = " + uri);
		if(uri.startsWith("/video.cgi")) {
			if(!mSessionMap.contains(session)) {
				SessionMap map = new SessionMap(session);
				mSessionMap.add(map);
			}
			serveStream(session);
			isStreaming = true;
			return new Response(Status.OK, "multipart/x-mixed-replace;boundary=" + MJpegStream.mBoundary+"\r\n" , null, -1);
		} else {
			return serveHome(session);
		}
	}

	private Response serveHome(IHTTPSession session) {
		AssetManager asset = mContext.getAssets();
		String uri = session.getUri();
		InputStream ins = null;
		int length = 0;
		String filename = uri.substring(1); 
		String mime = "text/html";
		if(uri.equals("/")) {
			filename = "index.html";
		} else if(uri.endsWith(".css"))  {
			mime="text/css";
		} else if(uri.endsWith(".gif")) {
			mime = "image/gif";
		} else if(uri.endsWith(".jpg")) {
			mime = "image/jpg";
		} else if(uri.endsWith(".js")) {
			mime = "text/javascripts";
		}
		try {
			ins = asset.open(filename);
			length = ins.available();
		} catch (IOException e) {
			Log.e(TAG, "can not send Stream Header! filename=" + filename);
		}		
		Response resp = new Response(Status.OK, "text/html", ins, length);
		return resp;
	}
	
	private void serveStream(IHTTPSession session) {
		sendHeader(session);
		return;
	}
	
	private void sendHeader(IHTTPSession session) {
		OutputStream outputStream = session.getOutputStream();
        SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), false);
            pw.print("HTTP/1.1 200 OK\r\n");
            pw.print("Content-Type: multipart/x-mixed-replace;boundary=" + MJpegStream.mBoundary + "\r\n");
            pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
            //pw.print("Expires: -1\r\n");
            pw.print("Server: AndroidServer\r\n");
            pw.print("Cache-Control: no-cache\r\n");
            pw.print("Pragma: no-cache\r\n");
            pw.print("\r\n");
            pw.flush();
            outputStream.flush();
        } catch (IOException ioe) {
            //NanoHTTPD.LOG.log(Level.SEVERE, "Could not send response to the client", ioe);
        }
        return;
	}
	
	public void sendStream(MJpegStream inputStream)  {
		if(mSessionMap.isEmpty()) {
			isStreaming = false;
			return;
		}
		
		for(SessionMap map : mSessionMap) {
			if(map !=null) {
				OutputStream outputStream = map.getOutputStream();
				if(outputStream != null) {
					try {
						sendData(outputStream, inputStream);
					} catch (IOException e) {
						Log.e(TAG, "can not send Stream!");
						closeSession(map.getHTTPSession());
						mSessionMap.remove(map);
						return;
					}
				} // if outputStream != null
			} // if map != null
		} // for
		return;
	} 

	public void sendData(OutputStream outputStream, MJpegStream inputStream)  throws IOException {
		//Log.d(TAG,"sendData len=" + inputStream.available());
		if(inputStream.available() <= 0) return;
		byte[] header = inputStream.getHeader();
		outputStream.write(header);
		outputStream.flush();
		byte[] imagebuffer = inputStream.getVideoBuffer().array();
		outputStream.write(imagebuffer);
		outputStream.flush();
	}
	
    public boolean isStreaming() {
    	return isStreaming;
    }

	
}
