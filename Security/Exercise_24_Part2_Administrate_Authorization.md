# Exercise 24 Part 2: Administrate Authorizations


## Learning Goal
TODO


## Step: Administrate Authorizations for your Business Application
As of now you've configured your xsuaa service with the application security model ([xs-security.json](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-webmvc/blob/solution-24-Make-App-Secure/security/xs-security.json)).<sub><b>[to-do]</b></sub> With that, the xsuaa has the knowledge about the role-templates. But you as a user have still no permission to access the advertisement endpoints, as the required scopes or roles are not yet assigned to your user. 

To administrate authorizations for your business application, perform the steps of the procedure [HowTo Administrate Authorizations for CF Applications using the SAP CP Cockpit][9].

Afterwards you need to logon again to your application so that the authorities are assigned to the user. You can provoke a logon screen when clearing your cache. Now you should have full access to all of your application endpoints.

> **Troubleshoot**
> You can analyze the authorities that are assigned to the current user via `https://d012345trial.authentication.sap.hana.ondemand.com/config?action=who`

## Further Reading
- [Spring Security Reference](http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#abstractsecuritywebapplicationinitializer)
- [Method Security Example](https://github.wdf.sap.corp/cc-java/cc-bulletinboard-ads-spring-boot/commit/c3150c398ba7e18f703dd06e8c5943a261445293)<sub><b>[to-do]</b></sub>
- [Expression-Based Access Control](https://docs.spring.io/spring-security/site/docs/3.0.x/reference/el-access.html)



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
