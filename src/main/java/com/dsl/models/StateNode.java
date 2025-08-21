package com.dsl.models;

import lombok.Data;
import java.util.List;


public class StateNode {
    @Override
    public String toString() {
        return "StateNode{" +
                "id='" + id + '\'' +
                ", actions=" + actions +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    private String id; // e.g., "S1", "S2"
    private List<Action> actions;
}