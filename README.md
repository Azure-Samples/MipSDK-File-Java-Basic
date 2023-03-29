---
page_type: sample
languages:
- java
products:
- m365
- office-365
name: "MIP File SDK Java Sample"
description: "This sample application demonstrates using the Microsoft Information Protection SDK Java wrapper to label and read a label from a file."
urlFragment: MipSDK-File-Java-Basic
---

# MIP SDK Java Wrapper Sample

This sample application demonstrates a very basic MIP SDK Java wrapper sample. It'll demonstrate how to create the project, add dependencies, and get to a place where the app can run on both Windows and Ubuntu. 

## Features

This project framework provides the following features:

* User authentication
* List Labels
* Apply Label
* Read Label
* Read Protection Information

## Getting Started

### Prerequisites

- Windows 10 or Ubuntu 18.04/20.04
- [MIP SDK Java Wrapper - 1.13 Preview](https://aka.ms/mipsdkbins)
- Visual Studio Code
- An Azure AD Application Registration for use with a [MIP SDK public client.](https://docs.microsoft.com/en-us/information-protection/develop/setup-configure-mip#register-a-client-application-with-azure-active-directory)

### Installation

- Windows
  - Install your JDK of choice.
  - Install [Maven](http://maven.apache.org/download.cgi)

- Ubuntu
  - Install JDK: `sudo apt-get install default-jdk`
  - Install Maven: `sudo apt-get install maven`
  - Install MIP SDK Dependencies: `sudo apt-get install scons libgsf-1-dev libssl-dev libsecret-1-dev freeglut3-dev libcpprest-dev libcurl3-dev uuid-dev libboost-all-dev`

### Quickstart

1. git clone https://github.com/Azure-Samples/mipsdk-filesdk-java-sample.git
2. cd mipsdk-filesdk-java-sample
3. Install the MIP SDK JAR:
  - Windows: `<PATH TO MAVEN>\mvn.cmd org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file  -Dfile=<PATH TO MIP SDK>\java-sdk-wrapper.jar`
  - Ubuntu: `mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file  -Dfile=java-sdk-wrapper.jar`

**Note:** Not using a fully qualified path for the -Dfile parameter may cause an exception. Use a full path.

4. The Java wrapper download contains two folders: file_sdk and java_wrapper. 
   
     - Copy all DLLs or SOs from the **bins\debug** folder in **file_sdk** for the appropriate architecture into your project folder root (the cloned project root). 
     - Copy **mip_java.dll** or **mip_java.so** from the java_wrapper folder into the project folder root (the cloned project root).
   >  This requirement of copying to the project root is a bug and will be fixed in a future version. 
5. Update the ApplicationId on line 22 in App.java to match your application registration in Azure AD.  
6. Open the project folder in VS Code and load the project when prompted. 
7. Update the following block in **App.java** to include your clientId.
  ```java
    appInfo.setApplicationId("YOUR CLIENT ID");
    appInfo.setApplicationName("MIP SDK Java Sample");
    appInfo.setApplicationVersion("1.13");
  ```
  
At this point, you should be able to build the project. If your app states that dependencies are missing:

1. Press **CTRL-SHIFT-P** and finding `Maven: Add a dependency...`
2. Search for **msal4j** and install.
3. Press **CTRL-SHIFT-P** and finding `Maven: Add a dependency...`
4. Search for **slf4j-simple** and install.

## Resources

- [SDK Docs](https://aka.ms/mipsdkdocs)
- [SDK Samples](https://aka.ms/mipsdksamples)
- [MSAL for Java](https://github.com/AzureAD/microsoft-authentication-library-for-java)
