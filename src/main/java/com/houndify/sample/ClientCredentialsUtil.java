package com.houndify.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static com.houndify.sample.R.id.input_client_key;

public class ClientCredentialsUtil {

    // update these as needed; or configure through editor ui
    private static final String DEFAULT_CLIENT_ID = "b2DknokS8_VpTua5XNz3Gg==";
    private static final String DEFAULT_CLIENT_KEY = "sHYDzbYx2UTt83vRgLlSF0TyjcXFVBwT5uihblhNYu2bfKYSrcNKcUgxZBHqkPIChfaVgxnQVNO4qA7kfxP-AQ==";

    public static void showEditPopup(final Context context) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.edit_client_credentials_popup, null);

        final EditText id = view.findViewById(R.id.input_client_id);
        id.setText(getDefaultSharedPreferences(context).getString(KEY_CLIENT_ID, null));

        final EditText key = view.findViewById(input_client_key);
        key.setText(getDefaultSharedPreferences(context).getString(KEY_CLIENT_KEY, null));

        final TextView link = view.findViewById(R.id.link);


        new AlertDialog.Builder(context).setTitle("Client API Credentials")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(

                        "Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String idString = id.getText().toString().trim();
                                if (idString.isEmpty()) {
                                    idString = null;
                                }
                                String keyString = key.getText().toString().trim();
                                if (keyString.isEmpty()) {
                                    keyString = null;
                                }
                                getDefaultSharedPreferences(context).edit()
                                        .putString(KEY_CLIENT_ID, idString)
                                        .putString(KEY_CLIENT_KEY, keyString)
                                        .apply();
                            }
                        })
                .create()
                .show();
    }

    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_KEY = "client_key";

    private static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getClientId(Context context) {
        return getDefaultSharedPreferences(context).getString(KEY_CLIENT_ID, DEFAULT_CLIENT_ID);
    }

    public static String getClientKey(Context context) {
        return getDefaultSharedPreferences(context).getString(KEY_CLIENT_KEY, DEFAULT_CLIENT_KEY);
    }
}
