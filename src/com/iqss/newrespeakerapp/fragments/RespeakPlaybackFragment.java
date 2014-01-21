/**
 * RespeakPlaybackFragment.java
 *  
 * Sets up specific functionality for respeaking playback (i.e. slowed down audio).
 * Uses the Sonic NDS open source code.
 * 
 * @author Jessica Yao
 */

package com.iqss.newrespeakerapp.fragments;

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

import com.iqss.newrespeakerapp.R;
import com.iqss.newrespeakerapp.fragments.PlaybackFragment;
import com.iqss.newrespeakerapp.utils.Microphone;

public class RespeakPlaybackFragment extends PlaybackFragment {
	
	private float speed = (float) 0.7;
	private float pitch = (float) 1.0;
	private float rate = (float) 1.0;
	private Thread thread = null;
	
	
	/*
	 * uses Sonic functions to play back audio at 0.7x speed
	 * (doesn't actually change the audio file itself)
	 */
	public void play(final Activity activity, final ImageButton stopButton) {
		thread = new Thread(new Runnable() {
			public void run() {
				AndroidAudioDevice device = new AndroidAudioDevice(Microphone.RECORDER_SAMPLE_RATE, 1);
				Sonic sonic = new Sonic(Microphone.RECORDER_SAMPLE_RATE, 1);
				int bytesRead;
				byte samples[] = new byte[4096];
				byte modifiedSamples[] = new byte[2048];
				InputStream soundFile = null;

				// opens file and seeks to previous playbackLoc
				try {
					soundFile = new FileInputStream(filename);
					try {
						// do NOT remove (skip takes place in call, even though within log)
						Log.d("Skipped", Long.toString(soundFile.skip(playbackLoc)));
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
						// reading data into buffer which is passed to sonic
						try {
							bytesRead = soundFile.read(samples, 0, samples.length);
						} catch (IOException e) {
							Log.e("RespeakPlaybackFragment","error reading file");
							return;
						}
						if (bytesRead > 0) {
							sonic.putBytes(samples, bytesRead);
							playbackLoc += bytesRead; // adjust seek location
						// if no longer reading bytes, must be done (flush and reset)
						} else {
							sonic.flush();
							// Log.d("total bytes", Integer.toString(playbackLoc));
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
					
					// using another thread to mimic stop button if playbackLoc reset to 0
					if (playbackLoc == 0) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								stopButton.performClick();
							}
						});
					}
				}
			}
		});
		thread.start();
	}
	
	// -------------------------------------------------------------
	// **********************ABSTRACT METHODS***********************
	// -------------------------------------------------------------
	

	@Override
	protected void playButtonAction(ImageButton stopButton, SeekBar mSeekBar) {
		play(this.getActivity(), stopButton);
	}

	@Override
	protected void pauseButtonAction() {
		Log.d("Paused at", Integer.toString(playbackLoc));
	}

	@Override
	protected void stopButtonAction() {
		playbackLoc = 0;
	}

	/*
	 * isPlaying already set in parent class, mainly for benefit of seekAction()
	 * @see com.iqss.respeakerapp.fragments.PlaybackFragment#pauseLogic()
	 */
	@Override
	protected void pauseLogic() {
		isPlaying = false;
	}

	/*
	 * newLoc is in seconds (pulled from seekbar, which is in sync with chronometer)
	 * @see com.iqss.respeakerapp.fragments.PlaybackFragment#seekAction(int)
	 */
	@Override
	protected void seekAction(int newLoc) {
		
		Log.d("Filesize", Long.toString(new File(filename).length()));
		pauseLogic();
		
		// used to stall execution so that thread dies before play called again
		while (thread.isAlive()) {
		}
		
		// magic number 61.8
		// (rate of bytes/s calculated from testing and gathering info on bytes and time during playback)
		playbackLoc = (int) 61.8 * newLoc * 1000;
		
		// start new instance of playback thread
		isPlaying = true;
		play(this.getActivity(), (ImageButton) playbackLayout.findViewById(R.id.stop_button));
	}

}