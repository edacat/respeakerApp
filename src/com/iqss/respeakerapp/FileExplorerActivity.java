/**
* FileExplorerActivity.java
*  
* Main activity for setting up three FileExplorerFragments, one for each tab.
* (1) New (2) In Progress (3) Completed
* Used to display both files for respeaking and for transcription.
* 
* @author Jessica Yao
*/

package com.iqss.respeakerapp;

import com.iqss.respeakerapp.fragments.FileExplorerFragment;
import com.iqss.respeakerapp.utils.TabConstants;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class FileExplorerActivity extends FragmentActivity{

	private FragmentTabHost mTabHost;
	private int inputActivity;
	private static final String STATE_TAB = "tab";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_explorer);
		Log.d("FileExplorerActivity", "View inflated.");
		
		// get info about which activity this is intended to launch (respeaking or transcription)
		inputActivity = getIntent().getIntExtra(TabConstants.INPUT_ACTIVITY, 1);
		
		setupTabs();
		
		if (savedInstanceState != null && mTabHost != null){
			mTabHost.setCurrentTabByTag(savedInstanceState.getString(STATE_TAB));
			inputActivity = savedInstanceState.getInt(TabConstants.INPUT_ACTIVITY);
		}
			
		Log.d("FileExplorerActivity", "Tabs set up.");

		// Show the Up button in the action bar.
		setupActionBar();
	}

	/*
	 * Get fragments for each tab set up.
	 */
	private void setupTabs(){
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		Log.d("FileExplorerActivity", "Tab host set up.");
		
		// pass bundles of arguments to each new tab
		Bundle[] bundles = new Bundle[TabConstants.TAB_CATEGORIES.length];
		
		for (int i = 0; i < TabConstants.TAB_CATEGORIES.length; i++){
			String tabCategoryName = TabConstants.TAB_CATEGORIES[i];
			bundles[i] = new Bundle();
			bundles[i].putInt(TabConstants.TAB_CATEGORY, i);
			bundles[i].putInt(TabConstants.INPUT_ACTIVITY, inputActivity);
			mTabHost.addTab(mTabHost.newTabSpec(tabCategoryName).setIndicator(tabCategoryName), FileExplorerFragment.class, bundles[i]);
			Log.d("FileExplorerActivity", "Tab from bundle array added.");
		}
	}
	
	/*
	 * Keeps track of which tab the user is currently looking at.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		 savedInstanceState.putString(STATE_TAB, mTabHost.getCurrentTabTag());
		 savedInstanceState.putInt(TabConstants.INPUT_ACTIVITY, inputActivity);
	}
	
	// -------------------------------------------------------------
	// ************STUFF BELOW THIS IS DEFAULT CODE*****************
	// -------------------------------------------------------------
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_explorer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}