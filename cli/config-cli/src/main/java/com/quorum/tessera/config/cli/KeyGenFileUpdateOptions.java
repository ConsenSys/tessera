package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import picocli.CommandLine;

import java.nio.file.Path;

public class KeyGenFileUpdateOptions {
    @CommandLine.Option(
            names = {"--configfile", "-configfile"},
            description = "Path to node configuration file",
            required = true)
    public Config config;

    @CommandLine.Option(
            names = {"--configout", "-output"},
            description = "Path to save updated configfile to.  Requires --configfile option to also be provided",
        required = true)
    public Path configOut;

    @CommandLine.Option(
            names = {"--pwdout"},
            description =
                    "Path to save updated password list to.  Requires --configfile and --configout options to also be provided")
    public Path pwdOut;

    public Config getConfig() {
        return config;
    }

    public Path getConfigOut() {
        return configOut;
    }

    public Path getPwdOut() {
        return pwdOut;
    }
}
