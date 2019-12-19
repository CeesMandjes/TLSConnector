package com.example.tlsconnector;

import android.os.AsyncTask;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
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

public class HttpURLConnectionConnector extends AsyncTask<String, String, String> {

    private TextView output;
    private InputStream certificate;

    public HttpURLConnectionConnector(InputStream certificate, TextView printResult)
    {
        this.certificate = certificate;
        this.output = printResult;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
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
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(urls[0]);
            HttpsURLConnection urlConnection =
                    (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());

            //Get response code and set it in output field
            return "HttpURLConnection \nURL: " + urls[0] + " - RESP: " + urlConnection.getResponseCode();

        } catch (CertificateException e) {
            return  "ERROR: " + e.toString();
        } catch (MalformedURLException e) {
            return  "ERROR: " + e.toString();
        } catch (IOException e) {
            return  "ERROR: " + e.toString();
        } catch (NoSuchAlgorithmException e) {
            return  "ERROR: " + e.toString();
        } catch (KeyStoreException e) {
            return  "ERROR: " + e.toString();
        } catch (KeyManagementException e) {
            return  "ERROR: " + e.toString();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        output.setText(result);
    }
}