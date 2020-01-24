package com.example.tlsconnector;

import android.os.AsyncTask;
import android.widget.TextView;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Key;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Sample code to verify the device attestation statement online.
 * You must add your API key for the Android Device Verification API here ({@link #API_KEY}),
 * otherwise all requests will fail.
 */
public class OnlineVerify extends AsyncTask<String, String, AttestationStatement> {

    // Please use the Google Developers Console (https://console.developers.google.com/)
    // to create a project, enable the Android Device Verification API, generate an API key
    // and add it here.
    private static final String API_KEY = "AIzaSyBzxfDEPiyGfGZPb6JwyVumYeWrjTspnkU";

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String URL =
            "https://www.googleapis.com/androidcheck/v1/attestations/verify?key="
                    + API_KEY;

    private static TextView output;

    public OnlineVerify(TextView printResult)
    {
        this.output = printResult;
    }

    /**
     * Class for parsing JSON data.
     */
    public static class VerificationRequest {
        public VerificationRequest(String signedAttestation) {
            this.signedAttestation = signedAttestation;
        }

        @Key
        public String signedAttestation;
    }

    /**
     * Class for parsing JSON data.
     */
    public static class VerificationResponse {
        @Key
        public boolean isValidSignature;

        /**
         * Optional field that is only set when the server encountered an error processing the
         * request.
         */
        @Key
        public String error;
    }

    private static VerificationResponse onlineVerify(VerificationRequest request) {
        // Prepare a request to the Device Verification API and set a parser for JSON data.
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        GenericUrl url = new GenericUrl(URL);
        HttpRequest httpRequest;
        try {
            // Post the request with the verification statement to the API.
            httpRequest = requestFactory.buildPostRequest(url, new JsonHttpContent(JSON_FACTORY,
                    request));
            // Parse the returned data as a verification response.
            return httpRequest.execute().parseAs(VerificationResponse.class);
        } catch (IOException e) {
            System.err.println(
                    "Failure: Network error while connecting to the Google Service " + URL + ".");
            System.err.println("Ensure that you added your API key and enabled the Android device "
                    + "verification API.");
            return null;
        }
    }

    /**
     * Extracts the data part from a JWS signature.
     */
    private static byte[] extractJwsData(String jws) {
        // The format of a JWS is:
        // <Base64url encoded header>.<Base64url encoded JSON data>.<Base64url encoded signature>
        // Split the JWS into the 3 parts and return the JSON data part.
        String[] parts = jws.split("[.]");
        if (parts.length != 3) {
            System.err.println("Failure: Illegal JWS signature format. The JWS consists of "
                    + parts.length + " parts instead of 3.");
            return null;
        }
        return Base64.decodeBase64(parts[1]);
    }

    private static AttestationStatement parseAndVerify(String signedAttestationStatment) {
        // Send the signed attestation statement to the API for verification.
        VerificationRequest request = new VerificationRequest(signedAttestationStatment);
        VerificationResponse response = onlineVerify(request);
        if (response == null) {
            return null;
        }

        if (response.error != null) {
            System.err.println(
                    "Failure: The API encountered an error processing this request: "
                            + response.error);
            return null;
        }

        if (!response.isValidSignature) {
            System.err.println(
                    "Failure: The cryptographic signature of the attestation statement couldn't be "
                            + "verified.");
            return null;
        }

        System.out.println("Sucessfully verified the signature of the attestation statement.");

        // The signature is valid, extract the data JSON from the JWS signature.
        byte[] data = extractJwsData(signedAttestationStatment);

        // Parse and use the data JSON.
        try {
            return JSON_FACTORY.fromInputStream(new ByteArrayInputStream(data),
                    AttestationStatement.class);
        } catch (IOException e) {
            System.err.println("Failure: Failed to parse the data portion of the JWS as valid " +
                    "JSON.");
            return null;
        }
    }

    private static AttestationStatement process(String signedAttestationStatement) {
        AttestationStatement stmt = parseAndVerify(signedAttestationStatement);
        if (stmt == null) {
            System.err.println("Failure: Failed to parse and verify the attestation statement.");
            return null;
        }

        System.out.println("The content of the attestation statement is:");

        // Nonce that was submitted as part of this request.
        System.out.println("Nonce: " + Arrays.toString(stmt.getNonce()));
        // Timestamp of the request.
        System.out.println("Timestamp: " + stmt.getTimestampMs() + " ms");

        if (stmt.getApkPackageName() != null && stmt.getApkDigestSha256() != null) {
            // Package name and digest of APK that submitted this request. Note that these details
            // may be omitted if the API cannot reliably determine the package information.
            System.out.println("APK package name: " + stmt.getApkPackageName());
            System.out.println("APK digest SHA256: " + Arrays.toString(stmt.getApkDigestSha256()));
        }
        // Has the device a matching CTS profile?
        System.out.println("CTS profile match: " + stmt.isCtsProfileMatch());
        // Has the device passed CTS (but the profile could not be verified on the server)?
        System.out.println("Basic integrity match: " + stmt.hasBasicIntegrity());

        System.out.println("\n** This sample only shows how to verify the authenticity of an "
                + "attestation response. Next, you must check that the server response matches the "
                + "request by comparing the nonce, package name, timestamp and digest.");

        return stmt;
    }

    @Override
    protected AttestationStatement doInBackground(String... signedAttestationStatement) {
       return process(signedAttestationStatement[0]);
    }

    @Override
    protected void onPostExecute(AttestationStatement result) {
        super.onPostExecute(result);
        String current = output.getText().toString();
        String printResult = "\nSafetyNet attestations result \n";
        printResult = printResult + "Nonce: " + Arrays.toString(result.getNonce()) + "\n";
        printResult = printResult + "Timestamp: " + result.getTimestampMs() + "\n";
        if (result.getApkPackageName() != null) {
            printResult = printResult + "APK package name: " + result.getApkPackageName() + "\n";

        }
        if(result.getApkDigestSha256() != null)
        {
            printResult = printResult + "APK digest SHA256: " + result.getApkDigestSha256() + "\n";
        }
        printResult = printResult + "CTS profile match: " + result.isCtsProfileMatch() + "\n";
        printResult = printResult + "Basic integrity match: " + result.hasBasicIntegrity() + "\n";
        output.setText(printResult + "\n" + current);
    }
}