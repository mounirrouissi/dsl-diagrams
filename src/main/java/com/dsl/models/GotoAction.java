package com.dsl.models;

import lombok.Data;

@Data
public class GotoAction implements Action {
    private String targetStateId;
}