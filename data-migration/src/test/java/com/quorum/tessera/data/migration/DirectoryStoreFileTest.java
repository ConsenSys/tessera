package com.quorum.tessera.data.migration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DirectoryStoreFileTest {

    @Test
    public void load() throws Exception {

        Path directory = Paths.get(getClass().getResource("/dir/").toURI());

        DirectoryStoreFile directoryStoreFile = new DirectoryStoreFile();

        Map<byte[], byte[]> results = directoryStoreFile.load(directory);

        assertThat(results).hasSize(22);

    }

    @Test
    public void loadLarge() throws Exception {

        Path baseDir = Paths.get(getClass().getResource("/").toURI());

        Path directory = baseDir.resolve(UUID.randomUUID().toString());

        Files.createDirectories(directory);

        Path largeFile = Paths.get(directory.toAbsolutePath().toString(), "loadLarge");

        Files.copy(getClass().getResourceAsStream("/loadLarge.sample"), largeFile);

        DirectoryStoreFile directoryStoreFile = new DirectoryStoreFile();

        Map<byte[], byte[]> results = directoryStoreFile.load(directory);

        assertThat(results).hasSize(1);

    }

}
