/**
* TabConstants.java
*  
* Class keeps track of constants used as keys/ID for setting up tabs.
* Used in MenuActivity.java, FileExplorerFragment.java, FileExplorerActivity.java
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp.utils;

import java.io.File;

import android.os.Environment;

public class TabConstants {
	
	public static final String INPUT_ACTIVITY = "Input Activity";
	public static final String[] FOLDER_TYPES = {"recordings", "respeakings", "transcriptions", "inProgress"};
	
	public static final String TAB_CATEGORY = "Tab Category";
	public static final String[] TAB_CATEGORIES = {"New", "In Progress", "Completed"};
	
	public static final String FILENAME = "File Name";
	
	private static final File sdCard = Environment.getExternalStorageDirectory();
	public static final String PREFIX = sdCard.getAbsolutePath() + "/respeakerApp/"; // parent folder for all files
}
