/**
* TranscribeActivity.java
*  
* NOT launched when transcribe button is pressed. 
* Launched when a file is chosen from FileExplorerFragment.java for transcribing.
* Contains PlaybackFragment.java and TranscribeFragment.java
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp;

import com.iqss.respeakerapp.fragments.PlaybackFragment.ExtraOnClickListener;
import com.iqss.respeakerapp.fragments.TranscribePlaybackFragment;
import com.iqss.respeakerapp.fragments.TranscribeFragment;
import com.iqss.respeakerapp.utils.TabConstants;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.annotation.TargetApi;
import android.os.Build;

public class  TranscribeActivity extends FragmentActivity implements OnTouchListener, ExtraOnClickListener, TextWatcher{
	private TranscribePlaybackFragment playbackFragment = null;
	private TranscribeFragment transcribeFragment = null;
	
	/*
	 * Setup when activity launched.
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transcribe);
		
		// might change this later, prevents standby mode
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// setting up arguments to pass to fragments (playback and transcribe)
		Bundle filenameBundle = new Bundle();
		filenameBundle.putString(TabConstants.FILENAME, getIntent().getStringExtra(TabConstants.FILENAME));
	
		FragmentManager fManager = getSupportFragmentManager();
		FragmentTransaction transaction = fManager.beginTransaction();
		
		playbackFragment = new TranscribePlaybackFragment();
		playbackFragment.setArguments(filenameBundle);
		transaction.add(R.id.transcribe_playback_frame, playbackFragment);
		
		transcribeFragment = new TranscribeFragment();
		transcribeFragment.setArguments(filenameBundle);
		transaction.add(R.id.transcribe_frame, transcribeFragment);
		
		transaction.commit();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	// -------------------------------------------------------------
	// *********************LISTENER FUNCTIONS**********************
	// -------------------------------------------------------------
	
	// pauses playback
	private void onTextFocus() {
		if (playbackFragment.getStatus()){ // if audio is playing
			playbackFragment.getView().findViewById(R.id.play_button).performClick();
			Log.d("TranscribeActivity", "Playback paused for transcription.");
		}	
	}
	
	// calls onTextFocus when textbox clicked
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		onTextFocus();
		return false; // important! do not remove (keeps keyboard functionality)
	}
	
	// calls onTextFocus when user starts typing
	@Override
	public void beforeTextChanged(CharSequence str, int start, int count, int after) {	
		
		// HACK: assumes that the only way for the number of characters in the textbox to
		// increase by exactly 9 or 10 characters is if time stamp is inserted
		// (might not work if 9-10 characters are copy and pasted in)
		if (after != 9 && after != 10)
			onTextFocus();
		
	}
	
	// implemented for ExtraOnClickListener (used in PlaybackFragment)
	// appends timestamp to textbox when playback (re)started
	@Override
	public void onClicked(String timestamp) {
		 transcribeFragment.addText("\n(" + timestamp + ") ");
	}

	// -------------------------------------------------------------
	// **********IGNORE (inserted to satisfy interface)*************
	// -------------------------------------------------------------

	@Override
	public void onTextChanged(CharSequence str, int start, int count, int after) {	
	}
	
	@Override
	public void afterTextChanged(Editable arg0) {
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
		getMenuInflater().inflate(R.menu.transcribe, menu);
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