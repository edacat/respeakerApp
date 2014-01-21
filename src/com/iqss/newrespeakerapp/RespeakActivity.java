/**
* RespeakActivity.java
*  
* NOT launched when respeak button is pressed. 
* Launched when a file is chosen from FileExplorerFragment.java for respeaking.
* Contains PlaybackFragment.java and RecordingFragment.java
*  
* @author Jessica Yao
*/

package com.iqss.newrespeakerapp;

import com.iqss.newrespeakerapp.R;
import com.iqss.newrespeakerapp.fragments.RecordingFragment;
import com.iqss.newrespeakerapp.fragments.RecordingFragment.OnButtonFocusListener;
import com.iqss.newrespeakerapp.fragments.RespeakPlaybackFragment;
import com.iqss.newrespeakerapp.utils.TabConstants;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.FragmentActivity;
import android.annotation.TargetApi;
import android.os.Build;

public class RespeakActivity extends FragmentActivity implements OnButtonFocusListener{
	private RespeakPlaybackFragment playbackFragment = null;
	private RecordingFragment recordFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_respeak);
		
		// might change this later, prevents standby mode
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// setting arguments to pass to fragments within activity (playback and record)
		Bundle filenameBundle = new Bundle();
		filenameBundle.putString(TabConstants.FILENAME, getIntent().getStringExtra(TabConstants.FILENAME));
		
		FragmentManager fManager = getSupportFragmentManager();
		FragmentTransaction transaction = fManager.beginTransaction();
		
		playbackFragment = new RespeakPlaybackFragment();
		playbackFragment.setArguments(filenameBundle);
		transaction.add(R.id.respeak_playback_frame, playbackFragment);
		Log.d("respeakActivity", "Playback fragment added.");
		
		recordFragment = new RecordingFragment();
		recordFragment.setArguments(filenameBundle);
		transaction.add(R.id.respeak_record_frame, recordFragment);
		Log.d("respeakActivity", "Record fragment added.");
		
		transaction.commit();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	/*
	 * Listener for record button (pauses playback if user starts recording the respeaking)
	 * @see com.iqss.respeakerapp.fragments.RecordingFragment.OnButtonFocusListener#onButtonFocus()
	 */
	@Override
	public void onButtonFocus() {
		// Simulates play button click (i.e. if there is audio playback, pauses it)
		if (playbackFragment.getStatus()){
			playbackFragment.getView().findViewById(R.id.play_button).performClick();
		}
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
		getMenuInflater().inflate(R.menu.respeak, menu);
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