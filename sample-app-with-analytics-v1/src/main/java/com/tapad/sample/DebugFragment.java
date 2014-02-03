package com.tapad.sample;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tapad.tapestry.TapestryRequest;
import com.tapad.tapestry.TapestryResponse;
import com.tapad.tapestry.TapestryService;
import com.tapad.tapestry.TapestryUICallback;

public class DebugFragment extends Fragment {
    private String[] parameters = {"Opt-out", "setData(color, blue)", "addData(color, red)", "addAudiences(2DSP1)", "listDevices()", "strength(5)", "depth(2)"};
    private boolean[] selected = new boolean[parameters.length];
	private DebugViewContainer viewContainer;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.debug_fragment, container, false);
		viewContainer = new DebugViewContainer(view);
    	viewContainer.setOnSendClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRequestBuilderDialog();
            }
        }));

		return view;
	}
    
    private void showRequestBuilderDialog() {
    	new DialogFragment() {
    		@Override
    		public Dialog onCreateDialog(Bundle savedInstanceState) {
    			return buildDialog();
    		}
    	}.show(getActivity().getSupportFragmentManager(), "");
    }

	private Dialog buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		viewContainer.setRequestText("");
		viewContainer.setResponseText("");
        builder.setTitle("Select Parameters")
                .setMultiChoiceItems(parameters, selected,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                selected[which] = isChecked;
                            }
                        })
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sendRequest(updateRequest());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        updateRequest();
                    }
                });
        return builder.create();
	}
	
    public TapestryRequest updateRequest() {
        TapestryRequest request = new TapestryRequest();
        int i = 0;
        if (selected[i++]) TapestryService.optOut(getActivity());
        else TapestryService.optIn(getActivity());
        if (selected[i++]) request.setData("color", "blue");
        if (selected[i++]) request.addData("color", "red");
        if (selected[i++]) request.addAudiences("2DSP1");
        if (selected[i++]) request.listDevices();
        if (selected[i++]) request.strength(5);
        if (selected[i++]) request.depth(2);
        viewContainer.setRequestText(prettifyRequest(TapestryService.client().addParameters(request).toQuery()));
        return request;
    }

	public void sendRequest(TapestryRequest request) {
		TapestryService.send(request, new TapestryUICallback(getActivity()) {
			@Override
			public void receiveOnUiThread(TapestryResponse response, Exception exception, long millisSinceInvocation) {
				viewContainer.setResponseText(response.toString());
			}
		});
	}
	
    public String prettifyRequest(String request) {
        try {
            return URLDecoder.decode(request, "UTF-8").replaceAll("&", "\n");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}