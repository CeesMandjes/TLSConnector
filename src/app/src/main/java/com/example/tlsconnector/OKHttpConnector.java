package com.example.tlsconnector;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.CertificatePinner;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttpConnector extends AsyncTask<String, Void, Void> {

    private TextView output;
    private String certificateHash;
    private String certificateDNWildcard;
    private MainActivity mainContext;
    private final String TAG = "OkHttp";

    public OKHttpConnector(String certificateHash, String certificateDNWildcard, TextView printResult, MainActivity context)
    {
        this.certificateHash = certificateHash;
        this.certificateDNWildcard = certificateDNWildcard;
        this.output = printResult;
        this.mainContext = context;
    }

    @Override
    protected Void doInBackground(final String... urls) {
        try {
            //** Pin certificate **
            //Pin certificate by its domain name and certificate's hash
            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add(certificateDNWildcard, certificateHash)
                    .build();
            //Create http client with pinned certificate
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .certificatePinner(certificatePinner)
                    .build();

            //** Get nonce from server **
            Request getNonceRequest = new Request.Builder()
                    .url(urls[0] + "api/getnonce").build();
            //Use pinned certificate
            Response getNonceResponse = okHttpClient.newCall(getNonceRequest).execute();
            String nonce = getNonceResponse.body().string();
            mainContext.addTextToOutputUI(TAG, "URL: " + getNonceRequest.url().toString() + "\nPinned certificate: correct \nNonce:" + nonce);

            //** Get signed attestation from Google **
            AndroidSafetyNet androidSafetyNet = new AndroidSafetyNet(mainContext);
            //Do request with given nonce
            String signedAttestation = androidSafetyNet.getJws(nonce.getBytes());
            mainContext.addTextToOutputUI("Google SafetyNet", "JWS response received");

            //** Forward signed attestation to server **
            //Set signed attestation in POST body
            RequestBody postRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("jws", signedAttestation)
                    .build();
            //Set POST body and do request
            Request validateJWSRequest = new Request.Builder()
                    .url(urls[0] + "api/validatejws")
                    .post(postRequestBody)
                    .build();
            //Use pinned certificate
            Response validateJWSResponse = okHttpClient.newCall(validateJWSRequest).execute();
            String result = validateJWSResponse.body().string();
            mainContext.addTextToOutputUI(TAG , "URL: " + validateJWSRequest.url().toString() + "\nPinned certificate: correct \nResult: " + result);

        }catch (IOException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (InterruptedException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (ExecutionException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } finally {

        }
        return null;
    }
}