package com.collection.creator.model;

import java.util.List;

public class Item {
    private String name;
    private Request request;
    private List<Object> response; // Assuming response is empty in this case

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public List<Object> getResponse() {
        return response;
    }

    public void setResponse(List<Object> response) {
        this.response = response;
    }
}

