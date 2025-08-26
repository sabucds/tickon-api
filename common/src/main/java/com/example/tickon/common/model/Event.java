package com.example.tickon.common.model;

import java.util.UUID;

public class Event {
    private UUID id;
    private String name;

    public Event() {}
    
    public Event(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }
}