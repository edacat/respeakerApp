package com.iqss.respeakerapp.fragments;

import java.io.File;

import com.iqss.respeakerapp.R;
import com.iqss.respeakerapp.utils.TabConstants;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class NameDialog extends DialogFragment{
	
	public interface SaveDialogListener{
	     public void onDialogPositiveClick(DialogFragment dialog, String absPath, String newName);
	     public void onDialogNegativeClick(DialogFragment dialog, String absPath);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		FragmentManager fm = getActivity().getSupportFragmentManager();
		final SaveDialogListener mListener = (SaveDialogListener) fm.findFragmentById(R.id.origin_recording_fragment);
		
		String filename = getArguments().getString(TabConstants.FILENAME);
		File dir = new File(TabConstants.PREFIX + "recordings");
		dir.mkdirs();
		final String absPath = new File(dir.getAbsolutePath(), filename).getAbsolutePath();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_view, null);
    	((EditText) v.findViewById(R.id.dialog_field)).setText(filename);
        builder.setView(v).setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogPositiveClick(NameDialog.this, absPath, ((EditText) v.findViewById(R.id.dialog_field)).getText().toString());
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
	
	// force return to previous page
	@Override
	public void onCancel(DialogInterface dialog){
		super.onCancel(dialog);
		getActivity().finish();
	}

}
