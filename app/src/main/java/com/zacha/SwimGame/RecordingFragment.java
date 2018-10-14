package com.zacha.SwimGame;

import android.Manifest;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Zach on 2018-03-07.
 */

public class RecordingFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private View rootView;
    private final int SAMPLE_RATE = 8000; //44100 original
    private String LOG_TAG = "logger";
    private boolean isRecording = false; // Indicates if recording / playback should stop
    private onRecording mCallback;
    private short[] audioBuffer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_recording, container, false);

        return rootView;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //Request permission to use microphone
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }


    public void recordAudio() {
        //Called from red button click
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }

                audioBuffer = new short[bufferSize / 2];

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                Log.v(LOG_TAG, "Start recording");

                long shortsRead = 0;
                int i = 0;
                while (isRecording) {
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;
                    // Do something with the audioBuffer
                    mCallback.recordedValues(audioBuffer);
                    i++;
                }

                record.stop();
                record.release();

                Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));

            }
        }).start();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (onRecording) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onRecording");
        }
    }

    public void setRecording(boolean recording){
        this.isRecording = recording;
    }

    public boolean getRecording(){
        return isRecording;
    }

    // Container Activity must implement this interface
    public interface onRecording {
        void recordedValues(short[] values);
    }
}
