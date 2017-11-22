package com.mybigday.rnthermalprinter;

import android.content.Context;
import android.widget.Toast;

import com.facebook.react.bridge.ReadableMap;

import org.json.JSONException;
import org.json.JSONObject;

import cn.weipass.pos.sdk.IPrint.*;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.LatticePrinter.*;
import cn.weipass.pos.sdk.Weipos.*;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * Created by pepper on 2017/11/22.
 */

public class WangPosPrinter implements Printer{

    private PrinterEventListener listener;

    private boolean is2s = false;
    private LatticePrinter latticePrinter;

    public WangPosPrinter(final Context context, final PrinterEventListener eventListener) {
        WeiposImpl.as().init(context, new OnInitListener() {
            @Override
            public void onInitOk() {
                try {
                    listener = eventListener;
                    String deviceInfo = WeiposImpl.as().getDeviceInfo();

                    if (deviceInfo != null) {
                        JSONObject deviceJson = new JSONObject(deviceInfo);
                        if (deviceJson.has("deviceType")) {
                            String deviceType = deviceJson.getString("deviceType");
                            if (deviceType.equals("2")) {
                                // 旺POS2设备
                                is2s = false;
                            } else if (deviceType.equalsIgnoreCase("2s")) {
                                // 旺POS2s设备
                                is2s = true;
                            }
                        }
                        latticePrinter = WeiposImpl.as().openLatticePrinter();
                        listener.onInitializeSuccess(deviceInfo);
                    } else {
                        listener.onInitializeError(new Error("ERROR can not find device"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String s) {
                listener.onInitializeError(new Error(s));
            }

            @Override
            public void onDestroy() {
                listener.onPrinterClosed("");
            }
        });
    }
    public void writeText(String text, ReadableMap property) {
        if (property.hasKey("linebreak") && property.getBoolean("linebreak") == true) {
            text = text + '\n';
        }
        FontSize size = FontSize.MEDIUM;
        if (property.hasKey("size")) {
            switch (property.getInt("size")) {
                case 0:
                    size = FontSize.SMALL;
                    break;
                case 1:
                    size = FontSize.MEDIUM;
                    break;
                case 2:
                    size = FontSize.LARGE;
                    break;
                case 3:
                    size = FontSize.EXTRALARGE;
                    break;
            }
        }
        FontStyle style = FontStyle.NORMAL;
        if (property.hasKey("bold") && property.getBoolean("bold") == true) {
            style = FontStyle.BOLD;
        } else if (property.hasKey("italic") && property.getBoolean("italic") == true) {
            style = FontStyle.ITALIC;
        }
        latticePrinter.printText(text, LatticePrinter.FontFamily.SONG, size, style);
    }
    public void writeQRCode(String content, ReadableMap property) {
        int size = 50;
        if (property.hasKey("size")) {
            int sizeIndex = property.getInt("size");
            if (sizeIndex >= 0) {
                size = (sizeIndex + 1) * 10;
            }
        }
        Gravity gravity = Gravity.LEFT;
        if (property.hasKey("align")) {
            switch (property.getString("align")) {
                case "left":
                    gravity = Gravity.LEFT;
                    break;
                case "right":
                    gravity = Gravity.RIGHT;
                    break;
                case "center":
                    gravity = Gravity.CENTER;
                    break;
            }
        }
        latticePrinter.printQrCode(content, size, gravity);
    }
    public void writeFeed(int length) {
        latticePrinter.feed(length);
    }
    public void print() {
        latticePrinter.submitPrint();
    }
}
