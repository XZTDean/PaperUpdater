package me.deanx.paperupdater.vo;

import java.util.Map;

public class BuildInfoResponse {
    private int id;
    private String time;
    private String channel;
    private Map<String, DownloadInfo> downloads;

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getChannel() {
        return channel;
    }

    public Map<String, DownloadInfo> getDownloads() {
        return downloads;
    }

    public DownloadInfo getServerDownload() {
        return downloads != null ? downloads.get("server:default") : null;
    }

    public static class DownloadInfo {
        private String name;
        private Checksums checksums;
        private long size;
        private String url;

        public String getName() {
            return name;
        }

        public Checksums getChecksums() {
            return checksums;
        }

        public long getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }
    }

    public static class Checksums {
        private String sha256;

        public String getSha256() {
            return sha256;
        }
    }
}