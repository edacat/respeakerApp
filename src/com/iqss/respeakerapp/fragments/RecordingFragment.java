/**
* RecordingFragment.java
*  
* Connects functionality from Microphone.java with layout in recording fragment (e.g. buttons).
* 
* @author Jessica Yao
*/

package com.iqss.respeakerapp.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.utils.Microphone;
import com.iqss.respeakerapp.utils.TabConstants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class RecordingFragment extends Fragment {
	// constants used for restoring fragment state
	static final String STATE_FILENAME = "outputFile";
	static final String STATE_RECORDING = "wasRecording";
	static final String STATE_TIME = "chronometerBase";

	private RelativeLayout recordingLayout = null;
	private Microphone mic = null;
	private Chronometer mChronometer = null;
	private boolean isRecording = false;
	private String filename = null;
	private long timeWhenStopped = 0;
	
	private Activity mActivity = null;
	private OnButtonFocusListener mCallback;
	
	public interface OnButtonFocusListener {
        public void onButtonFocus();
    }
	
	 @Override
	 public void onAttach(Activity activity) {
		 super.onAttach(activity);
		 mActivity = this.getActivity();
		 if (mActivity.getClass().getSimpleName().equals("RespeakActivity"))
				mCallback = (OnButtonFocusListener) mActivity;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View recordingView = inflater.inflate(R.layout.record_view, container,
				false);
		Log.d("RecordingFragment", "view inflated");
		if (getArguments() != null){
			filename = getArguments().getString(TabConstants.FILENAME) + ".wav";
			Log.d("Args", filename);
		}
		recordingLayout = (RelativeLayout) recordingView;
		return recordingView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// recreates fragment state
		if (savedInstanceState != null) {
			filename = savedInstanceState.getString(STATE_FILENAME);
			isRecording = savedInstanceState.getBoolean(STATE_RECORDING);
			timeWhenStopped = savedInstanceState.getLong(STATE_TIME);
			Log.d("RecordingFragment", "state recreated");			
		}

		// instantiating an audio recorder, passing name of activity which holds fragment
		mic = new Microphone(mActivity.getClass().getSimpleName());
		
		// set up chronometer
		mChronometer = (Chronometer) recordingLayout.findViewById(R.id.chronometer);
		Log.d("RecordingFragment", "Mic and chronometer set up.");
		
		// attach microphone functions to record/pause/stop buttons
		setUpButtons();
	}

	/*
	 * Saves fragment state in bundle.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(STATE_FILENAME, filename);
		savedInstanceState.putBoolean(STATE_RECORDING, isRecording);
		savedInstanceState.putLong(STATE_TIME, mChronometer.getBase()
				- SystemClock.elapsedRealtime());
	}
	
	/*
	 * When fragment is out of focus, microphone and chronometer must be paused.
	 */
	public void onPause() {
		super.onPause();
		mic.stopRecorder();
		timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
		mChronometer.stop();
	}
	
	/*
	 * Set filename for recording, in form yyyy-MM-dd_HH-mm-ss.wav
	 * Date format used to ensure uniqueness of filename.
	 */
	@SuppressLint("SimpleDateFormat")
	private void setNewFilename() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		StringBuilder dateFile = new StringBuilder(fmt.format(Calendar
				.getInstance().getTime()));
		filename = dateFile.append(".wav").toString();
	}

	/*
	 * Set up buttons and link with functionality from Microphone.java
	 */
	private void setUpButtons() {

		// setting up functionality for the record button
		final ImageButton record_button = (ImageButton) recordingLayout
				.findViewById(R.id.record_icon);
		
		// anonymous function call (should start/pause recording)
		record_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCallback != null){
					mCallback.onButtonFocus();
					Log.d("callback", "plz");
				}
				// pauses recording
				if (isRecording) {			
					timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
					mChronometer.stop();
					
					mic.pauseRecording();
					Log.d("Record Button", "pausing");
				}
				// restarts recording
				else {			
					// setup new filename if no pre-existing one
					if (filename == null)
						setNewFilename();
					mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
					mChronometer.start();
					
					mic.setup(filename);		
					mic.startRecording();
					Log.d("Record Button", "recording again");
				}
				isRecording = !isRecording; // flip flag
			}
		});

		// setting up similar functionality for the stop button
		final ImageButton stop_button = (ImageButton) recordingLayout
				.findViewById(R.id.stop_icon);
		
		// anonymous function call (should stop recording)
		stop_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {			
				mic.stopRecording();
				
				mChronometer.stop();
				mChronometer.setBase(SystemClock.elapsedRealtime());
				timeWhenStopped = 0;

				isRecording = false;
				filename = null;
				Log.d("Stop Button", "recording stopped");
			}
		});
	}
	
}