package me.deanx.paperupdater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelfUpdater {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/XZTDean/PaperUpdater/releases/latest";

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static class UpdateInfo {
        private final String version;
        private final String downloadUrl;
        private final boolean updateAvailable;

        public UpdateInfo(String version, String downloadUrl, boolean updateAvailable) {
            this.version = version;
            this.downloadUrl = downloadUrl;
            this.updateAvailable = updateAvailable;
        }

        public String getVersion() {
            return version;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public boolean isUpdateAvailable() {
            return updateAvailable;
        }
    }

    public UpdateInfo checkForUpdates() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_API_URL))
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Failed to check for updates. HTTP " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String latestVersion = json.get("tag_name").getAsString();

        // Remove 'v' prefix if present (e.g., "v1.3" -> "1.3")
        if (latestVersion.startsWith("v")) {
            latestVersion = latestVersion.substring(1);
        }

        // Find the JAR asset
        String downloadUrl = null;
        if (json.has("assets") && json.get("assets").isJsonArray()) {
            var assets = json.getAsJsonArray("assets");
            for (var asset : assets) {
                JsonObject assetObj = asset.getAsJsonObject();
                String name = assetObj.get("name").getAsString();
                if (name.endsWith(".jar") && name.startsWith("PaperUpdater-")) {
                    downloadUrl = assetObj.get("browser_download_url").getAsString();
                    break;
                }
            }
        }

        if (downloadUrl == null) {
            throw new RuntimeException("No JAR file found in latest release");
        }

        boolean updateAvailable = compareVersions(latestVersion, getCurrentVersion()) > 0;
        return new UpdateInfo(latestVersion, downloadUrl, updateAvailable);
    }

    public boolean downloadUpdate(String downloadUrl, String version) throws IOException, InterruptedException {
        Path currentDir = Paths.get("").toAbsolutePath();
        String fileName = "PaperUpdater-" + version + ".jar";
        Path outputPath = currentDir.resolve(fileName);

        HttpRequest request = HttpRequest.newBuilder(URI.create(downloadUrl))
                .header("Accept", "application/octet-stream")
                .GET()
                .build();

        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));

        if (response.statusCode() >= 400) {
            System.err.println("Failed to download update. HTTP " + response.statusCode());
            Files.deleteIfExists(outputPath);
            return false;
        }

        // Verify the file was actually downloaded and has content
        if (!Files.exists(outputPath) || Files.size(outputPath) == 0) {
            System.err.println("Downloaded file is empty or does not exist");
            Files.deleteIfExists(outputPath);
            return false;
        }

        return true;
    }

    public void deleteOldVersions(String currentVersion) throws IOException {
        Path currentDir = Paths.get("").toAbsolutePath();

        // Collect all old versions including the currently running JAR
        List<Path> oldVersions = Files.list(currentDir)
                .filter(path -> {
                    String name = path.getFileName().toString();
                    return name.startsWith("PaperUpdater-") &&
                           name.endsWith(".jar") &&
                           !name.equals("PaperUpdater-" + currentVersion + ".jar");
                })
                .collect(Collectors.toList());

        if (oldVersions.isEmpty()) {
            System.out.println("No old versions to delete.");
            return;
        }

        // Try to delete files that are not locked
        List<Path> lockedFiles = new ArrayList<>();
        for (Path path : oldVersions) {
            try {
                Files.delete(path);
                System.out.println("Deleted old version: " + path.getFileName());
            } catch (IOException e) {
                // File is locked (probably the running JAR)
                lockedFiles.add(path);
            }
        }

        // If there are locked files, create a cleanup script
        if (!lockedFiles.isEmpty()) {
            createCleanupScript(lockedFiles);
        }
    }

    private void createCleanupScript(List<Path> filesToDelete) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        StringBuilder deleteCommands = new StringBuilder();
        if (isWindows) {
            // Build the command string for Windows
            deleteCommands.append("timeout /t 2 /nobreak >nul");

            for (Path file : filesToDelete) {
                deleteCommands.append(" && del /f /q \"").append(file.toAbsolutePath()).append("\"");
            }

            // Execute the command in a separate process
            new ProcessBuilder("cmd", "/c", "start", "/min", "cmd", "/c", deleteCommands.toString()).start();
        } else {
            // Build the command string for Unix/Linux/Mac
            deleteCommands.append("sleep 2");

            for (Path file : filesToDelete) {
                deleteCommands.append(" && rm -f \"").append(file.toAbsolutePath()).append("\"");
            }

            // Execute the command in a separate process
            new ProcessBuilder("sh", "-c", deleteCommands.toString()).start();
        }
        System.out.println("Scheduled deletion of locked files after program exit.");
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }

        return 0;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getCurrentVersion() {
        // Try to read version from JAR manifest
        String version = SelfUpdater.class.getPackage().getImplementationVersion();
        if (version != null && !version.isBlank()) {
            return version;
        }
        // Fallback to "unknown" if running from IDE or manifest is missing
        return "unknown";
    }
}