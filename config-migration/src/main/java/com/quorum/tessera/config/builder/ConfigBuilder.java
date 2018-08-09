package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.*;
import com.quorum.tessera.nacl.Key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;

public class ConfigBuilder {

    private ConfigBuilder() {
    }

    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }

    public static ConfigBuilder from(Config config) {

        final ConfigBuilder configBuilder = ConfigBuilder.create();
        configBuilder.unixSocketFile(config.getUnixSocketFile());

        List<String> peers = Stream.of(config)
            .filter(c -> c.getPeers() != null)
            .map(Config::getPeers)
            .flatMap(List::stream)
            .map(Peer::getUrl)
            .collect(Collectors.toList());

        configBuilder.jdbcConfig(config.getJdbcConfig())
                .peers(peers)
                .serverHostname(config.getServerConfig().getHostName())
                .serverPort(config.getServerConfig().getPort())
                .useWhiteList(config.isUseWhiteList());

        final SslConfig sslConfig = config.getServerConfig().getSslConfig();


        configBuilder.sslAuthenticationMode(sslConfig.getTls())
                .sslClientTrustMode(sslConfig.getClientTrustMode())
                .sslClientKeyStorePath(Objects.toString(sslConfig.getClientKeyStore(), null))
                .sslClientKeyStorePassword(sslConfig.getClientKeyStorePassword())
                .sslClientTrustStorePath(Objects.toString(sslConfig.getClientTrustStore(), null))
                .sslClientTrustStorePassword(sslConfig.getClientTrustStorePassword())
                .sslClientTlsKeyPath(sslConfig.getClientTlsKeyPath())
                .sslClientTlsCertificatePath(sslConfig.getClientTlsCertificatePath())
                .sslClientTrustCertificates(Objects.isNull(sslConfig.getClientTrustCertificates()) ?
                    EMPTY_LIST :
                    sslConfig.getClientTrustCertificates()
                )
                .sslKnownServersFile(sslConfig.getKnownServersFile())
                .sslServerTrustMode(sslConfig.getServerTrustMode())
                .sslServerKeyStorePath(Objects.toString(sslConfig.getServerKeyStore(), null))
                .sslServerKeyStorePassword(sslConfig.getServerKeyStorePassword())
                .sslServerTrustStorePath(Objects.toString(sslConfig.getServerTrustStore(), null))
                .sslServerTrustStorePassword(sslConfig.getServerTrustStorePassword())
                .sslServerTlsKeyPath(sslConfig.getServerTlsKeyPath())
                .sslServerTlsCertificatePath(sslConfig.getServerTlsCertificatePath())
                .sslServerTrustCertificates(Objects.isNull(sslConfig.getServerTrustCertificates()) ?
                    EMPTY_LIST :
                    sslConfig.getServerTrustCertificates()
                )
                .sslKnownClientsFile(sslConfig.getKnownClientsFile())
                .sslKnownClientsFile(sslConfig.getKnownClientsFile())
                .sslKnownServersFile(sslConfig.getKnownServersFile())
                .sslClientTlsCertificatePath(sslConfig.getClientTlsCertificatePath())
                .sslServerTlsCertificatePath(sslConfig.getServerTlsCertificatePath())
                .keyData(config.getKeys())
                .sslClientTlsKeyPath(sslConfig.getClientTlsKeyPath())
                .sslServerTlsKeyPath(sslConfig.getServerTlsKeyPath())
                .alwaysSendToKeys(config.getFowardingList());

        return configBuilder;

    }

    private String serverHostname;

    private Integer serverPort;

    private JdbcConfig jdbcConfig;

    private Path unixSocketFile;

    private List<String> peers;

    private List<String> alwaysSendTo;

    private List<Key> alwaysSendToKeys;

    private KeyConfiguration keyData;

    private SslAuthenticationMode sslAuthenticationMode;

    private SslTrustMode sslServerTrustMode;

    private String sslServerKeyStorePath;

    private String sslServerTrustStorePassword;

    private String sslServerKeyStorePassword;

    private String sslServerTrustStorePath;

    private List<Path> sslServerTrustCertificates = emptyList();

    private String sslClientKeyStorePath;

    private String sslClientKeyStorePassword;

    private String sslClientTrustStorePassword;

    private String sslClientTrustStorePath;

    private List<Path> sslClientTrustCertificates = emptyList();

    private SslTrustMode sslClientTrustMode;

    private Path sslKnownClientsFile;

    private Path sslKnownServersFile;

    private Path sslServerTlsKeyPath;

    private Path sslServerTlsCertificatePath;

    private Path sslClientTlsKeyPath;

    private Path sslClientTlsCertificatePath;

    private boolean useWhiteList;

    public ConfigBuilder sslServerTrustMode(SslTrustMode sslServerTrustMode) {
        this.sslServerTrustMode = sslServerTrustMode;
        return this;
    }

    public ConfigBuilder sslClientTrustMode(SslTrustMode sslClientTrustMode) {
        this.sslClientTrustMode = sslClientTrustMode;
        return this;
    }

    public ConfigBuilder sslServerKeyStorePath(String sslServerKeyStorePath) {
        this.sslServerKeyStorePath = sslServerKeyStorePath;
        return this;
    }

    public ConfigBuilder sslServerTrustStorePassword(String sslServerTrustStorePassword) {
        this.sslServerTrustStorePassword = sslServerTrustStorePassword;
        return this;
    }

    public ConfigBuilder sslServerKeyStorePassword(String sslServerKeyStorePassword) {
        this.sslServerKeyStorePassword = sslServerKeyStorePassword;
        return this;
    }

    public ConfigBuilder sslServerTrustStorePath(String sslServerTrustStorePath) {
        this.sslServerTrustStorePath = sslServerTrustStorePath;
        return this;
    }

    public ConfigBuilder sslServerTrustCertificates(List<Path> sslServerTrustCertificates) {
        this.sslServerTrustCertificates = sslServerTrustCertificates;
        return this;
    }

    public ConfigBuilder sslClientTrustStorePassword(String sslClientTrustStorePassword) {
        this.sslClientTrustStorePassword = sslClientTrustStorePassword;
        return this;
    }

    public ConfigBuilder unixSocketFile(Path unixSocketFile) {
        this.unixSocketFile = unixSocketFile;
        return this;
    }

    public ConfigBuilder serverHostname(String serverHostname) {
        this.serverHostname = serverHostname;
        return this;
    }

    public ConfigBuilder serverPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public ConfigBuilder jdbcConfig(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
        return this;
    }

    public ConfigBuilder peers(List<String> peers) {
        this.peers = peers;
        return this;
    }

    public ConfigBuilder alwaysSendTo(List<String> alwaysSendTo) {
        this.alwaysSendTo = alwaysSendTo;
        return this;
    }

    public ConfigBuilder alwaysSendToKeys(List<Key> alwaysSendToKeys) {
        this.alwaysSendToKeys = alwaysSendToKeys;
        return this;
    }

    public ConfigBuilder sslKnownClientsFile(Path knownClientsFile) {
        this.sslKnownClientsFile = knownClientsFile;
        return this;
    }

    public ConfigBuilder sslKnownServersFile(Path knownServersFile) {
        this.sslKnownServersFile = knownServersFile;
        return this;
    }

    public ConfigBuilder sslAuthenticationMode(SslAuthenticationMode sslAuthenticationMode) {
        this.sslAuthenticationMode = sslAuthenticationMode;
        return this;
    }

    public ConfigBuilder sslClientKeyStorePath(String sslClientKeyStorePath) {
        this.sslClientKeyStorePath = sslClientKeyStorePath;
        return this;
    }

    public ConfigBuilder sslClientTrustCertificates(List<Path> sslClientTrustCertificates) {
        this.sslClientTrustCertificates = sslClientTrustCertificates;
        return this;
    }

    public ConfigBuilder sslClientTrustStorePath(String sslClientTrustStorePath) {
        this.sslClientTrustStorePath = sslClientTrustStorePath;
        return this;
    }

    public ConfigBuilder sslClientKeyStorePassword(String sslClientKeyStorePassword) {
        this.sslClientKeyStorePassword = sslClientKeyStorePassword;
        return this;
    }

    public ConfigBuilder sslServerTlsKeyPath(Path sslServerTlsKeyPath) {
        this.sslServerTlsKeyPath = sslServerTlsKeyPath;
        return this;
    }

    public ConfigBuilder sslServerTlsCertificatePath(Path sslServerTlsCertificatePath) {
        this.sslServerTlsCertificatePath = sslServerTlsCertificatePath;
        return this;
    }

    public ConfigBuilder sslClientTlsKeyPath(Path sslClientTlsKeyPath) {
        this.sslClientTlsKeyPath = sslClientTlsKeyPath;
        return this;
    }

    public ConfigBuilder sslClientTlsCertificatePath(Path sslClientTlsCertificatePath) {
        this.sslClientTlsCertificatePath = sslClientTlsCertificatePath;
        return this;
    }

    public ConfigBuilder keyData(KeyConfiguration keyData) {
        this.keyData = keyData;
        return this;
    }

    public ConfigBuilder useWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
        return this;
    }

    static Path toPath(String value) {
        return Optional.ofNullable(value)
                .map(Paths::get)
                .orElse(null);
    }

    public Config build() {

        boolean generateKeyStoreIfNotExisted = true;

        SslConfig sslConfig = new SslConfig(
                sslAuthenticationMode,
                generateKeyStoreIfNotExisted,
                toPath(sslServerKeyStorePath),
                sslServerKeyStorePassword,
                toPath(sslServerTrustStorePath),
                sslServerTrustStorePassword,
                sslServerTrustMode,
                toPath(sslClientKeyStorePath),
                sslClientKeyStorePassword,
                toPath(sslClientTrustStorePath),
                sslClientTrustStorePassword,
                sslClientTrustMode,
                sslKnownClientsFile,
                sslKnownServersFile,
                sslServerTrustCertificates.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                sslClientTrustCertificates.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                sslServerTlsKeyPath,
                sslServerTlsCertificatePath,
                sslClientTlsKeyPath,
                sslClientTlsCertificatePath
        );

        final ServerConfig serverConfig = new ServerConfig(serverHostname, serverPort, sslConfig, null);

        final List<Peer> peerList = peers
            .stream()
                .map(Peer::new)
                .collect(Collectors.toList());

        final List<Key> forwardingKeys;
        if(alwaysSendTo != null) {
            List<String> keyList = new ArrayList<>();

            for(String keyPath : alwaysSendTo) {
                try {
                    List<String> keysFromFile = Files.readAllLines(Paths.get(keyPath));
                    keyList.addAll(keysFromFile);
                } catch (IOException e) {
                    System.err.println("Error reading alwayssendto file: " + e.getMessage());
                }
            }

            forwardingKeys = keyList.stream()
                                    .map(Base64.getDecoder()::decode)
                                    .map(Key::new)
                                    .collect(Collectors.toList());
        } else {
            forwardingKeys = alwaysSendToKeys;
        }

        return new Config(jdbcConfig, serverConfig, peerList, keyData, forwardingKeys, unixSocketFile, useWhiteList,false);
    }

}
