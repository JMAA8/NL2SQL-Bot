package com.example.chatbot.DTO;

public class GroupDTO {
    private Long id;
    private String groupName;

    public GroupDTO(Long id, String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }
}
