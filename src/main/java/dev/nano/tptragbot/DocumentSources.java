package dev.nano.tptragbot;

import java.util.List;

public class DocumentSources {
    private List<String> urls;
    private List<String> paths;

    public DocumentSources(List<String> urls, List<String> paths) {
        this.urls = urls;
        this.paths = paths;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<String> getPaths() {
        return paths;
    }
}
