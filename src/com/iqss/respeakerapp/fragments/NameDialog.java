/**
* NameDialog.java
*  
* Launches dialog during RecordActivity to give the user option of renaming the file.
*  
* @author Jessica Yao
*/

package com.iqss.respeakerapp.fragments;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.utils.TabConstants;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;

public class NameDialog extends DialogFragment{
	
	/*
	 * Defines interface for listening to dialog button clicks (Save vs Cancel)
	 * Used by RecordingFragment.java
	 */
	public interface SaveDialogListener{
	     public void onDialogPositiveClick(DialogFragment dialog, String absPath, String newName);
	     public void onDialogNegativeClick(DialogFragment dialog, String absPath);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		// setting up listener
		FragmentManager fm = getActivity().getSupportFragmentManager();
		final SaveDialogListener mListener = (SaveDialogListener) fm.findFragmentById(R.id.origin_recording_fragment);
		
		// get original file path (used to rename)
		String filename = getArguments().getString(TabConstants.FILENAME);
		final String absPath = TabConstants.PREFIX + "recordings/" + filename;

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
              
        final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_view, null);
        final EditText filenameBox = (EditText) v.findViewById(R.id.dialog_field);
        
        // Set default text as default filename (auto-generated based on date and time)
    	filenameBox.setText(filename);
    	
    	// Setting up view with buttons and actions (for implementation see RecordingFragment.java)
        builder.setView(v).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogPositiveClick(NameDialog.this, absPath, filenameBox.getText().toString());
            }
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mListener.onDialogNegativeClick(NameDialog.this, absPath);
			}
		});
     
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	/*
	 * Force return to previous page (file explorer) when dialog closed
	 * @see android.support.v4.app.DialogFragment#onCancel(android.content.DialogInterface)
	 */
	@Override
	public void onCancel(DialogInterface dialog){
		super.onCancel(dialog);
		getActivity().finish();
	}

}