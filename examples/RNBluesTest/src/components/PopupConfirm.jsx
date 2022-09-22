import React, { memo } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Colors } from '../constants/colors';
import { tryCall } from '../util';
import Popup from './Popup';

const PopupConfirm = ({
  title,
  message,
  btnCancelCaption,
  btnConfirmCaption,
  onCancel,
  onConfirm,
  ...props
}) => {
  return (
    <Popup centeredViewStyle={styles.centeredView} {...props}>
      <View style={styles.titleWrapper}>
        <Text style={styles.title}>{title}</Text>
      </View>
      <View style={styles.messageWrapper}>
        <Text style={styles.message}>{message}</Text>
      </View>
      <View style={styles.buttonWrapper}>
        <TouchableOpacity style={[styles.button,styles.br1]} onPress={(e) => {tryCall(onCancel, e);}}>
          <Text style={styles.buttonText}>{btnCancelCaption ?? '취소'}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={(e) => {tryCall(onConfirm, e);}}>
          <Text style={styles.buttonText}>{btnConfirmCaption ?? '확인'}</Text>
        </TouchableOpacity>
      </View>
    </Popup>
  );
};

const styles = StyleSheet.create({
  centeredView: {
    width: 228,
    paddingLeft: 10,
    paddingRight: 10,
  },
  titleWrapper: {
    marginTop: 16,
    marginBottom: 8,
  },
  title: {
    color: Colors.primary,
    fontSize: 16,
  },
  messageWrapper: {
    marginBottom: 14,
  },
  message: {
    color: Colors.textGray,
    fontSize: 12,
    textAlign: 'center',
    lineHeight: 20,
    fontSize: 14,
  },
  buttonWrapper: {
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    borderTopWidth: 1,
    borderTopColor: Colors.itemBorder,
  },
  br1: {
    borderRightWidth: 1,
    borderRightColor: Colors.itemBorder,
  },
  button: {
    paddingTop: 15,
    paddingBottom: 15,
    paddingLeft: 44,
    paddingRight: 44,
  },
  buttonText: {
    color: Colors.textDefault,
    fontSize: 14,
  },
});

export default memo(PopupConfirm);