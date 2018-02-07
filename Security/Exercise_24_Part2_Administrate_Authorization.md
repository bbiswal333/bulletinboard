# Exercise 24 Part 2: Administrate Authorizations

## Learning Goal
As of now you've configured your xsuaa service with the application security model ([xs-security.json](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/blob/solution-24-Make-App-Secure/security/xs-security.json)).<sub><b>[to-do]</b></sub> With that, the xsuaa has the knowledge about the role-templates. But you as a User have still no permission to access the advertisement endpoints, as the required scopes or roles are not yet assigned to your user.

In this exercise you will use the SAP CP Cockpit to maintain authorizations for your application and assign them to you or to other members of your Subaccount (cloud foundry organization). 

## Prerequisite
Continue with your solution of the last exercise. If this does not work, you can checkout the branch [solution-24-Make-App-Secure](https://github.com/SAP/cloud-bulletinboard-ads/tree/solution-24-Make-App-Secure).

## Step 1: Administrate Authorizations for your Business Application
To administrate authorizations for your business application, perform the following steps:

- Navigate to your Subaccount
- Select menu item **Roles** from the **Security** menu on the left side of the screen and perform the procedure visualized in the screenshots:  
<img src="/Security/images/CockpitRoleCollectionCreate.jpg" width="700">  
<img src="/Security/images/CockpitRoleCollectionAddRole.jpg" width="700">  

- Navigate back to your Subaccount  
- Select menu item **Trust Configuration** from the **Security** menu on the left side of the screen and select the standard IdP SAP ID Service. Now perform the procedure visualized in the screenshot:    
<img src="/Security/images/CockpitRoleCollectionAssignToUser.jpg" width="700">  

## Step 2: Call deployed service
You need to logon again to your application so that the authorities are assigned to the user. You can provoke a logon screen when clearing your cache. 

Call your service endpoints via the approuter e.g. `https://<<your tenant>>-approuter-<<your user id>>.cfapps.<<region>>.hana.ondemand.com/ads/api/v1/ads` manually using the `Postman` Chrome plugin as explained [here](/Security/Exercise_24_MakeYourApplicationSecure.md#call-deployed-service).

Now you should have full access to all of your application endpoints.

> **Troubleshoot**
> You can analyze the authorities that are assigned to the current user via `https://<<your user id>>trial.authentication.<<region>>.hana.ondemand.com/config?action=who`. The `<<region>>` needs to be replaced with eu10 or us10 depending on the trial environment where you have registered. For more details, please refer the [documentation](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html)


***
<dl>
  <dd>
  <div class="footer">&copy; 2017 SAP SE</div>
  </dd>
</dl>
<hr>
<a href="Exercise_24_MakeYourApplicationSecure.md">
  <img align="left" alt="Previous Exercise">
</a>
