package com.example.tlsconnector;

import android.app.Activity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class AndroidsDefaultLibraryConnector extends SafetyNetConnector {

    private SSLContext session;

    public AndroidsDefaultLibraryConnector(String baseURL, CertificateInformation certificate, Activity context, IOutput output)
    {
        super("Android's default library", baseURL, certificate, context, output);
    }

    @Override
    protected void pinCertificate(String baseURL, CertificateInformation certificate) {
        String action = "Pin certificate";
        try {
            //** Pin certificate **
            //Load given certificate into certificate factory and create certificate object
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(certificate.file);

            //Create a local KeyStore containing the given certificate
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            //Create a TrustManager that trusts the CAs in the local KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            session = SSLContext.getInstance("TLS");
            session.init(null, tmf.getTrustManagers(), null);

            output.printText(tag, action, "Certificate for host " + certificate.wildcardDomainName + " is pinned");
        } catch (IOException e) {
            output.printError(tag, action, e.getMessage());
        } catch (CertificateException e) {
            output.printError(tag, action, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            output.printError(tag, action, e.getMessage());
        } catch (KeyStoreException e) {
            output.printError(tag, action, e.getMessage());
        } catch (KeyManagementException e) {
            output.printError(tag, action, e.getMessage());
        }
    }

    @Override
    protected byte[] requestNonce(String baseURL) {
        String action = "Request nonce";
        try {
            //** Get nonce from server **
            URL getNonceUrl = new URL(baseURL + "/index.php/api/getnonce");
            HttpsURLConnection getNonceRequest =
                    (HttpsURLConnection) getNonceUrl.openConnection();
            //Use pinned certificate
            getNonceRequest.setSSLSocketFactory(session.getSocketFactory());
            String nonce = bfsToString(new BufferedInputStream(getNonceRequest.getInputStream()));
            output.printText(tag, action, "URL: " + getNonceRequest.getURL().toString() + "\nPinned certificate: correct \nNonce:" + nonce);

            return nonce.getBytes();
        } catch (MalformedURLException e) {
            output.printError(tag, action,e.getMessage());
        } catch (IOException e) {
            output.printError(tag, action, e.getMessage());
        }
        return null;
    }

    @Override
    protected void sendSignedAttestation(String baseURL, String signedAttestation) {
        String action = "Send signed attestation";
        try {
            //** Forward signed attestation to server **
            URL sendSafetyNetJWS = new URL(baseURL + "/index.php/api/validatejws");
            HttpsURLConnection validateJWSRequest =
                    (HttpsURLConnection) sendSafetyNetJWS.openConnection();
            //Set signed attestation in POST request
            validateJWSRequest.setDoOutput(true);
            validateJWSRequest.setRequestMethod("POST");
            validateJWSRequest.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String postParameters = "jws=" + signedAttestation;
            validateJWSRequest.setFixedLengthStreamingMode(postParameters.getBytes().length);
            PrintWriter out = new PrintWriter(validateJWSRequest.getOutputStream());
            out.print(postParameters);
            out.close();
            //Use pinned certificate
            validateJWSRequest.setSSLSocketFactory(session.getSocketFactory());
            validateJWSRequest.connect();
            output.printText(tag, action , "URL: " + validateJWSRequest.getURL().toString() + "\nPinned certificate: correct \nResult: " + bfsToString(new BufferedInputStream(validateJWSRequest.getInputStream())));
        } catch (MalformedURLException e) {
            output.printText(tag, action, e.getMessage());
        } catch (IOException e) {
            output.printText(tag, action, e.getMessage());
        }
    }

    private String bfsToString(BufferedInputStream input)
    {
        String returnVal = "";
        try{
            int i;
            while((i = input.read()) != -1) {
                char c = (char) i;
                returnVal = returnVal + c;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnVal;
    }
}