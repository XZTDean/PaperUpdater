package me.deanx.paperupdater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class Downloader {
    private HttpClient client;
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
}
