/**
* TranscribePlaybackFragment.java
*  
* Takes care of main functionality of playing back audio at regular speed.
* Primarily uses MediaPlayer (which can handle WAV files).
*  
* @author Jessica Yao
*/
package com.iqss.respeakerapp.fragments;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class TranscribePlaybackFragment extends PlaybackFragment {
	
	private MediaPlayer player = null;

	protected void initialize(String fileLoc) throws Exception{
		Uri uri = Uri.parse(fileLoc);
		// media player in idle state
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// initializes media player
		player.setDataSource(getActivity().getApplicationContext(), uri);
		player.prepare();
		player.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				playbackLoc = 0;
				isPlaying = false;
				mChronometer.setBase(SystemClock.elapsedRealtime());
				timeWhenStopped = 0;
			}	
		});		
		Log.d("MediaPlayer", "initialization successful");
	}
	
	protected void playButtonAction(){
		if (player == null){
			try {
				initialize(filename);
				player.seekTo(playbackLoc);
			} catch (Exception e) {
				Log.e("Play Button", "error initializing");
			}
		}
		player.start();
	}
	
	protected void pauseButtonAction(){
//		Log.d("truthplayer", Boolean.toString(player == null));
		player.pause();
		playbackLoc = player.getCurrentPosition();
	}
	
	protected void stopButtonAction(){
		pauseLogic();
		playbackLoc = 0;
	}
	
	protected void pauseLogic(){
		if (player != null){
			player.stop();
			player.release();
			player = null;
		}
	}

	/*
	 * Saves fragment state when activity is left.
	 */
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		 savedInstanceState.putString(STATE_FILENAME, filename);
		 savedInstanceState.putBoolean(STATE_PLAYING, isPlaying);
		 savedInstanceState.putInt(STATE_TIME, playbackLoc);
	}

}