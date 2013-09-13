/**
* MenuActivity.java
*  
* App launches from this main activity.
* Contains three buttons--- (1) Record (2) Respeak (3) Transcribe
* Imports constants from TabConstants.java
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp;

import com.iqss.respeakerapp.utils.TabConstants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

public class MenuActivity extends Activity {
	private Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null){
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_menu);
		}
		
		setUpExplorerButtons();
	}
	
	/* 
	 * Launching RecordActivity from Record button.
	 */
	public void startRecordActivity(View view){
		Intent intent = new Intent(this, RecordActivity.class);
		startActivity(intent);
	}
	
	/* 
	 * Both the Respeak and Transcribe buttons launch FileExplorerActivity.
	 * Intent also passes info about which file types to display (inputActivity).
	 */
	private void setUpExplorerButtons(){
		
		final ImageButton respeakButton = (ImageButton) findViewById(R.id.respeak_button);
		respeakButton.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FileExplorerActivity.class);
				// 1 is for respeakings
				intent.putExtra(TabConstants.INPUT_ACTIVITY, 1);
				startActivity(intent);				
			}
		});
		
		final ImageButton transcribeButton = (ImageButton) findViewById(R.id.transcribe_button);
		transcribeButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FileExplorerActivity.class);
				// 2 is for transcriptions
				intent.putExtra(TabConstants.INPUT_ACTIVITY, 2);
				startActivity(intent);				
			}
		});
	}
	
	// -------------------------------------------------------------
	// ************STUFF BELOW THIS IS DEFAULT CODE*****************
	// -------------------------------------------------------------
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
}
