import React from 'react';
import * as Icons from '../assets/icon';

function SvgIcon({
  name,
  size,
  color,
  fill,
  stroke,
  width: _width,
  height: _height,
  ...props
}) {
  const IconComp = Icons[name];
  const width = _width ?? size;
  const height = _height ?? size;
  const sizeProps = {
    ...(width !== undefined ? { width } : {}),
    ...(height !== undefined ? { height } : {}),
  }

  return (
    <IconComp
      {...props}
      {...sizeProps}
      fill={fill ?? color ?? 'transparent'}
      stroke={stroke ?? color ?? 'transparent'}
    />
  )
}

export default SvgIcon;