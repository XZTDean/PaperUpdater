package me.deanx.paperupdater;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadController {
    private String version;
    private String versionFamily;
    private int build;
    private String outputFile;
    private Operation operation;
    private Downloader downloader = null;

    public DownloadController() {
        this(Operation.download);
    }

    public DownloadController(String outputFile) {
        this(outputFile, Operation.download);
    }

    public DownloadController(Operation operation) {
        this(DEFAULT_OUTPUT, operation);
    }

    public DownloadController(String outputFile, Operation operation) {
        this(null, null, outputFile, operation);
    }

    public DownloadController(String version, String versionFamily, String outputFile, Operation operation) {
        this(version, versionFamily, -1, outputFile, operation);
    }

    public DownloadController(String version, String versionFamily, int build, String outputFile, Operation operation) {
        setVersionFamily(versionFamily);
        setVersion(version);
        this.build = build;
        this.outputFile = outputFile;
        this.operation = operation;
    }

    public boolean download() {
        if (operation != Operation.download) {
            throw new IllegalStateException("Wrong operation");
        }
        updateDownloader();
        // TODO
        return true;
    }

    public String info() {
        if (operation != Operation.display) {
            throw new IllegalStateException("Wrong operation");
        }
        // TODO
        return "";
    }

    public String getCalculatedVersion() {
        if (version == null && versionFamily != null) {
            try {
                String[] versionList = getDownloader().getVersionsFromVersionFamily(versionFamily);
                return versionList[versionList.length - 1];
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (version == null || version.isBlank()) {
            this.version = null;
            return;
        }
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

    public String getCalculatedVersionFamily() {
        if (versionFamily == null && version != null) {
            return getVersionFamilyFromVersion(version);
        }
        return versionFamily;
    }

    public String getVersionFamily() {
        return versionFamily;
    }

    public void setVersionFamily(String versionFamily) {
        if (versionFamily == null || versionFamily.isBlank()) {
            this.versionFamily = null;
            return;
        }
        if (version != null) {
            if (!isInVersionFamily(version, versionFamily)) {
                throw new IllegalArgumentException("Version family does not match with the version");
            }
        }
        Pattern pattern = Pattern.compile("\\d\\.\\d{1,2}");
        Matcher matcher = pattern.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version family pattern");
        }
        this.versionFamily = versionFamily;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        if (outputFile == null) {
            this.outputFile = DEFAULT_OUTPUT;
        }
        this.outputFile = outputFile;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
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

    public enum Operation {
        download, display
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
