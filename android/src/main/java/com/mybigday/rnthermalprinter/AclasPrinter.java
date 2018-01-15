package com.mybigday.rnthermalprinter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ReadableMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;

import CommDevice.USBPort;
//import aclasdriver.AclasReceiptPrinter;
import aclasdriver.AclasBaseFunction;
import aclasdriver.Printer;

import static java.lang.Math.ceil;

/**
 * Created by pepper on 2017/11/28.
 */

public class AclasPrinter implements com.mybigday.rnthermalprinter.Printer {

    private PrinterEventListener listener;
    private Context reactContext;

    private aclasdriver.Printer printer;
    private String printerSerial = "";

    private String printerName;
    private boolean isEpson;
    private int PrinterType = 0;
    private int printMode = 0;
    static int DotLineWidth = 384;
    static int DotLineBytes = DotLineWidth / 8;
    static int SleepTime = 2000;

    private PrinterThread printThread;

    public AclasPrinter(final Context context, final PrinterEventListener eventListener) {
        listener = eventListener;
        reactContext = context;

        printer = new Printer();

        printerSerial = USBPort.getDeviceName(0);
        isEpson = Printer.isEpsonPrinter(printerSerial);
        printMode = isEpson?0:1;
        String archStr = System.getProperty("os.arch").toUpperCase();

//        Toast.makeText(
//                reactContext,
//                "PrintMode:" + printMode +
//                        " PrinterSerial:" + printerSerial +
//                        " isEpson:" + isEpson +
//                        " ARCH:" + archStr
//                , Toast.LENGTH_LONG).show();

        //AOW error
        if (archStr.contains("ARM")){
            if(printerSerial.length() == 0){
                printMode = 0;// 0 epson/1 dot
                isEpson = true;
            }
        } else {
            printMode = 1;
        }

        // Open Printer
        int retopen = -1;
        if(!printerSerial.isEmpty()){
            printer.SetStdEpsonMode(isEpson?1:0);
            retopen = printer.Open(PrinterType, new USBPort("", printerSerial, ""));
        }
        else{
            retopen = printer.Open(PrinterType,null);
        }
        if(retopen >= 1){
            int ret = printer.SetPrintMode(printMode);

            int dotWidth = printer.GetDotWidth();
            if (dotWidth > 0) {
                DotLineWidth = dotWidth;
            }
            DotLineBytes = DotLineWidth / 8;

            printThread = new PrinterThread();
            printThread.start();
        }
    }

