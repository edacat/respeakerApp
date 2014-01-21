/**
 * RecordingFragment.java
 *  
 * Connects functionality from Microphone.java with layout in recording fragment (e.g. buttons).
 * 
 * @author Jessica Yao
 */

package com.iqss.newrespeakerapp.fragments;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import com.iqss.newrespeakerapp.R;
import com.iqss.newrespeakerapp.fragments.NameDialog.SaveDialogListener;
import com.iqss.newrespeakerapp.utils.Microphone;
import com.iqss.newrespeakerapp.utils.TabConstants;

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
	private Activity mActivity = null;
	private OnButtonFocusListener mCallback;
	private SharedPreferences mem = null; // for storing data persistently
	
	private boolean isRecording = false;
	private String filename = null;
	private long timeWhenStopped = 0;

	/*
	 *  interface for listening to a button click
	 */
	public interface OnButtonFocusListener {
		public void onButtonFocus();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = this.getActivity();
		// need to attach a listener if respeaking
		// (for interactions between RecordingFragment and RespeakPlaybackFragment)
		if (mActivity.getClass().getSimpleName().equals("RespeakActivity"))
			mCallback = (OnButtonFocusListener) mActivity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View recordingView = inflater.inflate(R.layout.record_view, container, false);
		Log.d("RecordingFragment", "view inflated");
		recordingLayout = (RelativeLayout) recordingView;
		return recordingView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// recreates fragment state
		
		// if the file is new
		if (filename == null) {
			// should be passed arguments from FileExplorer activity
			if (getArguments() != null) {
				filename = getArguments().getString(TabConstants.FILENAME) + ".wav";
			}
		} 
		// otherwise pull previous data to recreate state
		else {
			if (savedInstanceState != null) {
				filename = savedInstanceState.getString(STATE_FILENAME);
				isRecording = savedInstanceState.getBoolean(STATE_RECORDING);
				timeWhenStopped = savedInstanceState.getLong(STATE_TIME);
				Log.d("RecordingFragment", "state recreated");
			}
		}
		
		// if file was previously being recorded, mem should have the old values
		mem = this.getActivity().getSharedPreferences("newRecording", 0);
		
		// for respeaking, each file has its own SharedPreference
		if (getActivity().getClass().getSimpleName().equals("RespeakActivity")) {
			mem = this.getActivity().getSharedPreferences(filename.split(".wav")[0], 0);
		}

		filename = mem.getString(STATE_FILENAME, filename);
		timeWhenStopped = mem.getLong(STATE_TIME, timeWhenStopped);

		// instantiating an audio recorder, passing name of activity which holds fragment
		mic = new Microphone(mActivity.getClass().getSimpleName());

		// set up chronometer
		mChronometer = (Chronometer) recordingLayout.findViewById(R.id.chronometer);
		Log.d("RecordingFragment", "Chronometer: " + Long.toString(timeWhenStopped));
		mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);

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
		savedInstanceState.putLong(STATE_TIME, mChronometer.getBase() - SystemClock.elapsedRealtime());
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
		final ImageButton record_button = (ImageButton) recordingLayout.findViewById(R.id.record_icon);

		// anonymous function call (should start/pause recording)
		record_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.onButtonFocus();
				}
				// pauses recording
				if (isRecording) {
					timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
					mChronometer.stop();
					mic.pauseRecording();
					// switch image to record (since currently paused)
					record_button.setImageResource(R.drawable.record_logo);
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

					record_button.setImageResource(R.drawable.pause);
					Log.d("Record Button", "recording again");
				}
				isRecording = !isRecording; // flip flag
			}
		});

		// setting up similar functionality for the stop button
		final ImageButton save_button = (ImageButton) recordingLayout.findViewById(R.id.save_icon);

		// anonymous function call (should stop recording)
		save_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopAndSave();
				isRecording = false;
				filename = null;
				record_button.setImageResource(R.drawable.record_logo);
				Log.d("Save Button", "recording saved");
			}
		});
		
		// setting up similar functionality for the trash/reset button
		final ImageButton trash_button = (ImageButton) recordingLayout.findViewById(R.id.trash_icon);
		
		// anonymous function call (should trash the current recording)
		trash_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mic.stopRecording();
				mChronometer.stop();
				mChronometer.setBase(SystemClock.elapsedRealtime());
				timeWhenStopped = 0;
				isRecording = false;
				
				File file = new File(TabConstants.PREFIX + "recordings/" + filename);
				file.delete();
				filename = null;			
				record_button.setImageResource(R.drawable.record_logo);
				Log.d("Trash Button", "recording deleted");
			}
		});
	}
	
	/*
	 * Deals with stopping microphone and chronometer and displaying
	 * save message to user to notify of file saving
	 */
	public void stopAndSave(){
		mic.stopRecording();
		mChronometer.stop();
		mChronometer.setBase(SystemClock.elapsedRealtime());
		timeWhenStopped = 0;

		// if recording button stopped, recording must be done and saved
		if (filename != null) {
			// if recording original, display dialog
			if (getActivity().getClass().getSimpleName().equals("RecordActivity")) {
				Bundle dialogArgs = new Bundle();
				dialogArgs.putString(TabConstants.FILENAME, filename);
				
				FragmentManager fm = getActivity().getSupportFragmentManager();
				NameDialog dialog = new NameDialog();
				dialog.setArguments(dialogArgs);
				dialog.show(fm, "naming dialog");
			// otherwise if respeaking, display toast
			} else {
				// move file from inProgress folder to respeakings folder
				File originalLoc = new File(TabConstants.PREFIX + "inProgress", filename);
				File newLoc = new File(TabConstants.PREFIX + "respeakings", filename);
				originalLoc.renameTo(newLoc);
				
				String toastText = "Saved respeaking as " + filename;
				Toast.makeText(getActivity().getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
				// waiting for toast to disappear before force directing to main page
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000); // Toast.LENGTH_SHORT
							getActivity().finish();
						} catch (Exception e) {
							Log.d("RecordingFragment", "error returning to activity");
						}
					}
				};
				thread.start();
			}
			
			// remove unique SharedPreferences if respeaking (to save memory)
			// because there could be many files
			SharedPreferences.Editor editor = mem.edit();
			editor.remove(STATE_FILENAME);
			editor.remove(STATE_TIME);
			editor.remove(PlaybackFragment.STATE_LOC);
			editor.remove(PlaybackFragment.STATE_CHRONOMETER);
			editor.commit();
		}
	}
	
	/*
	 * When "Save" is clicked in dialog during RecordActivity
	 * (saves file and checks for duplicate names)
	 * @see com.iqss.newrespeakerapp.fragments.NameDialog.SaveDialogListener#onDialogPositiveClick(android.support.v4.app.DialogFragment, java.lang.String, java.lang.String)
	 */
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String absPath, String newName) {
		File original = new File(absPath);
		// checking for .wav extension and adding one if missing
		if (!Pattern.compile("([^\\s]+(\\.(?i)(wav))$)").matcher(newName).matches())
			newName = newName + ".wav";
		File newFile = new File(original.getParentFile().getAbsolutePath(), newName);
		
		// keeps appending _copy tags until no more name conflicts 
		// *** Use lastModified to recognize whether filename belongs to same file (saved from previous
		//     pause) or an actual duplicate (ONLY FOR RECORDING, NOT RESPEAKING)
		while (newFile.exists() && newFile.lastModified() != 0) {
			newName = newName.replace(".wav", "_copy.wav");
			newFile = new File(original.getParentFile().getAbsolutePath(), newName);			
		}		
		original.renameTo(newFile);
		// undo the tag time of 0, reset to normal modification date
		newFile.setLastModified(new Date().getTime());
	}

	/*
	 * When "Cancel" is clicked in dialog during RecordActivity
	 * (deletes file)
	 * @see com.iqss.newrespeakerapp.fragments.NameDialog.SaveDialogListener#onDialogNegativeClick(android.support.v4.app.DialogFragment, java.lang.String)
	 */
	@Override
	public void onDialogNegativeClick(DialogFragment dialog, String absPath) {
		File file = new File(absPath);
		file.delete();
	}

}