package com.mybigday.rnthermalprinter;

import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

public class RNThermalPrinter extends ReactContextBaseJavaModule implements PrinterEventListener {
    private String TAG = "ThermalPrinter";

    Printer printer;
    
    public RNThermalPrinter(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    public String getName() {
        return "RNThermalPrinter";
    }

    @Override
    public void onInitializeSuccess(String deviceInfo) {
        Toast.makeText(getReactApplicationContext(), deviceInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializeError(Error error) {
        Toast.makeText(getReactApplicationContext(), "ERROR:" + error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPrinterClosed(String message) {
        Toast.makeText(getReactApplicationContext(), "Printer closed: " + message, Toast.LENGTH_LONG).show();
    }

    @ReactMethod
    public void initilize(String type) {
        switch (type) {
            case "THERMAL_PRINTER_WANG_POS":
                printer = new WangPosPrinter(getReactApplicationContext(), this);
                break;
        }
    }

    @ReactMethod
    public void writeText(String text, ReadableMap property) {
        printer.writeText(text, property);
    }

    @ReactMethod
    public void writeQRCode(String content, ReadableMap property) {
        printer.writeQRCode(content, property);
    }

    @ReactMethod
    public void writeFeed(int length) {
        printer.writeFeed(length);
    }

    @ReactMethod
    public void print() {
        printer.print();
    }
}
