package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;

@ApplicationScoped
public class JwtRoleService {

    @Inject SecurityIdentity identity;   // Quarkus
    @Inject JsonWebToken jwt;            // MicroProfile JWT

    public AppRole getCurrentAppRole() {
        Set<String> roles = new LinkedHashSet<>();

        // 1) Quarkus Identity (bevorzugt)
        if (identity != null && !identity.isAnonymous()) {
            roles.addAll(identity.getRoles());
        }

        // 2) MP-JWT groups
        if (roles.isEmpty() && jwt != null && jwt.getGroups() != null) {
            roles.addAll(jwt.getGroups());
        }

        // 3) generische Claims
        if (roles.isEmpty() && jwt != null) {
            addAnyClaim(roles, jwt.getClaim("role"));
            addAnyClaim(roles, jwt.getClaim("roles"));
            addAnyClaim(roles, jwt.getClaim("authorities"));
        }

        // 4) Keycloak-typisch
        if (roles.isEmpty() && jwt != null) {
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map<?,?> m) addAnyClaim(roles, m.get("roles"));
            Object resAccess = jwt.getClaim("resource_access");
            if (resAccess instanceof Map<?,?> rm) {
                for (Object v : rm.values()) if (v instanceof Map<?,?> c) addAnyClaim(roles, c.get("roles"));
            }
        }

        // Mapping-Priorität
        if (anyMatch(roles, "admin")) return AppRole.ADMIN;
        if (anyMatch(roles, "advanced_user", "professor")) return AppRole.ADVANCED_USER;
        return AppRole.BASIC_USER;
    }

    /** JWT userId → in deiner DB = benutzer.benutzer_id */
    public Long getCurrentUserId() {
        // Quarkus Identity Attribut?
        if (identity != null) {
            Object v = identity.getAttribute("userId");
            if (v instanceof Number n) return n.longValue();
            if (v != null) return Long.valueOf(v.toString());
        }
        // MP-JWT Claim?
        if (jwt != null) {
            Object v = jwt.getClaim("userId");
            if (v instanceof Number n) return n.longValue();
            if (v != null) return Long.valueOf(v.toString());
        }
        return null;
    }

    public String getCurrentUsername() {
        if (identity != null && identity.getPrincipal() != null) return identity.getPrincipal().getName();
        if (jwt != null && jwt.getName() != null) return jwt.getName();
        Object un = (jwt != null) ? jwt.getClaim("username") : null;
        return un == null ? null : String.valueOf(un);
    }

    private static boolean anyMatch(Collection<String> roles, String... needles) {
        for (String r : roles) {
            String x = r == null ? "" : r.toLowerCase(Locale.ROOT);
            for (String n : needles) if (x.contains(n)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void addAnyClaim(Set<String> out, Object claim) {
        if (claim == null) return;
        if (claim instanceof String s) {
            if (s.contains(",")) for (String p : s.split(",")) out.add(p.trim());
            else out.add(s.trim());
        } else if (claim instanceof Collection<?> c) {
            for (Object o : c) if (o != null) out.add(String.valueOf(o).trim());
        } else if (claim instanceof String[] arr) {
            for (String s : arr) if (s != null) out.add(s.trim());
        }
    }
}
