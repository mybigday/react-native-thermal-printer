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
//        Toast.makeText(getReactApplicationContext(), "Select type:" + type, Toast.LENGTH_LONG);
        switch (type) {
            case "THERMAL_PRINTER_WANG_POS":
                printer = new WangPosPrinter(getReactApplicationContext(), this);
                break;
            case "THERMAL_PRINTER_EPSON_MT532AP":
                printer = new EpsonMT532AP(getReactApplicationContext(), this);
                break;
            case "THERMAL_PRINTER_ACLAS":
                printer = new AclasPrinter(getReactApplicationContext(), this);
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
    public void writeImage(String path, ReadableMap property) {
        printer.writeImage(path, property);
    }

    @ReactMethod
    public void writeFeed(int length) {
        printer.writeFeed(length);
    }

    @ReactMethod
    public void writeCut(ReadableMap property) { printer.writeCut(property); }

    @ReactMethod
    public void startPrint() { printer.startPrint(); }

    @ReactMethod
    public void endPrint() {
        printer.endPrint();
    }
}
