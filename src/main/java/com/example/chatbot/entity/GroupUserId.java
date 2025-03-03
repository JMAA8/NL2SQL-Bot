package com.example.chatbot.entity;

import java.io.Serializable;
import java.util.Objects;

public class GroupUserId implements Serializable {
    private Long group;
    private Long user;

    public GroupUserId() {}

    public GroupUserId(Long group, Long user) {
        this.group = group;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupUserId that = (GroupUserId) o;
        return Objects.equals(group, that.group) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, user);
    }
}
