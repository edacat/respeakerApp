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

import java.net.URL;

import android.view.View.OnClickListener;

import com.iqss.respeakerapp.fragments.PlaybackFragment.ExtraOnClickListener;
import com.iqss.respeakerapp.fragments.TranscribePlaybackFragment;
import com.iqss.respeakerapp.fragments.TranscribeFragment;
import com.iqss.respeakerapp.utils.TabConstants;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Chronometer;
import android.widget.EditText;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.annotation.TargetApi;
import android.os.Build;

public class  TranscribeActivity extends FragmentActivity implements OnTouchListener, ExtraOnClickListener, TextWatcher{
	private TranscribePlaybackFragment playbackFragment = null;
	private TranscribeFragment transcribeFragment = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transcribe);
		
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

	private void onTextFocus() {
		if (playbackFragment.getStatus()){
			playbackFragment.getView().findViewById(R.id.play_button).performClick();
			Log.d("button", "focused");
		}	
		Log.d("button", "focused??");
	}

//	@Override
//	public String makeTimeStamp() {
//		return ((Chronometer) playbackFragment.getView().findViewById(R.id.playback_chronometer)).getText().toString();
//	}

	@Override
	public void onClicked(String timestamp) {
		 transcribeFragment.addText("\n(" + timestamp + ") ");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		onTextFocus();
		return false;
	}

	@Override
	public void afterTextChanged(Editable arg0) {
	}

	@Override
	public void beforeTextChanged(CharSequence str, int start, int count,
			int after) {	
		if (after != 9 && after != 10)
			onTextFocus();
	}

	@Override
	public void onTextChanged(CharSequence str, int start, int count, int after) {
		
	}

}