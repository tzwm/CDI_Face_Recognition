package com.cdi.recognition;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.faceplusplus.api.FaceDetecter;
import com.faceplusplus.api.FaceDetecter.Face;
import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class CameraPreview extends Activity implements Callback,
		PreviewCallback {
	SurfaceView camerasurface = null;
	FaceMask mask = null;
	Camera camera = null;
	HandlerThread handleThread = null;
	Handler detectHandler = null;
	Runnable detectRunnalbe = null;
	private int width = 320;
	private int height = 240;
	FaceDetecter facedetecter = null;
	HttpRequests request = null;
	byte frameData[], ori[];
	String fileUrl = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/CDI_Face/tmp.jpg";
	Button detect_btn, upload_btn;
	String face_id;
	String group_name, person_name, confidence;
	
	private EditText io_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camerapreview);
		camerasurface = (SurfaceView) findViewById(R.id.camera_preview);
		mask = (FaceMask) findViewById(R.id.mask);
		LayoutParams para = new LayoutParams(480, 800);
		handleThread = new HandlerThread("dt");
		handleThread.start();
		detectHandler = new Handler(handleThread.getLooper());
		para.addRule(RelativeLayout.CENTER_IN_PARENT);
		camerasurface.setLayoutParams(para);
		mask.setLayoutParams(para);
		camerasurface.getHolder().addCallback(this);
		camerasurface.setKeepScreenOn(true);

		facedetecter = new FaceDetecter();
		if (!facedetecter.init(this, "508423ba8aa6772014c2bf677f578437")) {
			Log.e("diff", "有错误 ");
		}
		facedetecter.setTrackingMode(true);

		request = new HttpRequests("508423ba8aa6772014c2bf677f578437",
				"m4m12wFJdmcoZRfRkFLkSKcuaayuYf3T", true, true);

		group_name = "test";
		io_text = (EditText)findViewById(R.id.name);
		detect_btn = (Button) findViewById(R.id.detect_btn);
		upload_btn = (Button) findViewById(R.id.upload_btn);
		
		detect_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Runnable uploadRun = new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						saveImg(frameData);
						try {
							getFaceID();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(face_id.length() == 0){
							return;
						}
						
						PostParameters tmp = new PostParameters();
						tmp.setGroupName(group_name);
						tmp.setMode("oneface");
						tmp.setImg(new File(fileUrl));
						JSONObject result = null;
						try {
							result = request.recognitionIdentify(tmp);
							person_name = result.getJSONArray("face").getJSONObject(0)
												.getJSONArray("candidate").getJSONObject(0)
												.getString("person_name");
							confidence = result.getJSONArray("face").getJSONObject(0)
												.getJSONArray("candidate").getJSONObject(0)
												.getString("confidence");
							
							CameraPreview.this.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									io_text.setText(person_name + ":" + confidence);
								}
							});
							
						} catch (FaceppParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				};
				new Thread(uploadRun).start();
			}
		});

		upload_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				face_id = "";
				
				// TODO Auto-generated method stub
				Runnable uploadRun = new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						saveImg(frameData);
						try {
							getFaceID();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(face_id.length() == 0){
							return;
						}
						JSONObject result;
						PostParameters tmp = new PostParameters();
						tmp.setPersonName(io_text.getText().toString());
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
						tmp.setGroupName(group_name);
						try {
							request.groupCreate(tmp);
						} catch (FaceppParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						tmp.setPersonName(io_text.getText().toString());
						try {
							request.groupAddPerson(tmp);
						} catch (FaceppParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						try {
							request.trainIdentify(new PostParameters().setGroupName(group_name));
						} catch (FaceppParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				new Thread(uploadRun).start();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open(1);
		Camera.Parameters para = camera.getParameters();
		para.setPreviewSize(width, height);
		camera.setParameters(para);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		facedetecter.release(this);
		handleThread.quit();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.setDisplayOrientation(90);
		camera.startPreview();
		camera.setPreviewCallback(this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		camera.setPreviewCallback(null);
		
		detectHandler.post(new Runnable() {

			@Override
			public void run() {

				ori = new byte[width * height];
				frameData = data;
				int is = 0;
				for (int x = width - 1; x >= 0; x--) {

					for (int y = height - 1; y >= 0; y--) {

						ori[is] = data[y * width + x];

						is++;
					}

				}
				final Face[] faceinfo = facedetecter.findFaces(ori, height,
						width);

				if (faceinfo != null) {
//					saveImg(ori);
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mask.setFaceInfo(faceinfo);
					}
				});
				CameraPreview.this.camera
						.setOneShotPreviewCallback(CameraPreview.this);
			}
		});
	}

	private void saveImg(byte[] data) {
		final YuvImage image = new YuvImage(data, ImageFormat.NV21, width,
				height, null);
		ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
		image.compressToJpeg(new Rect(0, 0, width, height), 100, os);
		byte[] tmp = os.toByteArray();
		Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
		
		Matrix matrix = new Matrix();
		matrix.postScale(1f, 1f);
		matrix.postRotate(-90);
		bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
		
		File f = new File(fileUrl);
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(f));
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String getFaceID() throws JSONException {
		File f = new File(fileUrl);
		try {
			PostParameters tmp = new PostParameters();
			tmp.setMode("oneface");
			tmp.setImg(f);
			JSONObject result = request.detectionDetect(tmp);
			
			face_id = result.getJSONArray("face").getJSONObject(0).getString("face_id");
			return result.getJSONArray("face").getJSONObject(0).getString("face_id");
			
		} catch (FaceppParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		face_id = "";
		return "";
	}
	
}
