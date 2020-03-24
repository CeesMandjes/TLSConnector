# TLSConnector
Client implementation for Android SafetyNet Attestation check. This Android application's implementation uses certificate pinning for the requests. The library for certificate pinning can be chosen by the user in the UI, the following libraries are supported:
<ul>
<li>Android's default library</li>
<li>OKHttp Library</li>
</ul>
<br/>
When the user chose the certificate pinning library, it can choose whether it wants to pin the correct certificate. Once all those settings are set, the user can start the SafetyNet Attestation check by clicking the button in the UI, this starts the following process:
<ul>
<li>Pin the certificate for the host</li>
<li>Request nonce from the application server</li>
<li>Use this nonce to start the SafetyNet Attestation evaluation</li>
<li>Send the SafetyNet's signed attestation to the application server which will provide the outcome of the SafetyNet Attestation check</li>
</ul>

NOTE: A server implementation for the SafetyNet Attestation check is required.
<br/><br/>
Configuration (MainActivity):
<ul>
<li>Set application's server domain in 'domainName'</li>
<li>Set application's server path to request nonce in 'pathNonce'</li>
<li>Set application's server path to send SafetyNet's signed attestation in 'pathNonce'</li>
<li>Save application's server certificate in '/src/app/src/main/res/raw/server.crt'</li>
<li>Set application's server certificate's hash in 'certificateHash'</li>
<li>Set application's server certificate's wildcard domain name in 'certificateWildcardDomainName'</li>
</ul>

Screenshots of the TLSConnector in action:<br>

<img src="/images/screenshotAndroidsDefault.jpeg" width="300">
<img src="/images/screenshotOkHttp.jpeg" width="300">


