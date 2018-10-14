package com.zacha.SwimGame;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class WaveGenerator {

    private static final int duration = 100; // seconds
    private static final int sampleRate = 8000;
    private static final int numSamples = duration * sampleRate;
    private double[] referenceWave;
    static AudioTrack audioTrack;


    public WaveGenerator(){

        referenceWave = new double[numSamples];
        byte generatedSnd[]  = new byte[2 * numSamples];
        int idx = 0;
        for (int i = 0; i < numSamples; ++i) {
            referenceWave[i] = Math.sin(2 * Math.PI * i / (sampleRate / 392.0));
            short val = (short) (referenceWave[i] * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);

        audioTrack.write(generatedSnd, 0, numSamples);

        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                track.play();
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {

            }
        });
    }

    public double[] getReferenceWave(){
        return this.referenceWave;
    }


    public void playTone(){
            audioTrack.play();
    }

    public void stopTone(){
        audioTrack.stop();
    }

}