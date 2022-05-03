package me.deanx.paperupdater;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        HttpResponse<Path> file = client.send(request, HttpResponse.BodyHandlers.ofFile(outputFile));
        if (file.statusCode() >= 400) {
            System.out.println("Error");
            return false;
        }
        return true;
    }

    public int[] getVersionBuilds(String version) throws IOException, InterruptedException {
        String url = BASE_URL + "versions/" + version;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> buildInfo = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (buildInfo.statusCode() >= 400) {
            System.out.println("Error");
        }
        JsonObject jsonObject = JsonParser.parseString(buildInfo.body()).getAsJsonObject();
        return gson.fromJson(jsonObject.get("builds"), int[].class);
    }

    public int getLatestBuild(String version) throws IOException, InterruptedException {
        int[] builds = getVersionBuilds(version);
        return builds[builds.length - 1];
    }

    public boolean downloadBuild(String version, int build) throws IOException, InterruptedException {
        String filename = String.format("paper-%s-%d.jar", version, build);
        String url = BASE_URL + "versions/" + version + "/builds/" + build + "/downloads/" + filename;
        return downloadJar(url);
    }

    public boolean downloadLatestBuild(String version) throws IOException, InterruptedException {
        int latestBuild = getLatestBuild(version);
        return downloadBuild(version, latestBuild);
    }
}
