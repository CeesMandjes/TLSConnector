package com.example.tlsconnector;

import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.concurrent.ExecutionException;

public class AndroidSafetyNet {

    private MainActivity mainContext;
    private final String API_KEY = "AIzaSyBzxfDEPiyGfGZPb6JwyVumYeWrjTspnkU";

    public AndroidSafetyNet(MainActivity context) {
        this.mainContext = context;
    }

    public String getJws(byte[] nonce) throws ExecutionException, InterruptedException {
        Task<SafetyNetApi.AttestationResponse> responseTask = SafetyNet.getClient(mainContext).attest(nonce, API_KEY);
        SafetyNetApi.AttestationResponse responseValue = Tasks.await(responseTask);
        return responseValue.getJwsResult();
    }
}