package com.iqss.respeakerapp.fragments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.vinuxproject.sonic.AndroidAudioDevice;
import org.vinuxproject.sonic.Sonic;

import android.util.Log;
import android.view.View;

import com.iqss.respeakerapp.fragments.PlaybackFragment;
import com.iqss.respeakerapp.utils.Microphone;

public class RespeakPlaybackFragment extends PlaybackFragment {
	private float speed = (float) 0.7;
	private float pitch = (float) 1.0;
	private float rate = (float) 1.0;
	
	public void play(View v) {	
		new Thread(new Runnable() {
			public void run() {
				AndroidAudioDevice device = new AndroidAudioDevice(Microphone.RECORDER_SAMPLE_RATE, 1);
				Sonic sonic = new Sonic(Microphone.RECORDER_SAMPLE_RATE, 1);
				int bytesRead;
				byte samples[] = new byte[4096];
				byte modifiedSamples[] = new byte[2048];
				InputStream soundFile = null;
				
				try {
					soundFile = new FileInputStream(filename);
					try {
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
						try {
							bytesRead = soundFile.read(samples, 0, samples.length);
						} catch (IOException e) {
							Log.e("RespeakPlaybackFragment", "error reading file");
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
				}
			}
		}).start();
	}

	@Override
	protected void playButtonAction() {
		play(this.getActivity().getCurrentFocus());
	}

	@Override
	protected void pauseButtonAction() {
		Log.d("num", Integer.toString(playbackLoc));		
	}

	@Override
	protected void stopButtonAction() {
		playbackLoc = 0;
	}

	@Override
	protected void pauseLogic() {
		isPlaying = false;
	}	

}
