package com.aqueleangelo.myfirstandroidapp;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class ListSound {
    private SoundPool soundPool;
    private TextToSpeech mTTs;
    private int doneSound, finishSound, startSound;
    private float soundVolume = 0.5f;

    public void buildSounds(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }



        doneSound = soundPool.load(context, R.raw.done, 1);
        finishSound = soundPool.load(context, R.raw.finished, 1);
        startSound = soundPool.load(context, R.raw.start, 1);

        mTTs = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS) {
                    int result = mTTs.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "English language not supported, setting default language.");
                        mTTs.setLanguage(Locale.getDefault());
                    }
                    mTTs.setPitch(1);
                    mTTs.setSpeechRate(1);
                } else {
                    Log.e("TTS", "Initialization failed.");
                }
            }
        });
    }

    public void playSound(String soundName) {
        switch (soundName) {
            case "done":
                soundPool.play(doneSound, soundVolume, soundVolume, 0, 0, 1);
                break;
            case "finish":
                soundPool.play(finishSound, soundVolume, soundVolume, 0, 0, 1);
                break;
            case "start":
                soundPool.play(startSound, soundVolume, soundVolume, 0, 0, 1);
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mTTs.speak(soundName, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    mTTs.speak(soundName, TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
        }
    }

    public void clear(){
        soundPool.release();
        soundPool = null;

        if(mTTs != null){
            mTTs.stop();
            mTTs.shutdown();
        }
    }
}
