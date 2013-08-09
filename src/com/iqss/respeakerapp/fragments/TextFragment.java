/**
* TextFragment.java
*  
* Used by TranscribeActivity.java to view completed text transcriptions.
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.utils.TabConstants;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextFragment extends Fragment{
	
	private String filename = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View textView = inflater.inflate(R.layout.text_view, container, false);
		Log.d("TextFragment", "View inflated.");
		
		// Should always be able to get arguments, as they are passed by the activity.
		if (getArguments() != null)
			filename = getArguments().getString(TabConstants.FILENAME);
		
		TextView textBox = (TextView) ((LinearLayout) textView).findViewById(R.id.textBox);
		String textData = "";
		
		try {
			textData = loadFile();
		} catch (Exception e) {
			Log.e("TextFragment", "Error loading text file.");
		}
		
		// set text in textBox
		if (textData.equals(""))
			textBox.setText(R.string.no_data);
		else
			textBox.setText(textData);
		
		return textView;		
	}

	/*
	 * Reads data from .txt file and loads into String, to be displayed in textBox
	 */
	public String loadFile() throws Exception{
		File file = new File(TabConstants.PREFIX + TabConstants.FOLDER_TYPES[2] + "/" + filename + ".txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder text = new StringBuilder("");
		String line = "";
		// reading line by line from file and appending to StringBuilder
		while (line != null){
			text.append(line);
			text.append("\n");
			line = reader.readLine();
		}
		reader.close();
		return text.toString();
	}
}