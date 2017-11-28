package com.mybigday.rnthermalprinter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.facebook.react.bridge.ReadableMap;
import com.tx.printlib.Const;
import com.tx.printlib.UsbPrinter;

import java.util.Map;

/**
 * Created by pepper on 2017/11/28.
 */

public class EpsonMT532AP implements Printer{

    private String TAG = "EPSONPrinter";
    private UsbPrinter mUsbPrinter;
    private Context reactContext;
    private PrinterEventListener listener;

    public EpsonMT532AP(final Context context, final PrinterEventListener eventListener) {
        try {
            reactContext = context;
            listener = eventListener;
            this.mUsbPrinter = new UsbPrinter(context.getApplicationContext());
            UsbDevice dev = getCorrectDevice();
            String message;
            if (dev == null || !this.mUsbPrinter.open(dev)) {
                message = "ERROR: Usb printer open fails";
                Toast.makeText(reactContext, message, 1).show();
                listener.onInitializeError(new Error(message));
                return;
            }
            long stat1 = (long) this.mUsbPrinter.getStatus();
            long stat2 = this.mUsbPrinter.getStatus2();
            this.mUsbPrinter.close();
            message = String.format("%04XH, %04XH", new Object[]{Long.valueOf(stat1), Long.valueOf(stat2)});
            Toast.makeText(reactContext, message, 1).show();
            listener.onInitializeSuccess(message);
        } catch (Throwable e) {
            listener.onInitializeError(new Error(e));
        }
    }

    @Override
    public void writeText(String text, ReadableMap property) {
        mUsbPrinter.resetFont();
        mUsbPrinter.doFunction(Const.TX_UNIT_TYPE, Const.TX_UNIT_MM, 0);
        mUsbPrinter.doFunction(Const.TX_CHINESE_MODE, Const.TX_ON, 0);
        mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_1X, Const.TX_SIZE_1X);
        if (property.hasKey("size")) {
            switch (property.getInt("size")) {
                case 0:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_1X, Const.TX_SIZE_1X);
                    break;
                case 1:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_2X, Const.TX_SIZE_2X);
                    break;
                case 2:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_3X, Const.TX_SIZE_3X);
                    break;
                case 3:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_4X, Const.TX_SIZE_4X);
                    break;
                case 4:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_5X, Const.TX_SIZE_5X);
                    break;
                case 5:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_6X, Const.TX_SIZE_6X);
                    break;
                case 6:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_7X, Const.TX_SIZE_7X);
                    break;
                case 7:
                    mUsbPrinter.doFunction(Const.TX_FONT_SIZE, Const.TX_SIZE_8X, Const.TX_SIZE_8X);
                    break;
            }
        }

        mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_RIGHT, 0);
        if (property.hasKey("align")) {
            switch (property.getString("align")) {
                case "center":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_CENTER, 0);
                    break;
                case "left":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_LEFT, 0);
                    break;
                case "right":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_RIGHT, 0);
                    break;
            }
        }

        mUsbPrinter.doFunction(Const.TX_FONT_BOLD, Const.TX_OFF, 0);
        if (property.hasKey("bold") && property.getBoolean("bold") == true) {
            mUsbPrinter.doFunction(Const.TX_FONT_BOLD, Const.TX_ON, 0);
        }
        mUsbPrinter.doFunction(Const.TX_FONT_ULINE, Const.TX_OFF, 0);
        if (property.hasKey("underline") && property.getBoolean("underline") == true) {
            mUsbPrinter.doFunction(Const.TX_FONT_ULINE, Const.TX_ON, 0);
        }
        if (property.hasKey("padding") && property.getInt("padding") > 0) {
            mUsbPrinter.doFunction(Const.TX_HOR_POS, property.getInt("padding"), 0);
        }
        if (property.hasKey("linebreak") && property.getBoolean("linebreak") == true) {
            this.mUsbPrinter.outputStringLn(text);
        } else {
            this.mUsbPrinter.outputString(text);
        }
    }

    @Override
    public void writeQRCode(String content, ReadableMap property) {
        mUsbPrinter.doFunction(Const.TX_UNIT_TYPE, Const.TX_UNIT_MM, 0);
        mUsbPrinter.doFunction(Const.TX_QR_DOTSIZE, 7, 0);
        mUsbPrinter.doFunction(Const.TX_QR_ERRLEVEL, Const.TX_QR_ERRLEVEL_H, 0);
        if (property.hasKey("level")) {
            switch (property.getString("level")) {
                case "H":
                    mUsbPrinter.doFunction(Const.TX_QR_ERRLEVEL, Const.TX_QR_ERRLEVEL_H, 0);
                    break;
                case "L":
                    mUsbPrinter.doFunction(Const.TX_QR_ERRLEVEL, Const.TX_QR_ERRLEVEL_L, 0);
                    break;
                case "M":
                    mUsbPrinter.doFunction(Const.TX_QR_ERRLEVEL, Const.TX_QR_ERRLEVEL_M,0 );
                    break;
                case "Q":
                    mUsbPrinter.doFunction(Const.TX_QR_ERRLEVEL, Const.TX_QR_ERRLEVEL_Q, 0);
                    break;
            }
        }
        mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_RIGHT, 0);
        if (property.hasKey("align")) {
            switch (property.getString("align")) {
                case "center":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_CENTER, 0);
                    break;
                case "left":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_LEFT, 0);
                    break;
                case "right":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_RIGHT, 0);
                    break;
            }
        }
        this.mUsbPrinter.printQRcode(content);
    }

    @Override
    public void writeImage(String path, ReadableMap property) {
        mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_RIGHT, 0);
        if (property.hasKey("align")) {
            switch (property.getString("align")) {
                case "center":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_CENTER, 0);
                    break;
                case "left":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_LEFT, 0);
                    break;
                case "right":
                    mUsbPrinter.doFunction(Const.TX_ALIGN, Const.TX_ALIGN_RIGHT, 0);
                    break;
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        Toast.makeText(reactContext, "Image: " + bitmap.getWidth() + " x " + bitmap.getHeight(), 1).show();
        mUsbPrinter.printImage(path);
    }

    @Override
    public void writeFeed(int length) {
        mUsbPrinter.doFunction(Const.TX_UNIT_TYPE, Const.TX_UNIT_MM, 0);
        mUsbPrinter.doFunction(Const.TX_FEED, length, 0);
    }

    @Override
    public void writeCut(ReadableMap property) {
        if (property.hasKey("cut")) {
            switch (property.getString("cut")) {
                case "full":
                    mUsbPrinter.doFunction(Const.TX_CUT, Const.TX_CUT_FULL, 0);
                    break;
                case "partial":
                    mUsbPrinter.doFunction(Const.TX_CUT, Const.TX_CUT_PARTIAL, 0);
                    break;
            }
        } else {
            mUsbPrinter.doFunction(Const.TX_CUT, Const.TX_CUT_FULL, 0);
        }
    }

    @Override
    public void startPrint() {
        mUsbPrinter = new UsbPrinter(reactContext);
        UsbDevice dev = getCorrectDevice();
        if (dev != null && mUsbPrinter.open(dev)) {
            mUsbPrinter.init();
        }
    }

    @Override
    public void endPrint() {
        mUsbPrinter.close();
    }

    private UsbDevice getCorrectDevice() {
        Map<String, UsbDevice> devMap = ((UsbManager) reactContext.getSystemService(Context.USB_SERVICE)).getDeviceList();
        for (String name : devMap.keySet()) {
            if (UsbPrinter.checkPrinter(devMap.get(name))) {
                return devMap.get(name);
            }
        }
        return null;
    }
}
