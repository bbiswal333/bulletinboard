Exercise 25: Implement Systemtests for Bulletinboard
====================================================
## Learning Goal
A System test is an end-to-end test. It has a broader test boundary and covers a business use case, unlike the tests you have already written in previous exercises. Unit and integration tests, written for one particular microservice, give us confidence that the logic of that microservice is correct.

![alt text][11]

However, we also need system tests to ensure that the system behavior of multiple cooperating microservices satisfies the requirements of a business use case.

The system test we are about to write will test the complete "bulletinboard" business app, which consists of three microservices: the actual advertisement service, the user service and the application router.

![alt text][13]

The task of this exercise is to write a system test to verify the following use case: a premium user can create an advertisement successfully. 

## Security Implications and Introduction of the `Muenchhausen Service`
The previous exercise ended with your application deployed on Cloud Foundry including full activated application security. When running the local JUnit tests via the service endpoints (not the approuter), we were able to mock authentication and authorization by "faking" a JWT Token (@see [`JwtGenerator`][3]) and injecting it into the request "Authorization" header field.

The system test will no longer call the service endpoints directly **but instead send requests to the application router of your business app**. The application router obtains the JWT Token from XS UAA. As for the fact that we do not have control over the application router, nor can we change its implementation, we are not anymore able to inject a JWT token into the request when running the system test.

### `Muenchhausen Service`
What we can do though is provide a mocked User with the help of a "fake" Identity Provider (IdP). The fake IdP is called the **Muenchhausen Service**. The Muenchhausen Service is also a microservice running in the Cloud Foundry landscape. The documentation can be [found here][8].

With the Muenchhausen Service we can mock a user with the necessary authorizations required by the system test. How this works is shown in the following picture:

![alt text][14]

1. The system test sends the first request via HTTP method POST to the **Muenchhausen Service**. The POST body contains the **tenantadmin credentials** of the **identity zone** and user information for which the **Muenchhausen Service** is supposed to create a **SAML 2 assertion**.
2. The **Muenchhausen Service** verifies the tenantadmin credentials, creates the SAML 2 assertion and sends it in a request to XSA UAA. XSA UAA parses the **SAML 2 user goups**, derives the **role collections** from the respective **SAML 2 user group mappings** and determines the roles, scopes and attributes. Finally it create the **authorization code**. The **Muenchhausen Service** sends the authorization code as part of the response back to the system test.
3.  From this point on, the request flow works exactly like in a productive environment: The system test sends the authorization code to the **login callback endpoint** of the application router (<approuter-url>/login/callback).
4. The application router sends a request with its own credentials **(client id and client secret)** and the authorization code to XSA UAA. XSA UAA creates a JWT token and includes the JWT token into the response. The application router creates a **JSESSIONID** and maps that to the JWT token. The application router includes the **JSESSIONID** into the response and sends it to the system test.
5. The system test includes the **JSESSIONID** in its request to the service endpoint and sends the request to the application router. The application router looks up the mapped JWT token, adds it to the request and forwards it to the respective service endpoint.
6. The service endpoint is now able to check if the request is authorized - which means that the request header contains a field "Authorization" with the JWT Token as value - and if the JWT token contains the required authorizations. The response is forwarded via the application router back to the system test. The system test can now perform assertions on the expected state of the response (OK, NOT AUTHORIZED, FORBIDDEN, etc.).

**Note:** The application router will regard the request to the service endpoint only then as authenticated if the request header contains a valid **JSESSIONID**. Valid in this context means that a **JWT token is mapped to the JSESSIONID** and that the respective session is still active. That is why the system test sends the request to the service endpoint only after the JSESSIONID has been obtained.

**Disclaimer:** As the name implies, Muenchhausen needs to be treated with caution: **it lies!** Muenchhausen automatically establishes a trust relationship and asserts any user with any attributes to the targeted UAA server. Due to its stateless nature and the sensitivity of the actions, it requires credentials with strong authorizations to be passed along in the API (a tenant administrator user), which should **NEVER** be uploaded to SCM like GitHub!

In order to use the service you need to know/setup the following infrastructure:
- tenant id (= UAA ID Zone) e.g. `d012345trial`
- tenant admin (id and secret) 
- create a tenant subscription for the authorization administration tool


