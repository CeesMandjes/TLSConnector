package com.example.tlsconnector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private TextView textView;
    private String[] tlsVersions = new String[]{"TLS 1.0", "TLS 1.1", "TLS 1.2"};


    private Button okHttpBtn;
    private OkHttpClient okHttpClient;
    private Request request;
    private String url = "https://tls-v1-2.badssl.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.tls_version_spin);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,tlsVersions);
        spinner.setAdapter(adapter);
    }

    public void performTLSHandshake(View view)
    {
        InputStream certificate = getResources().openRawResource(R.raw.tlsv12badsslcom);

        textView = findViewById(R.id.conn_details_tv);
        new Connector(certificate, textView).execute("https://google.com");
    }

    public void okHttp(View view)
    {
        textView = findViewById(R.id.conn_details_tv);
        okHttpBtn = findViewById(R.id.okhttp_btn);


        //Init pinning
        String hostname = "tls-v1-2.badssl.com";
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(hostname, "sha256/9SLklscvzMYj8f+52lp5ze/hY0CFHyLSPQzSpYYIBm8=")
                .build();

        //Create http client with pinning object
        okHttpClient = new OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .build();


        request = new Request.Builder()
                .url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println(e.toString());
                textView.setText(e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                textView.setText(response.body().string());
            }
        });
    }
}
