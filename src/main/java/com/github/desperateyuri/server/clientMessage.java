package com.github.desperateyuri.server;

import java.util.Map;

public record clientMessage(Command command, Map<String, Object> map) {
    public enum Command{
        LOGIN, REGISTER
    }
}