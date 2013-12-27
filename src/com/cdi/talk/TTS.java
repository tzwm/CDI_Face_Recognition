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
				String[] tmp = text.split("\\+");
				String last = tmp[0];
				Boolean haveCN = isChinese(last);
				for (int i = 1; i < tmp.length; i++) {
					if (isChinese(tmp[i]) == haveCN) {
						last = last + "+" + tmp[i];
						continue;
					}
					
					try {
						if (haveCN)
							playText(API_ZH_URL + last);
						else
							playText(API_EN_URL + last);
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					while (mp.isPlaying())
						;
					mp.release();
					mp = null;
					
					last = tmp[i];
					haveCN = isChinese(tmp[i]);
				}

				try {
					if (haveCN)
						playText(API_ZH_URL + last);
					else
						playText(API_EN_URL + last);
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		new Thread(r).start();
	}

	private final void playText(String url) throws IllegalStateException,
			IOException {
		mp = new MediaPlayer();
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mp.setDataSource(url);
		mp.prepare();
		mp.start();
	}

	private final boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public final boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

}
