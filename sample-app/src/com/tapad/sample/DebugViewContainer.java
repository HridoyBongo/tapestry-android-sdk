package com.tapad.sample;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.tapad.tapestry.R;

public class DebugViewContainer {

	private TextView requestTextView;
	private TextView responseTextView;
	private Button sendButton;

	public DebugViewContainer(View root) {
		requestTextView = (TextView) root.findViewById(R.id.request).findViewById(R.id.text);
		responseTextView = (TextView) root.findViewById(R.id.response).findViewById(R.id.text);
		sendButton = (Button) root.findViewById(R.id.send);
	}

	public void setRequestText(String msg) {
		requestTextView.setText(msg);
	}
	
	public void setResponseText(String msg) {
		responseTextView.setText(msg);
	}
	
	public void setOnSendClickListener(OnClickListener onClickListener) {
		sendButton.setOnClickListener(onClickListener);
	}

}
