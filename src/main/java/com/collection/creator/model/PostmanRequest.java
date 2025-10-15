package com.collection.creator.model;

import java.util.List;

public class PostmanRequest {

    private Info info;

    private List<Item> item;

    // Getters and setters
    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }
}
