import React from 'react';
import { StyleSheet, View } from 'react-native';

const EnabledIndicator = ({
  isEnabled,
  size = 12,
  ...props
}) => {
  const styles = StyleSheet.create({
    container: {
      alignItems: 'center',
      justifyContent: 'flex-start',
      padding: 4,
    },
    indicator: {
      width: size,
      height: size,
      borderRadius: size,
    },
  });

  return (
    <View style={styles.container}>
      <View style={[styles.indicator, {backgroundColor: isEnabled ? '#0f0' : '#ff0'}]} />
    </View>
  );
};


export default EnabledIndicator;