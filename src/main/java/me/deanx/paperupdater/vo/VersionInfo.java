package me.deanx.paperupdater.vo;

public class VersionInfo {
    private String id;
    private Java java;

    public String getId() {
        return id;
    }

    public int getMinimumJavaVersion() {
        return java != null && java.version != null ? java.version.minimum : 0;
    }

    private static class Java {
        private Version version;
    }

    private static class Version {
        private int minimum;
    }
}