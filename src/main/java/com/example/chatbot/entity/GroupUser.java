package com.example.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@IdClass(GroupUserId.class)
@Table(name = "group_users")
public class GroupUser {

    @Id
    @ManyToOne(fetch = FetchType.EAGER) // Sofort laden, um LazyInitializationException zu vermeiden
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Id
    @ManyToOne(fetch = FetchType.EAGER) // Sofort laden, um LazyInitializationException zu vermeiden
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public GroupUser() {}

    public GroupUser(Group group, User user) {
        this.group = group;
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
