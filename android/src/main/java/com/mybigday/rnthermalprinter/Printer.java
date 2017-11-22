package com.mybigday.rnthermalprinter;

import com.facebook.react.bridge.ReadableMap;

/**
 * Created by pepper on 2017/11/22.
 */

public interface Printer {
    void writeText(String text, ReadableMap property);
    void writeQRCode(String content, ReadableMap property);
    void writeFeed(int length);
    void print();
}
