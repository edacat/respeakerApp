/**
* TranscribeFragment.java
*  
* Contains main functionality for transcribing audio into text.
* (NOTE: transcription is manually done by user, this just presents an interface).
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp.fragments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.utils.TabConstants;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TranscribeFragment extends Fragment{
	
	private final static String SAVED_TEXT = "saved text";
	
	private LinearLayout transcribeLayout = null;
	private View transcribeView = null;
	private EditText transcribeField = null;
	private File output = null;
	private boolean done = false;
	private SharedPreferences mem = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		transcribeView = inflater.inflate(R.layout.transcribe_view, container, false);
		Log.d("TranscribeFragment", "view inflated");
		
		transcribeLayout = (LinearLayout) transcribeView;
		transcribeField = (EditText) transcribeView.findViewById(R.id.transcription_field);
		
		// set up arguments to get output filepath
		if (getArguments() != null && output == null){
			File dir = new File(TabConstants.PREFIX + "inProgress");
			output = new File(dir, getArguments().getString(TabConstants.FILENAME) + ".txt");
		}
		
		mem = this.getActivity().getSharedPreferences(getArguments().getString(TabConstants.FILENAME), 0);
		String text = mem.getString(SAVED_TEXT, "");
		
		// show previously added text
		if (text != null)
			transcribeField.append(text);	
		if (savedInstanceState != null)
			transcribeField.append(savedInstanceState.getString(SAVED_TEXT));
		
		transcribeField.setOnTouchListener((OnTouchListener) this.getActivity());	
		transcribeField.addTextChangedListener((TextWatcher) this.getActivity());			
		setUpButtons();
		
		return transcribeView;
	}

	/*
	 *  public to give other activities access to adding text
	 */
	public void addText(String str){
		transcribeField.append(str);
	}
	
	/*
	 * Set up button functionality (done button).
	 */
	private void setUpButtons(){
		((Button) transcribeLayout.findViewById(R.id.done_text_button)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				writeToFile();
				
				// move file to "completed" folder
				File dir = new File(TabConstants.PREFIX + "transcriptions");
				output.renameTo(new File(dir, output.getName()));
				done = true;
				
				// remove SharedPreferences data when transcription completed
				SharedPreferences.Editor editor = mem.edit();
				editor.remove(SAVED_TEXT);
				editor.remove(PlaybackFragment.STATE_LOC);
				editor.remove(PlaybackFragment.STATE_CHRONOMETER);
				editor.commit();
				
				// create toast (mini popup) to notify user that file has been saved
				String toastText = "Saved transcription as " + output.getName();
				Toast.makeText(getActivity().getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
			    
				// force redirect to file explorer after transcription completed
				Thread thread = new Thread(){
			    	@Override
		            public void run() {
		                 try {
		                    Thread.sleep(2000); // same as Toast.LENGTH_SHORT
		                    getActivity().finish();
		                } catch (Exception e) {
		                    Log.d("TranscribeFragment", "Error returning to activity.");
		                }
		             }  
		           };
				thread.start();
			}		
		});
	}
	
	/*
	 * Writes what is in the textbox into a file and saves it.
	 */
	private void writeToFile(){
		try {
			FileWriter writer = new FileWriter(output, false);
			writer.write(transcribeField.getText().toString());
			writer.close();
		} catch (IOException e) {
			Log.d("TranscribeFragment", "Error writing to file.");
		}
	}
	
	/*
	 * Saves fragment state in bundle.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(SAVED_TEXT, transcribeField.getText().toString());
	}
	
	/*
	 * On pause, saves text to SharedPreferences.
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	public void onPause() {
		super.onPause();
		String savedText = transcribeField.getText().toString();
		SharedPreferences.Editor editor = mem.edit();
		editor.putString(SAVED_TEXT, savedText);
		editor.commit();
		if (!done)
			writeToFile();
	}

}