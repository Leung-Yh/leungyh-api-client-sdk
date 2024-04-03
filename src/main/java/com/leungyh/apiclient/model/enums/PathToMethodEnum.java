package com.leungyh.apiclient.model.enums;

/**
 * @author Leungyh
 */
public enum PathToMethodEnum {
    name("/api/user", "getUsernameByPost"),
    loveTalk("/api/loveTalk", "getLoveTalk")
    ;

    private final String path;

    private final String method;

    PathToMethodEnum(String path, String method) {
        this.path = path;
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}
