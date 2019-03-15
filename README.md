# emsrc-android-core

This is the Mobile Sensing Android app of the European Mobile Sensing Research Consortium.


## How To Get Started

* Checkout _emsrc-android-core_ project to a folder of your choice ```git clone https://github.com/emsrc-team/emsrc-android-core.git```
* navigate into the folder that was created on checkout  ```cd emsrc-android-core```
* Here checkout all other modules' repositories you want to use. E.g. ```git clone https://github.com/emsrc-team/emsrc-android-wifi.git```

In order to work properly, it is important that the folders _common_, _core_, and all module folders (e.g. _logging_wifi_) are in the same directory.
Later you don't have to worry about having multiple repositories in one project, Android Studio handles this fine.

## Project Modules Structure

The EMSRC Android App consists of 2 + x modules, where x is the amount of logging components.

### core

This is the main module, containing the app stuff like UI, LoggingService, the main gradle build file, etc. All other modules (_common_ and the logging component modules) are used by this module as a dependency.

### common

This module contains code that is required as a dependency by both the _core_ module and the logging component modules. For example it implements interfaces for logging components or general helper classes.

### logging modules

E.g. _logging_wifi_
Each logging module implements a logging functionality - this is where the actual magic happens. They can be plugged in and out from the app build through Gradle dependency configuration - see below for more details.

## Build Flavors and logging component inclusion

Despite the build types _debug_ and _release_, the EMSRC project configures a build flavor dimension named _study_. There are the flavors _emsrc_, _isabella_, _phonestudy_ and _remap_. Which flavor actually is used can be selected in Android Studio's Build Variants window (on the very left of Android Studio). 
Build flavors affect the following:

### Sourcesets
The default sourceset used is _src/main/..._ . This one is used in the _emsrc_ flavor, which kinda is our default.
If the flavor _remap_ is used, the default sourceset and the sourceset _src/remap/..._ are merged. This allows customizing the app, e.g. overriding the app name by creating a _src/remap/res/values/strings.xml_ file (which would replace the default _src/main/res/values/strings.xml_ in _remap_ flavor builds).

### Logging Module Inclusion
The used study flavor also affects which logging component modules will be compiled into the app. This is defined in the core module's Gradle file _core/build.gradle_:
```
    // components that are included in all flavors
    implementation project(":logging_wifireceiver")

    // components that phonestudy builds will include
    phonestudyImplementation project(":logging_appusage")

    // components that isabella builds include
    // --- no ones yet ----

    // components that are included in remap builds
    remapImplementation project(":logging_appusage")
```
Here, all builds include the wifi logging component, but only phonestudy and remap additionally include the appusage logging component. This is the single point of configuration, defining which logging components will be compiled + started in the app.

## Creating a new Logging Component Module

In order to make that smooth component management work, a logging component must be setup correctly:
* Include the _common_ module as a dependency ```implementation project(":common")```
* Contain a "main" class, implementing the interface _ILoggingComponent_
* The module's Android Manifest must contain a meta-data entry, registering that "main" class:
```xml
<manifest ... >
    <application>
        ...
        <meta-data
            android:name="org.emsrc.logging.component.appusage"
            android:value="org.emsrc.logging.appusage.AppUsageLoggingComponent" />
    </application>
</manifest>
```
  The _name_ property must start with _org.emsrc.logging.component._ and end with a unique identifier
  The _value_ property must be the "main" class which implements _ILoggingComponent_
  
  
  
## WIP: Development Guidelines

* Customization through sourcesets should be used rarely (especially overriding Java classes can reduce maintainability!)
* Dependencies, permissions, ... belonging to a logging component should be implemented there. E.g. the _ACCESS_WIFI_ permission should be requested in the _logging_wifi_ module's AndroidManifest.xml, and not in the core's Manifest.
