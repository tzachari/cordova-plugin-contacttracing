# Contact Tracing Cordova Plugin

A [Cordova](https://cordova.apache.org/) plugin for iOS and Android that implements Bluetooth contact-tracing. 

This plugin is compatible with the [Bluetooth protocol specified by TCN Coalition](https://github.com/TCNCoalition/TCN#tcn-sharing-with-bluetooth-low-energy). 

In the near future, it will be updated to use the [joint protocol announced by Apple & Google](https://covid19-static.cdn-apple.com/applications/covid19/current/static/contact-tracing/pdf/ContactTracing-BluetoothSpecificationv1.1.pdf).

The current version requires iOS 13+ or Android 6+.

The plugin is used in [CV-19-Track](https://github.com/covid19database/phone-app), a reference phone app for contact-tracing efforts at UC Berkeley's RISE Lab.

For Bluetooth contact-tracing on stationary devices, try [Lab11's NodeJS contact-tracing app](https://github.com/lab11/contact-tracing).


## Installation

Install using the Apache Cordova command line:

    cordova plugin add cordova-plugin-contact-tracing

