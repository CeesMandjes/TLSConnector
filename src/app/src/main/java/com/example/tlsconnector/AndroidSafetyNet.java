package com.example.tlsconnector;

import android.app.Activity;

import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.concurrent.ExecutionException;

public class AndroidSafetyNet {

    private Activity context;
    private final String API_KEY = "AIzaSyBzxfDEPiyGfGZPb6JwyVumYeWrjTspnkU";

    public AndroidSafetyNet(Activity context) {
        this.context = context;
    }

    public String getJws(byte[] nonce) throws ExecutionException, InterruptedException {
        Task<SafetyNetApi.AttestationResponse> responseTask = SafetyNet.getClient(context).attest(nonce, API_KEY);
        SafetyNetApi.AttestationResponse responseValue = Tasks.await(responseTask);
        return responseValue.getJwsResult();
    }
}