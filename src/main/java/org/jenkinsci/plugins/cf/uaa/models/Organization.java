package org.jenkinsci.plugins.cf.uaa.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization {

    @JsonProperty("name")
    private String name;

    @JsonProperty("billing_enabled")
    private boolean billingEnabled;

    @JsonProperty("quota_definition_guid")
    private UUID quotaDefinitionGuid;

    @JsonProperty("status")
    private String status;

    @JsonProperty("quota_definition_url")
    private String quotaDefinitionUrl;

    @JsonProperty("spaces_url")
    private String spacesUrl;

    @JsonProperty("domains_url")
    private String domainsUrl;

    @JsonProperty("private_domains_url")
    private String privateDomains;

    @JsonProperty("users_url")
    private String usersUrl;

    @JsonProperty("managers_url")
    private String managersUrl;

    @JsonProperty("billing_managers_url")
    private String billingManagersUrl;

    @JsonProperty("auditors_url")
    private String auditorsUrl;

    @JsonProperty("app_events_url")
    private String appEventsUrl;

    @JsonProperty("space_quota_definitions_url")
    private String spaceQuotaDefinitionsUrl;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBillingEnabled() {
        return billingEnabled;
    }

    public void setBillingEnabled(boolean billingEnabled) {
        this.billingEnabled = billingEnabled;
    }

    public UUID getQuotaDefinitionGuid() {
        return quotaDefinitionGuid;
    }

    public void setQuotaDefinitionGuid(UUID quotaDefinitionGuid) {
        this.quotaDefinitionGuid = quotaDefinitionGuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQuotaDefinitionUrl() {
        return quotaDefinitionUrl;
    }

    public void setQuotaDefinitionUrl(String quotaDefinitionUrl) {
        this.quotaDefinitionUrl = quotaDefinitionUrl;
    }

    public String getSpacesUrl() {
        return spacesUrl;
    }

    public void setSpacesUrl(String spacesUrl) {
        this.spacesUrl = spacesUrl;
    }

    public String getDomainsUrl() {
        return domainsUrl;
    }

    public void setDomainsUrl(String domainsUrl) {
        this.domainsUrl = domainsUrl;
    }

    public String getPrivateDomains() {
        return privateDomains;
    }

    public void setPrivateDomains(String privateDomains) {
        this.privateDomains = privateDomains;
    }

    public String getUsersUrl() {
        return usersUrl;
    }

    public void setUsersUrl(String usersUrl) {
        this.usersUrl = usersUrl;
    }

    public String getManagersUrl() {
        return managersUrl;
    }

    public void setManagersUrl(String managersUrl) {
        this.managersUrl = managersUrl;
    }

    public String getBillingManagersUrl() {
        return billingManagersUrl;
    }

    public void setBillingManagersUrl(String billingManagersUrl) {
        this.billingManagersUrl = billingManagersUrl;
    }

    public String getAuditorsUrl() {
        return auditorsUrl;
    }

    public void setAuditorsUrl(String auditorsUrl) {
        this.auditorsUrl = auditorsUrl;
    }

    public String getAppEventsUrl() {
        return appEventsUrl;
    }

    public void setAppEventsUrl(String appEventsUrl) {
        this.appEventsUrl = appEventsUrl;
    }

    public String getSpaceQuotaDefinitionsUrl() {
        return spaceQuotaDefinitionsUrl;
    }

    public void setSpaceQuotaDefinitionsUrl(String spaceQuotaDefinitionsUrl) {
        this.spaceQuotaDefinitionsUrl = spaceQuotaDefinitionsUrl;
    }
}