package com.example.tlsconnector;

import android.app.Activity;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Performs the SafetyNet Attestation check process. This abstract class calls the following process steps in order: pin certificate,
 * request nonce, SafetyNet Attestation evaluation and send signed attestation. Only the order of the check process is implemented together
 * with the implementation of the SafetyNet Attestation evaluation. The rest of the process needs to be implemented by the class, which
 * implements a certificate pinning library, which extends this abstract class. Furthermore, this class also stores all the properties
 * which are required to perform the check process.
 *
 * @author Cees Mandjes
 */
public abstract class SafetyNetConnector extends AsyncTask<Void, Void, Void> {
    //Required properties to perform the SafetyNet Attestation process
    protected String tag;
    private String domainName;
    private String pathNonce;
    private String pathJWS;
    protected CertificateInformation certificate;
    private Activity context;
    protected IOutput output;

    /**
     * Sets all the required properties to perform the SafetyNet Attestation process.
     *
     * @param tag Certificate pinning library name
     * @param domainName Application's server domain name
     * @param certificate Certificate which needs to be pinned for the domain name
     * @param pathNonce Application's server path to request a nonce
     * @param pathJWS Application's server path to send the signed attestation
     * @param context Main thread used for the SafetyNet Attestation evaluation
     * @param output Output implementation for logs and errors
     */
    public SafetyNetConnector(String tag, String domainName, CertificateInformation certificate, String pathNonce, String pathJWS,
        Activity context, IOutput output)
    {
        this.tag = tag;
        this.domainName = domainName;
        this.pathNonce = pathNonce;
        this.pathJWS = pathJWS;
        this.certificate = certificate;
        this.context = context;
        this.output = output;
    }

    /**
     * Pins the certificate using a certificate pinning library using the given domain name and certificate information. The client with
     * the pinned object needs to be set in the parent class so that it can be used for request nonce and send signed attestation.
     *
     * @param domainName Domain name for which the certificate needs to be pinned
     * @param certificate Certificate information about the certificate which needs to be pinned
     */
    protected abstract void pinCertificate(String domainName, CertificateInformation certificate);

    /**
     * Requests a nonce from application's server using the given domain name and path which can be used to initiate the request.
     *
     * @param domainName Application's server domain name
     * @param pathNonce Application's server path to request a nonce
     * @return Nonce provided by application's server
     */
    protected abstract byte[] requestNonce(String domainName, String pathNonce);

    /**
     * Starts the SafetyNet Attestation evaluation and returns its result, called the signed attestation, in JWS format. The logs and/or
     * error are printed using the output module.
     *
     * @param nonce Nonce provided by application's server which is used to initiate the evaluation process
     * @return Signed attestation in JWS format
     */
    private String performSafetyNetEvaluation(byte[] nonce)
    {
        String safetyNetTag = "SafetyNet Attestation";
        String safetyNetAction = "SafetyNet check";
        try {
            if(nonce == null)
                throw new IOException("Provided nonce is null");

            //Get signed attestation from Google using the given nonce
            AndroidSafetyNet androidSafetyNet = new AndroidSafetyNet(context);
            String signedAttestation = androidSafetyNet.getSignedAttestation(nonce);
            output.printLog(safetyNetTag, safetyNetAction, "Signed Attestation received");

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

    /**
     * Sends the SafetyNet's signed attestation to application's server using the given domain name and path which can be used to initiate
     * the request.
     *
     * @param domainName Application's server domain name
     * @param pathJWS Application's server path to send the signed attestation
     * @param signedAttestation SafetyNet's signed attestation
     */
    protected abstract void sendSignedAttestation(String domainName, String pathJWS, String signedAttestation);

    /**
     * Calls the following process steps in order: pin certificate, request nonce, SafetyNet Attestation evaluation and send signed
     * attestation. This class only implements SafetyNet Attestation evaluation call; the rest needs to be implemented by the class, which
     * implements a certificate pinning library, which extends this abstract class.
     *
     * @param params Default params
     * @return Nothing
     */
    @Override
    protected Void doInBackground(Void... params) {
        pinCertificate(domainName, certificate);
        byte[] nonce = requestNonce(domainName, pathNonce);
        if(nonce != null) {
            String signedAttestation = performSafetyNetEvaluation(nonce);
            sendSignedAttestation(domainName, pathJWS, signedAttestation);
        }
        return null;
    }
}