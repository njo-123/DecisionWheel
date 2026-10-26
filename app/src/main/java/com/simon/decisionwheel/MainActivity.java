package com.simon.decisionwheel;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private WheelView wheelView;
    private LinearLayout inputContainer;
    private Button btnSpin, btnAddOption;
    private android.widget.FrameLayout winnerOverlay;
    private TextView tvWinnerName;
    private Button btnCloseWinner;
    private float currentRotation = 0f;

    private KonfettiView konfettiView;

    private Vibrator vibrator;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wheelView = findViewById(R.id.wheelView);
        inputContainer = findViewById(R.id.inputContainer);
        btnSpin = findViewById(R.id.btnSpin);
        btnAddOption = findViewById(R.id.btnAddOption);

        // Notice this is now FrameLayout
        winnerOverlay = findViewById(R.id.winnerOverlay);
        tvWinnerName = findViewById(R.id.tvWinnerName);
        btnCloseWinner = findViewById(R.id.btnCloseWinner);

        konfettiView = findViewById(R.id.konfettiView);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Add 4 default inputs to start
        // Initialize SharedPreferences
        prefs = getSharedPreferences("WheelAppPrefs", MODE_PRIVATE);

        // Load saved options
        String savedData = prefs.getString("options", "");
        if (!savedData.isEmpty()) {
            String[] items = savedData.split("\\|\\|\\|"); // Split by our custom separator
            for (String item : items) {
                addInputField();
                // Get the row we just added, then get the EditText inside it
                LinearLayout row = (LinearLayout) inputContainer.getChildAt(inputContainer.getChildCount() - 1);
                EditText et = (EditText) row.getChildAt(0);
                et.setText(item); // Restore the text
            }
        } else {
            // Add 4 default inputs if it's the first time opening the app
            for (int i = 0; i < 4; i++) {
                addInputField();
            }
        }

        // Add option button logic
        btnAddOption.setOnClickListener(v -> {
            if (inputContainer.getChildCount() < 10) {
                addInputField();
            } else {
                Toast.makeText(this, "Maximum 10 options reached!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSpin.setOnClickListener(v -> spinWheel());

        // Close overlay button
        btnCloseWinner.setOnClickListener(v -> winnerOverlay.setVisibility(View.GONE));
    }

    // Dynamically creates a new text box
    // Dynamically creates a new text box with a delete button
    private void addInputField() {
        // 1. Create the horizontal row
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);

        // 2. Create the Text Box
        EditText et = new EditText(this);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); // weight 1 to take up space
        et.setLayoutParams(etParams);
        et.setHint("Option " + (inputContainer.getChildCount() + 1));
        et.setTextColor(Color.WHITE);
        et.setHintTextColor(Color.GRAY);
        et.setGravity(Gravity.CENTER);

        // 3. Create the Delete Button
        Button deleteBtn = new Button(this);
        deleteBtn.setText("X");
        deleteBtn.setTextColor(Color.RED);
        deleteBtn.setBackgroundColor(Color.TRANSPARENT);
        deleteBtn.setTextSize(18f);
        deleteBtn.setOnClickListener(v -> {
            inputContainer.removeView(row); // Remove this specific row
            updateHintNumbers(); // Re-number the remaining options
            saveOptions(); // ADD THIS LINE
        });

        // 4. Add them to the row, then add the row to the screen
        row.addView(et);
        row.addView(deleteBtn);
        inputContainer.addView(row);
    }

    // Updates the "Option 1, Option 2..." text when one is deleted
    private void updateHintNumbers() {
        for (int i = 0; i < inputContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) inputContainer.getChildAt(i);
            EditText et = (EditText) row.getChildAt(0); // The text box is the first child
            et.setHint("Option " + (i + 1));
        }
    }

    // Saves the current text from all inputs to SharedPreferences
    private void saveOptions() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inputContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) inputContainer.getChildAt(i);
            EditText et = (EditText) row.getChildAt(0);
            String text = et.getText().toString().trim();

            if (!text.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("|||"); // Our custom separator
                }
                sb.append(text);
            }
        }
        prefs.edit().putString("options", sb.toString()).apply();

    }

    private void spinWheel() {
        // Short vibration when the button is clicked
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }

        ArrayList<String> optionsList = new ArrayList<>();

        // NEW CODE - Extract the EditText from the LinearLayout row
        for (int i = 0; i < inputContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) inputContainer.getChildAt(i);
            EditText et = (EditText) row.getChildAt(0); // The text box is the first child
            String text = et.getText().toString().trim();
            if (!text.isEmpty()) {
                optionsList.add(text);
            }
        }

        if (optionsList.size() < 2) {
            Toast.makeText(this, "Please enter at least 2 options!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] optionsArray = optionsList.toArray(new String[0]);
        wheelView.setOptions(optionsArray);

        Random random = new Random();
        float extraAngle = random.nextInt(360);
        float targetRotation = currentRotation + (360 * 5) + extraAngle;

        ValueAnimator animator = ValueAnimator.ofFloat(currentRotation, targetRotation);
        animator.setDuration(4000);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            wheelView.setRotationAngle(value);
            currentRotation = value;
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                calculateAndShowWinner(optionsArray, targetRotation);
            }
        });

        // Save the current options before spinning
        saveOptions();


        animator.start();
    }

    private void calculateAndShowWinner(String[] options, float finalRotation) {
        int numOptions = options.length;
        float sweepAngle = 360f / numOptions;

        float normalizedRotation = finalRotation % 360;
        int winningIndex = (int) (((270 - normalizedRotation + 360) % 360) / sweepAngle) % numOptions;

        String winner = options[winningIndex];
        showWinnerAnimation(winner);
    }

    private void showWinnerAnimation(String winnerName) {

        // Strong buzz for the winner
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(400);
            }
        }

        winnerOverlay.setVisibility(View.VISIBLE);

        // 1. Fade in the dark background
        winnerOverlay.setAlpha(0f);
        winnerOverlay.animate().alpha(1f).setDuration(300).start();

        // 2. Animate the text bouncing in (Scale from 0 to 1)
        tvWinnerName.setText(winnerName);
        tvWinnerName.setScaleX(0f);
        tvWinnerName.setScaleY(0f);

        tvWinnerName.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // 3. Blast the Confetti!
        konfettiView.start(
                new PartyFactory(new Emitter(1500, TimeUnit.MILLISECONDS).max(100))
                        .spread(360)
                        .colors(Arrays.asList(0x03A9F4, 0xFFFFFF, 0xFF5252, 0xFFAB40))
                        .position(0.5, 0.4)
                        .build()
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveOptions(); // Saves data automatically when leaving the app
    }
}