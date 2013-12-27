package com.cdi.recognition;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class FaceRecognition {
	static final String IMG_FRAME_URL = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/CDI_Face/tmp.jpg";
	static final String IMG_DETECT_URL = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/CDI_Face/detect.jpg";
	static final String IMG_IDENTIFY_URL = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/CDI_Face/identify.jpg";
	
	static final public String API_KEY = "508423ba8aa6772014c2bf677f578437";
	static final public String API_SECRET = "m4m12wFJdmcoZRfRkFLkSKcuaayuYf3T";
	
	static public String GROUP_NAME = "test";
	
	private HttpRequests request = null;
	private CameraPreview cameraPreview = null;
	
	String face_id = "";
	JSONObject result = null;
	
	public FaceRecognition(CameraPreview context) {
		request = new HttpRequests(API_KEY, API_SECRET, true, true);
		cameraPreview = context;
	}
	
	public void detect(final String imgUrl, final String name) {

		// TODO Auto-generated method stub
		Runnable uploadRun = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					face_id = getFaceID(imgUrl);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(face_id.length() == 0){
					return;
				}
				
				PostParameters tmp = new PostParameters();
				tmp.setPersonName(name);
				try {
					request.personCreate(tmp);							
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tmp.setFaceId(face_id);
				try {
					request.personAddFace(tmp);
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				tmp = new PostParameters();
				tmp.setGroupName(GROUP_NAME);
				try {
					request.groupCreate(tmp);
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tmp.setPersonName(name);
				try {
					request.groupAddPerson(tmp);
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					request.trainIdentify(new PostParameters().setGroupName(GROUP_NAME));
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		new Thread(uploadRun).start();
	}
	
	public void identify(final String imgUrl){
		
		Runnable uploadRun = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					face_id = getFaceID(imgUrl);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(face_id.length() == 0){
					return;
				}
				
				PostParameters tmp = new PostParameters();
				tmp.setGroupName(GROUP_NAME);
				tmp.setMode("oneface");
				tmp.setImg(new File(imgUrl));
				String person_name = null, confidence = null;
				try {
					result = request.recognitionIdentify(tmp);
					person_name = result.getJSONArray("face").getJSONObject(0)
										.getJSONArray("candidate").getJSONObject(0)
										.getString("person_name");
					confidence = result.getJSONArray("face").getJSONObject(0)
										.getJSONArray("candidate").getJSONObject(0)
										.getString("confidence");
					cameraPreview.setPerson(person_name, confidence);
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(uploadRun).start();
	}
	
	private String getFaceID(String imgUrl) throws JSONException {
		File f = new File(imgUrl);
		try {
			PostParameters tmp = new PostParameters();
			tmp.setMode("oneface");
			tmp.setImg(f);
			JSONObject result = request.detectionDetect(tmp);
			
			return result.getJSONArray("face").getJSONObject(0).getString("face_id");
			
		} catch (FaceppParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
}
