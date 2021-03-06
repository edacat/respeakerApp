/**
* ReviewActivity.java
*  
* Launched when clicking a "completed" file in respeaking or transcription.
* Either plays back audio or shows text.
* 
* @author Jessica Yao
*/

package com.iqss.newrespeakerapp;

import com.iqss.newrespeakerapp.R;
import com.iqss.newrespeakerapp.fragments.TranscribePlaybackFragment;
import com.iqss.newrespeakerapp.fragments.TextFragment;
import com.iqss.newrespeakerapp.utils.TabConstants;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class ReviewActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review);
		
		// setting arguments to pass to fragment (filename)
		Bundle filenameBundle = new Bundle();
		filenameBundle.putString(TabConstants.FILENAME, getIntent().getStringExtra(TabConstants.FILENAME));
		
		FragmentManager fManager = getSupportFragmentManager();
		FragmentTransaction transaction = fManager.beginTransaction();
		
		// in Respeaking section (launching playback)
		// 1 is hard-coded (could prob associate INPUT_ACTIVITY with enums, but...)
		if (getIntent().getIntExtra(TabConstants.INPUT_ACTIVITY, 1) == 1){ 
			TranscribePlaybackFragment playbackFragment = new TranscribePlaybackFragment();
			playbackFragment.setArguments(filenameBundle);
			transaction.add(R.id.review_frame, playbackFragment);
		} 
		// in Transcribe section (launching text viewer)
		else { 
			TextFragment textFragment = new TextFragment();
			textFragment.setArguments(filenameBundle);
			transaction.add(R.id.review_frame, textFragment);
		}
		
		transaction.commit();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}

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
		getMenuInflater().inflate(R.menu.review, menu);
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
