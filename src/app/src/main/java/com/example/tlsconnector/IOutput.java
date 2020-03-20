package com.example.tlsconnector;

/**
 * Blueprint for output for logs and errors.
 *
 * @author Cees Mandjes
 */
public interface IOutput {
    void printLog(String tag, String action, String value);
    void printError(String tag, String action, String value);
}