package com.example.tlsconnector;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import java.io.InputStream;

/**
 * Starting point of the TLSConnector. It initializes the UI of the application and implements functionality to check Google Service
 * availability version 13 or above and the initialization of the SafetyNet Attestation check process. The SafetyNet Attestation check
 * is using certificate pinning for its requests. The TLSConnector supports two libraries: Android's default library and OkHttp library.
 * The library which is used for the request can be chosen by the user in the UI before the check is performed.
 *
 * @author Cees Mandjes
 */
public class MainActivity extends AppCompatActivity implements IOutput {
    //URL config
    private final String domainName = "https://cees.nwlab.nl";
    private final String pathNonce = "/index.php/api/getnonce";
    private final String pathJWS = "/index.php/api/validatejws";
    //Certificate config
    private final int certificateFile = R.raw.server;
    private final String certificateHash = "sha256/I229IU8zTiGr65j3A9yMMHUXgXg25hfReZ1vnV9pIBo=";
    private final String certificateWildcardDomainName = "cees.nwlab.nl";

    //UI properties
    private Spinner apiSpn;
    private final String[] apiNames = new String[] {"Android's default library", "OKHttp library"};
    private TextView tlsConnectionOutputTv;
    private CheckBox pinCorrectCertificateCb;

    /**
     * Initializes the UI of the application. This includes the dropdown which can be used to choose the certificate pinning library, a
     * checkbox which the user can choose whether it want to pin the correct certificate for the requests and the output box which is used
     * to print logs and errors in the UI. Furthermore, it also initializes a check whether the device has the correct Google Play Services
     * version installed.
     *
     * @param savedInstanceState Default param
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize API names for dropdown
        apiSpn = findViewById(R.id.tls_API_spn);
        ArrayAdapter<String> APIAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, apiNames);
        apiSpn.setAdapter(APIAdapter);
        //Initialize checkbox for correct certificate insertion
        pinCorrectCertificateCb = findViewById(R.id.pin_correct_certificate_cb);
        //Initialize output field with scrollbar
        tlsConnectionOutputTv = findViewById(R.id.output_tls_connection_tv);
        tlsConnectionOutputTv.setMovementMethod(new ScrollingMovementMethod());
        //Checks whether the device has the correct Google Play Services version installed
        verifyGooglePlayServices();
    }

    /**
     * Checks whether the device has Google Play Services installed and whether it is version 13 and above. When the condition is met, a
     * log message is printed; otherwise, an error message.
     */
    private void verifyGooglePlayServices()
    {
        final String googlePlayServiceTag = "Google play services";
        final String googlePlayServiceAction = "Availability and version check";
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this, 13000000) ==
                ConnectionResult.SUCCESS) {
            printLog(googlePlayServiceTag, googlePlayServiceAction, "Success: Device has Google Play service version 13+ installed");
        } else {
            printError(googlePlayServiceTag, googlePlayServiceAction, "Device does not have Google Play service version 13+ installed");
        }
    }

    /**
     * Called when the user chose to Initialize the SafetyNet Attestation check by clicking a button in the UI. This function starts the
     * SafetyNet Attestation check process by the settings which are set by the user, such as the certificate pinning library, in the UI.
     *
     * @param view Default param
     */
    public void performSafetyNetCheck(View view)
    {
        //Initialize certificates
        CertificateInformation correctCertificate = new CorrectCertificate(getResources().openRawResource(certificateFile), certificateHash, certificateWildcardDomainName);
        CertificateInformation incorrectCertificate = new IncorrectCertificate();

        //Initialize (correct) certificate to pin for connection
        CertificateInformation certificate;
        if(pinCorrectCertificateCb.isChecked())
            certificate = correctCertificate;
        else
            certificate = incorrectCertificate;

        //Get chosen library for certificate pinning and use it to perform the SafetyNet check
        String apiNameVal = apiSpn.getSelectedItem().toString();
        switch (apiNameVal) {
            case "Android's default library":
                new AndroidsDefaultLibraryConnector(domainName, certificate, pathNonce, pathJWS, this, this).execute();
                break;
            case "OKHttp library":
                new OKHttpLibraryConnector(domainName, certificate, pathNonce, pathJWS, this, this).execute();
                break;
        }
    }

    /**
     * Prints logs in the output box in the UI.
     *
     * @param tag Process/library which is the log about
     * @param action Action taken which is the log about
     * @param value Log value
     */
    public void printLog(String tag, String action, String value)
    {
        String current = tlsConnectionOutputTv.getText().toString();
        tlsConnectionOutputTv.setText(current + "\n\n" + tag +  " - " + action + "\n" + value );
    }

    /**
     * Prints errors in the output box in the UI.
     *
     * @param tag Process/library which is the error about
     * @param action Action taken which is the error about
     * @param value Error value
     */
    public void printError(String tag, String action, String value)
    {
        printLog(tag, action , "Error: " + value);
    }

    /**
     * Correct certificate's information config.
     */
    private final class CorrectCertificate extends CertificateInformation{
        public CorrectCertificate(InputStream file, String hash, String wildcardDomainName)
        {
            super(file, hash, wildcardDomainName);
        }
    }

    /**
     * Incorrect certificate's information config.
     */
    private final class IncorrectCertificate extends CertificateInformation{
        public IncorrectCertificate()
        {
            super(
                 getResources().openRawResource(R.raw.nunl),
                "sha256/1OLklscvzMYj8f888lp5ze/hY0CFHyLSPQzSpYYIBm8=",
                 certificateWildcardDomainName
            );
        }
    }
}