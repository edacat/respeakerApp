/**
* FileExplorerFragment.java
*  
* Contains functionality to list appropriate files for a certain tab in FileExplorerActivity.java
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp.fragments;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.RespeakActivity;
import com.iqss.respeakerapp.ReviewActivity;
import com.iqss.respeakerapp.TranscribeActivity;
import com.iqss.respeakerapp.utils.TabConstants;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileExplorerFragment extends ListFragment{	
	
	private static final File[] DIRECTORIES = new File[4];
	private int tabCategory; // (1) New (2) In Progress (3) Completed
	private int inputActivity; // (1) RespeakActivity (2) TranscribeActivity
	private ArrayAdapter<String> myListAdapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		tabCategory = getArguments().getInt(TabConstants.TAB_CATEGORY);
    	inputActivity = getArguments().getInt(TabConstants.INPUT_ACTIVITY);
    	
    	// set up array of directory paths that files can come from
    	for (int i = 0; i < TabConstants.FOLDER_TYPES.length; i++){
    		DIRECTORIES[i] = new File(TabConstants.PREFIX + TabConstants.FOLDER_TYPES[i]);
    		DIRECTORIES[i].mkdirs();
    	}	  
    	Log.d("FileExplorerFragment", "Directories set up.");
    	
    	myListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, getFilenameList());
    	setListAdapter(myListAdapter);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View fileExplorerView = inflater.inflate(R.layout.file_view, container, false);
    	Log.d("FileExplorerFragment", "View inflated.");
        return fileExplorerView;
    }
    
    // Force list to update everytime activity is returned to (i.e. after saving a recording).
    @Override
    public void onResume(){
    	super.onResume();
    	myListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, getFilenameList());
    	setListAdapter(myListAdapter);
    }
	
    /*
     * Return list of filenames, filtered accordingly.
     */
    private String[] getFilenameList(){
    	ArrayList<String> filenameArrayList = new ArrayList<String>();
    	FilenameFilter filter = new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// determines file extension based on parent activity
				String ext = (inputActivity == 1) ? ".wav" : ".txt";
				// "\\." is equivalent to "." under REGEX escaping
				String filename = name.split("\\.")[0] + ext; 
				// for "new" tab
				if (tabCategory == 0){ 
					// 3 is the inProgress folder
					// filters out filenames that are already inProgress or completed
					if (new File(DIRECTORIES[inputActivity], filename).exists() || new File(DIRECTORIES[3], filename).exists())
						return false;
				// for "in progress" tab
				} else {
					// filters out filenames that are completed
					if (!new File(DIRECTORIES[3], filename).exists())
						return false;
				}
				return true;
			}	
    	};	
    	// different filters depending on if files are inProgress (no filter needed) or other (filter needed)
    	File[] fileList = (tabCategory == 2) ? DIRECTORIES[inputActivity].listFiles() : DIRECTORIES[inputActivity - 1].listFiles(filter);
    	
    	// sorting files by last modified date (oldest on top)
    	Arrays.sort(fileList, new Comparator<File>(){
    	    public int compare(File f1, File f2)
    	    {
    	        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
    	    } 
    	 });
    	
    	// gets name (without extension) of all filtes that passed that filter
    	for (File f : fileList)
    		filenameArrayList.add(f.getName().split("\\.")[0]);
    	
    	return filenameArrayList.toArray(new String[0]); 
    }
    
    /*
     * Launches appropriate activity with filename arguments when list item clicked.
     * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Intent intent;
    	// if new addition is completed
    	if (tabCategory == 2){ 
    		intent = new Intent(this.getActivity(), ReviewActivity.class);
    		intent.putExtra(TabConstants.FILENAME, getListView().getItemAtPosition(position).toString());
    		intent.putExtra(TabConstants.INPUT_ACTIVITY, inputActivity);
    	} 
    	// if recording/transcribing still in progress
    	else {
    		Class<?> launchingClass = (inputActivity == 1) ? RespeakActivity.class : TranscribeActivity.class;
    		intent = new Intent(this.getActivity(), launchingClass);
    		intent.putExtra(TabConstants.FILENAME, getListView().getItemAtPosition(position).toString());
    	}
    	startActivity(intent);
    }
    
}