    @Override
    public void writeText(String text, ReadableMap property) {
        try {
            final byte SetBigFont[] = {0x1B, 0x21, 0x30};
            final byte SetSmallFont[] = {0x1B, 0x21, 0x00};

            final byte SetUnderlineOff[] = {0x1B, 0x2D, 0x00};
            final byte SetUnderlineOn[] = {0x1B, 0x2D, 0x01};

            final byte SetAlignLeft[] = {0x1B, 0x61, 0x00};
            final byte SetAlignCenter[] = {0x1B, 0x61, 0x01};
            final byte SetAlignRight[] = {0x1B, 0x61, 0x02};

            final byte ChineseCommand[] = {0x1C, 0x21};

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte chineseSetting = 0x00;

            if (property.hasKey("size")) {
                switch (property.getInt("size")) {
                    case 0:
                        output.write(SetSmallFont);
                        break;
                    case 1:
                        output.write(SetBigFont);
                        chineseSetting += 0x0C;
                        break;
                    default:
                        output.write(SetSmallFont);
                }
            } else {
                output.write(SetSmallFont);
            }

            if (property.hasKey("align")) {
                switch (property.getString("align")) {
                    case "center":
                        output.write(SetAlignCenter);
                        break;
                    case "left":
                        output.write(SetAlignLeft);
                        break;
                    case "right":
                        output.write(SetAlignRight);
                        break;
                    default:
                        output.write(SetAlignLeft);
                }
            } else {
                output.write(SetAlignLeft);
            }

            if (property.hasKey("underline") && property.getBoolean("underline") == true) {
                output.write(SetUnderlineOn);
                chineseSetting += 0x80;
            } else {
                output.write(SetUnderlineOff);
            }

            output.write(ChineseCommand);
            output.write(chineseSetting);

            if (property.hasKey("linebreak") && property.getBoolean("linebreak") == true) {
                text = text + '\n';
            }

            output.write(text.getBytes("big5"));

            byte[] out = output.toByteArray();
            printer.Write(out);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeQRCode(String content, ReadableMap property) {
        final byte SetQRCodeMode2[] = {0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00};
        final byte SetQRCodeSize[] = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43};

        final byte SetQRCodeErrorL[] = {0x1D,0x28,0x6B,0x03,0x00,0x31,0x45,0x30};
        final byte SetQRCodeErrorM[] = {0x1D,0x28,0x6B,0x03,0x00,0x31,0x45,0x31};
        final byte SetQRCodeErrorQ[] = {0x1D,0x28,0x6B,0x03,0x00,0x31,0x45,0x32};
        final byte SetQRCodeErrorH[] = {0x1D,0x28,0x6B,0x03,0x00,0x31,0x45,0x33};

        final byte SetQRCodeData[] = {0x1D, 0x28, 0x6B};
        final byte SetQRCodeDataHead[] = {0x31, 0x50, 0x30};

        final byte PrintQRCodeCommand[] = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30};

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte printData[] = content.getBytes("ascii");
            int dataLength = printData.length + 3;
            int pL = dataLength % 256;
            int pH = dataLength / 256;

            printer.Write(SetQRCodeMode2);
            if (property.hasKey("size") && property.getInt("size") > 0) {
                output.write(SetQRCodeSize);
                int size = property.getInt("size");
                if (size < 1) {
                    size = 1;
                } else if (size > 16) {
                    size = 16;
                }
                output.write(size);
            }

            if (property.hasKey("level")) {
                switch (property.getString("level")) {
                    case "H":
                        output.write(SetQRCodeErrorH);
                        break;
                    case "L":
                        output.write(SetQRCodeErrorL);
                        break;
                    case "M":
                        output.write(SetQRCodeErrorM);
                        break;
                    case "Q":
                        output.write(SetQRCodeErrorQ);
                        break;
                }
            }

            output.write(SetQRCodeData);
            output.write(pL);
            output.write(pH);
            output.write(SetQRCodeDataHead);
            output.write(printData);
            output.write(PrintQRCodeCommand);
            output.write(0x0C);

            byte[] out = output.toByteArray();
            printer.Write(out);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeImage(String path, ReadableMap property) {

    }

    @Override
    public void writeFeed(int length) {
        double lengthInch = length * 0.0393700787;
        int lengthInt = (int)Math.ceil(lengthInch);
        printer.Feed(lengthInt);
    }

    @Override
    public void writeCut(ReadableMap property) {
        final byte FullCut[] = {0x1D, 0x56, 0x00};
        final byte PartialCut[] = {0x1D, 0x56, 0x01};

        if (property.hasKey("cut")) {
            switch (property.getString("cut")) {
                case "full":
                    printer.Write(FullCut);
                    break;
                case "partial":
                    printer.Write(PartialCut);
                    break;
            }
        } else {
            printer.Write(FullCut);
        }
    }

    @Override
    public void startPrint() {

    }

    @Override
    public void endPrint() {
//        printer.Feed(10);
    }

    class PrinterThread extends Thread{
        ReentrantLock bufferLock = new ReentrantLock();
        boolean runflag = false;
        boolean enablePrint = false;
        public int cutterType = 1;
        public boolean bSerial	= false;

        byte[] data = new byte[DotLineWidth / 8 * 16 * 1024];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

//        ByteArrayBuffer printerBuffer = new ByteArrayBuffer(DotLineWidth / 8 * 16 * 1024);

        public synchronized void clearPrintBuffer(){
            bufferLock.lock();
            buffer.reset();
//            printerBuffer.clear();
            //printspaceline(24);
            bufferLock.unlock();
        }

        public synchronized void appendPrintData(byte[] data, int offset, int len) {
            bufferLock.lock();
//            printerBuffer.append(data, offset, len);
            buffer.write(data, offset, len);
//            data.write(data,0,current);
            bufferLock.unlock();
        }

        public synchronized int startPrintData(){
            if (printer == null ){
                return -1;
            }
            if (enablePrint == true){
                return 1;
            }

            enablePrint = true;
            return 0;
        }

        public synchronized void setFlagSerielPrint(boolean bFlag){
            bSerial	= bFlag;
        }

        private synchronized int printData(int cuttype){
            int ret = 0;
            final byte cutcmd[]         = {0x1d,0x56,0x00};
            final byte halfcutcmd[]     = {0x1d,0x56,0x01};

            if (buffer.size() > 0) {
                bufferLock.lock();

                ret = printer.Write(buffer.toByteArray());
//                Log.d(tag, "kwq print printData printerBuffer len:"+buffer.size());

                if (cuttype >= 0) {
                    if (printMode != 0) {
                        printer.SetPrintMode(0);
                    }
                    switch (cuttype) {
                        case 1:
                            printer.Write(halfcutcmd);
                            break;
                        case 0:
                            printer.Write(cutcmd);
                            break;
                        default:
                            break;
                    }
                    if (printMode != 0) {
                        printer.SetPrintMode(1);
                    }
                }

                bufferLock.unlock();
                //clearPrintBuffer();
            }
            return ret;
        }
        public synchronized boolean checkPaper(){
            boolean havePaper = false;
            havePaper = printer.IsPaperExist();

//            Message msg_paperstatus = gui_show.obtainMessage();
//            msg_paperstatus.arg1 = MSG_TYPE.MSG_PAPERSTATUS;
//            if(havePaper)
//            {
//                msg_paperstatus.obj = new String(PAPER_STATUS_STRING + HAVE_PAPER_STRING);
//            }
//            else
//            {
//                msg_paperstatus.obj = new String(PAPER_STATUS_STRING + NO_PAPER_STRING);
//            }
//            gui_show.sendMessage(msg_paperstatus);
            return havePaper;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            int timer = 0;
            int timerMax = 5;
            boolean havePaper = true;
            while(runflag)
            {
                try {
                    sleep(100);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if(!runflag) break;
//                Log.d(tag, "Printer ---> Thread Run");
                if (timer++ > timerMax){
                    timer = 0;
                    timerMax = 5;

                    checkPaper();   //check paper status
                }
                if (havePaper && enablePrint) {
                    int ret = printData(cutterType);

//                    Log.d(tag, "Print data result0:" + ret);


                    if(bSerial){

                        try {
                            sleep(SleepTime);
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    enablePrint = bSerial;

                    if (ret > 0) {  //delay some seconds to wait printer buffer clear
                        timer = 0;
                        timerMax = 10;
                    }
                } else if (enablePrint){
                    enablePrint = false;
                }
            }
        }

        @Override
        public synchronized void start() {
            // TODO Auto-generated method stub
            runflag = true;
            super.start();
        }
    }
}
