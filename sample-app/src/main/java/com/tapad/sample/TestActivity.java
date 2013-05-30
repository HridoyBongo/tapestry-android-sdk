package com.tapad.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.tapad.tapestry.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TestActivity extends FragmentActivity {
    private String[] parameters = {"Opt-out", "setData(color, blue)", "addData(color, red)", "addAudiences(2DSP1)", "listDevices()", "strength(5)", "depth(2)"};
    private boolean[] selected = new boolean[parameters.length];

    public TapestryRequest updateRequest() {
        TapestryRequest request = new TapestryRequest();
        int i = 0;
        if (selected[i++]) TapestryService.optOut(this);
        else TapestryService.optIn(this);
        if (selected[i++]) request.setData("color", "blue");
        if (selected[i++]) request.addData("color", "red");
        if (selected[i++]) request.addAudiences("2DSP1");
        if (selected[i++]) request.listDevices();
        if (selected[i++]) request.strength(5);
        if (selected[i++]) request.depth(2);
        getTextView(R.id.request).setText(prettifyRequest(TapestryService.client().addParameters(request).toQuery()));
        return request;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_content);

        getTextView(R.id.response).setHorizontallyScrolling(true);
        getTextView(R.id.request).setHorizontallyScrolling(true);

        Logging.setThrowExceptions(true);
        Logging.setEnabled(true);

        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectParametersDialogFragment dialog = new SelectParametersDialogFragment();
                dialog.show(getSupportFragmentManager(), "");
            }
        });
    }

    public void sendRequest(TapestryRequest request) {
        TapestryService.send(request, new TapestryUICallback(this) {
            @Override
            public void receiveOnUiThread(TapestryResponse response) {
                getTextView(R.id.response).setText(response.toString());
            }
        });
    }

    private TextView getTextView(int response) {
        return ((TextView) findViewById(response).findViewById(R.id.text));
    }

    public String prettifyRequest(String request) {
        try {
            return URLDecoder.decode(request, "UTF-8").replaceAll("&", "\n");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public class SelectParametersDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
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
    }
}