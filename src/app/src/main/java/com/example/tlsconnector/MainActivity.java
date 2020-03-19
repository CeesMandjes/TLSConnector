package com.example.tlsconnector;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements IOutput {

    //UI Fields properties
    private Spinner apiSpn;
    private final String[] apiNames = new String[] {"Android's default library", "OKHttp library"};
    private TextView tlsConnectionOutputTv;
    private CheckBox pinCorrectCertificateCb;

    //TLS URLS config
    private final String url = "https://cees.nwlab.nl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize API names
        apiSpn = findViewById(R.id.tls_API_spn);
        ArrayAdapter<String> APIAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, apiNames);
        apiSpn.setAdapter(APIAdapter);

        //Initialize checkbox for correct certificate insertion
        pinCorrectCertificateCb = findViewById(R.id.pin_correct_certificate_cb);

        //Initialize output field
        tlsConnectionOutputTv = findViewById(R.id.output_tls_connection_tv);
        tlsConnectionOutputTv.setMovementMethod(new ScrollingMovementMethod());

        //Do Google Play service check
        final String googlePlayServiceTag = "Google play services";
        final String googlePlayServiceAction = "Availability and version check";
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this, 13000000) ==
                ConnectionResult.SUCCESS) {
            printText(googlePlayServiceTag, googlePlayServiceAction, "Success: Device has Google Play service version 13+ installed");
        } else {
            printError(googlePlayServiceTag, googlePlayServiceAction, "Device does not have Google Play service version 13+ installed");
        }
    }

    public void executeTLSConnection(View view)
    {
        //Initialize certificates
        CertificateInformation badSSLCertificate = new CeesNWLabCertificate(getResources().openRawResource(R.raw.ceesnwlab));
        CertificateInformation incorrectCertificate = new IncorrectCertificate(getResources().openRawResource(R.raw.nunl));

        //Initialize (correct) certificate to pin for connection
        CertificateInformation certificateInformation;
        if(pinCorrectCertificateCb.isChecked())
            certificateInformation = badSSLCertificate;
        else
            certificateInformation = incorrectCertificate;

        //Initialize API for connection based on user's input
        String apiNameVal = apiSpn.getSelectedItem().toString();
        switch (apiNameVal) {
            case "Android's default library":
                executeHttpURLConnection(url, certificateInformation);
                break;
            case "OKHttp library":
                executeOKHttp(url, certificateInformation);
                break;
        }
    }

    public void executeHttpURLConnection(String baseURL, CertificateInformation certificate)
    {
        new AndroidsDefaultLibraryConnector(baseURL, certificate, this, this).execute();
    }

    public void executeOKHttp(String baseURL, CertificateInformation certificate)
    {
        new OKHttpLibraryConnector(baseURL, certificate, this, this).execute();
    }

    public void printText(String tag, String action, String value)
    {
        String current = tlsConnectionOutputTv.getText().toString();
        tlsConnectionOutputTv.setText(current + "\n\n" + tag +  " - " + action + "\n" + value );
    }

    public void printError(String tag, String action, String value)
    {
        printText(tag, action , "Error: " + value);
    }

    private final class CeesNWLabCertificate extends CertificateInformation{
        public CeesNWLabCertificate(InputStream file)
        {
            super(
                file,
                "sha256/PUI0MHiv1VYRDKQAhUU72iatxZb+NYiBHNVMlOOiz8c=",
                "cees.nwlab.nl"
            );
        }
    }

    private final class IncorrectCertificate extends CertificateInformation{
        public IncorrectCertificate(InputStream file)
        {
            super(
                file,
                "sha256/1OLklscvzMYj8f888lp5ze/hY0CFHyLSPQzSpYYIBm8=",
                "cees.nwlab.nl"
            );
        }
    }
}