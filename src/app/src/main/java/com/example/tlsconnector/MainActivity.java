package com.example.tlsconnector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.io.PushbackInputStream;

public class MainActivity extends AppCompatActivity {

    //UI Fields properties
    private Spinner tlsVersionSpn;
    private final String[] tlsVersions = new String[]{"TLS 1.0", "TLS 1.1", "TLS 1.2"};
    private Spinner apiSpn;
    private final String[] apiNames = new String[] {"HttpURLConnection", "OKHttp"};
    private TextView tlsConnectionOutputTv;
    private CheckBox pinCorrectCertificateCb;

    //TLS URLS config
    private final String[] tlsURLS = new String[] {"https://tls-v1-0.badssl.com", "https://tls-v1-1.badssl.com", "https://tls-v1-2.badssl.com"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize TLS version spinner
        tlsVersionSpn = findViewById(R.id.tls_version_spn);
        ArrayAdapter<String> TLSVersionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, tlsVersions);
        tlsVersionSpn.setAdapter(TLSVersionAdapter);

        //Initialize API names
        apiSpn = findViewById(R.id.tls_API_spn);
        ArrayAdapter<String> APIAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, apiNames);
        apiSpn.setAdapter(APIAdapter);

        //Initialize checkbox for correct certificate insertion
        pinCorrectCertificateCb = findViewById(R.id.pin_correct_certificate_cb);

        //Initialize output field
        tlsConnectionOutputTv = findViewById(R.id.output_tls_connection_tv);
    }

    public void executeTLSConnection(View view)
    {
        //Initialize certificates
        CertificateInformation badSSLCertificate = new BadSSLCertificate(getResources().openRawResource(R.raw.tlsv12badsslcom));
        CertificateInformation incorrectCertificate = new IncorrectCertificate(getResources().openRawResource(R.raw.nunl));

        //Initialize (correct) certificate to pin for connection
        CertificateInformation certificateInformation;
        if(pinCorrectCertificateCb.isChecked())
            certificateInformation = badSSLCertificate;
        else
            certificateInformation = incorrectCertificate;

        //Initialize URL for connection based in user's input
        String tlsURL;
        String tlsVersionVal = tlsVersionSpn.getSelectedItem().toString();
        switch (tlsVersionVal) {
            case "TLS 1.0":
                tlsURL = tlsURLS[0];
                break;
            case "TLS 1.1":
                tlsURL = tlsURLS[1];
                break;
            case "TLS 1.2":
                tlsURL = tlsURLS[2];
                break;
            default:
                tlsURL = tlsURLS[0];
                break;
        }

        //Initialize API for connection based on user's input
        String tlsAPI;
        String apiNameVal = apiSpn.getSelectedItem().toString();
        switch (apiNameVal) {
            case "HttpURLConnection":
                executeHttpURLConnection(certificateInformation, tlsURL);
                break;
            case "OKHttp":
                executeOKHttp(certificateInformation, tlsURL);
                break;
        }
    }

    public void executeHttpURLConnection(CertificateInformation certificate, String url)
    {
        new HttpURLConnectionConnector(certificate.file, tlsConnectionOutputTv).execute(url);
    }

    public void executeOKHttp(CertificateInformation certificate, String url)
    {
        new OKHttpConnector(certificate.hash, certificate.wildcardDomainName, tlsConnectionOutputTv).execute(url);
    }

    private final class BadSSLCertificate extends CertificateInformation{
        public BadSSLCertificate(InputStream file)
        {
            super(
                file,
                "sha256/9SLklscvzMYj8f+52lp5ze/hY0CFHyLSPQzSpYYIBm8=",
                "*.badssl.com"
            );
        }
    }

    private final class IncorrectCertificate extends CertificateInformation{
        public IncorrectCertificate(InputStream file)
        {
            super(
                file,
                "sha256/1OLklscvzMYj8f888lp5ze/hY0CFHyLSPQzSpYYIBm8=",
                "*.badssl.com"
            );
        }
    }
}