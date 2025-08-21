package com.dsl.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SwitchAction.class, name = "SWITCH"),
    @JsonSubTypes.Type(value = SendTemplateAction.class, name = "SEND"),
    @JsonSubTypes.Type(value = GotoAction.class, name = "GOTO")
})
public interface Action {}