package me.deanx.paperupdater;

public class DownloadController {
    private String version = null;
    private String versionFamily = null;
    private int build;
    private String outputFile;
    private Operation operation;
    private Downloader downloader = null;

    public DownloadController() {
        build = 0;
        outputFile = DEFAULT_OUTPUT;
        operation = Operation.download;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (versionFamily != null && version != null) {
            if (!isInVersionFamily(version, versionFamily)) {
                throw new IllegalArgumentException("Version does not match with the version family");
            }
        }
        this.version = version;
    }

    public String getVersionFamily() {
        return versionFamily;
    }

    public void setVersionFamily(String versionFamily) {
        if (version != null && versionFamily != null) {
            if (!isInVersionFamily(version, versionFamily)) {
                throw new IllegalArgumentException("Version family does not match with the version");
            }
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
        return version.contains(versionFamily);
    }

    private static final String DEFAULT_OUTPUT = "./paper.jar";
}
