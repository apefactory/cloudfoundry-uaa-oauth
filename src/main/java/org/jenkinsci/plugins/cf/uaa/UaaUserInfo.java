package org.jenkinsci.plugins.cf.uaa;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.tasks.Mailer;

import java.io.IOException;

/**
 * Represents an identity information from the oauth provider.
 */
public class UaaUserInfo extends UserProperty {

    private String email;

    private String name;

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Updates the user information on Hudson based on the information in this identity.
     */
    public void updateProfile(hudson.model.User u) throws IOException {
        // update the user profile by the externally given information
        if (this.email != null) {
            u.addProperty(new Mailer.UserProperty(this.email));
        }
        if (this.name != null) {
            u.setFullName(this.name);
        }

        u.addProperty(this);
    }

    @Extension
    public static class DescriptorImpl extends UserPropertyDescriptor {

        @Override
        public UserProperty newInstance(User user) {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }
    }
}
