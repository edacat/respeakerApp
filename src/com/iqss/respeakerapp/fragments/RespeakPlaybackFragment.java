/**
 * RespeakPlaybackFragment.java
 *  
 * Sets up specific functionality for respeaking playback (i.e. slowed down audio).
 * Uses the Sonic NDS open source code.
 * 
 * @author Jessica Yao
 */

package com.iqss.respeakerapp.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.vinuxproject.sonic.AndroidAudioDevice;
import org.vinuxproject.sonic.Sonic;

import android.app.Activity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.fragments.PlaybackFragment;
import com.iqss.respeakerapp.utils.Microphone;

public class RespeakPlaybackFragment extends PlaybackFragment {
	
	private float speed = (float) 0.7;
	private float pitch = (float) 1.0;
	private float rate = (float) 1.0;
	private Thread thread = null;

	public void play(final Activity activity, final ImageButton button) {
		thread = new Thread(new Runnable() {
			public void run() {
				AndroidAudioDevice device = new AndroidAudioDevice(
						Microphone.RECORDER_SAMPLE_RATE, 1);
				Sonic sonic = new Sonic(Microphone.RECORDER_SAMPLE_RATE, 1);
				int bytesRead;
				byte samples[] = new byte[4096];
				byte modifiedSamples[] = new byte[2048];
				InputStream soundFile = null;

				try {
					soundFile = new FileInputStream(filename);
					try {
						Log.d("Skipped",
								Long.toString(soundFile.skip(playbackLoc)));
					} catch (IOException e) {
						Log.e("RespeakPlaybackFragment", "skip failed");
					}
				} catch (FileNotFoundException e1) {
					Log.e("RespeakPlaybackFragment", "file not found");
				}

				if (soundFile != null) {
					sonic.setSpeed(speed);
					sonic.setPitch(pitch);
					sonic.setRate(rate);
					do {
						try {
							bytesRead = soundFile.read(samples, 0,
									samples.length);
						} catch (IOException e) {
							Log.e("RespeakPlaybackFragment",
									"error reading file");
							return;
						}
						if (bytesRead > 0) {
							sonic.putBytes(samples, bytesRead);
							playbackLoc += bytesRead;
						} else {
							sonic.flush();
							Log.d("sonic", "flush");
							Log.d("total bytes", Integer.toString(playbackLoc));
							playbackLoc = 0;
							isPlaying = false;
						}
						int available = sonic.availableBytes();
						if (available > 0) {
							if (modifiedSamples.length < available) {
								modifiedSamples = new byte[available * 2];
							}
							sonic.receiveBytes(modifiedSamples, available);
							device.writeSamples(modifiedSamples, available);
						}
					} while (isPlaying && bytesRead > 0);
					device.flush();
					if (playbackLoc == 0) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								button.performClick();
							}
						});
					}
				}
			}
		});
		thread.start();
	}

	@Override
	protected void playButtonAction(ImageButton button, SeekBar mSeekBar) {
		play(this.getActivity(), button);
	}

	@Override
	protected void pauseButtonAction() {
		Log.d("num", Integer.toString(playbackLoc));
	}

	@Override
	protected void stopButtonAction() {
	}

	@Override
	protected void pauseLogic() {
		isPlaying = false;
	}

	@Override
	protected void seekAction(int newLoc) {
		Log.d("filesize", Long.toString(new File(filename).length()));
		pauseLogic();
		while (thread.isAlive()) {
		}
		playbackLoc = (int) 61.8 * newLoc * 1000;
		isPlaying = true;
		play(this.getActivity(), (ImageButton) playbackLayout
		 .findViewById(R.id.stop_button));

	}

}
