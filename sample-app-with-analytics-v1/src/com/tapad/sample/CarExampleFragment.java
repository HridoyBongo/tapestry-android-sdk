package com.tapad.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.tapad.tapestry.*;

import java.util.Arrays;
import java.util.List;

public class CarExampleFragment extends Fragment {
    private List<String> colorNames = Arrays.asList("black", "blue", "gray", "red", "white");
    private int[] colors = {R.drawable.black_car, R.drawable.blue_car, R.drawable.gray_car, R.drawable.red_car, R.drawable.white_car};
    private int selectedColor;
    private CarExampleViewContainer viewContainer;

    // store the updated color in Tapestry
    private void changeColor() {
		selectedColor = (selectedColor + 1) % (colors.length - 1);
		viewContainer.setCarColor(colors[selectedColor]);
    	TapestryService.send(new TapestryRequest().setData("color", colorNames.get(selectedColor)));
    }

    // get the color out of Tapestry and use it to update the car image
    private void refreshColor() {
        TapestryService.send(new TapestryUICallback(getActivity()) {
            @Override
            public void receiveOnUiThread(TapestryResponse response, Exception exception, long millisSinceInvocation) {
                List<String> savedColors = response.getData("color");
                if (savedColors.isEmpty()) return;
                selectedColor = colorNames.indexOf(savedColors.get(0));
                viewContainer.setCarColor(colors[selectedColor]);
            }
        });
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.car_fragment, container, false);
    	viewContainer = new CarExampleViewContainer(view);
    	viewContainer.setCarImageListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeColor();
			}
		});
    	viewContainer.setRefreshListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshColor();
			}
		});
		return view;
	}
}