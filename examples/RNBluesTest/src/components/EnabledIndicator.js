import React, { memo, useState } from 'react';
import { StyleSheet, View } from 'react-native';

const EnabledIndicator = ({
  isEnabled,
  ...props
}) => {
  const [enabled, setEnabled] = useState(isEnabled);
  const styles = StyleSheet.create({
    container: {
      alignItems: 'center',
      justifyContent: 'flex-start',
      padding: 4,
    },
    indicator: {
      
      width: 16,
      height: 16,
      borderRadius: 16,
    },
  });

  return (
    <View style={styles.container}>
      <View style={[styles.indicator, {backgroundColor: enabled ? '#0f0' : '#ff0'}]} />
    </View>
  );
};


export default memo(EnabledIndicator);