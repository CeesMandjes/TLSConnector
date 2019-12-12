package com.example.tlsconnector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private TextView textView;
    private String[] tlsVersions = new String[]{"TLS 1.0", "TLS 1.1", "TLS 1.2"};

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
        textView = findViewById(R.id.conn_details_tv);
        new Connector(textView).execute("https://google.com/");
    }
}
