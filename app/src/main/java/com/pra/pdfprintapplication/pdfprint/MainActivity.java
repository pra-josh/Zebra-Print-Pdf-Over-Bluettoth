package com.pra.pdfprintapplication.pdfprint;

import android.Manifest;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.pra.pdfprintapplication.R;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String DIALOG_PRINTER_CONNECT_TAG = "printer_connect";
    private static final String DIALOG_PDF_PICK_TAG = "pdf_pick";
    private static final String DIALOG_PRINT_TAG = "print";

    protected TextView printerSelectionStatus;
    protected TextView printerInfoTableName;
    protected TextView printerInfoTableAddress;
    protected TableLayout printerInfoTable;

    protected TextView pdfSelectionStatus;
    protected TextView pdfInfoTableName;
    protected TextView pdfInfoTablePath;
    protected TableLayout pdfInfoTable;

    protected TextView printButton;

    protected DiscoveredPrinter chosenPrinter;
    protected String MacAddress;
    protected String filePath = null;
    protected Integer fileWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        printerSelectionStatus = (TextView) findViewById(R.id.printerSelectionStatus);
        printerInfoTableName = (TextView) findViewById(R.id.printerInfoTableName);
        printerInfoTableAddress = (TextView) findViewById(R.id.printerInfoTableAddress);
        printerInfoTable = (TableLayout) findViewById(R.id.printerInfoTable);

        pdfSelectionStatus = (TextView) findViewById(R.id.pdfSelectionStatus);
        pdfInfoTableName = (TextView) findViewById(R.id.pdfInfoTableName);
        pdfInfoTablePath = (TextView) findViewById(R.id.pdfInfoTablePath);
        pdfInfoTable = (TableLayout) findViewById(R.id.pdfInfoTable);


        TextView selectPrinterButton = (TextView) findViewById(R.id.selectPrinterButton);
        selectPrinterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DIALOG_PRINTER_CONNECT_TAG, "Select Printer button clicked");

                Dexter.withContext(MainActivity.this)
                        .withPermissions(
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            PrinterConnectionDialog printerConnectionDialog = (PrinterConnectionDialog) getFragmentManager().findFragmentByTag(DIALOG_PRINTER_CONNECT_TAG);

                            if (printerConnectionDialog == null) {
                                printerConnectionDialog = new PrinterConnectionDialog();
                                printerConnectionDialog.show(ft, DIALOG_PRINTER_CONNECT_TAG);
                            }
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            Toast.makeText(MainActivity.this, "permission is denied permenantly, navigate user to app settings",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
            }
        });

        TextView selectPDFButton = (TextView) findViewById(R.id.selectPDFButton);
        selectPDFButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DIALOG_PDF_PICK_TAG, "Select PDF button clicked");


                Dexter.withContext(MainActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                            }
                        }).check();

                FragmentTransaction ft = MainActivity.this.getFragmentManager().beginTransaction();
                PDFPick pdfPick = new PDFPick();
                pdfPick.show(ft, DIALOG_PDF_PICK_TAG);

            }
        });

        printButton = (TextView) findViewById(R.id.printNowButton);
        printButton.setOnClickListener(null);
    }

    public String MacAddress() {
        return MacAddress;
    }

    public String FilePath() {
        return filePath;
    }

    public Integer FileWidth() {
        return fileWidth;
    }

    public void displayConnectingStatus() {
        printerSelectionStatus.setText(getString(R.string.connecting_to_printer));
    }

    public void updatePrinterInfoTable(DiscoveredPrinter discoveredPrinter) {
        printerInfoTableName.setText(discoveredPrinter.getDiscoveryDataMap().get("SYSTEM_NAME"));
        printerInfoTableAddress.setText(discoveredPrinter.getDiscoveryDataMap().get("HARDWARE_ADDRESS"));
        printerInfoTable.setVisibility(View.VISIBLE);
        printerSelectionStatus.setText(getString(R.string.connected_to_printer));
        chosenPrinter = discoveredPrinter;

        MacAddress = discoveredPrinter.toString();
        updatePrintButton();
    }

    public void updatePDFInfoTable(String pdfName, String pdfPath) {
        pdfInfoTableName.setText(pdfName);
        pdfInfoTablePath.setText(pdfPath);
        pdfInfoTable.setVisibility(View.VISIBLE);
        pdfSelectionStatus.setText(getString(R.string.selected_a_pdf));

        filePath = pdfPath;
        updatePrintButton();
    }

    public void resetConnectingStatus() {
        printerInfoTable.setVisibility(View.GONE);
        printerSelectionStatus.setText(getString(R.string.no_printer_selected));
    }

    public void showSnackbar(String snackbarText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), snackbarText, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.near_black));
        TextView snackbarTextView = (TextView) snackbarView.findViewById(R.id.snackbar_text);
        snackbarTextView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void updatePrintButton() {
        if ((filePath != null) && (MacAddress != null)) {
            printButtonEnable();
        } else {
            printButtonDisable();
        }
    }

    public void printButtonEnable() {
        printButton = (TextView) findViewById(R.id.printNowButton);

        printButton.setBackgroundColor(ContextCompat.getColor(this, R.color.zebra_red));
        printButton.setText(R.string.print);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                PrintDialog printDialog = new PrintDialog();
                printDialog.show(ft, DIALOG_PRINT_TAG);
            }
        });
    }

    public void printButtonDisable() {
        printButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        printButton.setText(R.string.unable_to_print);
        printButton.setOnClickListener(null);
    }
}
