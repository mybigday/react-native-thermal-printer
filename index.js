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
      type: 'THERMAL_PRINTER_WANG_POS'
    }
    const config = Object.assign({}, defaultSetting, props);
    RNThermalPrinter.initilize(config.type);
  }
  writeText(text, property) {
    RNThermalPrinter.writeText(text, property);
  }
  writeQRCode(content, property) {
    RNThermalPrinter.writeText(content, property);
  }
  writeFeed(length) {
    RNThermalPrinter.writeFeed(length);
  }
  print() {
    RNThermalPrinter.print();
  }
  printDemo() {
    RNThermalPrinter.writeText('Hello!!!', {
      size: 0,

    });
    RNThermalPrinter.writeText('Hello!!!', {
      size: 1,
      linebreak: true,
    });
    RNThermalPrinter.writeText('Hello!!!', {
      size: 2,
      italic: true,
      linebreak: true,
    });
    RNThermalPrinter.writeText('Hello!!!', {
      size: 3,
      bold: true,
    });
    RNThermalPrinter.writeQRCode('http://www.mybigday.com.tw', {
      size: 20,
      align: 'left',
    });
    RNThermalPrinter.print();
  }
}
