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

public class MainActivity extends AppCompatActivity {

    //UI Fields properties
    private final String[] tlsVersions = new String[]{"TLS 1.0", "TLS 1.1", "TLS 1.2"};
    private Spinner apiSpn;
    private final String[] apiNames = new String[] {"HttpURLConnection", "OKHttp"};
    private TextView tlsConnectionOutputTv;
    private CheckBox pinCorrectCertificateCb;

    //TLS URLS config
    private final String url = "https://cees.nwlab.nl/index.php/";

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
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this, 13000000) ==
                ConnectionResult.SUCCESS) {
            addTextToOutputUI(googlePlayServiceTag, "SUCCESS: Device has Google Play service version 13+ installed");
        } else {
            addTextToOutputUI(googlePlayServiceTag, "ERROR: Device does not have Google Play service version 13+ installed");
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
            case "HttpURLConnection":
                executeHttpURLConnection(certificateInformation, url);
                break;
            case "OKHttp":
                executeOKHttp(certificateInformation, url);
                break;
        }
    }

    public void addTextToOutputUI(String tag, String value)
    {
        String current = tlsConnectionOutputTv.getText().toString();
        tlsConnectionOutputTv.setText(current + "\n\n" + tag + "\n" + value );
    }

    public void addErrorToOutputUI(String tag, String value)
    {
        addTextToOutputUI(tag, "Error: " + value);
    }

    public void executeHttpURLConnection(CertificateInformation certificate, String url)
    {
        new HttpURLConnectionConnector(certificate.file, tlsConnectionOutputTv, this).execute(url);
    }

    public void executeOKHttp(CertificateInformation certificate, String url)
    {
        new OKHttpConnector(certificate.hash, certificate.wildcardDomainName, tlsConnectionOutputTv).execute(url);
    }

    private final class CeesNWLabCertificate extends CertificateInformation{
        public CeesNWLabCertificate(InputStream file)
        {
            super(
                file,
                "sha256/aIuSVfYXa9EUIbVv7mS5AhiUTE4+UQ3+blpu0QOP2+I=",
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