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
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class FileExplorerFragment extends ListFragment{	
	
	private static final File[] DIRECTORIES = new File[4];
	private int tabCategory;
	private int inputActivity;
	private ArrayAdapter<String> myListAdapter = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		tabCategory = getArguments().getInt(TabConstants.TAB_CATEGORY);
    	inputActivity = getArguments().getInt(TabConstants.INPUT_ACTIVITY);
    	Log.d("FileExplorerFragment", "constants obtained");
    	
    	// set up array of directory paths that files can come from
    	for (int i = 0; i < TabConstants.FOLDER_TYPES.length; i++){
    		DIRECTORIES[i] = new File(TabConstants.PREFIX + TabConstants.FOLDER_TYPES[i]);
    		DIRECTORIES[i].mkdirs();
    	}	  
    	Log.d("FileExplorerFragment", "directories set up");
    	
    	myListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, getFilenameList());
    	setListAdapter(myListAdapter);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View fileExplorerView = inflater.inflate(R.layout.file_view, container, false);
    	Log.d("FileExplorerFragment", "view inflated");
        return fileExplorerView;
    }
    
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
				String ext = (inputActivity == 1) ? ".wav" : ".txt";
				String filename = name.split("\\.")[0] + ext;
				if (tabCategory == 0){ // for "new" tab
					// 3 is the inProgress folder
					if (new File(DIRECTORIES[inputActivity], filename).exists() || new File(DIRECTORIES[3], filename).exists())
						return false;
				} else { // for "in progress" tab
					if (!new File(DIRECTORIES[3], filename).exists())
						return false;
				}
				return true;
			}	
    	};	
    	File[] fileList = (tabCategory == 2) ? DIRECTORIES[inputActivity].listFiles() : DIRECTORIES[inputActivity - 1].listFiles(filter);
    	Arrays.sort(fileList, new Comparator<File>(){
    	    public int compare(File f1, File f2)
    	    {
    	        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
    	    } 
    	 });
    	
    	for (File f : fileList)
    		filenameArrayList.add(f.getName().split("\\.")[0]);
    	
    	return filenameArrayList.toArray(new String[0]); 
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	Intent intent;
    	if (tabCategory == 2){ // completed
    		intent = new Intent(this.getActivity(), ReviewActivity.class);
    		intent.putExtra(TabConstants.FILENAME, getListView().getItemAtPosition(position).toString());
    		intent.putExtra(TabConstants.INPUT_ACTIVITY, inputActivity);
    	} else {
    		Class<?> launchingClass = (inputActivity == 1) ? RespeakActivity.class : TranscribeActivity.class;
    		intent = new Intent(this.getActivity(), launchingClass);
    		intent.putExtra(TabConstants.FILENAME, getListView().getItemAtPosition(position).toString());
    	}
    	startActivity(intent);
    }
    
}