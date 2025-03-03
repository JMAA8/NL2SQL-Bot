package com.example.chatbot.repository;

import com.example.chatbot.entity.GroupUser;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GroupUserRepository implements PanacheRepository<GroupUser> {
}

