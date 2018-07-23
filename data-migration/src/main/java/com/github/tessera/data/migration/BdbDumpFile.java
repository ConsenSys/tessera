package com.github.tessera.data.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bouncycastle.util.encoders.Hex;

/**
 * Assumes that user has exported data from bdb using db_dump
 * <pre>
 *  db_dump -f exported.txt c1/cn§.db/payload.db
 * </pre>
 *
 */
public class BdbDumpFile implements StoreLoader {

    @Override
    public Map<byte[], byte[]> load(Path inputFile) throws IOException {

        Map<byte[], byte[]> results = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {

            while (true) {
                String line = reader.readLine();
                if (Objects.isNull(line)) {
                    break;
                }

                if (!line.startsWith(" ")) {
                    continue;
                }

                final String key = line.trim();

                final String value = reader.readLine();
                
                
                results.put(Hex.decode(key), Hex.decode(value));
            }
            return Collections.unmodifiableMap(results);

        }
    }

}
