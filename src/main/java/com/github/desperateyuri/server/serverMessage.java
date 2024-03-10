package com.github.desperateyuri.server;

import java.util.Map;

public record serverMessage(Command command, Status status, Map<String, Object> map) {
    public enum Command{
        LOGIN, REGISTER
    }
    public enum Status{
        OK,  ERROR
    }
}
