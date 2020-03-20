package com.example.tlsconnector;

import android.app.Activity;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.concurrent.ExecutionException;

/**
 * Starts SafetyNet Attestation evaluation with its API key and the provided nonce.
 *
 * @author Cees Mandjes
 */
public class AndroidSafetyNet {
    //Main thread
    private Activity context;
    //API config
    private final String API_KEY = "AIzaSyBzxfDEPiyGfGZPb6JwyVumYeWrjTspnkU";

    /**
     * Initializes the main thread which is used to perform the SafetyNet Attestation check.
     *
     * @param context Main thread used to initialize the SafetyNet client
     */
    public AndroidSafetyNet(Activity context) {
        this.context = context;
    }

    /**
     * Starts the SafetyNet Attestation evaluation and returns its result, called the signed attestation, in JWS format.
     *
     * @param nonce Nonce used to start SafetyNet Attestation evaluation process
     * @return Signed attestation in JWS format
     * @throws ExecutionException Google's error
     * @throws InterruptedException Thread error
     */
    public String getSignedAttestation(byte[] nonce) throws ExecutionException, InterruptedException {
        Task<SafetyNetApi.AttestationResponse> responseTask = SafetyNet.getClient(context).attest(nonce, API_KEY);
        SafetyNetApi.AttestationResponse responseValue = Tasks.await(responseTask);
        return responseValue.getJwsResult();
    }
}