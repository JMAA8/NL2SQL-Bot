package com.example.chatbot.unidb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;

@ApplicationScoped
public class JwtRoleService {

    @Inject SecurityIdentity identity;
    @Inject JsonWebToken jwt;

    public AppRole getCurrentAppRole() {
        Set<String> roles = new LinkedHashSet<>();
        if (identity != null && !identity.isAnonymous()) roles.addAll(identity.getRoles());
        if (roles.isEmpty() && jwt != null && jwt.getGroups() != null) roles.addAll(jwt.getGroups());
        if (roles.isEmpty() && jwt != null) {
            addAny(roles, jwt.getClaim("roles"));
            addAny(roles, jwt.getClaim("authorities"));
            Object realm = jwt.getClaim("realm_access");
            if (realm instanceof Map<?,?> m) addAny(roles, m.get("roles"));
            Object res = jwt.getClaim("resource_access");
            if (res instanceof Map<?,?> rm) rm.values().forEach(v -> {
                if (v instanceof Map<?,?> client) addAny(roles, client.get("roles"));
            });
        }
        String s = roles.stream().map(x -> x == null ? "" : x.toLowerCase(Locale.ROOT)).findFirst().orElse("");
        if (s.contains("admin")) return AppRole.ADMIN;
        if (s.contains("advanced") || s.contains("professor")) return AppRole.ADVANCED_USER;
        return AppRole.BASIC_USER;
    }

    /** Erwartet im JWT einen Claim 'userId' (long) oder im PrincipalName eine Zahl. */
    public Long getCurrentUserId() {
        if (jwt != null) {
            Object v = jwt.getClaim("userId");
            if (v instanceof Number n) return n.longValue();
            if (v instanceof String st && st.matches("\\d+")) return Long.parseLong(st);
            String sub = jwt.getSubject();
            if (sub != null && sub.matches("\\d+")) return Long.parseLong(sub);
        }
        if (identity != null && identity.getPrincipal() != null) {
            String name = identity.getPrincipal().getName();
            if (name != null && name.matches("\\d+")) return Long.parseLong(name);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void addAny(Set<String> out, Object claim) {
        if (claim == null) return;
        if (claim instanceof String s) {
            if (s.contains(",")) Arrays.stream(s.split(",")).forEach(p -> out.add(p.trim()));
            else out.add(s.trim());
        } else if (claim instanceof Collection<?> c) {
            c.forEach(o -> { if (o != null) out.add(String.valueOf(o).trim()); });
        } else if (claim instanceof String[] arr) {
            for (String s : arr) if (s != null) out.add(s.trim());
        }
    }
}