## Prerequisites
Continue with the solution of your last exercise. If this does not work, you can checkout the branch [`solution-24-Make-App-Secure`][15] and follow the desciption [here](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/blob/solution-24-Make-App-Secure/README.md#steps-to-deploy-to-cloud-foundry) to deploy your application on Cloud Foundry.

In particular ensure that you have changed the D-Number `d012345` to your D/C/I-Number to adjust the `xsappname` in all relevant files:
  - localEnvironmentSetup.bat
  - localEnvironmentSetup.sh
  - manifest.yml
  - xs-security.json
  - WebSecurityConfig.java
  
Call command `cf service uaa-bulletinboard` to check that UAA service instance is created with **`application`** plan.


## Step 1: Setup your Test Environment

### Create `bulletinboard-systemtest` project
Import the master branch of this [Git Project](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-systemtest) as described below:
- Select `File - Import - Git - Projects from Git`. 
- In the next dialog, select `Clone URI`. In the next dialog, enter the URI `git@github.wdf.sap.corp:cc-java/cc-bulletinboard-systemtest.git`. 
- Click `Next` (and `Finish`) in the following dialogs, and make sure to select the `master` branch. The default settings should be OK.

**Note**: The system test is an end-to-end JUnit test, whereas the "system" is treated as a black box and the tests exercise as much of the fully deployed system as possible . As the system tests should be executed as an independent job as part of your Continuous Integration/Delivery Pipeline the bulletinboard-systemtest needs to be established as an independent project.

### Get to know the Code 
Please have a look into the code of the imported `bulletinboard-systemtests` project. A more detailed description can be found [here][6].


## Step 2: Prepare Muenchhausen for your `trial` Subaccount

The following steps are done in the [SAP Cloud Cockpit](https://account.int.sap.hana.ondemand.com/cockpit#/home).   
 
### Establish trust to Muenchhausen Fake IdP
- Download the `metadata.xml` of Muenchhausen via the corresponding /saml/metadata endpoint e.g. `https://xsuaa-monitoring-idp.cfapps.sap.hana.ondemand.com/`
- Navigate to the Subaccount of the Cloud Foundry Organization in which your application is running.
- Create a **new Trust Configuration** by uploading the `metadata.xml` **(enter as name the `EntityId`)**:

![](/Security/images/AddMuenchhausenFakeIdP.png)

### Map Role Collection to SAML2 User Group
- Navigate to the Trust Configuration of the IdP.
- Create a **new Role Collection Mapping**. Choose the role collection from the drop-down list and enter a value for the SAML2 User Group e.g. `UG_CC_M2_USER`.


## Step 3: Adapt System Test properties to the values of your Cloud Environment

The system test requires information from the cloud environment of your business app and is configured in file `src/main/resources/config.properties`. The following steps describe how to derive and maintain this information:

- Set property `saml2.usergroup.admin` to your SAML2 User Group that you have previously mapped to the role colllection of your business application e.g. `UG_CC_M2_USER`.
- Display the environment of your application router with command `cf env approuter` and scroll back to the **XSUAA Client Credentials String**, which can be found in `VCAP_SERVICES` environment variable:
    ```
     "VCAP_SERVICES": {
      "xsuaa": [
       {
        "credentials": {
         "clientid": "sb-bulletinboard-d012345!27",
         "clientsecret": "A6asa8NKNQqasdrDOukshgPiqmPc="
     ...
    ```
  - Set property `xsuaa.clientid` to the value of `clientid` from the XSUAA Client Credentials String in `VCAP_SERVICES`.
  - Set property `xsuaa.clientsecret` to the value of `clientsecret` from the XSUAA Client Credentials String in `VCAP_SERVICES`.

- Set property `approuter.url` to the URL of your approuter (e.g. `https://d012345trial-approuter-d012345.cfapps.sap.hana.ondemand.com`), which is displayed in the output of command `cf apps`.
- Set property `fakeidp.redirect.uri` to the URL of your approuter (e.g. `https://d012345trial-approuter-d012345.cfapps.sap.hana.ondemand.com`) plus endpoint `/login/callback`.


## Step 4: Run the Tests

The system test is based on the assumption that all services, the approuter and the advertisement are deployed in a testing space of Cloud Foundry. The user service is provided to you as an external service and is accessible at this [url](https://bulletinboard-users-course.cfapps.sap.hana.ondemand.com).

### Run in Eclipse

Right click on the `bulletinboard-systemtests` project, choose `Run As`, then select `JUnit Test`.
  
### Run on the Command Line
Ensure that you are in the project root e.g. `~/git/cc-bulletinboard-systemtest`.

```
$     mvn clean verify
```


## Used Frameworks and Tools
- [Hamcrest Tutorial][7]

## Further Reading
- [Muenchhausen][8]

[2]: https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/Security/Exercise_22_DeployApplicationRouter.md "Exercise 22"
[3]: https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/blob/solution-24-Make-App-Secure/src/test/java/com/sap/bulletinboard/ads/testutils/JwtGenerator.java "JWT Token Generator"
[6]: https://github.wdf.sap.corp/cc-java/cc-bulletinboard-systemtest/tree/master "Get to know the code"
[7]: https://code.google.com/p/hamcrest/wiki/Tutorial "Hamcrest Tutorial"
[8]: https://xsuaa-monitoring-idp.cfapps.sap.hana.ondemand.com "Muenchhausen Service"
[11]: https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/TestStrategy/images/ComponentTest_JWTToken.png
[13]: https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/TestStrategy/images/SystemTest.png
[14]: https://github.wdf.sap.corp/cc-java-dev/cc-coursematerial/blob/master/TestStrategy/images/SystemTest_detailed.png
[15]: https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/tree/solution-24-Make-App-Secure "Solution Exercise 24"


***
<dl>
  <dd>
  <div class="footer">&copy; 2018 SAP SE</div>
  </dd>
</dl>
<hr>
<a href="/Security/Exercise_24_MakeYourApplicationSecure.md">
  <img align="left" alt="Previous Exercise">
</a>
