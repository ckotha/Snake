package com.charan.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class SnakeEngine extends SurfaceView implements Runnable {

    private Thread thread = null;

    public enum Heading {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    public Heading heading = Heading.RIGHT;

    private int screenX;
    private int screenY;

    private int snakeLength;

    private ArrayList<Integer> bobX;
    private ArrayList<Integer> bobY;

    public boolean greater;
    public int numBobs;

    private int blockSize;

    private final int NUM_BLOCKS_WIDE = 40;
    private int numBlocksHigh;

    private long nextFrameTime;

    public long FPS = 10;

    private final long MILLIS_PER_SECOND = 1000;

    private int score;

    private int[] snakeX;
    private int[] snakeY;

    private volatile boolean isPlaying;

    private Canvas canvas;

    private SurfaceHolder surfaceHolder;

    private Paint paint;

    public SnakeEngine(Context context, Point size) {
        super(context);

        screenX = size.x;
        screenY = size.y;

        blockSize = screenX / NUM_BLOCKS_WIDE;
        numBlocksHigh = screenY / blockSize;

        surfaceHolder = getHolder();
        paint = new Paint();

        snakeX = new int[200];
        snakeY = new int[200];

        numBobs = 1;
        greater = false;

        bobX = new ArrayList<>();
        bobY = new ArrayList<>();
        newGame();
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (updateRequired()) {
                update();
                draw();
            }
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {
        snakeLength = 1;
        snakeX[0] = NUM_BLOCKS_WIDE / 2;
        snakeY[0] = numBlocksHigh / 2;
        greater = false;

        spawnBob();

        score = 0;

        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnBob() {
        if (bobX.size() == 0) {
            int count = 0;
            if (greater) {
                numBobs++;
            }
            while (count < numBobs) {
                Random random = new Random();
                bobX.add(random.nextInt(NUM_BLOCKS_WIDE - 1) + 1);
                bobY.add(random.nextInt(numBlocksHigh - 1) + 1);
                count++;
            }
            greater = false;
            numBobs = 1;
        }
    }

    private void eatBob() {
        snakeLength++;
        spawnBob();
        score = score + 1;
    }

    private void moveSnake() {

        for (int i = snakeLength; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }

        switch (heading) {
            case UP:
                snakeY[0]--;
                break;

            case RIGHT:
                snakeX[0]++;
                break;

            case DOWN:
                snakeY[0]++;
                break;

            case LEFT:
                snakeX[0]--;
                break;
        }
    }

    private boolean detectDeath() {
        boolean dead = false;

        // Hit the screen edge
        if (snakeX[0] == -1
                || snakeX[0] >= NUM_BLOCKS_WIDE
                || snakeY[0] == -1
                || snakeY[0] == numBlocksHigh) {
            dead = true;
        }

        // Eaten itself?
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void update() {
        for (int i = 0; i < bobX.size(); i++) {
            if (snakeX[0] == bobX.get(i) && snakeY[0] == bobY.get(i)) {
                bobX.remove(i);
                bobY.remove(i);
                eatBob();
            }
        }

        moveSnake();

        if (detectDeath()) {
            newGame();
        }
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            int offset = 255 / snakeLength;
            canvas = surfaceHolder.lockCanvas();

            canvas.drawColor(Color.argb(255, 0, 0, 0));

            paint.setColor(Color.argb(255, 255, 255, 255));

            paint.setTextSize(90);
            canvas.drawText("Score: " + score, 10, 70, paint);

            for (int i = 0; i < snakeLength; i++) {
                canvas.drawRect(snakeX[i] * blockSize,
                        (snakeY[i] * blockSize),
                        (snakeX[i] * blockSize) + blockSize,
                        (snakeY[i] * blockSize) + blockSize,
                        paint);
                paint.setColor(Color.argb(255, 0, 255 - offset, 0));
            }

            paint.setColor(Color.argb(255, 255, 0, 0));

            for (int i = 0; i < bobX.size(); i++) {
                canvas.drawRect(bobX.get(i) * blockSize,
                        (bobY.get(i) * blockSize),
                        (bobX.get(i) * blockSize) + blockSize,
                        (bobY.get(i) * blockSize) + blockSize,
                        paint);

            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {

        if (nextFrameTime <= System.currentTimeMillis()) {
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;
            return true;
        }

        return false;
    }
}
