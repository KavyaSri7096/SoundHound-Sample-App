package com.houndify.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

public class MusicActivity extends AppCompatActivity {

    MediaPlayer music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        // Adding the music file to our
        // newly created object music
        music = MediaPlayer.create(
                this, R.raw.sound);
    }

    // Plaing the music
    public void musicplay(View v)
    {
        music.start();
    }

    // Pausing the music
    public void musicpause(View v)
    {
        music.pause();
    }

    // Stopping the music
    public void musicstop(View v)
    {
        music.stop();
        music
                = MediaPlayer.create(
                this, R.raw.sound);
    }
}
