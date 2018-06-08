# CogniCrypt_SAST for Android

This repository contains CogniCrypt_SAST for Android which extends [CogniCrypt_SAST](https://github.com/CROSSINGTUD/CryptoAnalysis) to be able to run on Android Application.
It relies on [FlowDroid](https://github.com/secure-software-engineering/FlowDroid) to compute an android-specific callgraph before the actual static analysis is executed.

## Pre-requesite

The code of this repository depends on [FlowDroid](https://github.com/secure-software-engineering/FlowDroid) to be installed as a maven artifact on your machine.
Follow the commands on their readme and run `mvn install` to install FlowDroid into your local maven repository.

## Checkout and Build

This repository uses git submodules, to checkout this repository use the following command for git

```git clone --recurse-submodules git@github.com:CROSSINGTUD/CryptoAnalysis-Android.git```

CogniCrypt_SAST for Android uses maven as build tool. To compile this project `cd` into the newly checked out folder and run

```mvn package -DskipTests=true```

Once build, a packaged  `jar` artifact including all dependency is found in `CryptoAnalysis-Android/build/CryptoAnalysis-Android-1.0.0-jar-with-dependencies.jar` 

## Usage

CogniCrypt_SAST for Android can be started via the class `main.CogniCryptAndroid`. It requires three arguments in this order: 
* The absolute path to the .apk file
* The absolute path to the android SDK platforms
* The absolute path to the CrySL rules in binary format (see description in Readme [here](https://github.com/CROSSINGTUD/CryptoAnalysis))

```
java -cp CryptoAnalysis-Android/build/CryptoAnalysis-Android-1.0.0-jar-with-dependencies.jar main.CogniCryptAndroid \
      <path-to-apk> <path-to-android-platforms> <path-to-crysl-rules-binary>
```
