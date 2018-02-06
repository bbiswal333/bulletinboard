Exercise 23: Setup Generic Authorization
========================================

## Learning Goal
After this exercise you will know how to define an authorization model for your application and how to configure generic authorizations for any endpoint (path) of your application without any code changes.

The task of this exercise is to declare a "start authorization" for the security descriptor of your application and reference it from the configuration file of the approuter, so that the approuter is able to perform a generic authorization check on the declared start authorization.

## Prerequisite
Continue with your solution of the last exercise. If this does not work, you can checkout the branch [`origin/solution-22-Deploy-AppRouter`](https://github.com/SAP/cloud-bulletinboard-ads/tree/solution-22-Deploy-AppRouter).
## Step 1: Define Scopes and Role Templates

Before we can enable generic authorization checks we need to define so-called **"application scopes"** in order to define functional authorizations such as "Display", "Update" and "Delete".

And we also have to define **"role templates"** that specify roles and its scopes. Later, users are assigned to roles and thereby get the specified scopes as authorization keys. 

##### Create the security descriptor file `xs-security.json`

To declare the **Role Templates**, create the security descriptor file `xs-security.json` either in the root folder or in a `security` subfolder. Include the following authorization model of your application:

```json
{
    "xsappname"     : "bulletinboard-<<your user id>>",
    "description"   : "Enabled bulletinboard for multi tenants",
    "tenant-mode"   : "shared",
    "scopes"        : [
                        {
                          "name"                 : "$XSAPPNAME.Display",
                          "description"          : "Display advertisements"
                        },
                        {
                          "name"                 : "$XSAPPNAME.Update",
                          "description"          : "Update advertisements"
                        }
                      ],
    "role-templates": [
                        {
                          "name"                 : "Viewer",
                          "description"          : "View advertisements",
                          "scope-references"     : [
                                                        "$XSAPPNAME.Display"             
                                                   ]                                            
                        },
                        {
                          "name"                 : "Advertiser",
                          "description"          : "Maintain advertisements",
                          "scope-references"     : [
                                                        "$XSAPPNAME.Display",             
                                                        "$XSAPPNAME.Update"             
                                                   ]                                            
                        }
    ]
}
```

Notes: 
* Don't forget to **replace `<<your user id>>` as part of the xsappname** to refer to the same xsappname you have created the UAA service instance with. Furthermore the value of `xsappname` must be unique within the whole Cloud Foundry org. 
* The `shared tenant-mode` leads the XSUAA service instance to trust other tenants that are different to the one that corresponds to the Cloud Foundry org.
* We have now defined a generic scope for starting the application. Typically, every authorization model of an application contains application/domain specific scopes ([see next exercise](Exercise_24_MakeYourApplicationSecure.md)).

## Step 2: Configure Start Conditions for Routes / Endpoints

In order to define the **Start Condition** we need to configure our route(s) with the required scope definition: In the approuter configuration file `.src/main/approuter/xs-app.json` we add an additional line that references the **scope** previously defined in `./security/xs-security.json`:

```
{
    "welcomeFile": "index.html",
    "routes": [{
        "source": "^/ads/api/",
        "target": "/api/",
        "destination": "ads-destination",
        "scope": "$XSAPPNAME.Display"
      }, {
        "source": "^/ads",
        "target": "/",
        "destination": "ads-destination"
    }]
}
```

## Step 3: Deploy the (Updated) Application Security Descriptor to XSUAA

With the following command you can update the existing XSUAA service instance, which needs to know the authorization model of your application (`bulletinboard-<<your user id>>`).
```bash
# Ensure that you are in the project root e.g. ~/git/cc-bulletinboard-ads
$    cf update-service uaa-bulletinboard -c security/xs-security.json
```


## Step 4: Push and Test the Service

Push your application to Cloud Foundry:
```
$    cf push
```

Then enter the approuter URL  
e.g. `https://<<your tenant>>-approuter-<<your user id>>.cfapps.<<region>>.hana.ondemand.com/ads/api/v1/ads` in the browser. Make sure that it responds with message "Forbidden" (HTTP Status code `403`). The `<<region>>` needs to be replaced with eu10 or us10 depending on the trial environment where you have registered. For more details, please refer the [documentation](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/350356d1dc314d3199dca15bd2ab9b0e.html).


 
***
<dl>
  <dd>
  <div class="footer">&copy; 2018 SAP SE</div>
  </dd>
</dl>
<hr>
<a href="Exercise_22_DeployApplicationRouter.md">
  <img align="left" alt="Previous Exercise">
</a>
<a href="Exercise_24_MakeYourApplicationSecure.md">
  <img align="right" alt="Next Exercise">
</a>
