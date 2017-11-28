import {
  Platform,
  NativeModules,
} from 'react-native';

RNThermalPrinter = NativeModules.RNThermalPrinter;

function CheckPlatformSupport() {
  if (Platform.OS !== 'android') {
    throw new Error('Currently only support Android platform.');
  }
  return true;
}

export default class ThermalPrinter {
  constructor(props) {
    const defaultSetting = {
      type: 'THERMAL_PRINTER_EPSON_MT532AP'
    }
    const config = Object.assign({}, defaultSetting, props);
    RNThermalPrinter.initilize(config.type);
  }
  writeText(text, property) {
    RNThermalPrinter.writeText(text, property);
  }
  writeQRCode(content, property) {
    RNThermalPrinter.writeQRCode(content, property);
  }
  writeImage(path, property) {
    RNThermalPrinter.writeImage(path, property);
  }
  writeFeed(length) {
    RNThermalPrinter.writeFeed(length);
  }
  writeCut(type) {
    RNThermalPrinter.writeCut({
      'cut': type,
    });
  }
  startPrint() {
    RNThermalPrinter.startPrint();
  }
  endPrint() {
    RNThermalPrinter.endPrint();
  }
  printDemo() {
    this.startPrint();
    this.writeText('Hello!!!', {
      size: 0,
      linebreak: true,
      align: 'left',
    });
    this.writeText('Hello!!!', {
      size: 1,
      linebreak: true,
      align: 'center',
    });
    this.writeText('Hello!!!', {
      size: 2,
      italic: true,
      linebreak: true,
      align: 'right',
    });
    this.writeText('Hello!!!', {
      size: 3,
      bold: true,
      linebreak: true,
      underline: true,
    });
    this.writeFeed(5);
    this.writeQRCode('http://www.mybigday.com.tw', {
      size: 20,
      align: 'left',
      level: 'H',
    });
    this.writeFeed(10);
    this.writeImage('/mnt/sdcard/Download/1.png', {});
    this.writeFeed(30);

    this.writeCut('full');
    this.endPrint();
  }
}
