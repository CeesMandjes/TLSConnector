package com.example.tlsconnector;

import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Holds certificate information such as the file as inputStream, hash of the certificate and its wildcard domain name.
 *
 * @author Cees Mandjes
 */
public class CertificateInformation {
    public final InputStream file;
    public final String hash;
    public final String wildcardDomainName;

    /**
     * Initilizes certificate's information.
     *
     * @param file File as inputstream
     * @param hash Hash of the certificate
     * @param wildcardDomainName Certificate's wildcard domain name
     */
    public CertificateInformation(InputStream file, String hash, String wildcardDomainName) {
        this.file = file;
        this.hash = hash;
        this.wildcardDomainName = wildcardDomainName;
    }
}