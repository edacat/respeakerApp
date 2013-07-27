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

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.fragments.RecordingFragment.OnButtonFocusListener;
import com.iqss.respeakerapp.utils.TabConstants;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TranscribeFragment extends Fragment{
	private final static String SAVED_TEXT = "saved text";
	
	private LinearLayout transcribeLayout = null;
	private View transcribeView = null;
	private EditText transcribeField = null;
	private File output = null; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		transcribeView = inflater.inflate(R.layout.transcribe_view, container,
				false);
		Log.d("TranscribeFragment", "view inflated");
		transcribeLayout = (LinearLayout) transcribeView;
		transcribeField = (EditText) transcribeView.findViewById(R.id.transcription_field);
		
		if (getArguments() != null){
			File output = new File(TabConstants.PREFIX + "inProgress", getArguments().getString(TabConstants.FILENAME) + ".txt");
			output.mkdirs();
			Log.d("TranscribeFragment", output.toString());
		}
		
		if (savedInstanceState != null)
			transcribeField.setText(savedInstanceState.getString(SAVED_TEXT));
		
		transcribeField.setOnTouchListener((OnTouchListener) this.getActivity());	
		transcribeField.addTextChangedListener((TextWatcher) this.getActivity());		
		
		return transcribeView;
	}
	
	/*
	 * Saves fragment state in bundle.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(SAVED_TEXT, transcribeField.getText().toString());
	}
	
	public void onPause() {
		super.onPause();
		String savedText = transcribeField.getText().toString();
//		transcribeField.get
		SharedPreferences mem = this.getActivity().getSharedPreferences(getArguments().getString(TabConstants.FILENAME), 0);
		SharedPreferences.Editor editor = mem.edit();
		editor.putString(SAVED_TEXT, transcribeField.getText().toString());
		editor.commit();
	}

	public void addText(String str){
		transcribeField.append(str);
	}

}