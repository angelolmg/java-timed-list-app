package com.aqueleangelo.myfirstandroidapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class EditDialog extends AppCompatDialogFragment {
    private EditText editTextActivityName;
    private EditText editTextTime;
    private EditDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.editor_dialog, null);

        builder.setView(view)
                .setTitle("Editor")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String activityName = editTextActivityName.getText().toString();
                        String time = editTextTime.getText().toString();

                        listener.applyTexts(activityName, time);
                    }
                });

        editTextActivityName = view.findViewById(R.id.edit_name);
        editTextTime = view.findViewById(R.id.edit_time);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (EditDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EditDialogListener");
        }
    }

    public interface EditDialogListener{
        void applyTexts(String activityName, String time);
    }
}
