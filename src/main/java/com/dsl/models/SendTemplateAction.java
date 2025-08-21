package com.dsl.models;

import lombok.Data;

@Data
public class SendTemplateAction implements Action {
    private String templateName;
}