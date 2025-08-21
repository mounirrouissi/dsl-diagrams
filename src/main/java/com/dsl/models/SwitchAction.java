package com.dsl.models;

import lombok.Data;
import java.util.Map;

@Data
public class SwitchAction implements Action {
    private String function;
    private Map<String, String> branches; // e.g., {"VIP": "R1", "REG": "R2"}
}