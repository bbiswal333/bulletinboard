openSAP course: [Cloud-Native Development with SAP Cloud Platform](https://open.sap.com/courses/cp5) - Advertisement Service
=============================================

This repo contains the source code for the **bulletinboard-ads** service that is developed during the course. There are multiple branches with the solution state (source code) after each exercise. 
 
# Characteristics
- Java 8, [Spring](https://github.com/spring-projects/spring-framework) with additional components like `Spring Web MVC` and `JPA` as documented [here](https://github.wdf.sap.corp/refapps/cc-bulletinboard-ads-spring-webmvc/blob/Documentation/Knowledge/Tools.md).
- Offers RESTful CRUD-Services on a [Tomcat](http://tomcat.apache.org/) web server
- Persists data on PostgreSQL database
- Provides Service and Unit tests
- Sends notification to a MessageQueue whenever an advertisement is requested. This is consumed by a `cc-bulletinboard-statistics` app.
- Does a direct REST call to the app [`cc-bulletinboard-users`](https://bulletinboard-users.cfapps.sap.hana.ondemand.com) to get user information, only premium users are allowed to create advertisements.

<img src="https://github.wdf.sap.corp/refapps/cc-bulletinboard-ads-spring-webmvc/blob/Documentation/Z_ReuseImages/images/Domain.png" width="600" />

# How to run

## Prerequisites
- Have a trial account on [SAP CP Cloud Foundry](https://help.cf.sap.hana.ondemand.com/).
- Setup your development environment with java, maven, node.js, npm, postgresql ... Therefore please follow the [installations steps](https://github.wdf.sap.corp/refapps/cc-bulletinboard-ads-spring-webmvc/blob/Documentation/VMImage/VMImage_GettingStarted.md) to prepare your VM image that provides the whole development environment.
- Run `VirtualBox` and start your Virtual Machine (VM).

## Import project from GitHub
Open the terminal and execute the following commands to download the source code from this GitHub repository:
```
mkdir git
cd git 
git clone git@github.wdf.sap.corp:refapps/cc-bulletinboard-ads-spring-webmvc.git
cd cc-bulletinboard-ads-spring-webmvc
```
Ensure that you are in the **project root e.g. ~/git/cc-bulletinboard-ads-spring-webmvc**.

## Personalize the application
Especially when you like to you deploy the application you need to ensure that you have adjusted the `xsappname` in all relevant files, i.e. you have changed the `userid` to your `<<your-user-id>>`:
  - localEnvironmentSetup.bat
  - localEnvironmentSetup.sh
  - manifest.yml
  - xs-security.json
  - WebSecurityConfig.java
  - JwtGenerator.java

## Run the application in your local environment
To run the service locally you have two options: start it directly from Eclipse as described [here](https://github.wdf.sap.corp/refapps/cc-bulletinboard-ads-spring-webmvc/blob/Documentation/CreateMicroservice/Exercise_1_GettingStarted.md) or via Maven on the command line.

### Using the command line
Execute in terminal (within project root e.g. ~/git/cc-bulletinboard-ads-spring-webmvc, which contains the`pom.xml`):
```
source localEnvironmentSetup.sh
mvn tomcat7:run
```

In both cases, your application will be deployed to an embedded Tomcat web server and is visible at the address `http://localhost:8080/api/v1/ads`.

## Test locally

The service endpoints are secured, that means no unauthorized user can access the endpoint. The application expects a so called `JWT token` as part of the `Authorization` header of the service that also contains the scope, the user is assigned to.

Test the REST Service `http://localhost:8080/api/v1/ads` manually using a REST client

**Note**: For all requests make sure, that you provide a header namely `Authorization` with a JWT token as value e.g. `Bearer eyJhbGciOiJSUzI1NiIs...`. You can generate a valid JWT token as described [in Exercise 24](https://github.wdf.sap.corp/refapps/cc-bulletinboard-ads-spring-webmvc/blob/Documentation/Security/Exercise_24_MakeYourApplicationSecure.md).

- **POST two advertisements**
- As shown in the screen shot above, provide the body type as **raw JSON (application/json)**. This results in the content-type to be set in the header as `Content-Type=application/json` 
- Ensure that the location is returned in the header and that the entity is returned in the body.
- **GET all advertisements**  
Ensure that all created advertisements are returned.
- **GET advertisement by id**    
Ensure that the advertisement you created before is returned.


## Steps to deploy to Cloud Foundry

### [Optionally] Build Approuter (a Node.JS application)
Build the Approuter that connects your service to the centrally provided "user account and authentication (UAA) service" which is a JavaScipt application running on NodeJS. With `npm install` the NPM package manager downloads all packages (node modules) it depends on (as defined in [package.json](/src/main/approuter/package.json). With this the node modules are downloaded from the npm registry and are copied into the directory `src/main/approuter/node_modules/approuter`. 

Execute in terminal (within `src/main/approuter` directory, which contains the`package.json`):
```
npm install
```

### Build Advertisement Service (our Java application)
Build the Advertisement Service which is a Java web application running in a Java VM. With `mvn package` Maven build tool takes the compiled code and package it in its distributable format, such as a `WAR` (Java web archive). With this the maven dependencies are downloaded from the SAP internal Nexus registry and are copied into the directory `~/.m2/repository`. Furthermore the JUnit tests (unit tests and component tests) are executed and the `target/bulletinboard-ads.war` is created. 

Execute in terminal (within root directory, which contains the`pom.xml`):
```
mvn package
```

### Login to Cloud Foundry
Make sure your are logged in to Cloud Foundry and you target your trial space. Run the following commands in the terminal:
```
cf api https://api.cf.sap.hana.ondemand.com
cf login
cf target -o  <<your-user-id>>trial_trial -s dev   ## replace by your space name
```

### Create Services
Create the (backing) services that are specified in the [`manifest.yml`](/manifest.yml).

Execute in terminal (within root directory, which contains the `security` folder):
```
cf create-service postgresql v9.4-dev postgres-bulletinboard-ads
cf create-service rabbitmq  v3.6-dev mq-bulletinboard
cf create-service xsuaa application uaa-bulletinboard -c security/xs-security.json
cf create-service application-logs lite applogs-bulletinboard
```
Using the marketplace (`cf m`) you can see the backing services and the plans which are currently available in the cloud.

### Deploy the approuter and the advertisement service
As a prerequisite step open the `manifest.yml` and replace the d-user by your sap user, to make the routes unique.

The application can be built and pushed using these commands (within root directory, which contains the`manifest.yml`):
```
cf push -f manifest.yml
```
The application will be pushed using the settings in the provided in `manifest.yml`. You can get the exact urls/routes that have been assigned to the application with `cf apps`.

### Create approuter routes per tenant
We make use of the trial subaccount such as `<<your-user-id>>trial` that has a 1-1 relationship to the **Identity Zone `useridtrial`** and which is configured for the **trial CF Org** and is under your control. Note furthermore that the `TENANT_HOST_PATTERN` environment variable ( see `manifest.yml` file) specifies how the approuter should derive the tenant from the URL.
```
cf map-route approuter cfapps.sap.hana.ondemand.com -n <<your-user-id>>trial-approuter-<<your-user-id>>
```

### Test the deployed application 
Open a browser to test whether your microservice runs in the cloud. For this use the approuter URL `https://<<your-user-id>>trial-approuter-<<your-user-id>>.cfapps.sap.hana.ondemand.com/ads/health`. This will bring you the **login page**. Note: You have to enter here not your Cloud Foundry credentials. You need to sign up to the XSUAA identity provider first. After successful login you get redirected to the advertisement service that return you an empty list of advertisements `[]`.

This [`xs-app.json`](/src/main/approuter/xs-app.json) file specifies how the approuter routes are mapped to the advertisement routes.

Find a step-by-step description on how to test within `REST client` [here](https://github.wdf.sap.corp/refapps/cc-bulletinboard-ads-spring-webmvc/tree/Documentation/Security/Exercise_25_Create_SystemTest).

