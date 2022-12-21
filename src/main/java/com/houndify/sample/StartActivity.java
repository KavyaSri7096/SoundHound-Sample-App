package com.houndify.sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    private static class Sample {
        final private CharSequence title;
        final private CharSequence subtitle;
        final private CharSequence[] features;
        final private Class activityClass;

        Sample(CharSequence title, CharSequence subtitle, CharSequence[] features,
               Class activityClass) {
            this.title = title;
            this.subtitle = subtitle;
            this.features = features;
            this.activityClass = activityClass;
        }
    }

    private static final List<Sample> SAMPLE_LIST = new ArrayList();

    static {
        SAMPLE_LIST.add(new Sample("Houndify Voice Search with Phrase Spotter",
                "Recommended Integration",
                new CharSequence[]{
                        "Wake-up phrase spotter for hands-free experience",
                        "Phrase spotter threshold adjustment",
                        "One-time initialization with Houndify class",
                        "Interactive search panel with live transcriptions",
                        "Response spoken with Android TTS engine",
                },
                HoundifyVoiceSearchWithPhraseSpotterActivity.class));

        SAMPLE_LIST.add(new Sample("Custom Voice Search",
                "For Customized UI",
                new CharSequence[]{
                        "Custom voice search API",
                        "Live transcriptions",
                        "Access to raw JSON response",
                },
                CustomVoiceSearchActivity.class));

        SAMPLE_LIST.add(new Sample("Text Search",
                "Text-based Search",
                new CharSequence[]{
                        "Text search API",
                        "Access to raw JSON response",
                }, CustomTextSearchActivity.class));
        SAMPLE_LIST.add(new Sample("Music list",
                "Search",
                new CharSequence[]{
                        "Play",
                        "Pause",
                }, MusicActivity.class));
    }

    private ListView listView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        listView = findViewById(R.id.listview);

        listView.setAdapter(new SamplesAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(),
                        SAMPLE_LIST.get(position).activityClass));
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        }, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TextUtils.isEmpty(ClientCredentialsUtil.getClientId(this)) ||
                TextUtils.isEmpty(ClientCredentialsUtil.getClientKey(this))) {
            Toast.makeText(this,
                    "Client API Credentials missing.\nPlease update in settings.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private MenuItem menuSettings;
    private MenuItem menuVersionInfo;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menuSettings = menu.add("Client API Credentials");
        menuSettings.setIcon(android.R.drawable.ic_menu_edit);

        menuVersionInfo = menu.add("Version Info");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == menuSettings) {
            ClientCredentialsUtil.showEditPopup(this);
            return true;
        }
        else if (item == menuVersionInfo) {
            showVersionInfoDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SamplesAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return SAMPLE_LIST.size();
        }

        @Override
        public Sample getItem(int position) {
            return SAMPLE_LIST.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;

            if (convertView == null) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_sample, parent, false);
            }
            else {
                itemView = convertView;
            }

            Sample sample = getItem(position);

            TextView title = itemView.findViewById(R.id.title);
            title.setText(sample.title);

            TextView subtitle = itemView.findViewById(R.id.subtitle);
            subtitle.setText(sample.subtitle);

            TextView description = itemView.findViewById(R.id.description);

            StringBuilder stringBuilder = new StringBuilder();
            for (CharSequence feature : sample.features) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append('\n');
                }
                stringBuilder.append(" - ").append(feature);
            }

            description.setText(stringBuilder.toString());

            return itemView;
        }
    }

    private void showVersionInfoDialog() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Sample App: ").append(BuildConfig.VERSION_NAME).append('\n');

        stringBuilder.append("Houndify Lib: ")
                .append(com.hound.android.voicesdk.BuildConfig.ARTIFACT_ID)
                .append(':')
                .append(com.hound.android.voicesdk.BuildConfig.VERSION_NAME)
                .append('\n');

        stringBuilder.append("Phrase Spotter Lib: ")
                .append(com.hound.android.libphs.BuildConfig.ARTIFACT_ID)
                .append(':')
                .append(com.hound.android.libphs.BuildConfig.VERSION_NAME);

        new AlertDialog.Builder(this).setTitle("Version Info")
                .setMessage(stringBuilder.toString())
                .create()
                .show();
    }
}
