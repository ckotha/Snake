package com.charan.snake;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends Activity {

    SnakeEngine snakeEngine;
    GestureLibrary gestureLib;
    GestureOverlayView gestureOverlayView;
    FrameLayout frameLayout;
    GestureDetector mDetector;
    public static final int SWIPE_THRESHOLD = 120;
    public static final int VELOCITY_THRESHOLD = 300;
    boolean paused = false;
    boolean slowActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        gestureOverlayView = new GestureOverlayView(this);
        snakeEngine = new SnakeEngine(this, size);
        frameLayout = new FrameLayout(this);

        mDetector = new GestureDetector(this, mGestureListener);

        gestureOverlayView.setEventsInterceptionEnabled(true);

        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gesture);
        if (!gestureLib.load()) {
            Toast.makeText(this, "Can't load lib", Toast.LENGTH_SHORT).show();
            finish();
        }
        gestureOverlayView.addOnGesturePerformedListener(mGesturePerformedListener);

        frameLayout.addView(snakeEngine, 0);
        frameLayout.addView(gestureOverlayView, 1);

        setContentView(frameLayout);
    }


    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        Log.i("INFO", "TOUCH EVENT");
        mDetector.onTouchEvent(e);

        return super.dispatchTouchEvent(e);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
            Log.d("TAG", "onFling: Called");

            boolean result = false;
            float diffY = moveEvent.getY() - downEvent.getY();
            float diffX = moveEvent.getX() - downEvent.getX();
            //which was greater movement across y or x.

            if (Math.abs(diffX) > Math.abs(diffY)) {
                //right or left swipe
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        snakeEngine.heading = snakeEngine.heading.RIGHT;
                    } else {
                        snakeEngine.heading = snakeEngine.heading.LEFT;
                    }
                    result = true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > VELOCITY_THRESHOLD) {
                    //up or down swipe
                    if (diffY > 0) {
                        snakeEngine.heading = snakeEngine.heading.DOWN;
                    } else {
                        snakeEngine.heading = snakeEngine.heading.UP;
                    }
                    result = true;
                }
            }
            return result;
        }
    };

    private final GestureOverlayView.OnGesturePerformedListener mGesturePerformedListener = new GestureOverlayView.OnGesturePerformedListener() {
        @Override
        public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
            Log.i("Info", "Gesture Detected");
            ArrayList<Prediction> predictions = gestureLib.recognize(gesture);

            if (predictions.size() > 0) {
                Prediction prediction = predictions.get(0);

                // checking prediction
                if (prediction.score > 3.0) {

                    Toast.makeText(MainActivity.this, prediction.name, Toast.LENGTH_SHORT).show();
                    if (prediction.name.equalsIgnoreCase("slow down") && !slowActive) {
                        snakeEngine.FPS = 5;
                        slowActive = true;
                        startTimer();
                    } else if (prediction.name.equalsIgnoreCase("stop")) {
                        if (paused) {
                            snakeEngine.resume();
                            paused = false;
                        } else {
                            snakeEngine.pause();
                            paused = true;
                        }
                    } else if (prediction.name.equalsIgnoreCase("greater") && !slowActive) {
                        snakeEngine.greater = true;
                    }
                }
            }
        }
    };

    private void startTimer() {

        new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished <= 5000) {
                    snakeEngine.FPS = 10;
                }
            }

            @Override
            public void onFinish() {
                slowActive = false;
            }
        }.start();

    }
}
