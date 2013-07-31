package com.tapad.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.tapad.tapestry.*;

import java.util.Arrays;
import java.util.List;

public class CarExampleFragment extends Fragment implements OnClickListener {
    private List<String> colorNames = Arrays.asList("black", "blue", "gray", "red", "white");
    private int[] colors = {R.drawable.black_car, R.drawable.blue_car, R.drawable.gray_car, R.drawable.red_car, R.drawable.white_car};
    private int selectedColor;
    private ImageView carImage;

    // store the updated color in Tapestry
    private void changeColor() {
		selectedColor = (selectedColor + 1) % (colors.length - 1);
		carImage.setImageResource(colors[selectedColor]);
    	TapestryService.send(new TapestryRequest().setData("color", colorNames.get(selectedColor)));
    }

    // get the color out of Tapestry and use it to update the car image
    private void refreshColor() {
        TapestryService.send(new TapestryUICallback(getActivity()) {
            @Override
            public void receiveOnUiThread(TapestryResponse response) {
                List<String> savedColors = response.getData("color");
                if (savedColors.isEmpty()) return;
                selectedColor = colorNames.indexOf(savedColors.get(0));
    			carImage.setImageResource(colors[selectedColor]);
            }
        });
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.image:
			changeColor();
			break;
		case R.id.refresh:
			refreshColor();
		}
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	final View view = inflater.inflate(R.layout.demo_content, container, false);
    	carImage = (ImageView) view.findViewById(R.id.image);
        view.findViewById(R.id.image).setOnClickListener(this);
        view.findViewById(R.id.refresh).setOnClickListener(this);
		return view;
	}
}