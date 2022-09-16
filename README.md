
# react-native-blues

## Description

**Blues** is a package for connecting **Blue**tooth **S**peaker in React Native application.

1. Blues uses "A2DP" Bluetooth profile in Android.
2. Blues supports only Android.
3. Blues is developed refer to '[react-native-a2dp](https://www.npmjs.com/package/react-native-a2dp).'

---

## Getting started (updating...)

### 1. Install

#### 1.1 <strike>with npm</strike> **(not available yet)**

```sh
$ npm install react-native-blues --save
```

#### 1.2 without npm

##### 1.2.1 copy package src
```sh
$ git clone https://github.com/raondata/Blues.git
$ cp -R ./Blues {/your/reactnative/project}/node_modules/react-native-blues
```
##### 1.2.2 add package info to package.json
```json
"dependencies": {
    ...
    "react-native-blues": "1.0.0",
	...
```

### 2. Link to your project

#### 2.1 Most automatic linking

```sh
$ react-native link react-native-blues
```

#### 2.2 <strike>Manual linking</strike> (**currently not available**)

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

## API Docs

(updating)

## Ref

- [react-native-a2dp](https://www.npmjs.com/package/react-native-a2dp)
