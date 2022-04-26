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
    private final HttpClient client;
    private final Gson gson = new Gson();

    public Downloader() {
        client = HttpClient.newHttpClient();
    }

    public void downloadJar(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<Path> file = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of("./paper.jar")));
        if (file.statusCode() >= 400) {
            System.out.println("Error");
        }
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
}
