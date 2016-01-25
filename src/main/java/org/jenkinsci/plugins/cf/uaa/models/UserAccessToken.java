package org.jenkinsci.plugins.cf.uaa.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAccessToken extends AccessToken {

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_id")
    private String userId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
