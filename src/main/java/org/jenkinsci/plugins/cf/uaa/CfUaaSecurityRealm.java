package org.jenkinsci.plugins.cf.uaa;



import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.security.ImpersonatingUserDetailsService;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.cf.uaa.models.UaaUserProfile;
import org.jenkinsci.plugins.cf.uaa.models.UserAccessToken;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.dao.DataAccessException;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Login with Cloudfoundry UAA OAuth2
 */
public class CfUaaSecurityRealm extends SecurityRealm implements UserDetailsService {

    /** Used for logging purposes. */
    private static final Logger LOG = Logger.getLogger(CfUaaSecurityRealm.class.getName());

    private String clientId;
    private Secret clientSecret;
    private String uaaServerEndpoint;
    private String loginServerEndpoint;
    private String apiServerEndpoint;

    private CfApiUtil api;

    @DataBoundConstructor
    public CfUaaSecurityRealm(final String clientId, final String clientSecret, final String uaaServerEndpoint,
                              final String loginServerEndpoint, final String apiServerEndpoint) throws IOException {
        this.assertNotEmpty(clientId, "clientId");
        this.assertNotEmpty(clientSecret, "clientSecret");
        this.assertUrl(uaaServerEndpoint, "uaaServerEndpoint");
        this.assertUrl(apiServerEndpoint, "apiServerEndpoint");
        this.assertUrl(loginServerEndpoint, "loginServerEndpoint");

        this.clientId = clientId;
        this.clientSecret = Secret.fromString(clientSecret);
        this.uaaServerEndpoint = uaaServerEndpoint;
        this.loginServerEndpoint = loginServerEndpoint;
        this.apiServerEndpoint = apiServerEndpoint;

        api = new CfApiUtil(
                clientId, this.clientSecret.getPlainText(),
                uaaServerEndpoint, loginServerEndpoint, apiServerEndpoint
        );
    }

    public String getClientId() {
        return clientId;
    }

    public Secret getClientSecret() {
        return clientSecret;
    }


    public String getUaaServerEndpoint() {
        return uaaServerEndpoint;
    }

    public String getLoginServerEndpoint() {
        return loginServerEndpoint;
    }

    public String getApiServerEndpoint() {
        return apiServerEndpoint;
    }


    @Override
    public boolean allowsSignup() {
        return false;
    }

    @Override
    public String getLoginUrl() {
        return "securityRealm/commenceLogin";
    }

    public HttpResponse doCommenceLogin(final StaplerRequest request, @Header("Referer") final String referer) throws IOException {
        final String redirectOnFinish;
        if (request.getParameter("from") != null) {
            redirectOnFinish = request.getParameter("from");
        } else if (referer != null) {
            redirectOnFinish = referer;
        } else {
            redirectOnFinish = Jenkins.getInstance().getRootUrl();
        }

        return new OAuthSession(loginServerEndpoint, buildOAuthRedirectUrl(), clientId) {

            @Override
            protected HttpResponse onSuccess(final String authorizationCode) throws IOException {
                UserAccessToken token = api.getAccessTokenByAuthorizationCode(
                        authorizationCode, request.getRootPath() + "/securityRealm/finishLogin"
                );
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("UAA's UserAccount Token: " + token.getAccessToken());
                }

                UaaUserProfile userProfile = api.getUserProfile(token);
                token.setUserId(userProfile.getUserId());
                token.setUserName(userProfile.getUserName());
                final GrantedAuthority[] authorities = api.getUserGrantedAuthorities(token);
                // logs this user in.
                final UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(
                        userProfile.getEmail(), "", authorities
                );
                SecurityContextHolder.getContext().setAuthentication(upToken);

                // update the user profile.
                UaaUserInfo info = new UaaUserInfo();
                User u = User.get(upToken.getName());
                info.updateProfile(u);
                return new HttpRedirect(redirectOnFinish);
            }
        }.doCommenceLogin(request);
    }

    public HttpResponse doFinishLogin(final StaplerRequest request) throws IOException {
        return OAuthSession.getCurrent().doFinishLogin(request);
    }

    public UserDetails loadUserByUsername(final String userName) throws UsernameNotFoundException, DataAccessException {
        return api.loadUserByUsername(userName);
    }

    public GroupDetails loadGroupByGroupname(final String groupName)
            throws UsernameNotFoundException, DataAccessException {
        return api.loadGroupByGroupname(groupName);
    }

    @Override
    protected String getPostLogOutUrl(final StaplerRequest request, final Authentication auth) {
        return loginServerEndpoint + "/logout.do?redirect=" + Jenkins.getInstance().getRootUrl();
    }

    private String buildOAuthRedirectUrl() {
        String rootUrl = Jenkins.getInstance().getRootUrl();
        if (rootUrl == null) {
            throw new NullPointerException("Jenkins root url should not be null");
        } else {
            return rootUrl + "securityRealm/finishLogin";
        }
    }


    @Override
    public SecurityComponents createSecurityComponents() {
        return new SecurityComponents(
                new AuthenticationManager() {
                    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                        if (authentication instanceof AnonymousAuthenticationToken) {
                            return authentication;
                        }
                        throw new BadCredentialsException(
                                String.format("Unexpected authentication type %s.",authentication)
                        );
                    }
                },
                new ImpersonatingUserDetailsService(this)
        );
    }



    private void assertNotEmpty(String value, String name) {
        if(StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(String.format("%s must not be empty", new Object[]{name}));
        }
    }

    private void assertUrl(String uaaUrl, String name) {
        if(!uaaUrl.startsWith("http://") && !uaaUrl.startsWith("https://")) {
            throw new IllegalArgumentException(
                    String.format("%s must start with http: or https: protocol", new Object[]{name})
            );
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
        public String getDisplayName() {
            return "Login with Cloud Foundry UAA";
        }
    }

}
