package com.example.standalone;

import  android.graphics.Canvas;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.zip.Adler32;

public class GameLoop extends Thread{
    public static final double MAX_UPS = 60.0;
    private static final double UPS_PERIOD = 1E+3 / MAX_UPS;
    private boolean isRunning = false;
    private SurfaceHolder surfaceHolder;
    private Game game;
    private double averageUPS;
    private double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceholder) {
        this.game = game;
        this.surfaceHolder = surfaceholder;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public void startLoop() {
        Log.d("GameLoop.java", "startLoop()");
        isRunning = true;
        start();
    }

    @Override
    public void run() {
        Log.d("GameLoop.java", "run()");
        super.run();

        //Deklarasi time dan cycle
        int updateCount = 0;
        int frameCount = 0;

        long startTime;
        long elapsedTime;
        long sleepTime;

        // game loop
        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        while(isRunning){

            //Mencoba untuk update dan Render Game
            try{
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){

                    game.update();
                    updateCount++;
                    game.draw(canvas);
                }

            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }finally {
                if (canvas != null){
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            //Pause Game loop agar tidak Melewati target UPS
            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long) (updateCount * UPS_PERIOD - elapsedTime);
            if (sleepTime > 0){
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            //Skip Frame untuk keep up dengan target UPS
            while(sleepTime < 0 && updateCount < MAX_UPS - 1){
                game.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long) (updateCount * UPS_PERIOD - elapsedTime);
            }

            //Menghitung Rata2 UPS dan FPS
            elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime >= 1000) {
                averageUPS = updateCount / (1E-3 * elapsedTime);
                averageFPS = frameCount / (1E-3 * elapsedTime);
                updateCount = 0;
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
    }

    public void stopLoop() {
        Log.d("GameLoop.java", "stopLoop()");
        isRunning = false;
        try{
            join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
