package com.example.chatbot.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserRolesId implements Serializable {
    private Long user;
    private Long role;

    public UserRolesId() {}

    public UserRolesId(Long user, Long role) {
        this.user = user;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRolesId that = (UserRolesId) o;
        return Objects.equals(user, that.user) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, role);
    }
}
