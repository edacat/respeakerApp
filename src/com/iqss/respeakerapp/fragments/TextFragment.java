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
		View textView = inflater.inflate(R.layout.text_view, container,
				false);
		Log.d("TextFragment", "view inflated");
		if (getArguments() != null)
			filename = getArguments().getString(TabConstants.FILENAME);
		
		TextView textBox = (TextView) ((LinearLayout) textView).findViewById(R.id.textBox);
		String textData = "";
		try {
			textData = loadFile();
		} catch (Exception e) {
			Log.e("TextFragment", "Error loading text file");
		}
		
		Log.d("textData", textData);
		Log.d("textBox", Boolean.toString(textBox == null));
		if (textData.equals(""))
			textBox.setText(R.string.no_data);
		else
			textBox.setText(textData);
		return textView;		
	}

	public String loadFile() throws Exception{
		File file = new File(TabConstants.PREFIX + TabConstants.FOLDER_TYPES[2] + "/" + filename + ".txt");
		Log.d("textData", file.getAbsolutePath());
		Log.d("textData", Boolean.toString(file.exists()));
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder text = new StringBuilder("");
		String line = "";
		while (line != null){
			text.append(line);
			text.append("\n");
			line = reader.readLine();
		}
		reader.close();
		return text.toString();
	}
}
