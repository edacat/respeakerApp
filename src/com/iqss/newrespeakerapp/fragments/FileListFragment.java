/**
* FileListFragment.java
*  
* Contains functionality to list appropriate files for a certain tab in FileExplorerActivity.java
*  
* @author Jessica Yao
*/

package com.iqss.newrespeakerapp.fragments;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.iqss.newrespeakerapp.R;
import com.iqss.newrespeakerapp.RespeakActivity;
import com.iqss.newrespeakerapp.ReviewActivity;
import com.iqss.newrespeakerapp.TranscribeActivity;
import com.iqss.newrespeakerapp.utils.TabConstants;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileListFragment extends ListFragment{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
