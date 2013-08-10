/**
 * PlaybackFragment.java
 *  
 * Parent class which sets up shared playback functionality for respeaking and transcription.
 * 
 * @author Jessica Yao
 */

package com.iqss.respeakerapp.fragments;

import java.io.File;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.utils.TabConstants;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public abstract class PlaybackFragment extends Fragment implements OnChronometerTickListener, OnSeekBarChangeListener{

	static final String STATE_FILENAME = "outputFile";
	static final String STATE_PLAYING = "wasPlaying";
	static final String STATE_LOC = "playbackLoc";
	static final String STATE_CHRONOMETER = "chronometerTime";

	protected RelativeLayout playbackLayout = null;
	protected Chronometer mChronometer = null;
	protected SeekBar mSeekBar = null;
	private ExtraOnClickListener onStartPlaybackListener = null;
	private SharedPreferences mem = null;

	// For keeping track of listening location.
	// in RespeakPlaybackFragment, playbackLoc is in bytes
	// in TranscribePlaybackFragement, it's in milliseconds (~timeWhenStopped)
	protected int playbackLoc = 0; 
	
	protected long timeWhenStopped = 0; // might be a duplicate, not sure
	protected boolean isPlaying = false;
	protected boolean continued = false; // to avoid a glitch where when playback starts, seekbar goes ahead too much 
	protected String filename = null;
	protected String subFolder = null;
	
	/*
	 * interface for listening to when start button clicked
	 * (in which timestamp is automatically tacked onto the text box)
	 */
	public interface ExtraOnClickListener {
        public void onClicked(String timestamp);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View playbackView = inflater.inflate(R.layout.playback_view, container, false);
		Log.d("PlaybackFragment", "View inflated.");
		playbackLayout = (RelativeLayout) playbackView;
		return playbackView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// recreate fragment state
		if (savedInstanceState != null) {
			filename = savedInstanceState.getString(STATE_FILENAME);
			isPlaying = savedInstanceState.getBoolean(STATE_PLAYING);
			playbackLoc = savedInstanceState.getInt(STATE_LOC);
			timeWhenStopped = savedInstanceState.getLong(STATE_CHRONOMETER);
			Log.d("PlaybackFragment", "state recreated");
		}
		
		// calculated multiplier as inverse of respeaking playback rate (10/7)
		double multiplier = 1.43; 
		if (this.getActivity().getClass().getSimpleName().equals("TranscribeActivity")){
			onStartPlaybackListener = (ExtraOnClickListener) this.getActivity();
			multiplier = 1.0;
		}
		
		// pull data from SharedPreferences	
		mem = this.getActivity().getSharedPreferences(getArguments().getString(TabConstants.FILENAME), 0);
		playbackLoc = mem.getInt(STATE_LOC, playbackLoc);
		timeWhenStopped = mem.getLong(STATE_CHRONOMETER, timeWhenStopped);

		// set up Chronometer
		mChronometer = (Chronometer) playbackLayout.findViewById(R.id.playback_chronometer);
		mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
		mChronometer.setOnChronometerTickListener((OnChronometerTickListener) this);

		// set up filename
		subFolder = (this.getActivity().getClass().getSimpleName().equals("RespeakActivity")) ? "recordings" : "respeakings";
		File dir = new File(TabConstants.PREFIX + subFolder, getArguments().getString(TabConstants.FILENAME) + ".wav");
		filename = dir.toString();
		
		// set seekbar (max is the length of the audio in seconds, increments by one each second)
		mSeekBar = (SeekBar) playbackLayout.findViewById(R.id.seekbar);
		mSeekBar.setMax(getDuration(multiplier));
		mSeekBar.setOnSeekBarChangeListener((OnSeekBarChangeListener) this);
		mSeekBar.setProgress((int) Math.floor(timeWhenStopped * -1 / 1000.));
		
		setUpButtons();
	}

	/*
	 * When fragment is put on pause (no longer visible in background or foreground)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	public void onPause() {
		super.onPause();
		if (isPlaying)
			timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
		mChronometer.stop();
		isPlaying = false;
		pauseLogic();
		
		// save data in SharedPreferences
		SharedPreferences.Editor editor = mem.edit();
		editor.putInt(STATE_LOC, playbackLoc);
		editor.putLong(STATE_CHRONOMETER, timeWhenStopped);
		editor.commit();
	}

	/*
	 * Sets up and links MediaPlayer functionality to play/pause/stop buttons.
	 */
	private void setUpButtons() {
		// setting up functionality for the record button
		final ImageButton playback_button = (ImageButton) playbackLayout.findViewById(R.id.play_button);

		// setting up functionality for the stop button
		final ImageButton stop_button = (ImageButton) playbackLayout.findViewById(R.id.stop_button);
		
		// anonymous function call (should start/pause recording)
		playback_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isPlaying = !isPlaying;
				// pauses playback
				if (!isPlaying) {
					timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
					mChronometer.stop();				
					
					pauseButtonAction();
					continued = false;
					playback_button.setImageResource(R.drawable.play);
					Log.d("Play Button", "pausing");					
				}
				// restarts recording
				else {
					mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
					mChronometer.start();
					
					// appends timestamp to textbox when recording (re)started
					// (TranscribeActivity only, otherwise listener is null)
					if (onStartPlaybackListener != null)
						onStartPlaybackListener.onClicked((String) mChronometer.getText());
					
					playButtonAction(stop_button, mSeekBar);
					playback_button.setImageResource(R.drawable.pause);
					Log.d("Play Button", "playing again");				
				}
			}
		});

		// anonymous function call (should stop playing)
		stop_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {;
				mChronometer.stop();
				mChronometer.setBase(SystemClock.elapsedRealtime());
				timeWhenStopped = 0;
				mSeekBar.setProgress(0);
				
				stopButtonAction();
				playback_button.setImageResource(R.drawable.play);
				isPlaying = false;
				continued = false;
				Log.d("Stop Button", "playing stopped");
			}
		});
	}
	
	/*
	 * returns whether playback is occuring or not
	 */
	public boolean getStatus(){
		return isPlaying;
	}
	
	/*
	 * Increments seekbar every time the chronometer ticks. Avoids issue of chronometer
	 * skipping ahead by 2 when playback first started.
	 * @see android.widget.Chronometer.OnChronometerTickListener#onChronometerTick(android.widget.Chronometer)
	 */
	@Override
	public void onChronometerTick(Chronometer chronometer) {
		if (continued)
			mSeekBar.incrementProgressBy(1);
		continued = true;
	}
	
	/*
	 * Listener that implements seeking (rewinding/fastforwarding) functionality
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int currentProgress, boolean fromUser) {
		// only reacts if seekbar changes because of user input
		if (fromUser){
			seekAction(currentProgress);
			timeWhenStopped = -1000 * currentProgress; // timeWhenStopped is negative, in ms
			mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
		}	
	}
	
	/*
	 * Get the length of the audio (slowed down or regular, depending on multiplier)
	 */
	private int getDuration(double multiplier){
		int duration = 30;
		MediaPlayer player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// need to instantiate MediaPlayer to get the duration
		try {
			player.setDataSource(getActivity().getApplicationContext(), Uri.parse(filename));
			player.prepare();
			duration = (int) Math.ceil(player.getDuration() * multiplier / 1000.);
			player.release();
		} catch (Exception e) {
			Log.e("PlaybackFragment", "Error getting duration of audio.");
		}
		return duration;		
	}
	
	/*
	 * Saves fragment state when activity is left.
	 */
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(STATE_FILENAME, filename);
		savedInstanceState.putBoolean(STATE_PLAYING, isPlaying);
		savedInstanceState.putLong(STATE_CHRONOMETER, timeWhenStopped);
		savedInstanceState.putLong(STATE_LOC, playbackLoc);
	}

	// -------------------------------------------------------------
	// **********MANDATORY INTERFACE FUNCTIONS (unused)*************
	// -------------------------------------------------------------
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.d("Seekbar", "start tracking touch");
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d("Seekbar", "stop tracking touch");
		
	}
	
	// -------------------------------------------------------------
	// ********************ABSTRACT FUNCTIONS***********************
	// -------------------------------------------------------------
	
	protected abstract void playButtonAction(ImageButton button, SeekBar mSeekBar);
	
	protected abstract void pauseButtonAction();

	protected abstract void stopButtonAction();

	protected abstract void pauseLogic();
	
	protected abstract void seekAction(int newLoc);
	
}
