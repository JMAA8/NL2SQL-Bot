package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;

@ApplicationScoped
public class JwtRoleService {

    @Inject SecurityIdentity identity;         // Quarkus Security, falls verfügbar
    @Inject JsonWebToken jwt;                  // MicroProfile JWT

    public AppRole getCurrentAppRole() {
        Set<String> roles = new LinkedHashSet<>();

        // 1) SecurityIdentity (bevorzugt)
        if (identity != null && !identity.isAnonymous()) {
            roles.addAll(identity.getRoles());
        }

        // 2) MicroProfile JWT „groups“
        if (roles.isEmpty() && jwt != null && jwt.getGroups() != null) {
            roles.addAll(jwt.getGroups());
        }

        // 3) Diverse gängige Claims (roles, authorities)
        if (roles.isEmpty() && jwt != null) {
            addAnyClaim(roles, jwt.getClaim("roles"));
            addAnyClaim(roles, jwt.getClaim("authorities"));
        }

        // 4) Keycloak-üblich: realm_access.roles
        if (roles.isEmpty() && jwt != null) {
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map<?,?> m) {
                addAnyClaim(roles, m.get("roles"));
            }
        }

        // 5) Keycloak-üblich: resource_access.<client>.roles
        if (roles.isEmpty() && jwt != null) {
            Object resAccess = jwt.getClaim("resource_access");
            if (resAccess instanceof Map<?,?> rm) {
                for (Object v : rm.values()) {
                    if (v instanceof Map<?,?> client) {
                        addAnyClaim(roles, client.get("roles"));
                    }
                }
            }
        }

        // Mapping mit Priorität: ADMIN > ADVANCED_USER > BASIC_USER
        if (anyMatch(roles, "admin")) return AppRole.ADMIN;
        if (anyMatch(roles, "advanced_user", "professor")) return AppRole.ADVANCED_USER;
        return AppRole.BASIC_USER;
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
            // könnte CSV oder einzelner Wert sein
            if (s.contains(",")) for (String p : s.split(",")) out.add(p.trim());
            else out.add(s.trim());
        } else if (claim instanceof Collection<?> c) {
            for (Object o : c) if (o != null) out.add(String.valueOf(o).trim());
        } else if (claim instanceof String[] arr) {
            for (String s : arr) if (s != null) out.add(s.trim());
        }
        // andere Typen ignorieren wir bewusst
    }
}
