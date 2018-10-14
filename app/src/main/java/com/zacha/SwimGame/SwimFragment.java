package com.zacha.SwimGame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Zach on 2018-03-07.
 */

public class SwimFragment extends Fragment {

    private View rootView;
    private int width;
    private int height;
    private boolean isSwimming;
    private MyView mView;
    private int yValue;
    private int xValue;
    private double[] referenceWave;
    private Integer[] colors = {Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA};
    private int counter = 0;
    private ArrayList<rectangle> myRects;
    private MainActivity activity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = (MainActivity) getActivity();
        rootView =  inflater.inflate(R.layout.fragment_swim, container, false);
        isSwimming = false;

        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.swim);
        mView = new MyView(getContext());
        relativeLayout.addView(mView);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        yValue = height/2;
        xValue = width/2;
        myRects = new ArrayList<>();
    }

    /*Class responsible for updating the S.W.I.M. (Sequential Wave Imprinting Machine) */
    public class MyView extends View
    {
        Paint paint = null;
        int radius = 100;

        public MyView(Context context)
        {
            super(context);
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            this.setBackgroundColor(Color.BLACK);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(xValue, yValue, radius, paint);

            //Spawn Rectangles
            if(counter %200 == 0){
                double rand = Math.random()*100;
                if(rand > 80){
                    Paint p = new Paint();
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(colors[(int) (Math.random()*colors.length)]);
                    rectangle rect = new rectangle((float) (100*Math.random() + 40), (float) (100* Math.random() + 40), width,(float) Math.random()*height,p );
                    myRects.add(rect);
                }
            }
            // Move Rectangles
            if(counter %10==0){
                Iterator<rectangle> iter = myRects.iterator();
                while( iter.hasNext()){
                    rectangle rect = iter.next();
                    rect.left -= 30;
                    if(rect.left < 0 ){
                       iter.remove();
                    }
                }
            }
            // Collision Detection
            for (rectangle rect : myRects){
                canvas.drawRect(rect.left,rect.top, rect.left+rect.width, rect.top + rect.height ,rect.paint);
                boolean collision = false;
                if(rect.top < yValue + radius & rect.top > yValue - radius){
                    if(rect.left > xValue - radius & rect.left < xValue + radius ){
                        collision = true;
                    }
                }else if(rect.top + rect.height <yValue +radius & rect.top+ rect.height >yValue - radius){
                    if(rect.left > xValue - radius & rect.left < xValue  +  radius ){
                        collision = true;
                    }
                }
                if(collision){
                    myRects = new ArrayList<>();
                    activity.getRecFragment().setRecording(false);
                    activity.getWaveGenerator().stopTone();
                    activity.getCountDownTimer().cancel();
                    activity.getPlay().setAlpha(1f);
                    activity.getPlay().setClickable(true);
                    isSwimming = false;
                }
            }
        }
    }


    class rectangle{
        //Our obstacles
        float width;
        float height;
        float left;
        float top;
        Paint paint;

        public rectangle(float width,float height,float left,float top, Paint paint){
            this.width = width;
            this.height = height;
            this.left = left;
            this.top = top;
            this.paint = paint;
        }
    }
    public void setReferenceWave(double[] referenceWave){
        this.referenceWave = referenceWave;
    }

    public void updateSwim(final double[] recordedVals){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Make our filters
                if(activity.getRecFragment().getRecording()) {
                    if (!isSwimming) {
                        try {
                            synchronized (this) {
                                this.wait(800);
                                this.notify();
                                isSwimming = true;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Filter lowPass = new Filter(5, 8000, Filter.PassType.Lowpass, 0);
                        for (int i = 0; i < recordedVals.length; i++) {
                            double mult = recordedVals[i] * referenceWave[counter % referenceWave.length];

                            //Filter the multiplication
                            lowPass.Update((float) mult);

                            yValue = (int) (lowPass.getValue()*5) + height / 2;
                            mView.postInvalidate();
                            counter++;
                        }
                    }
                }
            }
        });
    }
}

