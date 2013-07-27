package com.iqss.respeakerapp.fragments;

import java.io.File;
import java.util.Observer;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.utils.TabConstants;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public abstract class PlaybackFragment extends Fragment{

	static final String STATE_FILENAME = "outputFile";
	static final String STATE_PLAYING = "wasPlaying";
	static final String STATE_TIME = "playerTime";
	static final String STATE_BYTES = "playerBytes";

	protected RelativeLayout playbackLayout = null;

	protected int playbackLoc = 0; // for keeping track of listening location
	protected boolean isPlaying = false;
	protected String filename = null;
	protected String subFolder = null;
	protected Chronometer mChronometer = null;
	protected long timeWhenStopped = 0;
	
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
			playbackLoc = savedInstanceState.getInt(STATE_BYTES);
			timeWhenStopped = savedInstanceState.getLong(STATE_TIME);
			Log.d("PlaybackFragment", "state recreated");
		}
		
		if (this.getActivity().getClass().getSimpleName().equals("TranscribeActivity"))
			onStartPlaybackListener = (ExtraOnClickListener) this.getActivity();
		
		SharedPreferences mem = this.getActivity().getSharedPreferences(getArguments().getString(TabConstants.FILENAME), 0);
		playbackLoc = mem.getInt(STATE_TIME, playbackLoc);
		
		mChronometer = (Chronometer) playbackLayout.findViewById(R.id.playback_chronometer);

		subFolder = (this.getActivity().getClass().getSimpleName().equals("RespeakActivity")) ? "recordings" : "respeakings";
		File dir = new File(TabConstants.PREFIX + subFolder, getArguments().getString(TabConstants.FILENAME) + ".wav");
		filename = dir.toString();
		setUpButtons();
	}

	public void onPause() {
		super.onPause();
		SharedPreferences mem = this.getActivity().getSharedPreferences(getArguments().getString(TabConstants.FILENAME), 0);
		SharedPreferences.Editor editor = mem.edit();
		editor.putInt(STATE_TIME, playbackLoc);
		editor.commit();
		
		pauseLogic();
		timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
		mChronometer.stop();
	}

	/*
	 * Saves fragment state when activity is left.
	 */
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(STATE_FILENAME, filename);
		savedInstanceState.putBoolean(STATE_PLAYING, isPlaying);
		savedInstanceState.putInt(STATE_BYTES, playbackLoc);
		savedInstanceState.putLong(STATE_TIME, mChronometer.getBase() - SystemClock.elapsedRealtime());
	}

	/*
	 * Sets up and links MediaPlayer functionality to play/pause/stop buttons.
	 */
	private void setUpButtons() {
		// setting up functionality for the record button
		final ImageButton playback_button = (ImageButton) playbackLayout
				.findViewById(R.id.play_button);

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
					Log.d("Play Button", "pausing");					
				}
				// restarts recording
				else {
					mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
					mChronometer.start();
					
					Log.d("truth", Boolean.toString(onStartPlaybackListener == null));
					if (onStartPlaybackListener != null)
						onStartPlaybackListener.onClicked((String) mChronometer.getText());
					playButtonAction();
					
					Log.d("Play Button", "playing again");				
				}
			}
		});

		// setting up functionality for the stop button
		final ImageButton stop_button = (ImageButton) playbackLayout
				.findViewById(R.id.stop_button);

		// anonymous function call (should stop playing)
		stop_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {;
				mChronometer.stop();
				mChronometer.setBase(SystemClock.elapsedRealtime());
				timeWhenStopped = 0;
				
				stopButtonAction();
				Log.d("Stop Button", "playing stopped");
				isPlaying = false;
			}
		});
	}
	
	protected abstract void playButtonAction();
	
	protected abstract void pauseButtonAction();

	protected abstract void stopButtonAction();

	protected abstract void pauseLogic();

}
