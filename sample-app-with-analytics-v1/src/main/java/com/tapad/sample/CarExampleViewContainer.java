package com.tapad.sample;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CarExampleViewContainer {
    private ImageView carImage;
	private Button refreshButton;
    
	public CarExampleViewContainer(View view) {
		carImage = (ImageView) view.findViewById(R.id.image);
        refreshButton = (Button) view.findViewById(R.id.refresh);
	}

	public void setCarColor(int color) {
		carImage.setImageResource(color);
	}
	
	public void setCarImageListener(OnClickListener listener)  {
		carImage.setOnClickListener(listener);
	}
	
	public void setRefreshListener(OnClickListener listener)  {
		refreshButton.setOnClickListener(listener);
	}
}
