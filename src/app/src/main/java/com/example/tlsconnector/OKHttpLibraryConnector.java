package com.example.tlsconnector;

import android.app.Activity;
import java.io.IOException;
import okhttp3.CertificatePinner;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Implements the abstract class SafetyNetConnector using the OkHttp library for certificate pinning; this class implements functions
 * for pin certificate, request nonce and send signed attestation. The SafetyNet Attestation evaluation and the order of the SafetyNet
 * Attestation check process is implemented in the parent class.
 *
 * @author Cees Mandjes
 */
public class OKHttpLibraryConnector extends SafetyNetConnector {

    OkHttpClient session;

    public OKHttpLibraryConnector(String domainName, CertificateInformation certificate, String pathNonce, String pathJWS,
        Activity context, IOutput output)
    {
        super("OKHttp library", domainName, certificate, pathNonce, pathJWS, context, output);
    }

    @Override
    protected void pinCertificate(String domainName, CertificateInformation certificate) {
        String action = "Pin certificate";
        //Pin certificate by its domain name and certificate's hash
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
            .add(certificate.wildcardDomainName, certificate.hash)
            .build();
        //Create http client with pinned certificate and store it as a session
        session = new OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .build();

        output.printLog(tag, action, "Certificate for host " + certificate.wildcardDomainName + " is pinned");
    }

    @Override
    protected byte[] requestNonce(String domainName, String pathNonce) {
        String action = "Request nonce";
        try {
            Request getNonceRequest = new Request.Builder()
                    .url(domainName + pathNonce).build();
            //Use pinned certificate for request
            Response getNonceResponse = session.newCall(getNonceRequest).execute();
            String nonce = getNonceResponse.body().string();
            output.printLog(tag, action, "URL: " + getNonceRequest.url().toString() + "\nPinned certificate: correct \nNonce:" + nonce);

            return nonce.getBytes();
        }
        catch (IOException e)
        {
            output.printError(tag, action, e.getMessage());
        }

        return null;
    }

    @Override
    protected void sendSignedAttestation(String domainName, String pathJWS, String signedAttestation) {
        String action = "Send signed attestation";
        try {
            //Set signed attestation in POST body
            RequestBody postRequestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("jws", signedAttestation)
                    .build();
            //Set POST body and do request
            Request validateJWSRequest = new Request.Builder()
                    .url(domainName + pathJWS)
                    .post(postRequestBody)
                    .build();
            //Use pinned certificate for request
            Response validateJWSResponse = session.newCall(validateJWSRequest).execute();
            String result = validateJWSResponse.body().string();
            output.printLog(tag, action, "URL: " + validateJWSRequest.url().toString() + "\nPinned certificate: correct \nResult: " + result);
        }
        catch (IOException e)
        {
            output.printError(tag, action, e.getMessage());
        }
    }
}