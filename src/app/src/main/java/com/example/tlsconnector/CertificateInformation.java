package com.example.tlsconnector;

import java.io.InputStream;
import java.io.PushbackInputStream;

public class CertificateInformation {
    public final InputStream file;
    public final String hash;
    public final String wildcardDomainName;

    public CertificateInformation(InputStream file, String hash, String wildcardDomainName) {
        this.file = file;
        this.hash = hash;
        this.wildcardDomainName = wildcardDomainName;
    }
}