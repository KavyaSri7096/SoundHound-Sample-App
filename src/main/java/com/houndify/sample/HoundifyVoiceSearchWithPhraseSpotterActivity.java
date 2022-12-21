package com.houndify.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.fd.Houndify;
import com.hound.android.libphs.PhraseSpotter;
import com.hound.android.libphs.PhraseSpotterReader;
import com.hound.android.sdk.Search;
import com.hound.android.sdk.VoiceSearch;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.core.model.sdk.CommandResult;
import com.hound.core.model.sdk.HoundResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This sample demonstrates how to run a Hound voice search.
 * This is the preferred and easiest method for integrating Houndify into an application since
 * much of the
 * detailed search UI and state management is automatically taken care of for you.
 */
public class HoundifyVoiceSearchWithPhraseSpotterActivity extends AppCompatActivity {

    private static final String LOG_TAG = HoundifyVoiceSearchWithPhraseSpotterActivity.class.getSimpleName();

    private TextView textView;
    private PhraseSpotterReader phraseSpotterReader;
    private TextToSpeechMgr textToSpeechMgr;
    private Houndify houndify;

    private MenuItem menuSetPhraseSpotterThreshold;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hound_search);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("To start a voice search:\n")
                .append("Say the wakeup phrase \"")
                .append(PhraseSpotter.getPhrase()).append("\"")
                .append("  or\n")
                .append("Tap the microphone button below");

        // Text view for displaying written result
        textView = findViewById(R.id.textView);
        textView.setText(stringBuilder.toString());

        // Setup TextToSpeech
        textToSpeechMgr = new TextToSpeechMgr(this, new UtteranceProgressListener() {

            @Override
            public void onStart(String utteranceId) {
                Log.d(LOG_TAG, "TTS onStart");
            }

            @Override
            public void onDone(String utteranceId) {
                Log.d(LOG_TAG, "TTS onDone");
                attemptPhraseSpottingAsync();
            }

            @Override
            public void onError(String utteranceId) {
                Log.d(LOG_TAG, "TTS onError");
                attemptPhraseSpottingAsync();
            }
        });

        // Normally you'd only have to do this once in your Application#onCreate
        houndify = Houndify.get(this);
        houndify.setClientId(ClientCredentialsUtil.getClientId(this));
        houndify.setClientKey(ClientCredentialsUtil.getClientKey(this));
        houndify.setRequestInfoFactory(new DefaultRequestInfoFactory(this));

        // Turn on debug output. This should be disable for your production code.
        Search.setDebug(true);

        houndify.setVoiceSearchBuilderInterceptor(new Houndify.VoiceSearchBuilderInterceptor() {
            @Override
            public void intercept(VoiceSearch.Builder builder) {
                builder.requestInfo.setWakeUpPattern("[[\"OK\"] . \"Hound\"]");

                // Input Language
                builder.setInputLanguageIetfTag("en");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeechMgr != null) {
            textToSpeechMgr.shutdown();
            textToSpeechMgr = null;
        }
    }

    private boolean isResumed = false;

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        attemptPhraseSpotting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
        stopPhraseSpotting();
    }

    /**
     * A slight delay will help clear all pending conditions.
     */
    private void attemptPhraseSpottingAsync() {
        textView.postDelayed(new Runnable() {
            @Override
            public void run() {
                attemptPhraseSpotting();
            }
        }, 100);
    }

    /**
     * Some extra checks before phrase spotting.
     */
    private void attemptPhraseSpotting() {
        if (!isResumed) {
            Log.w(LOG_TAG, "can't start phrase spotter: activity not resumed");
            return;
        }

        if (textToSpeechMgr != null && textToSpeechMgr.isSpeaking()) {
            Log.w(LOG_TAG, "can't start phrase spotter: TTS active");
            return;
        }

        startPhraseSpotting();
    }

    /**
     * Called to start the Phrase Spotter
     */
    private void startPhraseSpotting() {
        if (phraseSpotterReader != null) {
            // already started
            return;
        }

        phraseSpotterReader = new PhraseSpotterReader(new SimpleAudioByteStreamSource());
        phraseSpotterReader.setBufferSizeMs(160);
        phraseSpotterReader.setListener(phraseSpotterListener);
        phraseSpotterReader.start();
    }

    /**
     * Called to stop the Phrase Spotter
     */
    private void stopPhraseSpotting() {
        if (phraseSpotterReader == null) {
            // not running
            return;
        }

        phraseSpotterReader.stop();
        phraseSpotterReader = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menuSetPhraseSpotterThreshold = menu.add("Set Phrase Spotter Threshold");
        menuSetPhraseSpotterThreshold.setIcon(android.R.drawable.ic_menu_edit);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == menuSetPhraseSpotterThreshold) {
            showSetPhraseSpotterThresholdDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void showSetPhraseSpotterThresholdDialog() {
        if (phraseSpotterReader == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Phrase Spotter Threshold [0.0, 1.0]");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(Float.toString(phraseSpotterReader.getConfidenceScoreThreshold()));

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                if (phraseSpotterReader != null) {
                    try {
                        float threshold = Float.parseFloat(text);
                        if (threshold >= 0.0f && threshold <= 1.0f) {
                            phraseSpotterReader.setConfidenceScoreThreshold(threshold);
                        }
                        else {
                            Toast.makeText(
                                    HoundifyVoiceSearchWithPhraseSpotterActivity.this,
                                    "Threshold value out of range, must be a value between 0 and 1",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (NumberFormatException e) {
                        Toast.makeText(
                                HoundifyVoiceSearchWithPhraseSpotterActivity.this,
                                "Invalid input; must be a value between 0 and 1",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Implementation of the PhraseSpotterReader.Listener interface used to handle PhraseSpotter
     * call back.
     */
    private final PhraseSpotterReader.Listener phraseSpotterListener =
            new PhraseSpotterReader.Listener() {

                @Override
                public void onPhraseSpotted() {
                    Log.d(LOG_TAG, "Phrase Spotter: onPhraseSpotted");

                    // It's important to note that when the phrase spotter detects "Ok Hound" it
                    // closes
                    // the input stream it was provided.
                    stopPhraseSpotting();
                    // Now start the HoundifyVoiceSearchActivity to begin the search.
                    houndify.setSoundEnabled(true);
                    houndify.voiceSearch(HoundifyVoiceSearchWithPhraseSpotterActivity.this);
                }

                @Override
                public void onStopped() {
                    Log.d(LOG_TAG, "Phrase Spotter: onStopped");
                }

                @Override
                public void onError(final Exception ex) {
                    Log.d(LOG_TAG, "Phrase Spotter: onError");

                    // for this sample we don't care about errors from the "Ok Hound" phrase
                    // spotter.

                }
            };

    /**
     * The HoundifyVoiceSearchActivity returns its result back to the calling Activity
     * using the Android's onActivityResult() mechanism.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Houndify.REQUEST_CODE) {
            final HoundSearchResult result =
                    Houndify.get(this).fromActivityResult(resultCode, data);

            if (result.hasResult()) {
                onResponse(result.getResponse());
            }
            else if (result.getErrorType() != null) {
                onError(result.getException(), result.getErrorType());
            }
            else {
                textView.setText("Aborted search");
            }
            attemptPhraseSpotting();
        }
    }

    /**
     * Called from onActivityResult() above
     *
     * @param response
     */
    private void onResponse(final HoundResponse response) {

        // Make sure the request succeeded with OK
        if (!response.getStatus().equals(HoundResponse.Status.OK)) {
            textView.setText("Request failed with: " + response.getErrorMessage());
            return;
        }

        if (response.getResults().isEmpty()) {
            textView.setText("Received empty response!");
            return;
        }

        final CommandResult topResult = response.getResults().get(0);

        StringBuilder stringBuilder = new StringBuilder();

        if (!response.getDisambiguation().getChoiceData().isEmpty()) {
            stringBuilder.append("Transcription:\n");
            stringBuilder.append(response.getDisambiguation()
                    .getChoiceData()
                    .get(0)
                    .getTranscription());
            stringBuilder.append("\n\n");
        }

        if (!TextUtils.isEmpty(topResult.getWrittenResponse())) {
            stringBuilder.append("Written Response:\n");
            stringBuilder.append(topResult.getWrittenResponse());
            stringBuilder.append("\n\n");
        }

        textView.setText(stringBuilder.toString());

        textToSpeechMgr.speak(topResult.getSpokenResponse());
    }

    /**
     * Called from onActivityResult() above
     *
     * @param ex
     * @param errorType
     */
    private void onError(final Exception ex, final VoiceSearchInfo.ErrorType errorType) {
        textView.setText(errorType.name() + "\n\n" + exceptionToString(ex));
        attemptPhraseSpotting();
    }

    private static String exceptionToString(final Exception ex) {
        try {
            final StringWriter sw = new StringWriter(1024);
            final PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.close();
            return sw.toString();
        }
        catch (final Exception e) {
            return "";
        }
    }
}
