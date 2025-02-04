package com.example.chatbot.DTO;

public class GroupCreate {
    public String groupNameCreated;
    public Long ownerIdCreated;
    public String groupPasswordCreated;

    public String getGroupNameCreated() {
        return groupNameCreated;
    }

    public void setGroupNameCreated(String groupNameCreated) {
        this.groupNameCreated = groupNameCreated;
    }

    public Long getOwnerIdCreated() {
        return ownerIdCreated;
    }

    public void setOwnerIdCreated(Long ownerIdCreated) {
        this.ownerIdCreated = ownerIdCreated;
    }

    public String getGroupPasswordCreated() {
        return groupPasswordCreated;
    }

    public void setGroupPasswordCreated(String groupPasswordCreated) {
        this.groupPasswordCreated = groupPasswordCreated;
    }
}
