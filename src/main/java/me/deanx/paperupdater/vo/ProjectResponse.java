package me.deanx.paperupdater.vo;

import java.util.List;
import java.util.Map;

public class ProjectResponse {
    private Map<String, List<String>> versions;

    public Map<String, List<String>> getVersions() {
        return versions;
    }
}