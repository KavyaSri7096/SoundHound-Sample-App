package com.houndify.sample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hound.android.sdk.Search;
import com.hound.android.sdk.VoiceSearch;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.android.sdk.util.HoundRequestInfoFactory;
import com.hound.android.sdk.util.UserIdFactory;
import com.hound.core.model.sdk.HoundRequestInfo;
import com.hound.core.model.sdk.PartialTranscript;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

public class CustomVoiceSearchActivity extends AppCompatActivity {

    private LocationManager locationManager;

    private VoiceSearch voiceSearch;

    private TextView statusTextView;
    private TextView contentTextView;
    private Button btnSearch;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_voice_search_phrase);

        statusTextView = findViewById(R.id.status_text_view);
        contentTextView = findViewById(R.id.textView);

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (voiceSearch == null) {
                    startSearch(new SimpleAudioByteStreamSource());
                }
                else {
                    voiceSearch.stopRecording();
                }
            }
        });

        Search.setDebug(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void startSearch(InputStream inputStream) {
        if (voiceSearch != null) {
            return; // We are already searching
        }

        voiceSearch =
                new VoiceSearch.Builder().setRequestInfo(getHoundRequestInfo())
                        .setClientId(ClientCredentialsUtil.getClientId(this))
                        .setClientKey(ClientCredentialsUtil.getClientKey(this))
                        .setListener(voiceListener)
                        .setAudioSource(inputStream)
                        .setInputLanguageIetfTag("en")
                        .build();

        statusTextView.setText("Listening...");
        btnSearch.setText("Stop Listening");

        voiceSearch.start();
    }

    private final Listener voiceListener = new Listener();

    /**
     * Listener to receive search state information and final search results.
     */
    private class Listener implements VoiceSearch.RawResponseListener {

        @Override
        public void onTranscriptionUpdate(final PartialTranscript transcript) {
            switch (voiceSearch.getState()) {
                case STATE_STARTED:
                    statusTextView.setText("Listening...");
                    break;

                case STATE_SEARCHING:
                    statusTextView.setText("Receiving...");
                    break;

                default:
                    statusTextView.setText("Unknown");
                    break;
            }

            contentTextView.setText("Transcription:\n" + transcript.getPartialTranscript());
        }

        @Override
        public void onResponse(String rawResponse, VoiceSearchInfo voiceSearchInfo) {
            voiceSearch = null;

            statusTextView.setText("Received Response");

            String jsonString;
            try {
                jsonString = new JSONObject(rawResponse).toString(2);
            } catch (final JSONException ex) {
                jsonString = "Failed to parse content:\n" + rawResponse;
            }

            contentTextView.setText(jsonString);
            btnSearch.setText("Search");
        }

        @Override
        public void onError(final Exception ex, final VoiceSearchInfo info) {
            voiceSearch = null;

            statusTextView.setText("Something went wrong");
            contentTextView.setText(exceptionToString(ex));
        }

        @Override
        public void onRecordingStopped() {
            statusTextView.setText("Receiving...");
        }

        @Override
        public void onAbort(final VoiceSearchInfo info) {
            voiceSearch = null;
            statusTextView.setText("Aborted");
        }
    }

    ;

    private HoundRequestInfo getHoundRequestInfo() {
        final HoundRequestInfo requestInfo = HoundRequestInfoFactory.getDefault(this);

        // Client App is responsible for providing a UserId for their users which is meaningful
        // to the client.
        requestInfo.setUserId(UserIdFactory.get(this));
        // Each request must provide a unique request ID.
        requestInfo.setRequestId(UUID.randomUUID().toString());

        // Attach location info if permission granted
        setLocation(requestInfo);

        return requestInfo;
    }

    /**
     * This method configure the TTS on a HoundRequestInfo object.
     */
    private void configTTS(HoundRequestInfo requestInfo) {

        // Example for configuring Acapela TTS Voice Collection
        // https://www.houndify.com/domains/540c271e-cf06-4f33-ab26-731f0bd9b79d

        // choose the voice 'Laura'
        requestInfo.setExtraField("ResponseAudioVoice", "Laura");

        // response type for audio
        requestInfo.setExtraField("ResponseAudioShortOrLong", "Short");

        // https://docs.houndify.com/reference/VoiceParameters
        final ObjectNode voiceParameters = JsonNodeFactory.instance.objectNode();
        voiceParameters.put("Speed", 100);
        voiceParameters.put("Volume", 100);
        voiceParameters.put("Pitch", 100);
        requestInfo.setExtraField("AcapelaVoiceParameters", voiceParameters);

        // specify the expected audio encoding formats
        final ArrayNode encodings = JsonNodeFactory.instance.arrayNode();
        encodings.add("WAV");
        encodings.add("Speex");
        requestInfo.setExtraField("ResponseAudioAcceptedEncodings", encodings);
    }

    private void setLocation(final HoundRequestInfo requestInfo) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        final Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastKnownLocation != null) {
            requestInfo.setLatitude(lastKnownLocation.getLatitude());
            requestInfo.setLongitude(lastKnownLocation.getLongitude());
            requestInfo.setPositionHorizontalAccuracy((double) lastKnownLocation.getAccuracy());
        }
    }

    private static String exceptionToString(final Exception ex) {
        try {
            final StringWriter sw = new StringWriter(1024);
            final PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.close();
            return sw.toString();
        } catch (final Exception e) {
            return "";
        }
    }
}
