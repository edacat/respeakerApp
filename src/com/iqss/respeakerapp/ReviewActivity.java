package com.iqss.respeakerapp;

import com.iqss.respeakerapp.fragments.TranscribePlaybackFragment;
import com.iqss.respeakerapp.fragments.TextFragment;
import com.iqss.respeakerapp.utils.TabConstants;

import android.os.Bundle;
import android.util.Log;
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
		
		Bundle filenameBundle = new Bundle();
		filenameBundle.putString(TabConstants.FILENAME, getIntent().getStringExtra(TabConstants.FILENAME));
		Log.d("review", filenameBundle.getString(TabConstants.FILENAME));
		FragmentManager fManager = getSupportFragmentManager();
		FragmentTransaction transaction = fManager.beginTransaction();
		
		if (getIntent().getIntExtra(TabConstants.INPUT_ACTIVITY, 1) == 1){ // in Respeaking section
			TranscribePlaybackFragment playbackFragment = new TranscribePlaybackFragment();
			playbackFragment.setArguments(filenameBundle);
			transaction.add(R.id.review_frame, playbackFragment);
		} else { // in Transcribe section
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
