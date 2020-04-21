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

    private ArrayList<Integer> appleX;
    private ArrayList<Integer> appleY;

    public boolean greater;
    public int numApples;

    private int blockSize;

    private final int NUM_BLOCKS_WIDE = 15;
    private int numBlocksHigh;

    private long nextFrameTime;

    public long FPS = 10;

    private final long MILLIS_PER_SECOND = 1300;

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
        snakeX[0] = NUM_BLOCKS_WIDE / 2;
        snakeY[0] = numBlocksHigh / 2;

        numApples = 1;
        greater = false;

        appleX = new ArrayList<>();
        appleY = new ArrayList<>();
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
        spawnApple();
        score = 0;
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnApple() {
        if (appleX.size() == 0) {
            int count = 0;
            if (greater) {
                numApples++;
            }
            while (count < numApples) {
                Random random = new Random();
                appleX.add(random.nextInt(NUM_BLOCKS_WIDE - 1) + 1);
                appleY.add(random.nextInt(numBlocksHigh - 1) + 1);
                count++;
            }
            greater = false;
            numApples = 1;
        }
    }

    private void eatApple() {
        snakeLength++;
        spawnApple();
        if (score >= 100) {
            score += 20;
        } else if (score >= 200) {
            score += 40;
        } else {
            score += 10;
        }

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

        if (snakeX[0] == -1
                || snakeX[0] >= NUM_BLOCKS_WIDE
                || snakeY[0] == -1
                || snakeY[0] == numBlocksHigh) {
            dead = true;
        }

        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void update() {
        for (int i = 0; i < appleX.size(); i++) {
            if (snakeX[0] == appleX.get(i) && snakeY[0] == appleY.get(i)) {
                appleX.remove(i);
                appleY.remove(i);
                eatApple();
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

            for (int i = 0; i < appleX.size(); i++) {
                canvas.drawRect(appleX.get(i) * blockSize,
                        (appleY.get(i) * blockSize),
                        (appleX.get(i) * blockSize) + blockSize,
                        (appleY.get(i) * blockSize) + blockSize,
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
