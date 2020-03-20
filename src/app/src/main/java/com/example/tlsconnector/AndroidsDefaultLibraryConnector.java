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

/**
 * Implements the abstract class SafetyNetConnector using Android's default library for certificate pinning; this class implements
 * functions for pin certificate, request nonce and send signed attestation. The SafetyNet Attestation evaluation and the order of
 * the SafetyNet Attestation check process is implemented in the parent class.
 *
 * @author Cees Mandjes
 */
public class AndroidsDefaultLibraryConnector extends SafetyNetConnector {
    //Session which contains the pinned certificate
    private SSLContext session;

    /**
     * Forwards all the required properties to perform the SafetyNet Attestation process to the SafetyNetConnector.
     *
     * @param domainName Application's server domain name
     * @param certificate Certificate which needs to be pinned for the domain name
     * @param pathNonce Application's server path to request a nonce
     * @param pathJWS Application's server path to send the signed attestation
     * @param context Main thread used for the SafetyNet Attestation evaluation
     * @param output Output implementation for logs and errors
     */
    public AndroidsDefaultLibraryConnector(String domainName, CertificateInformation certificate, String pathNonce, String pathJWS,
        Activity context, IOutput output)
    {
        super("Android's default library", domainName, certificate, pathNonce, pathJWS, context, output);
    }

    @Override
    protected void pinCertificate(String domainName, CertificateInformation certificate) {
        String action = "Pin certificate";
        try {
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

            //Create an SSLContext that uses our TrustManager and store it in the session
            session = SSLContext.getInstance("TLS");
            session.init(null, tmf.getTrustManagers(), null);

            output.printLog(tag, action, "Certificate for host " + certificate.wildcardDomainName + " is pinned");
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
    protected byte[] requestNonce(String domainName, String pathNonce) {
        String action = "Request nonce";
        try {
            URL getNonceUrl = new URL(domainName + pathNonce);
            HttpsURLConnection getNonceRequest =
                    (HttpsURLConnection) getNonceUrl.openConnection();
            //Use pinned certificate for request
            getNonceRequest.setSSLSocketFactory(session.getSocketFactory());
            String nonce = bfsToString(new BufferedInputStream(getNonceRequest.getInputStream()));
            output.printLog(tag, action, "URL: " + getNonceRequest.getURL().toString() + "\nPinned certificate: correct \nNonce:" + nonce);

            return nonce.getBytes();
        } catch (MalformedURLException e) {
            output.printError(tag, action,e.getMessage());
        } catch (IOException e) {
            output.printError(tag, action, e.getMessage());
        }
        return null;
    }

    @Override
    protected void sendSignedAttestation(String domainName, String pathJWS, String signedAttestation) {
        String action = "Send signed attestation";
        try {
            URL sendSafetyNetJWS = new URL(domainName + pathJWS);
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
            //Use pinned certificate for request
            validateJWSRequest.setSSLSocketFactory(session.getSocketFactory());
            validateJWSRequest.connect();
            output.printLog(tag, action , "URL: " + validateJWSRequest.getURL().toString() + "\nPinned certificate: correct \nResult: " + bfsToString(new BufferedInputStream(validateJWSRequest.getInputStream())));
        } catch (MalformedURLException e) {
            output.printLog(tag, action, e.getMessage());
        } catch (IOException e) {
            output.printLog(tag, action, e.getMessage());
        }
    }

    /**
     * Transfers a bufferedinputstream into a String.
     *
     * @param input bufferedinputstream input
     * @return String output
     */
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