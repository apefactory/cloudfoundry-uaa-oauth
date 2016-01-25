package org.jenkinsci.plugins.cf.uaa;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.representation.Form;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.jenkinsci.plugins.cf.uaa.models.AccessToken;
import org.jenkinsci.plugins.cf.uaa.models.CfGroupDetails;
import org.jenkinsci.plugins.cf.uaa.models.CfResource;
import org.jenkinsci.plugins.cf.uaa.models.CfResources;
import org.jenkinsci.plugins.cf.uaa.models.ClientAccessToken;
import org.jenkinsci.plugins.cf.uaa.models.Organization;
import org.jenkinsci.plugins.cf.uaa.models.SearchResults;
import org.jenkinsci.plugins.cf.uaa.models.UaaUserProfile;
import org.jenkinsci.plugins.cf.uaa.models.UserAccessToken;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


class CfApiUtil {

    /** Used for logging purposes. */
    private static final Logger LOG = Logger.getLogger(CfApiUtil.class.getName());

    private static final Function<String, GrantedAuthority> GROUP_NAME_TO_GRANTED_AUTHORITY
            = new Function<String, GrantedAuthority>() {
        public GrantedAuthority apply(String organization) {
            return new GrantedAuthorityImpl(organization);
        }
    };

    private String clientId;
    private String clientSecret;
    private String uaaServerEndpoint;
    private String loginServerEndpoint;
    private String apiServerEndpoint;

