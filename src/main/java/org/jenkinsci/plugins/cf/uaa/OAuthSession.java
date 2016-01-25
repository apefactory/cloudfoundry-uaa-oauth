package org.jenkinsci.plugins.cf.uaa;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import hudson.remoting.Base64;
import hudson.util.HttpResponses;
import org.apache.http.client.utils.URIBuilder;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * The state of the OAuth request.
 *
 * Verifies the validity of the response by comparing the state.
 */
public abstract class OAuthSession {

    private String uuid = Base64.encode(UUID.randomUUID().toString().getBytes()).substring(0,20);

    private String loginServerUrl;
    private String uaaClientId;
    private String redirectUrl;


    public OAuthSession(final String loginServerUrl, final String redirectUrl, final String uaaClientId) {
        this.loginServerUrl = loginServerUrl;
        this.redirectUrl = redirectUrl;
        this.uaaClientId= uaaClientId;
    }


    /** Starts the login session. */
    public HttpResponse doCommenceLogin(final StaplerRequest request) throws IOException {
        Stapler.getCurrentRequest().getSession().setAttribute(SESSION_NAME, this);

        final ArrayList scopes = Lists.newArrayList(
                new String[]{"openid", "oauth.approvals", "scim.me", "cloud_controller.read"}
        );
        try {
            final URIBuilder uriBuilder = new URIBuilder(loginServerUrl);
            uriBuilder
                    .setPath("/oauth/authorize")
                    .addParameter("response_type", "code")
                    .addParameter("redirect_uri", redirectUrl)
                    .addParameter("client_id", this.uaaClientId)
                    .addParameter("scope", Joiner.on(' ').join(scopes))
                    .addParameter("state", uuid);
            final String redirectToLoginUri = uriBuilder.build().toString();

            return new HttpRedirect(redirectToLoginUri);
        } catch (final URISyntaxException s) {
            throw new IOException(s);
        }
    }

    /** When the identity provider is done with its thing, the user comes back here. */
    public HttpResponse doFinishLogin(final StaplerRequest request) throws IOException {
        if (!uuid.equals(request.getParameter("state"))) {
            return HttpResponses.error(401, "State is invalid");
        }
        String code = request.getParameter("code");
        if (request.getParameter("error") != null) {
            return HttpResponses.error(401, "Error from provider: " + code);
        } else if (code == null) {
            return HttpResponses.error(404, "Missing authorization code");
        } else {
            return onSuccess(code);
        }
    }

    protected abstract HttpResponse onSuccess(final String authorizationCode) throws IOException;

    /** Gets the {@link OAuthSession} associated with HTTP session in the current extend. */
    public static OAuthSession getCurrent() {
        return (OAuthSession) Stapler.getCurrentRequest().getSession().getAttribute(SESSION_NAME);
    }

    private static final String SESSION_NAME = OAuthSession.class.getName();
}
