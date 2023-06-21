---
page_type: sample
languages:
- java
products:
- azure
description: "This is a sample application to showcase the use of Spring Cloud Function on top of Azure Functions."
urlFragment: hello-spring-function-azure
---

# Example "Hello, world" Spring Boot application that runs on Azure Functions

This is a sample application to showcase the use of Spring Cloud Function on top of Azure Functions.

## Features

This is just a "Hello, world", but it uses domain objects so it's easy to extend to do something more complex.

## Getting Started

### Prerequisites

This project uses the Maven Wrapper, so all you need is Java installed.

### Installation

- Clone the project: `git clone https://github.com/Azure-Samples/hello-spring-function-azure.git`
- Configure the project to use your own resource group and your own application name (it should be unique across Azure)
  - Open the `pom.xml` file
  - Customize the `functionResourceGroup` and `functionAppName` properties, make sure the function app name is unique, as your function will have an endpoint in the format: `<functionAppName>.azurewebsites.net`
  - Update `applicationPhoneNumber`, `phoneNumberToAddToCall`, `connectionString` and `callbackBaseUri` in Constants.java
- Build the project: `./mvnw clean package`

### Quickstart

Once the application is built, you can run it locally using the Azure Function Maven plug-in:

`./mvnw azure-functions:run`

And you can test it using a cURL command:

`curl http://localhost:7071/api/hello -d '{"name": "Azure"}'`

## Deploying to Azure Functions

Deploy the application on Azure Functions with the Azure Function Maven plug-in:

`./mvnw azure-functions:deploy`

You can then test the running application, by running a POST request:

```
curl https://<YOUR_SPRING_FUNCTION_NAME>.azurewebsites.net/api/hello -d '{"name": "Azure"}'
```

Or a GET request:

```
curl https://<YOUR_SPRING_FUNCTION_NAME>.azurewebsites.net/api/hello?name=Azure
```

Replace the `<YOUR_SPRING_FUNCTION_NAME>` part by the name of your Spring Function.

## Setup Event Grid event for call actions, e.g: Incoming Call.

See guide here: `https://learn.microsoft.com/en-us/azure/communication-services/quickstarts/voice-video-calling/handle-calling-events?source=recommendations`

You need to setup an Incoming call event webhook that points to: `https://<functionAppName>.azurewebsites.net/api/incomingCall`
