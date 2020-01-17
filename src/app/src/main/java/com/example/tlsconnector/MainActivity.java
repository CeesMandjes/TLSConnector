package com.example.tlsconnector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.UUID;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.InputStream;

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
        tlsConnectionOutputTv.setMovementMethod(new ScrollingMovementMethod());

        //Do Google Play service check
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this, 13000000) ==
                ConnectionResult.SUCCESS) {
            String current = tlsConnectionOutputTv.getText().toString();
            tlsConnectionOutputTv.setText("\n\nGoogle Play services \nSUCCESS: Device has Google Play service version 13+ installed\n" + current);
        } else {
            String current = tlsConnectionOutputTv.getText().toString();
            tlsConnectionOutputTv.setText("\n\nGoogle Play services \nERROR: Device does not have Google Play service version 13+ installed\n" + current);
        }

        //Get SafetyNet JWS
        final byte[] nonce = UUID.randomUUID().toString().getBytes();
        SafetyNet.getClient(this).attest(nonce, "AIzaSyBzxfDEPiyGfGZPb6JwyVumYeWrjTspnkU")
                .addOnSuccessListener(this,
                        new OnSuccessListener<SafetyNetApi.AttestationResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.AttestationResponse response) {
                                String current = tlsConnectionOutputTv.getText().toString();
                                tlsConnectionOutputTv.setText("\nSafetyNet \nSUCCESS: Google's response received\n" +
                                        "Nonce: " + Arrays.toString(nonce) + current);

                                OnlineVerify verifyObject = new OnlineVerify(tlsConnectionOutputTv);
                                verifyObject.execute(response.getJwsResult());
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // An error occurred while communicating with the service.
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            String current = tlsConnectionOutputTv.getText().toString();
                            tlsConnectionOutputTv.setText("\nSafetyNet \nERROR: " + apiException.toString() + "\n" + current);
                        } else {
                            String current = tlsConnectionOutputTv.getText().toString();
                            tlsConnectionOutputTv.setText("\nSafetyNet \nERROR: " + e.toString() + "\n" + current);
                        }
                    }
                });
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