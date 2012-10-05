package com.example;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.widget.Toast.LENGTH_SHORT;
import static java.util.Calendar.DECEMBER;

import java.util.Calendar;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MyActivity extends Activity {

    private CountDownTimer timer;
    private TextView textTime;
    MediaPlayer mediaPlayer;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        buttonCreate();
        startCountDown();
        playSound();

        Button button = (Button) findViewById(R.id.buttonNotif);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                NotificationUtils n = NotificationUtils.getInstance(getApplicationContext());
                n.createInfoNotification("info notification");
            }
        });
    }

    public void playSound() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.stopwatch);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void startCountDown() {
        textTime = (TextView) findViewById(R.id.textTime);
        final Calendar finalDate = Calendar.getInstance();
        finalDate.set(2012, DECEMBER, 21, 0, 0);
        timer = new FinalCountdown(finalDate.getTimeInMillis(), 17, this).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                showMessage("All the same, this does not stop. I'll wait for you");
                System.exit(0);
                return true;
            case R.id.help:
                showMessage("Who will help you?");
                showMessage("No one!");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer.start();
        mediaPlayer.start();
    }

    public void updateTimerText(String timeString) {
        textTime.setText(timeString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void buttonCreate() {
        final Toast toast = Toast.makeText(getApplicationContext(), "is not available for you, sorry :(", LENGTH_SHORT);
        toast.setGravity(0, 0, 0);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                toast.show();
            }
        });
    }

    public void showMessage(String string) {
        final Toast toast = Toast.makeText(getApplicationContext(), string, LENGTH_SHORT);
        toast.setGravity(0, 0, 0);
        toast.show();
    }

}

