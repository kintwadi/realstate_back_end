package com.imovel.api.security;

public class Policies {
    // Property-related permissions
    public static final String PROPERTY_CREATE = "property:create";
    public static final String PROPERTY_READ = "property:read";
    public static final String PROPERTY_UPDATE = "property:update";
    public static final String PROPERTY_DELETE = "property:delete";
    public static final String PROPERTY_DEACTIVATE = "property:deactivate";
    public static final String PROPERTY_MANAGE_ALL = "property:manage_all";
    
    // User-related permissions
    public static final String USER_CREATE = "user:create";
    public static final String USER_READ = "user:read";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_DEACTIVATE = "user:deactivate";
    
    // Financial permissions
    public static final String FINANCIAL_READ = "financial:read";
    public static final String FINANCIAL_REPORT = "financial:report";
    
    // System permissions
    public static final String SYSTEM_CONFIGURE = "system:configure";
    
    // Agent permissions
    public static final String AGENT_APPROVE = "agent:approve";
    
    // Dispute permissions
    public static final String DISPUTE_RESOLVE = "dispute:resolve";
    
    // Tenant permissions
    public static final String TENANT_APPLICATION = "tenant:application";
    public static final String TENANT_WISHLIST = "tenant:wishlist";
    public static final String TENANT_COMPARE = "tenant:compare";
}
