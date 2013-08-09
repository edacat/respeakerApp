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
	static final String STATE_TIME = "playerTime";
	static final String STATE_CHRONOMETER = "chronometerTime";
//	static final String STATE_BYTES = "playerBytes";

	protected RelativeLayout playbackLayout = null;

	protected int playbackLoc = 0; // for keeping track of listening location
	protected boolean isPlaying = false;
	protected String filename = null;
	protected String subFolder = null;
	protected Chronometer mChronometer = null;
	protected SeekBar mSeekBar = null;
	protected long timeWhenStopped = 0;
	protected boolean continued = false;
	
	private ExtraOnClickListener onStartPlaybackListener = null;
	
	public interface ExtraOnClickListener {
        public void onClicked(String timestamp);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View playbackView = inflater.inflate(R.layout.playback_view, container,
				false);
		Log.d("PlaybackFragment", "view inflated");
		if (getArguments() != null)
			Log.d("PlaybackArgs", getArguments().getString(TabConstants.FILENAME));
		playbackLayout = (RelativeLayout) playbackView;
		return playbackView;
	}

	public boolean getStatus(){
		return isPlaying;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null) {
			filename = savedInstanceState.getString(STATE_FILENAME);
			isPlaying = savedInstanceState.getBoolean(STATE_PLAYING);
//			playbackLoc = savedInstanceState.getInt(STATE_BYTES);
			timeWhenStopped = savedInstanceState.getLong(STATE_TIME);
			Log.d("PlaybackFragment", "state recreated");
		}
		double multiplier = 1.43;
		if (this.getActivity().getClass().getSimpleName().equals("TranscribeActivity")){
			onStartPlaybackListener = (ExtraOnClickListener) this.getActivity();
			multiplier = 1.0;
		}
		
		
		
		SharedPreferences mem = this.getActivity().getSharedPreferences(getArguments().getString(TabConstants.FILENAME), 0);
		playbackLoc = mem.getInt(STATE_TIME, playbackLoc);
		timeWhenStopped = mem.getLong(STATE_CHRONOMETER, timeWhenStopped);

		mChronometer = (Chronometer) playbackLayout.findViewById(R.id.playback_chronometer);
		mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
		mChronometer.setOnChronometerTickListener((OnChronometerTickListener) this);

		subFolder = (this.getActivity().getClass().getSimpleName().equals("RespeakActivity")) ? "recordings" : "respeakings";
		File dir = new File(TabConstants.PREFIX + subFolder, getArguments().getString(TabConstants.FILENAME) + ".wav");
		filename = dir.toString();
		
		mSeekBar = (SeekBar) playbackLayout.findViewById(R.id.seekbar);
		mSeekBar.setMax(getDuration(multiplier));
		mSeekBar.setOnSeekBarChangeListener((OnSeekBarChangeListener) this);
		mSeekBar.setProgress((int) Math.floor(timeWhenStopped * -1 / 1000.));
		Log.d("progress", Integer.toString(mSeekBar.getProgress()));
		
		setUpButtons();
	}

	public void onPause() {
		super.onPause();
		if (isPlaying)
			timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
		mChronometer.stop();
		isPlaying = false;
		pauseLogic();
		
		SharedPreferences mem = this.getActivity().getSharedPreferences(getArguments().getString(TabConstants.FILENAME), 0);
		SharedPreferences.Editor editor = mem.edit();
		editor.putInt(STATE_TIME, playbackLoc);
		editor.putLong(STATE_CHRONOMETER, timeWhenStopped);
		editor.commit();
	}

	/*
	 * Saves fragment state when activity is left.
	 */
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(STATE_FILENAME, filename);
		savedInstanceState.putBoolean(STATE_PLAYING, isPlaying);
//		savedInstanceState.putInt(STATE_BYTES, playbackLoc);
		savedInstanceState.putLong(STATE_TIME, playbackLoc);
	}

	/*
	 * Sets up and links MediaPlayer functionality to play/pause/stop buttons.
	 */
	private void setUpButtons() {
		// setting up functionality for the record button
		final ImageButton playback_button = (ImageButton) playbackLayout
				.findViewById(R.id.play_button);

		// setting up functionality for the stop button
		final ImageButton stop_button = (ImageButton) playbackLayout
				.findViewById(R.id.stop_button);
		
		// anonymous function call (should start/pause recording)
		playback_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isPlaying = !isPlaying;
				// pauses playback
				if (!isPlaying) {
					timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
					Log.d("fragment paused", (String) mChronometer.getText());
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
					
					Log.d("truth", Boolean.toString(onStartPlaybackListener == null));
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
				playbackLoc = 0;
				
				stopButtonAction();
				playback_button.setImageResource(R.drawable.play);
				mSeekBar.setProgress(0);
				Log.d("Stop Button", "playing stopped");
				isPlaying = false;
				continued = false;
			}
		});
	}
	
	@Override
	public void onChronometerTick(Chronometer chronometer) {
		if (continued)
			mSeekBar.incrementProgressBy(1);
		continued = true;
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int currentProgress, boolean fromUser) {
		if (fromUser){
			Log.d("new loc", Integer.toString(currentProgress));
			seekAction(currentProgress);
			timeWhenStopped = -1000 * currentProgress;
			mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.d("Seekbar", "start tracking touch");
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d("Seekbar", "stop tracking touch");
		
	}
	
	private int getDuration(double multiplier){
		int duration = 30;
		MediaPlayer player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);	
		try {
			player.setDataSource(getActivity().getApplicationContext(), Uri.parse(filename));
			player.prepare();
			Log.d("Duration", Integer.toString(player.getDuration()));
			duration = (int) Math.ceil(player.getDuration() * multiplier / 1000.);
			player.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return duration;		
	}
	
	protected abstract void playButtonAction(ImageButton button, SeekBar mSeekBar);
	
	protected abstract void pauseButtonAction();

	protected abstract void stopButtonAction();

	protected abstract void pauseLogic();
	
	protected abstract void seekAction(int newLoc);
}
