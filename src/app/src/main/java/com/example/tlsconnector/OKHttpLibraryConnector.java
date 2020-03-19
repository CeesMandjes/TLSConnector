package com.example.tlsconnector;

import android.app.Activity;
import java.io.IOException;
import okhttp3.CertificatePinner;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OKHttpLibraryConnector extends SafetyNetConnector {

    OkHttpClient session;

    public OKHttpLibraryConnector(String baseURL, CertificateInformation certificate, Activity context, IOutput output)
    {
        super("OKHttp library", baseURL, certificate, context, output);
    }

    @Override
    protected void pinCertificate(String baseURL, CertificateInformation certificate) {
        String action = "Pin certificate";

        //** Pin certificate **
        //Pin certificate by its domain name and certificate's hash
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
            .add(certificate.wildcardDomainName, certificate.hash)
            .build();
        //Create http client with pinned certificate
        session = new OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .build();

        output.printText(tag, action, "Certificate for host " + certificate.wildcardDomainName + " is pinned");
    }

    @Override
    protected byte[] requestNonce(String baseURL) {
        String action = "Request nonce";
        try {
            //** Get nonce from server **
            Request getNonceRequest = new Request.Builder()
                    .url(baseURL + "/index.php/api/getnonce").build();
            //Use pinned certificate
            Response getNonceResponse = session.newCall(getNonceRequest).execute();
            String nonce = getNonceResponse.body().string();
            output.printText(tag, action, "URL: " + getNonceRequest.url().toString() + "\nPinned certificate: correct \nNonce:" + nonce);

            return nonce.getBytes();
        }
        catch (IOException e)
        {
            output.printError(tag, action, e.getMessage());
        }

        return null;
    }

    @Override
    protected void sendSignedAttestation(String baseURL, String signedAttestation) {
        String action = "Send signed attestation";
        try {
            //** Forward signed attestation to server **
            //Set signed attestation in POST body
            RequestBody postRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("jws", signedAttestation)
                    .build();
            //Set POST body and do request
            Request validateJWSRequest = new Request.Builder()
                    .url(baseURL + "/index.php/api/validatejws")
                    .post(postRequestBody)
                    .build();
            //Use pinned certificate
            Response validateJWSResponse = session.newCall(validateJWSRequest).execute();
            String result = validateJWSResponse.body().string();
            output.printText(tag, action, "URL: " + validateJWSRequest.url().toString() + "\nPinned certificate: correct \nResult: " + result);
        }
        catch (IOException e)
        {
            output.printError(tag, action, e.getMessage());
        }
    }
}