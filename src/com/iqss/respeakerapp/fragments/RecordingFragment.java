/**
 * RecordingFragment.java
 *  
 * Connects functionality from Microphone.java with layout in recording fragment (e.g. buttons).
 * 
 * @author Jessica Yao
 */

package com.iqss.respeakerapp.fragments;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.fragments.NameDialog.SaveDialogListener;
import com.iqss.respeakerapp.utils.Microphone;
import com.iqss.respeakerapp.utils.TabConstants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class RecordingFragment extends Fragment implements SaveDialogListener {

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
	
	private SharedPreferences mem = null;

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

		recordingLayout = (RelativeLayout) recordingView;
		return recordingView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// recreates fragment state
		if (filename == null) {
			if (getArguments() != null) {
				filename = getArguments().getString(TabConstants.FILENAME)
						+ ".wav";
				Log.d("Args", filename);
			}
		} else {
			if (savedInstanceState != null) {
				filename = savedInstanceState.getString(STATE_FILENAME);
				isRecording = savedInstanceState.getBoolean(STATE_RECORDING);
				timeWhenStopped = savedInstanceState.getLong(STATE_TIME);
				Log.d("RecordingFragment", "state recreated");
			}
		}
		mem = this.getActivity().getSharedPreferences(
				"newRecording", 0);
		if (!getActivity().getClass().getSimpleName().equals("RecordActivity")) {
			mem = this.getActivity().getSharedPreferences(
					filename.split(".wav")[0], 0);
		}

		filename = mem.getString(STATE_FILENAME, filename);
		timeWhenStopped = mem.getLong(STATE_TIME, timeWhenStopped);

		if (filename != null)
			Log.d("filename", filename);
		else
			Log.d("filename", "null");
		Log.d("time", Long.toString(timeWhenStopped));

		// instantiating an audio recorder, passing name of activity which holds
		// fragment
		mic = new Microphone(mActivity.getClass().getSimpleName());

		// set up chronometer
		mChronometer = (Chronometer) recordingLayout
				.findViewById(R.id.chronometer);
		Log.d("elapsed", Long.toString(SystemClock.elapsedRealtime()));
		Log.d("time when stopped", Long.toString(timeWhenStopped));
		mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
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
		if (filename != null) {
			if (isRecording)
				timeWhenStopped = mChronometer.getBase()
						- SystemClock.elapsedRealtime();
			isRecording = false;
			mic.stopRecorder();
			mChronometer.stop();

			SharedPreferences.Editor editor = mem.edit();
			editor.putString(STATE_FILENAME, filename);
			editor.putLong(STATE_TIME, timeWhenStopped);
			editor.commit();
		}
	}

	/*
	 * Set filename for recording, in form yyyy-MM-dd_HH-mm-ss.wav Date format
	 * used to ensure uniqueness of filename.
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
				if (mCallback != null) {
					mCallback.onButtonFocus();
					Log.d("callback", "plz");
				}
				// pauses recording
				if (isRecording) {
					timeWhenStopped = mChronometer.getBase()
							- SystemClock.elapsedRealtime();
					mChronometer.stop();

					mic.pauseRecording();

					record_button.setImageResource(R.drawable.record_logo);
					Log.d("Record Button", "pausing");
				}
				// restarts recording
				else {
					// setup new filename if no pre-existing one
					if (filename == null)
						setNewFilename();
					mChronometer.setBase(SystemClock.elapsedRealtime()
							+ timeWhenStopped);
					mChronometer.start();

					mic.setup(filename);
					mic.startRecording();

					record_button.setImageResource(R.drawable.pause);
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
				Log.d("derp", "hi");
				mChronometer.stop();
				mChronometer.setBase(SystemClock.elapsedRealtime());
				timeWhenStopped = 0;

				if (filename != null) {
					if (getActivity().getClass().getSimpleName()
							.equals("RecordActivity")) {
						Bundle dialogArgs = new Bundle();
						dialogArgs.putString(TabConstants.FILENAME, filename);

						FragmentManager fm = getActivity()
								.getSupportFragmentManager();
						NameDialog dialog = new NameDialog();
						dialog.setArguments(dialogArgs);
						dialog.show(fm, "naming dialog");
					} else {
						// make toast TODO
						String toastText = "Saved respeaking as " + filename;
						Toast.makeText(getActivity().getApplicationContext(),
								toastText, Toast.LENGTH_SHORT).show();
						Thread thread = new Thread() {
							@Override
							public void run() {
								try {
									Thread.sleep(2000);
									getActivity().finish();
								} catch (Exception e) {
									Log.d("RecordingFragment",
											"error returning to activity");
								}
							}
						};
						thread.start();
					}
					
					Log.d("Filename", filename.split(".wav")[0]);
					SharedPreferences.Editor editor = mem.edit();
					editor.remove(STATE_FILENAME);
					editor.remove(STATE_TIME);
					editor.remove(PlaybackFragment.STATE_TIME);
					editor.remove(PlaybackFragment.STATE_CHRONOMETER);
					editor.commit();
				}

				

				isRecording = false;
				filename = null;
				record_button.setImageResource(R.drawable.record_logo);

				Log.d("Stop Button", "recording stopped");
			}
		});
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String absPath,
			String newName) {
		Log.d("RecordingFragment", "Positive click from DialogFragment");
		File original = new File(absPath);
		if (!Pattern.compile("([^\\s]+(\\.(?i)(wav))$)").matcher(newName)
				.matches())
			newName = newName + ".wav";
		File newFile = new File(original.getParentFile().getAbsolutePath(),
				newName);
		if (newFile.exists())
			original.renameTo(new File(original.getParentFile()
					.getAbsolutePath(), newName.replace(".wav", "_copy.wav")));
		else
			original.renameTo(newFile);
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog, String absPath) {
		Log.d("RecordingFragment", "Negative click from DialogFragment");
		Log.d("RecordingFragment", absPath);
		File file = new File(absPath);
		file.delete();
	}

}