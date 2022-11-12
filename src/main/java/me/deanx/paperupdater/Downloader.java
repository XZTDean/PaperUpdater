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

public class Downloader {
    private static final String BASE_URL = "https://papermc.io/api/v2/projects/paper/";
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

    public int[] getVersionBuilds(String version) throws IOException, InterruptedException {
        String url = BASE_URL + "versions/" + version;
        HttpJsonResponse response = httpGetJson(url);
        if (response.statusCode() >= 400) {
            System.out.println("Error");
        }
        return gson.fromJson(response.getJson().get("builds"), int[].class);
    }

    public int getLatestBuild(String version) throws IOException, InterruptedException {
        int[] builds = getVersionBuilds(version);
        return builds[builds.length - 1];
    }

    public boolean downloadBuild(String version, int build) throws IOException, InterruptedException {
        String url = getUrl(version, build);
        return downloadJar(url);
    }

    public String getUrl(String version, int build) {
        String filename = String.format("paper-%s-%d.jar", version, build);
        return BASE_URL + "versions/" + version + "/builds/" + build + "/downloads/" + filename;
    }

    public boolean downloadLatestBuild(String version) throws IOException, InterruptedException {
        int latestBuild = getLatestBuild(version);
        return downloadBuild(version, latestBuild);
    }

    public String[] getVersionsFromVersionFamily(String versionFamily) throws IOException, InterruptedException {
        String url = BASE_URL + "version_group/" + versionFamily;
        HttpJsonResponse response = httpGetJson(url);
        if (response.statusCode() == 404) {
            throw new IllegalArgumentException(response.getJson().getAsJsonPrimitive("error").getAsString());
        } else if (response.statusCode() >= 400) {
            throw new RuntimeException("Get response code " + response.statusCode() + " from " + url);
        }
        return gson.fromJson(response.getJson().get("versions"), String[].class);
    }

    public String[] getVersions() throws IOException, InterruptedException {
        return getProjectInfo("versions");
    }

    public String[] getVersionFamilies() throws IOException, InterruptedException {
        return getProjectInfo("version_groups");
    }

    private String[] getProjectInfo(String field) throws IOException, InterruptedException {
        HttpJsonResponse response = httpGetJson(BASE_URL);
        if (response.statusCode() == 404) {
            throw new IllegalArgumentException(response.getJson().getAsJsonPrimitive("error").getAsString());
        } else if (response.statusCode() >= 400) {
            throw new RuntimeException("Get response code " + response.statusCode() + " from " + BASE_URL);
        }
        return gson.fromJson(response.getJson().get(field), String[].class);
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
