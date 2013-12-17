package com.cdi.recognition.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.cdi.recognition.CameraPreview;

public class FaceAPIHandler {
    private String api_key = "508423ba8aa6772014c2bf677f578437";
    private String api_secret = "m4m12wFJdmcoZRfRkFLkSKcuaayuYf3T";
	private String api_url = "apicn.faceplusplus.com";
	
    public void detection_detect() throws IOException {
    	// 定义数据分隔线
    	String BOUNDARY = "---------7d4a6d158c9"; 
    	byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
    	
    	URL url  = new URL(api_url);
    	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    	connection.setDoOutput(true);
    	connection.setDoInput(true);
    	connection.setRequestMethod("POST");
    	connection.setUseCaches(false);
    	connection.setRequestProperty("connection", "Keep-Alive");
    	connection.setRequestProperty("Charsert", "UTF-8");
    	connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
    	
    	OutputStream out = new DataOutputStream(connection.getOutputStream());
    	File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/tmp.jpg");
    	StringBuilder sb = new StringBuilder();
    	sb.append("--");
    	sb.append(BOUNDARY);
    	sb.append("\r\n");
    	sb.append("Content-Disposition: form-data;name=\"img\";filename=\"" + file.getName() + "\"\r\n");
    	sb.append("Content-Type:application/octet-stream\r\n\r\n");
    	
    	byte[] data = sb.toString().getBytes();
    	out.write(data);
    	DataInputStream in = new DataInputStream(new FileInputStream(
    	file));
    	int bytes = 0;
    	byte[] bufferOut = new byte[1024];
    	while ((bytes = in.read(bufferOut)) != -1) {
    	out.write(bufferOut, 0, bytes);
    	}
    	out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
    	in.close();
    	
    	sb = new StringBuilder();
    	sb.append("--");
    	sb.append(BOUNDARY);
    	sb.append("\r\n");
    	sb.append("Content-Disposition: form-data;name=\"params\"\r\n\r\n");
    	out.write(sb.toString().getBytes());
    	out.write(("{'api_key':'" + api_key + "','api_secret':'" + api_secret + "'}").getBytes());
    	out.write("\r\n".getBytes());
    	out.write(end_data);
    	out.flush();
    	out.close();
    	
    	BufferedReader reader = new BufferedReader(new InputStreamReader(
    			connection.getInputStream()));
    	
    	String line = null;
    	while ((line = reader.readLine()) != null) {
    			Log.d("out", line);
    	}
    }
}
