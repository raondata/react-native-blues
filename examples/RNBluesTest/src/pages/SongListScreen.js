import React, { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, LogBox, Text, ToastAndroid, TouchableHighlight, View } from 'react-native';
import * as Auth from '../modules/auth';
import * as Blues from "../modules/bluetooth";
import * as Music from '../modules/music';
import { remoteUrl } from "../modules/music";
import * as Storage from '../modules/storage';
import { commonStyles } from "../styles/commonStyles";
LogBox.ignoreLogs(['new NativeEventEmitter']); // Ignore log notification by message

const SongListScreen = () => {
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [isScanning, setScanning] = useState(false);
  const [isConnected, setConnected] = useState(false);
  const [songList, setSongList] = useState([]);
  const [packetText, setPacketText] = useState(null);

  useEffect(() => {
    console.log('>> useEffect()');
    Auth.requestStoragePermission().then(async (allowed) => {
      console.log('storage permission:', allowed);
      if (allowed == "granted") {
        Music.init();
        // const musicFileList = await Storage.read(Music.storagePath);
        const musicFileList = Storage.listRemote(remoteUrl);
        setSongList(musicFileList);
      }
    });

    Auth.enablePermissions().then(res => {
      Blues.setEvent("deviceDiscovered", async (device) => {
        console.log('>> deviceDiscovered:', device);
        if (device.name === 'MH-M38') {
          console.log('>> deviceDiscovered(): speaker found. start connecting...');
          const isScanStopped = await Blues.stopScan();
          if (isScanStopped) {
            try {
              const conn = await Blues.connect(device.id);
              console.log('>> deviceDiscovered: conn=', conn);
            } catch (e) {
              console.error('error occurred when connecting:', e);
            }
          } else {
            console.error('>> deviceDiscovered: error: scan could not stop.');
          }
        }
      });
      Blues.setEvent("scanStarted", () => {
        console.log('>> scanStarted: scanning started.');
      });
      Blues.setEvent("scanStopped", () => {
        console.log('>> scanStopped: scanning stopped.');
        setScanning(false);
      });
      Blues.setEvent("deviceConnected", () => {
        console.log('>> deviceConnected');
        setConnected(true);
      });
      Blues.setEvent("deviceDisconnected", () => {
        console.log('>> deviceDisconnected');
        setConnected(false);
      });
    }).catch((e) => {
      console.error(`블루투스 권한 요청 중 오류가 발생했습니다. ${e}`);
      ToastAndroid.show(`블루투스 권한 요청 중 오류가 발생했습니다. ${e}`, ToastAndroid.LONG);
    });
    
    const onUnmount = () => {
      Blues.removeAllEvents();
      Music.close();
    };
    
    return onUnmount;
  }, []);

  const startScan = async () => {
    setScanning(true);
    const pairedDevices = await Blues.getPairedDeviceList();
    console.log('>> startScan(): devices=', pairedDevices);
    let foundDevice = pairedDevices?.find(d => d.name === 'MH-M38');
    if (foundDevice) {
      setScanning(false);
      console.log('>> startScan(): speaker found. start connecting...', foundDevice);
      const conn = await Blues.connect(foundDevice.id);
      console.log('>> startScan(): conn:', conn);
    } else {
      console.log('>> startScan(): device not found in paired devices. start scanning...');
      Blues.startScan();
    }
  };

  return (
    <View style={commonStyles.container}>
      <View style={commonStyles.header}>
        <Text style={{flex:1}}>블루투스 스피커 연결</Text>
        {isScanning ? <ActivityIndicator /> : null}
        <TouchableHighlight
          disabled={isScanning}
          style={[commonStyles.btn, isScanning ? {backgroundColor: '#ccc'} : null]}
          underlayColor='#ddd'
          activeOpacity={0.95}
          onPress={()=>{
            if (isConnected) {
              Blues.disconnect();
            } else {
              startScan();
            }
          }}
        >
          <Text style={commonStyles.btnText}>{isConnected ? 'DISCONNECT' : 'CONNECT'}</Text>
        </TouchableHighlight>
      </View>
      <View style={commonStyles.body}>
        <FlatList style={commonStyles.list}
          data={songList}
          renderItem={({item}) => {
            return (
              <TouchableHighlight
                underlayColor='#ddd'
                activeOpacity={0.95}
                onPress={() => {
                  console.log("selected file :", item.path);
                  Music.play(item.name, item.path);
                }}
                style={commonStyles.item}>
                <>
                  <Text style={commonStyles.itemTitle}>{item.name}</Text>
                  <Text style={commonStyles.itemSubtitle}>{item.path}</Text>
                </>
              </TouchableHighlight>
            );
          }}
          keyExtractor={item => item.id} />
      </View>
      <View style={commonStyles.footer}>
        <View style={{flex: 0}}>

        </View>
        <View style={{flex: 0}}>
          <TouchableHighlight
            underlayColor='#ddd'
            style={[commonStyles.btn]}
            onPress={() => {
              (async () => {
                console.log('TEST> get connected device:', await Blues.getConnectedDevice());
              })();
            }}
          >
            <Text style={commonStyles.btnText}>Check connected Device</Text>
          </TouchableHighlight>
        </View>
      </View>
    </View>
  );
};

export default SongListScreen;