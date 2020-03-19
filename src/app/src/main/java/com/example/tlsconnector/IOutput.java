package com.example.tlsconnector;

public interface IOutput {
    void printText(String tag, String action, String value);
    void printError(String tag, String action, String value);
}