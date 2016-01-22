package com.example.matos.pso;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;

/**
 * Created by matos on 21/11/2015.
 */
public class DisplayDialog {
    private final SharedPreferences settings;
    private final SharedPreferences.Editor editor;
    public AlertDialog alertDialog;
    public AlertDialog.Builder builder;

    public DisplayDialog(final Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();

        builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        builder.setView(inflater.inflate(R.layout.dialog_validation, null))
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        editor.putBoolean("firstrun", false);
                        editor.putBoolean("info_submitted", true);
                        editor.commit();

                        ((Activity) context).finish();
                    }
                });
        builder.setCancelable(false);
        alertDialog = builder.create();
        builder.show();
    }
}