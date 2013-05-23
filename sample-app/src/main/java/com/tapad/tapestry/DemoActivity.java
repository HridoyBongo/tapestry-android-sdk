package com.tapad.tapestry;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;

public class DemoActivity extends Activity {
    private List<String> colorNames = Arrays.asList("black", "blue", "gray", "red", "white");
    private int[] colors = {R.drawable.black_car, R.drawable.blue_car, R.drawable.gray_car, R.drawable.red_car, R.drawable.white_car};
    private int selectedColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_content);

        Logging.setEnabled(true);
        Logging.setThrowExceptions(true);

        // install swipe detection
        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setOnTouchListener(new GestureListener(this) {
            @Override
            protected void swipe(int dX) {
                selectedColor += dX;
                updateColor();
                TapestryService.send(new TapestryRequest().setData("color", colorNames.get(selectedColor)));
            }
        });

        Button button = (Button) findViewById(R.id.refresh);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshColor();
            }
        });
    }

    private void refreshColor() {
        TapestryService.send(new TapestryRequest(), new TapestryUICallback(this) {
            @Override
            public void receiveOnUiThread(TapestryResponse response) {
                List<String> savedColors = response.getData("color");
                if (savedColors.size() > 0) {
                    selectedColor = colorNames.indexOf(savedColors.get(0));
                    Logging.debug(getClass(), "Selected color " + savedColors.get(0) + " " + selectedColor);
                    updateColor();
                }
            }
        });
    }

    private void updateColor() {
        if (selectedColor < 0)
            selectedColor = colors.length - 1;
        if (selectedColor == colors.length)
            selectedColor = 0;
        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageResource(colors[selectedColor]);
    }

    private abstract static class GestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;
        private final GestureDetector detector;

        public GestureListener(Context context) {
            detector = new GestureDetector(context, this);
        }

        public boolean onTouch(View v, MotionEvent event) {
            return detector.onTouchEvent(event);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            float dX = e2.getX() - e1.getX();
            if (Math.abs(dX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
                swipe(dX > 0 ? 1 : -1);
            return result;
        }

        protected abstract void swipe(int dX);
    }
}