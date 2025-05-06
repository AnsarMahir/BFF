
package com.med4all.bff.security;
public class UserContext {

    private final String userId;
    private final String role;
    private final String orgId;

    public UserContext(String userId, String role, String orgId) {
        this.userId = userId;
        this.role = role;
        this.orgId = orgId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getOrgId() {
        return orgId;
    }
}
