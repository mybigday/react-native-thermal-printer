package com.mybigday.rnthermalprinter;

import com.facebook.react.bridge.ReadableMap;

/**
 * Created by pepper on 2017/11/22.
 */

public interface Printer {
    void writeText(String text, ReadableMap property);
    void writeQRCode(String content, ReadableMap property);
    void writeImage(String path, ReadableMap property);
    void writeFeed(int length);
    void writeCut(ReadableMap property);
    void startPrint();
    void endPrint();
}
