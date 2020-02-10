package com.example.tlsconnector;

import android.os.AsyncTask;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.ExecutionException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class HttpURLConnectionConnector extends AsyncTask<String, Void, Void> {

    private TextView output;
    private InputStream certificate;
    private MainActivity mainContext;
    private final String TAG = "HttpURLConnection";
    private SSLContext session;

    public HttpURLConnectionConnector(InputStream certificate, TextView printResult, MainActivity context)
    {
        this.certificate = certificate;
        this.output = printResult;
        this.mainContext = context;
    }

    @Override
    protected Void doInBackground(final String... urls) {
        try
        {
            //** Pin certificate **
            //Load given certificate into certificate factory and create certificate object
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(certificate);

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

            //** Get nonce from server **
            URL getNonceUrl = new URL(urls[0] + "api/getnonce");
            HttpsURLConnection getNonceRequest =
                    (HttpsURLConnection) getNonceUrl.openConnection();
            //Use pinned certificate
            getNonceRequest.setSSLSocketFactory(session.getSocketFactory());
            String nonce = returnVal(new BufferedInputStream(getNonceRequest.getInputStream()));
            mainContext.addTextToOutputUI(TAG , "URL: " + getNonceRequest.getURL().toString() + "\nPinned certificate: correct \nNonce:" + nonce);

            //** Get signed attestation from Google **
            AndroidSafetyNet androidSafetyNet = new AndroidSafetyNet(mainContext);
            //Do request with given nonce
            String signedAttestation = androidSafetyNet.getJws(nonce.getBytes());
            mainContext.addTextToOutputUI("Google SafetyNet" , "JWS response received");

            //** Forward signed attestation to server **
            URL sendSafetyNetJWS = new URL(urls[0] + "api/validatejws");
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
            mainContext.addTextToOutputUI(TAG , "URL: " + validateJWSRequest.getURL().toString() + "\nResult: " + returnVal(new BufferedInputStream(validateJWSRequest.getInputStream())));

        } catch (CertificateException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (MalformedURLException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (IOException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (KeyStoreException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (KeyManagementException e) {
            mainContext.addErrorToOutputUI(TAG, e.getMessage());
        } catch (InterruptedException e) {
            mainContext.addErrorToOutputUI(TAG, e.toString());
        } catch (ExecutionException e) {
            mainContext.addErrorToOutputUI(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    private String returnVal(BufferedInputStream input)
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