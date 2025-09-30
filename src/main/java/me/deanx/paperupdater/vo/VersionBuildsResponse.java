package me.deanx.paperupdater.vo;

import java.util.List;

public class VersionBuildsResponse {
    private VersionInfo version;
    private List<Integer> builds;

    public VersionInfo getVersion() {
        return version;
    }

    public List<Integer> getBuilds() {
        return builds;
    }
}