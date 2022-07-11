package me.deanx.paperupdater;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadController {
    private String version;
    private String versionFamily;
    private int build;
    private String outputFile;
    private Downloader downloader = null;

    public DownloadController() {
        this(DEFAULT_OUTPUT);
    }

    public DownloadController(String outputFile) {
        this(null, null, outputFile);
    }

    public DownloadController(String version, String versionFamily, String outputFile) {
        this(version, versionFamily, -1, outputFile);
    }

    public DownloadController(String version, String versionFamily, int build, String outputFile) {
        setVersionFamily(versionFamily);
        setVersion(version);
        setBuild(build);
        setOutputFile(outputFile);
    }

    public boolean download() {
        updateDownloader();
        String version = getCalculatedVersion();
        boolean success;
        try {
            if (build < 0) {
                success = downloader.downloadLatestBuild(version);
            } else {
                success = downloader.downloadBuild(version, build);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return success;
    }

    public String getUrl() {
        String version = getCalculatedVersion();
        int build = getCalculatedBuild();
        return downloader.getUrl(version, build);
    }

    public String getCalculatedVersion() {
        if (version == null) {
            try {
                String[] versionList;
                if (versionFamily != null) {
                    versionList = getDownloader().getVersionsFromVersionFamily(versionFamily);
                } else {
                    versionList = getDownloader().getVersions();
                }
                return versionList[versionList.length - 1];
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return version;
    }

    public String getCalculatedVersionFamily() {
        if (versionFamily == null && version != null) {
            return getVersionFamilyFromVersion(version);
        }
        return versionFamily;
    }

    public int getCalculatedBuild() {
        if (build < 0) {
            try {
                return getDownloader().getLatestBuild(getCalculatedVersion());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return build;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (version == null || version.isBlank()) {
            this.version = null;
            return;
        }
        version = version.strip();
        if (versionFamily != null) {
            if (!isInVersionFamily(version, versionFamily)) {
                throw new IllegalArgumentException("Version does not match with the version family");
            }
        }
        Pattern pattern = Pattern.compile("\\d(?:\\.\\d{1,2})+");
        Matcher matcher = pattern.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version pattern");
        }
        this.version = version;
    }

    public String getVersionFamily() {
        return versionFamily;
    }

    public void setVersionFamily(String versionFamily) {
        if (versionFamily == null || versionFamily.isBlank()) {
            this.versionFamily = null;
            return;
        }
        versionFamily = versionFamily.strip();
        if (version != null) {
            if (!isInVersionFamily(version, versionFamily)) {
                throw new IllegalArgumentException("Version family does not match with the version");
            }
        }
        Pattern pattern = Pattern.compile("\\d\\.\\d{1,2}");
        Matcher matcher = pattern.matcher(versionFamily);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version family pattern");
        }
        this.versionFamily = versionFamily;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        if (build < -1) {
            throw new IllegalArgumentException("Invalid build");
        }
        this.build = build;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        if (outputFile == null || outputFile.isBlank()) {
            this.outputFile = DEFAULT_OUTPUT;
            return;
        }
        this.outputFile = outputFile.strip();
    }

    private Downloader getDownloader() {
        if (downloader == null) {
            downloader = new Downloader();
        }
        return downloader;
    }

    private void updateDownloader() {
        if (downloader == null) {
            downloader = new Downloader(outputFile);
        } else {
            downloader.setOutputFile(outputFile);
        }
    }

    static private boolean isInVersionFamily(String version, String versionFamily) {
        String calculatedVersionFamily = getVersionFamilyFromVersion(version);
        return versionFamily.equals(calculatedVersionFamily);
    }

    static private String getVersionFamilyFromVersion(String version) {
        Pattern pattern = Pattern.compile("^\\d\\.\\d{1,2}");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return matcher.group();
        } else {
            throw new IllegalArgumentException("Invalid version pattern");
        }
    }

    private static final String DEFAULT_OUTPUT = "./paper.jar";
}
