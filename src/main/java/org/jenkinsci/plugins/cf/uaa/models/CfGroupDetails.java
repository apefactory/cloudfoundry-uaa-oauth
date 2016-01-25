package org.jenkinsci.plugins.cf.uaa.models;

import hudson.security.GroupDetails;

public class CfGroupDetails extends GroupDetails {

    final String name;

    public CfGroupDetails(final String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
