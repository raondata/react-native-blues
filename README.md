
# react-native-blues

## Getting started

`$ npm install react-native-blues --save`

### Mostly automatic installation

`$ react-native link react-native-blues`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import ai.raondata.blues.RNBluesPackage;` to the imports at the top of the file
  - Add `new RNBluesPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-blues'
  	project(':react-native-blues').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-blues/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-blues')
  	```


## Usage
```javascript
import RNBlues from 'react-native-blues';

// TODO: What to do with the module?
RNBlues;
```
  