    public CfApiUtil(final String clientId, final String clientSecret, final String uaaServerEndpoint,
                     final String loginServerEndpoint, final String apiServerEndpoint) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.uaaServerEndpoint = uaaServerEndpoint;
        this.loginServerEndpoint = loginServerEndpoint;
        this.apiServerEndpoint = apiServerEndpoint;
    }

    public ClientAccessToken getClientAccessToken() {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(clientId, clientSecret));
        WebResource webResource = client.resource(uaaServerEndpoint + "/oauth/token");
        Form form = new Form();
        form.add("client_id", clientId);
        form.add("grant_type", "client_credentials");
        form.add("response_type", "token");

        ClientResponse response = webResource
                .accept("application/json")
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, form);

        return getTokenFromResponse(response, ClientAccessToken.class);
    }

    public UserAccessToken getAccessTokenByAuthorizationCode(final String authorizationCode, final String redirectUri) {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(clientId, clientSecret));
        WebResource webResource = client.resource(loginServerEndpoint + "/oauth/token");
        Form form = new Form();
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);

        ClientResponse response = webResource
                .accept("application/json")
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ClientResponse.class, form);

        return getTokenFromResponse(response, UserAccessToken.class);
    }

    public UserDetails loadUserByUsername(final String userName) {
        final ClientAccessToken accessToken = getClientAccessToken();
        final String userId = getUserId(userName, accessToken);
        LOG.fine("loadUserByUsername user details for " + userName + " -> " + userId);

        final List authorities = Lists.newArrayList(
                new GrantedAuthority[]{SecurityRealm.AUTHENTICATED_AUTHORITY}
        );
        authorities.addAll(
                Lists.transform(
                        getOrganizations( "/v2/users/" + userId + "/organizations", accessToken),
                        GROUP_NAME_TO_GRANTED_AUTHORITY)
        );

        return new User(userName, "RANDOM_PASSWORD", true, true, true, true, Iterables.toArray(authorities, GrantedAuthority.class)
        );
    }

    public GroupDetails loadGroupByGroupname(final String groupName) {
        LOG.fine("loadGroupByGroupname for " + groupName);
        return new CfGroupDetails(groupName);
    }

    public UaaUserProfile getUserProfile(final AccessToken accessToken)  {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource webResource = client.resource(uaaServerEndpoint + "/userinfo");

        ClientResponse response = webResource
                .accept("application/json")
                .header("Authorization", accessToken.getTokenType() + " " + accessToken.getAccessToken())
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            LOG.warning("Couldn't retrieve user profile - response code: " + response.getStatus());
            throw new AuthenticationServiceException(
                    "Couldn't retrieve user profile - response code: " + response.getStatus()
            );
        }
        ObjectMapper mapper = new ObjectMapper();
        UaaUserProfile userProfile = null;
        try {
            userProfile = mapper.readValue(response.getEntityInputStream(), UaaUserProfile.class);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An exception raised while trying get user profile.", e);
            throw new AuthenticationServiceException("An exception raised while trying get user profile.", e);
        }
        return userProfile;
    }

    public GrantedAuthority[] getUserGrantedAuthorities(final AccessToken accessToken) {
        final List authorities = Lists.newArrayList(
                new GrantedAuthority[]{SecurityRealm.AUTHENTICATED_AUTHORITY}
        );
        final List<String> groups = getOrganizations("/v2/organizations", accessToken);
        authorities.addAll(Lists.transform(groups, GROUP_NAME_TO_GRANTED_AUTHORITY));
        return Iterables.toArray(authorities, GrantedAuthority.class);
    }

    private String getUserId(final String userName, final ClientAccessToken clientToken) {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource webResource = client.resource(uaaServerEndpoint + "/Users/")
                .queryParam("attributes", "id")
                .queryParam("filter", "userName eq \"" + userName + "\"");

        ClientResponse response = webResource.accept("application/json")
                .header("Authorization", clientToken.getTokenType() + " " + clientToken.getAccessToken())
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            LOG.warning("Couldn't retrieve user id - response code: " + response.getStatus());
            throw new AuthenticationServiceException(
                    "Couldn't retrieve user id - response code: " + response.getStatus()
            );
        }

        ObjectMapper mapper = new ObjectMapper();
        String userId = null;
        try {
            SearchResults<Map<String, Object>> results = mapper.readValue(response.getEntityInputStream(),
                    new TypeReference<SearchResults<Map<String, Object>>>() {}
            );
            if (results.getTotalResults() == 1) {
                userId = (String) results.getResources().get(0).get("id");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An exception raised while trying get user id.", e);
            throw new AuthenticationServiceException("An exception raised while trying get user id.", e);
        }
        return userId;
    }

    private <T> T getTokenFromResponse(final ClientResponse response, Class<T> valueType) {
        if (response.getStatus() != 200) {
            LOG.warning("Couldn't retrieve the access token - response code: " + response.getStatus());
            throw new AuthenticationServiceException(
                    "Couldn't retrieve the access token - response code: " + response.getStatus()
            );
        }
        ObjectMapper mapper = new ObjectMapper();
        T token = null;
        try {
            token = mapper.readValue(response.getEntityInputStream(), valueType);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An exception raised while trying get user token.", e);
            throw new AuthenticationServiceException("An exception raised while trying get user token.", e);
        }
        return token;
    }


    private List<String> getOrganizations(final String path, final AccessToken accessToken) {
        final List<String> caOrgNames = new ArrayList<String>();

        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource webResource = client.resource(apiServerEndpoint + path);
        ClientResponse response = webResource
                .accept("application/json")
                .header("Authorization", accessToken.getTokenType() + " " + accessToken.getAccessToken())
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            LOG.warning("Couldn't retrieve user's organizations - response code: " + response.getStatus());
            throw new AuthenticationServiceException(
                    "Couldn't retrieve user's organizations - response code: " + response.getStatus()
            );
        }

        ObjectMapper mapper = new ObjectMapper();
        CfResources<Organization> result = null;
        try {
            result = mapper.readValue(response.getEntityInputStream(),
                    new TypeReference<CfResources<Organization>>() {}
            );

            int totalPages = result.getTotalPages();
            if(totalPages > 1) {
                LOG.log(Level.WARNING, "More than 1 ("
                                + totalPages
                                + ") pages of organizations for users - only process the first page"
                );
            }
            List<CfResource<Organization>> caOrgs = result.getResources();
            for (CfResource<Organization> organization : caOrgs) {
                if ("active".equals(organization.getEntity().getStatus())) {
                    caOrgNames.add(organization.getEntity().getName());
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "An exception raised while trying get user's organizations.", e);
            throw new AuthenticationServiceException("An exception raised while trying get user's organizations.", e);
        }

        return caOrgNames;
    }

}
