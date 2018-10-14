package com.zacha.SwimGame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Zach on 2018-03-07.
 */

public class MainActivity extends FragmentActivity implements RecordingFragment.onRecording{

    private WaveGenerator waveGenerator;
    private RecordingFragment recFragment;
    private TextView timer;
    private boolean firstPlay = true;
    private CountDownTimer countDownTimer;
    private ImageView play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get Our recording Fragment
        recFragment = (RecordingFragment) getSupportFragmentManager().findFragmentById(R.id.recording_fragment);
        //Initialize Wave Generator
        waveGenerator = new WaveGenerator();

        timer = (TextView) findViewById(R.id.timer);
        countDownTimer =  new CountDownTimer(20*60000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText(new SimpleDateFormat("ss").format(new Date(1000 - millisUntilFinished)));
            }

            public void onFinish() {
                timer.setText("done!");
            }
        };
        play = (ImageView) findViewById(R.id.play);
        //Will set reference wave for the Swim Fragment
        setReferenceWave(waveGenerator.getReferenceWave());
    }

    @Override
    public void recordedValues(short[] values){
        //Called from Recording Fragment
        //Get values recorded from the recording fragment
        double[] transformed = new double[values.length];
        for (int j=0;j<values.length;j++) {
            transformed[j] = (double)values[j];
        }

        SwimFragment swimFrag = (SwimFragment)
                getSupportFragmentManager().findFragmentById(R.id.swim_fragment);
        swimFrag.updateSwim(transformed);
    }

    private void setReferenceWave(double[] referenceWave) {
        //Get the values of our reference referenceWave
        SwimFragment swimFrag = (SwimFragment)
                getSupportFragmentManager().findFragmentById(R.id.swim_fragment);
        swimFrag.setReferenceWave(referenceWave);
    }


    public void recordAudio(View v){

        if(!recFragment.getRecording()) {
            play.setAlpha(0.50f);
            play.setClickable(false);

            waveGenerator.playTone();
            countDownTimer.start();

            recFragment.setRecording(true);
            recFragment.recordAudio();
        }
    }

    public WaveGenerator getWaveGenerator() {
        return waveGenerator;
    }

    public RecordingFragment getRecFragment() {
        return recFragment;
    }

    public CountDownTimer getCountDownTimer() {
        return countDownTimer;
    }

    public ImageView getPlay() {
        return play;
    }
}
