package me.deanx.paperupdater;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.deanx.paperupdater.vo.BuildInfoResponse;
import me.deanx.paperupdater.vo.ProjectResponse;
import me.deanx.paperupdater.vo.VersionBuildsResponse;

public class Downloader {
    private static final String BASE_URL = "https://fill.papermc.io/v3/projects/paper";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private Path outputFile;

    public Downloader() {
        outputFile = Path.of("./paper.jar");
    }

    public Downloader(String filename) {
        outputFile = Path.of(filename);
    }

    public void setOutputFile(String filename) {
        outputFile = Path.of(filename);
    }

    private boolean downloadJar(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        if (Files.isDirectory(outputFile)) {
            System.out.println("Error: The output path is a directory");
        }
        Path backupFile = null;
        if (Files.isRegularFile(outputFile)) {
            Path tmpFolder = Files.createTempDirectory("PaperUpdater.");
            backupFile = Path.of(tmpFolder.toString(), "paper.jar");
            Files.move(outputFile, backupFile);
        }
        HttpResponse<Path> file = client.send(request, HttpResponse.BodyHandlers.ofFile(outputFile));
        if (file.statusCode() >= 400) {
            System.out.println("Error");
            if (backupFile != null) {
                Files.move(backupFile, outputFile);
                Files.deleteIfExists(backupFile.getParent());
            }
            return false;
        }
        if (backupFile != null) {
            Files.deleteIfExists(backupFile);
            Files.deleteIfExists(backupFile.getParent());
        }
        return true;
    }

    public List<Integer> getVersionBuilds(String version) throws IOException, InterruptedException {
        String url = BASE_URL + "/versions/" + version;
        HttpJsonResponse response = httpGetJson(url);
        if (response.statusCode() >= 400) {
            System.out.println("Error");
        }
        VersionBuildsResponse versionBuildsResponse = gson.fromJson(response.getJson(), VersionBuildsResponse.class);
        return versionBuildsResponse.getBuilds();
    }

    public int getLatestBuild(String version) throws IOException, InterruptedException {
        List<Integer> builds = getVersionBuilds(version);
        return builds.get(0);
    }

    public boolean downloadBuild(String version, String build) throws IOException, InterruptedException {
        String url = getUrl(version, build);
        return downloadJar(url);
    }

    public String getUrl(String version, String build) throws IOException, InterruptedException {
        String url = BASE_URL + "/versions/" + version + "/builds/" + build;
        HttpJsonResponse response = httpGetJson(url);
        if (response.statusCode() == 404) {
            throw new IllegalArgumentException("Build " + build + " not found for version " + version);
        } else if (response.statusCode() >= 400) {
            throw new RuntimeException("Get response code " + response.statusCode() + " from " + url);
        }
        BuildInfoResponse buildInfo = gson.fromJson(response.getJson(), BuildInfoResponse.class);
        BuildInfoResponse.DownloadInfo serverDownload = buildInfo.getServerDownload();
        if (serverDownload == null) {
            throw new RuntimeException("No server download available for version " + version + " build " + build);
        }
        return serverDownload.getUrl();
    }

    public boolean downloadLatestBuild(String version) throws IOException, InterruptedException {
        return downloadBuild(version, "latest");
    }

    public List<String> getVersionsFromVersionFamily(String versionFamily, boolean includePreRelease) throws IOException, InterruptedException {
        ProjectResponse projectResponse = getProjectResponse();
        List<String> versions = projectResponse.getVersions().get(versionFamily);
        if (versions == null) {
            throw new IllegalArgumentException("Version family '" + versionFamily + "' not found");
        }
        return filterVersions(versions, includePreRelease);
    }

    public List<String> getVersions(boolean includePreRelease) throws IOException, InterruptedException {
        ProjectResponse projectResponse = getProjectResponse();
        List<String> versions = projectResponse.getVersions().values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return filterVersions(versions, includePreRelease);
    }

    private List<String> filterVersions(List<String> versions, boolean includePreRelease) {
        if (includePreRelease) {
            return versions;
        }
        return versions.stream()
                .filter(version -> !version.contains("-"))
                .collect(Collectors.toList());
    }

    public List<String> getVersionFamilies() throws IOException, InterruptedException {
        ProjectResponse projectResponse = getProjectResponse();
        return new ArrayList<>(projectResponse.getVersions().keySet());
    }

    private ProjectResponse getProjectResponse() throws IOException, InterruptedException {
        HttpJsonResponse response = httpGetJson(BASE_URL);
        if (response.statusCode() == 404) {
            throw new IllegalArgumentException(response.getJson().getAsJsonPrimitive("error").getAsString());
        } else if (response.statusCode() >= 400) {
            throw new RuntimeException("Get response code " + response.statusCode() + " from " + BASE_URL);
        }
        return gson.fromJson(response.getJson(), ProjectResponse.class);
    }

    private HttpJsonResponse httpGetJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new HttpJsonResponse(response.body(), response.statusCode());
    }

    private static class HttpJsonResponse {
        private final JsonObject json;
        private final int responseCode;

        public HttpJsonResponse(String json, int responseCode) {
            this.json = JsonParser.parseString(json).getAsJsonObject();
            this.responseCode = responseCode;
        }

        public HttpJsonResponse(JsonObject json, int responseCode) {
            this.json = json;
            this.responseCode = responseCode;
        }

        public JsonObject getJson() {
            return json;
        }

        public int statusCode() {
            return responseCode;
        }
    }
}
