import React from 'react';
import { Modal, View } from 'react-native';
import { Colors } from '../constants/colors';

const Popup = ({
  centeredViewStyle,
  children,
  onClose,
  ...props // visible, onShow
}) => {
  return (
    <Modal
      animationType="fade" useNativeDriver={true}
      transparent={true}
      onRequestClose={onClose}
      {...props}
    >
      <View style={styles.container}>
        <View style={[styles.centeredView, centeredViewStyle]}>
          {children}
        </View>
      </View>
    </Modal>
  );
};

const styles = {
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.background + '70',
  },
  centeredView: {
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.modalBackground,
    borderRadius: 8,
  },
};

export default React.memo(Popup);