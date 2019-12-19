package com.example.tlsconnector;

import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpConnector {

    private TextView output;
    private String certificateHash;
    private String certificateDNWildcard;

    public OKHttpConnector(String certificateHash, String certificateDNWildcard, TextView printResult)
    {
        this.certificateHash = certificateHash;
        this.certificateDNWildcard = certificateDNWildcard;
        this.output = printResult;
    }

    public void execute(final String url)
    {
        //Create certificate pinner object and set certificate hash
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(certificateDNWildcard, certificateHash)
                .build();

        //Create http client with pinning object
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .build();

       Request request = new Request.Builder()
                .url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                output.setText("ERROR: " + e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                output.setText("OKHttp \nURL: " + url + " - RESP: " + response.networkResponse().code());
            }
        });
    }
}