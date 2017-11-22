package com.mybigday.rnthermalprinter;

/**
 * Created by pepper on 2017/11/22.
 */

public interface PrinterEventListener {
    void onInitializeSuccess(String deviceInfo);
    void onInitializeError(Error error);
    void onPrinterClosed(String message);
}
