package com.example.punt_a_horsh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.punt_a_horsh.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    boolean gameStarted = false;
    ArrayList<ImageView> horseImagesList, docImagesList;
    int gameTime;
    ArrayList<AtomicBoolean> enabledHorsesList;
    AtomicInteger pointsScored = new AtomicInteger(0);
    int highScore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        //use timer task to background thread the timer

        horseImagesList = new ArrayList<>();
        horseImagesList.add(binding.horseImage1);
        horseImagesList.add(binding.horseImage2);
        horseImagesList.add(binding.horseImage3);
        horseImagesList.add(binding.horseImage4);
        horseImagesList.add(binding.horseImage5);
        horseImagesList.add(binding.horseImage6);
        horseImagesList.add(binding.horseImage7);
        horseImagesList.add(binding.horseImage8);
        horseImagesList.add(binding.horseImage9);

        docImagesList = new ArrayList<>();
        docImagesList.add(binding.docImage1);
        docImagesList.add(binding.docImage2);
        docImagesList.add(binding.docImage3);
        docImagesList.add(binding.docImage4);
        docImagesList.add(binding.docImage5);
        docImagesList.add(binding.docImage6);
        docImagesList.add(binding.docImage7);
        docImagesList.add(binding.docImage8);
        docImagesList.add(binding.docImage9);

        enabledHorsesList = new ArrayList<>();
        for (int i = 0; i < 9; i++){
            enabledHorsesList.add(new AtomicBoolean(false));
        }


    // ------------ start game button -------------
        binding.button.setBackgroundResource(R.drawable.button_background);
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameStarted = true;
                gameTime = 60;
                pointsScored.set(0);
                binding.scoreDisplay.setText("Score: " +pointsScored);
                startTimer();
                binding.button.setEnabled(false);
                binding.button.setVisibility(View.INVISIBLE);
                binding.scoreLinLayout1.removeAllViews();
                binding.scoreLinLayout2.removeAllViews();
                binding.scoreLinLayout3.removeAllViews();
            }
        });

    // ---------- allow horses to get punted ------------
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            horseImagesList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (enabledHorsesList.get(finalI).get()) {
                        enabledHorsesList.get(finalI).set(false);
                        setPuntAnimation(horseImagesList.get(finalI), docImagesList.get(finalI));
                        if (horseImagesList.get(finalI).getTag().equals(R.drawable.goldfish)){
                            gameTime -= 10;
                            if (gameTime < 0)
                                gameTime = 0;
                        }
                        else {
                            pointsScored.incrementAndGet();
                            addPuntedHorse();
                            binding.scoreDisplay.setText("Score: " + pointsScored);
                            if (highScore < pointsScored.get())
                                highScore = pointsScored.get();
                            binding.highScoreDisplay.setText("High Score: " + highScore);
                        }
                    }
                }
            });
        }

    }// end of on create

    public void startTimer() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                binding.timerDisplay.post(() -> {
                    displayGameTime();
                });

        // --------- sending the horses down ------------
                for (int i = 0; i < 9; i++){
                    boolean putDown = randomizer(10);
                    if (putDown && enabledHorsesList.get(i).get()){
                        int finalI = i;
                        horseImagesList.get(i).post(() -> {
                            setDownAnimation(horseImagesList.get(finalI));
                        });
                        enabledHorsesList.get(i).set(false);
                    }
                }

        // --------- sending the horses up ------------
                for (int i = 0; i < 9; i++){
                    boolean sendUp = randomizer(16);
                    if (sendUp && !enabledHorsesList.get(i).get()){
                        int finalI = i;
                        horseImagesList.get(i).post(() -> {
                            boolean loseTime = randomizer(20);
                            if (loseTime) {
                                horseImagesList.get(finalI).setImageResource(R.drawable.goldfish);
                                horseImagesList.get(finalI).setTag(R.drawable.goldfish);
                            }
                            else {
                                horseImagesList.get(finalI).setImageResource(R.drawable.cartoon_horse);
                                horseImagesList.get(finalI).setTag(R.drawable.cartoon_horse);
                            }
                            setUpAnimation(horseImagesList.get(finalI));
                        });
                        enabledHorsesList.get(i).set(true);
                    }
                }

                if (gameTime < 1){
                    cancel();
                    gameStarted = false;
                    binding.button.post(() -> {
                        binding.button.setEnabled(true);
                        binding.button.setVisibility(View.VISIBLE);
                    });

                    for (int i = 0; i < 9; i++){
                        if (enabledHorsesList.get(i).get()){
                            int finalI = i;
                            horseImagesList.get(i).post(() -> {
                                setDownAnimation(horseImagesList.get(finalI));
                            });
                            enabledHorsesList.get(i).set(false);
                        }
                    }

                }

            }// end of run method of timer task
        }; // end of timer task

        timer.schedule(timerTask, 0, 1000);
    }

    public void displayGameTime(){
        if (gameTime > 0)
            gameTime--;


        Drawable progressDrawable = binding.timerDisplayBar.getProgressDrawable().mutate();

        if (gameTime <= 5) {
            progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            binding.timerDisplay.setTextColor(Color.RED);
        }
        else if (gameTime <= 15) {
            progressDrawable.setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
            binding.timerDisplay.setTextColor(Color.YELLOW);
        }
        else if (gameTime <= 60) {
            progressDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
            binding.timerDisplay.setTextColor(Color.WHITE);
        }
        binding.timerDisplayBar.setProgressDrawable(progressDrawable);
        binding.timerDisplayBar.setProgress(gameTime, true);
        binding.timerDisplay.setText(String.valueOf(gameTime));
    }

    public boolean randomizer(int chances){
        int num = (int)(Math.random()*chances)+1;
        if (num == 1)
            return true;
        return false;
    }

    public void setUpAnimation(ImageView horseImage) {
        //set fill after method to true will keep it at the end
        //goes down after 1-3 seconds
        horseImage.setVisibility(View.VISIBLE);
        final ScaleAnimation upAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);
        horseImage.startAnimation(upAnimation);
    }

    public void setPuntAnimation(ImageView horseImage, ImageView docImage){
        docImage.setVisibility(View.VISIBLE);
        final RotateAnimation docRotateAnimation = new RotateAnimation(330.0f, 405.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        docRotateAnimation.setDuration(100);
        final ScaleAnimation docScaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        docScaleAnimation.setDuration(1);
        docScaleAnimation.setStartOffset(250L);
        AnimationSet docAnimations = new AnimationSet(true);
        docAnimations.addAnimation(docRotateAnimation);
        docAnimations.addAnimation(docScaleAnimation);
        docImage.startAnimation(docAnimations);
        docAnimations.setFillAfter(true);

        horseImage.setVisibility(View.VISIBLE);
        final ScaleAnimation horseScaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        horseScaleAnimation.setDuration(750);
        final RotateAnimation horseRotateAnimation = new RotateAnimation(0.0f, -360.0f, Animation.RELATIVE_TO_SELF, 0.25f, Animation.RELATIVE_TO_SELF, 0.25f);
        horseRotateAnimation.setDuration(750);
        AnimationSet horseAnimations = new AnimationSet(true);
        horseAnimations.addAnimation(horseScaleAnimation);
        horseAnimations.addAnimation(horseRotateAnimation);
        horseAnimations.setStartOffset(50L);
        horseImage.startAnimation(horseAnimations);
        horseAnimations.setFillAfter(true);

    }

    public void setDownAnimation(ImageView horseImage){
        final ScaleAnimation downAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);
        horseImage.startAnimation(downAnimation);
    }

    public void addPuntedHorse(){
        ImageView puntedHorse = new ImageView(this);
        puntedHorse.setId(View.generateViewId());
        puntedHorse.setImageResource(R.drawable.crying_horse);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
        params.gravity = Gravity.CENTER;
        puntedHorse.setLayoutParams(params);


        if (pointsScored.get() <= 8)
            binding.scoreLinLayout1.addView(puntedHorse);
        else if (pointsScored.get() <= 18)
            binding.scoreLinLayout2.addView(puntedHorse);
        else if (pointsScored.get() <= 28)
            binding.scoreLinLayout3.addView(puntedHorse);

    }
} // end of main activity