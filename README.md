# TLSConnector
Client implementation for Android SafetyNet Attestation check. This Android application's implementation uses certificate pinning for the requests. The library for certificate pinning can be chosen by the user in the UI, the following libraries are supported:
- Android's default library
- OKHttp Library

When the user chose the certificate pinning library, it can chose whether it wants to pin the correct certificate. Once all those settings are set, the user can start the SafetyNet Attestation check by clicking the button in the UI, this will do the following:
- Pin the certificate for the host
- Request nonce from the application server
- Use this nonce to start the SafetyNet Attestation evaluation
- Send the SafetyNet's signed attestation to the application server which will provide the outcome of the SafetyNet Attestation check
<br/>
NOTE: A server implementation for the SafetyNet Attestation check is required.
<br/>
Configuration (MainActivity):
- Set application's server domain in 'domainName'
- Set application's server path to request nonce in 'pathNonce'
- Set application's server path to send SafetyNet's signed attestation in 'pathNonce'
- Set application's server certificate /src/app/src/main/res/raw/server.crt
- Set application's server certificate's hash in 'certificateHash'
- Set application's server certificate's wildcard domain name in 'certificateWildcardDomainName'

Screenshots of the TLSConnector in action:<br>

![Android's default library](/images/screenshotAndroidsDefault.jpeg?raw=true "Android's default library]")

