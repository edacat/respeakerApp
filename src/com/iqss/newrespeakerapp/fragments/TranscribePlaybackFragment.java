/**
* TranscribePlaybackFragment.java
*  
* Takes care of main functionality of playing back audio at regular speed.
* Primarily uses MediaPlayer (which can handle WAV files).
*  
* @author Jessica Yao
*/
package com.iqss.newrespeakerapp.fragments;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class TranscribePlaybackFragment extends PlaybackFragment {
	
	private MediaPlayer player = null;

	/*
	 * Initializes MediaPlayer.
	 */
	protected void initialize(String fileLoc, final ImageButton stopButton, SeekBar mSeekBar) throws Exception{
		
		final FragmentActivity parentActivity = getActivity();
		Uri uri = Uri.parse(fileLoc); // datasource
		
		// media player in idle state
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		// initializes media player
		player.setDataSource(parentActivity.getApplicationContext(), uri);
		player.prepare();
		// Log.d("TranscribePlaybackFragment", "Audio length: " + Integer.toString((int) Math.ceil(player.getDuration() / 1000.)));
		player.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				parentActivity.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						// simulates stop button
						stopButton.performClick();							
					}						
				});
				 
			}	
		});		
		Log.d("TranscribePlaybackFragment", "Initialization of MediaPlayer successful.");
	}
	
	/*
	 * Called when playing starts
	 * @see com.iqss.respeakerapp.fragments.PlaybackFragment#playButtonAction(android.widget.ImageButton, android.widget.SeekBar)
	 */
	protected void playButtonAction(ImageButton stopButton, SeekBar mSeekBar){
		if (player == null){
			try {
				initialize(filename, stopButton, mSeekBar);
				player.seekTo(playbackLoc);
			} catch (Exception e) {
				Log.e("TranscribePlaybackFragment", "Error initializing play button.");
			}
		}
		player.start();
	}
	
	/*
	 * Called when audio paused
	 * @see com.iqss.respeakerapp.fragments.PlaybackFragment#pauseButtonAction()
	 */
	protected void pauseButtonAction(){
		player.pause();
		playbackLoc = player.getCurrentPosition();
	}
	
	/*
	 * Called when audio stopped (releases player resources)
	 * @see com.iqss.respeakerapp.fragments.PlaybackFragment#stopButtonAction()
	 */
	protected void stopButtonAction(){
		pauseLogic();
		playbackLoc = 0;
	}
	
	/*
	 * Takes care of player resource when paused
	 * @see com.iqss.respeakerapp.fragments.PlaybackFragment#pauseLogic()
	 */
	protected void pauseLogic(){
		if (player != null){
			player.stop();
			player.release();
			player = null;
		}
	}

	/*
	 * Takes care of seeking (rewinding, fast-forwading)
	 * @see com.iqss.respeakerapp.fragments.PlaybackFragment#seekAction(int)
	 */
	@Override
	protected void seekAction(int newLoc) {
		playbackLoc = newLoc * 1000; // converting seconds to ms
		if (player != null)
			player.seekTo(playbackLoc);	
	}

}