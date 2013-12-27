package com.cdi.talk;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;

public class TTS {
	final private String API_EN_URL = "http://translate.google.com/translate_tts?ie=utf-8&tl=en&q=";
	final private String API_ZH_URL = "http://translate.google.com/translate_tts?ie=utf-8&tl=zh&q=";
	
	private MediaPlayer mp = null;
	
	public TTS() {
	}
	
	public void speech(final String text) {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mp = new MediaPlayer();
				mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					mp.setDataSource(API_EN_URL + text);
					mp.prepare();
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
				mp.start();
			}
		};
		
		new Thread(r).start();
	}
}
