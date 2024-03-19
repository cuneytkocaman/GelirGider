package com.cuneyt.gelirgider.entities;

public class UserModel {
    private String id;
    private String name;
    private String password;
    private String memberDateTime;

    public UserModel() {
    }

    public UserModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public UserModel(String id, String name, String memberDateTime) {
        this.id = id;
        this.name = name;
        this.memberDateTime = memberDateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMemberDateTime() {
        return memberDateTime;
    }

    public void setMemberDateTime(String memberDateTime) {
        this.memberDateTime = memberDateTime;
    }
}
