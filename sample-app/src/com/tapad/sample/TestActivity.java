package com.tapad.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.tapad.tapestry.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TestActivity extends Fragment {
    private String[] parameters = {"Opt-out", "setData(color, blue)", "addData(color, red)", "addAudiences(2DSP1)", "listDevices()", "strength(5)", "depth(2)"};
    private boolean[] selected = new boolean[parameters.length];
	private View view;

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
        getTextView(R.id.request).setText(prettifyRequest(TapestryService.client().addParameters(request).toQuery()));
        return request;
    }

	public void sendRequest(TapestryRequest request) {
        TapestryService.send(request, new TapestryUICallback(getActivity()) {
            @Override
            public void receiveOnUiThread(TapestryResponse response) {
                getTextView(R.id.response).setText(response.toString());
            }
        });
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.test_content, container, false);
    	getTextView(R.id.text).setHorizontallyScrolling(true);
    	getTextView(R.id.request).setHorizontallyScrolling(true);

        Button send = (Button) view.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        return buildDialog();
                    }
                }.show(getActivity().getSupportFragmentManager(), "");
            }
        });

		return view;
	}

	private Dialog buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        getTextView(R.id.request).setText("");
        getTextView(R.id.response).setText("");
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

    private TextView getTextView(int response) {
        return ((TextView) view.findViewById(response).findViewById(R.id.text));
    }

    public String prettifyRequest(String request) {
        try {
            return URLDecoder.decode(request, "UTF-8").replaceAll("&", "\n");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}