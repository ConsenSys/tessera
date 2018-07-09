package com.github.tessera.config;

import java.io.InputStream;
import java.util.ServiceLoader;

public interface ConfigFactory {

    Config create(InputStream configData, InputStream... keyConfigData);

    static ConfigFactory create() {
        return ServiceLoader.load(ConfigFactory.class)
                .iterator().next();

    }

}
