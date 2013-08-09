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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class TranscribePlaybackFragment extends PlaybackFragment {
	
	private MediaPlayer player = null;

	protected void initialize(String fileLoc, final ImageButton button, SeekBar mSeekBar) throws Exception{
		final FragmentActivity parentActivity = getActivity();
		Uri uri = Uri.parse(fileLoc);
		// media player in idle state
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// initializes media player
		player.setDataSource(parentActivity.getApplicationContext(), uri);
		player.prepare();
		Log.d("Audio length", Integer.toString((int) Math.ceil(player.getDuration() / 1000.)));
		player.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				playbackLoc = 0;
				isPlaying = false;
				mChronometer.setBase(SystemClock.elapsedRealtime());
				timeWhenStopped = 0;
				parentActivity.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						button.performClick();							
					}						
				});
				 
			}	
		});		
		Log.d("MediaPlayer", "initialization successful");
	}
	
	protected void playButtonAction(ImageButton button, SeekBar mSeekBar){
		if (player == null){
			try {
				initialize(filename, button, mSeekBar);
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

	@Override
	protected void seekAction(int newLoc) {
		playbackLoc = newLoc * 1000;
		if (player != null)
			player.seekTo(playbackLoc);	
	}

}