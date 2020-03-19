package com.example.tlsconnector;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public abstract class SafetyNetConnector extends AsyncTask<Void, Void, Void> {

    protected String tag;
    private String baseURL;
    protected CertificateInformation certificate;
    private Activity context;
    protected IOutput output;

    public SafetyNetConnector(String tag, String baseURL, CertificateInformation certificate, Activity context, IOutput output)
    {
        this.tag = tag;
        this.baseURL = baseURL;
        this.certificate = certificate;
        this.context = context;
        this.output = output;
    }

    protected abstract void pinCertificate(String baseURL, CertificateInformation certificate);
    protected abstract byte[] requestNonce(String baseURL);


    private String performSafetyNetCheck(byte[] nonce)
    {
        String safetyNetTag = "SafetyNet Attestation";
        String safetyNetAction = "SafetyNet check";
        try {
            if(nonce == null)
                throw new IOException("Provided nonce is null");

            //** Get signed attestation from Google **
            AndroidSafetyNet androidSafetyNet = new AndroidSafetyNet(context);
            //Do request with given nonce
            String signedAttestation = androidSafetyNet.getJws(nonce);
            output.printText(safetyNetTag, safetyNetAction, "Signed Attestation received");

            return signedAttestation;
        } catch (ExecutionException e) {
            output.printError(safetyNetTag, safetyNetAction, e.getMessage());
        } catch (InterruptedException e) {
            output.printError(safetyNetTag, safetyNetAction, e.getMessage());
        } catch (IOException e) {
            output.printError(safetyNetTag, safetyNetAction, e.getMessage());
        }

        return null;
    }

    protected abstract void sendSignedAttestation(String baseURL, String signedAttestation);

    @Override
    protected Void doInBackground(Void... params) {
        pinCertificate(baseURL, certificate);
        byte[] nonce = requestNonce(baseURL);
        if(nonce != null) {
            String signedAttestation = performSafetyNetCheck(nonce);
            sendSignedAttestation(baseURL, signedAttestation);
        }
        return null;
    }
}