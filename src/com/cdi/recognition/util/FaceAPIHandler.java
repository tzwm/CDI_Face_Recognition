package com.cdi.recognition.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FaceAPIHandler {
    private String api_key = "508423ba8aa6772014c2bf677f578437";
    private String api_secret = "m4m12wFJdmcoZRfRkFLkSKcuaayuYf3T";
	private String api_url = "apicn.faceplusplus.com";
	
    public void detection_detect() throws IOException {
    	URL url  = new URL(api_url);
    	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    	connection.setDoOutput(true);
    	connection.setDoInput(true);
    	connection.setRequestMethod("POST");
    	connection.setUseCaches(false);
    	connection.setInstanceFollowRedirects(true);
//    	connection.setRequestProperty("img", newValue);
    	connection.connect();
    }
}
