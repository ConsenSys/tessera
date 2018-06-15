package com.github.nexus;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.configuration.model.KeyData;

import javax.json.JsonObject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Collections.emptyList;

public class TestConfiguration implements Configuration {

    @Override
    public List<String> publicKeys() {
        return emptyList();
    }

    @Override
    public List<JsonObject> privateKeys() {
        return emptyList();
    }

    @Override
    public List<String> passwords() {
        return emptyList();
    }

    @Override
    public List<KeyData> keyData() {
        return emptyList();
    }

    @Override
    public String url() {
        return "http://localhost";
    }

    @Override
    public int port() {
        return 8080;
    }

    @Override
    public List<String> othernodes() {
        return emptyList();
    }

    @Override
    public Path keygenBasePath() {
        return Paths.get("./").toAbsolutePath();
    }

    @Override
    public List<String> generatekeys() {
        return emptyList();
    }
}
