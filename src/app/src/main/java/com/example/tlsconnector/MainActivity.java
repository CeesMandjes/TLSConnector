package com.example.tlsconnector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
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
    private String url = "https://google.com";

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
        new Connector(textView).execute("https://revoked.badssl.com/");
    }

    public void okHttp(View view)
    {
        textView = findViewById(R.id.conn_details_tv);
        okHttpBtn = findViewById(R.id.okhttp_btn);

        okHttpClient = new OkHttpClient();
        request = new Request.Builder()
                .url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                textView.setText(e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                textView.setText(response.body().string());
            }
        });
    }
}
