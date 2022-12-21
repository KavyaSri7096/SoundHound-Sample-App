package com.houndify.sample;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * Helper class used for managing the TextToSpeech engine
 */
public class TextToSpeechMgr implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private UtteranceProgressListener utteranceProgressListener;

    public TextToSpeechMgr(Activity activity) {
        this(activity, null);
    }

    public TextToSpeechMgr(Activity activity,
                           UtteranceProgressListener utteranceProgressListener) {
        this.utteranceProgressListener = utteranceProgressListener;
        textToSpeech = new TextToSpeech(activity, this);
    }

    @Override
    public void onInit(int status) {
        // Set language to use for playing text
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true;
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.setSpeechRate(2f);
            if (utteranceProgressListener != null) {
                textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
            }
        }
    }

    public void shutdown() {
        textToSpeech.shutdown();
    }

    /**
     * Play the text to the device speaker
     *
     * @param textToSpeak
     */
    public void speak(String textToSpeak) {
        final HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, textToSpeak);
        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, params);
    }

    public boolean isSpeaking() {
        return isInitialized && textToSpeech.isSpeaking();
    }
}
