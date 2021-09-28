package com.pra.pdfprintapplication.pdfprint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pra.pdfprintapplication.R;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.util.ArrayList;

public class PrinterConnectionDialog extends DialogFragment {
    private static final String TAG = "PRINTER_CNNCTN_DIALOG";

    private MainActivity mainActivity;
    private TextView emptyView;
    private DiscoveredPrinterAdapter adapter;
    private ArrayList<DiscoveredPrinter> discoveredPrinters;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog()");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return dialog;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(androidx.fragment.app.DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog_Custom);
        mainActivity = (MainActivity) getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = View.inflate(getActivity(), R.layout.dialog_printer_connect, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setView(view);

        discoveredPrinters = new ArrayList<>();
        adapter = new DiscoveredPrinterAdapter(getActivity(), R.layout.list_item_discovered_printer, discoveredPrinters);

        emptyView = (TextView) view.findViewById(R.id.discoveredPrintersEmptyView);

        ListView discoveredPrintersListView = (ListView) view.findViewById(R.id.discoveredPrintersListView);
        discoveredPrintersListView.setEmptyView(emptyView);
        discoveredPrintersListView.setAdapter(adapter);

       // final AlertDialog dialog = builder.create();

        discoveredPrintersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.displayConnectingStatus();

                new SelectedPrinterTask((MainActivity) getActivity(), adapter.getItem(position)).execute();
                dismiss();
               // dialog.dismiss();
            }
        });

        try {
            BluetoothDiscoverer.findPrinters(getActivity(), new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    discoveredPrinters.add(discoveredPrinter);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "Discovered a printer");
                }

                @Override
                public void discoveryFinished() {
                    Log.i(TAG, "Discovery finished");
                }

                @Override
                public void discoveryError(String s) {
                    Log.i(TAG, "Discovery error");
                }
            });
        } catch (ConnectionException e) {
            Log.i(TAG, "Printer connection error");
        }

        return view;
    }

}
