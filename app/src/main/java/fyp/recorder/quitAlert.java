package fyp.recorder;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import fyp.layout.R;

public class quitAlert extends DialogFragment {
    Context context;
    boolean quit;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getContext();
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setCancelable(true);
        builder.setMessage("Quit app?");

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                quit = true;
                MainActivity.getInstance().quitApp();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                quitAlert.this.getDialog().dismiss();
            }
        });

        return builder.show();
    }

    public boolean isQuit () {
        return quit;
    }
}
