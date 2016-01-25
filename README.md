cloudfoundry-uaa-plugin
============

A Jenkins plugin which lets you login to Jenkins with your CloudFoundry UAA account. 

To use this plugin, you must obtain OAuth 2.0 credentials from your Cloud Foundry administrator, with the following client configuration

```ruby
jenkins_ci
    scope: cloud_controller.admin cloud_controller.read oauth.approvals openid scim.me scim.read scim.userids
    resource_ids: none
    authorized_grant_types: authorization_code client_credentials refresh_token
    authorities: scim.me scim.userids scim.read cloud_controller.admin oauth.login
```

