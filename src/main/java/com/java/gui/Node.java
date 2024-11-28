package com.java.gui;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Node {
    private String path;
    private String name;

    @Override
    public String toString() {
        return this.name;
    }
}