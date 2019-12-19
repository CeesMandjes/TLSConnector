package com.example.tlsconnector;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    //UI Fields properties
    private Spinner tlsVersionSpn;
    private final String[] tlsVersions = new String[]{"TLS 1.0", "TLS 1.1", "TLS 1.2"};
    private Spinner apiSpn;
    private final String[] apiNames = new String[] {"HttpURLConnection", "OKHttp"};
    private TextView tlsConnectionOutputTv;

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

        //Initialize button
        tlsConnectionOutputTv = findViewById(R.id.output_tls_connection_tv);
    }

    public void executeTLSConnection(View view)
    {
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

        String apiNameVal = apiSpn.getSelectedItem().toString();
        switch (apiNameVal) {
            case "HttpURLConnection":
                executeHttpURLConnection(tlsURL);
                break;
            case "OKHttp":
                executeOKHttp(tlsURL);
                break;
        }
    }

    public void executeHttpURLConnection(String url)
    {
        InputStream certificate = getResources().openRawResource(R.raw.tlsv12badsslcom);
        new HttpURLConnectionConnector(certificate, tlsConnectionOutputTv).execute(url);
    }

    public void executeOKHttp(String url)
    {
        String certificateHash = "sha256/9SLklscvzMYj8f+52lp5ze/hY0CFHyLSPQzSpYYIBm8=";
        String certificateDNWildcard = "*.badssl.com";
        new OKHttpConnector(certificateHash, certificateDNWildcard, tlsConnectionOutputTv).execute(url);
    }
}
