package com.quorum.tessera.config.migration;

import com.quorum.tessera.cli.CliDelegate;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.migration.test.FixtureUtil;
import com.quorum.tessera.io.SystemAdapter;
import com.quorum.tessera.test.util.ElUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

public class LegacyCliAdapterTest {

    @Rule public SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    private final ConfigBuilder builderWithValidValues = FixtureUtil.builderWithValidValues();

    private final LegacyCliAdapter instance = new LegacyCliAdapter();

    private Path dataDirectory;

    @Before
    public void onSetUp() throws IOException {
        dataDirectory = Files.createTempDirectory("data");

        Files.createFile(dataDirectory.resolve("foo.pub"));
        Files.createFile(dataDirectory.resolve("foo.key"));
        Files.createFile(dataDirectory.resolve("foo2.pub"));
        Files.createFile(dataDirectory.resolve("foo2.key"));
    }

    @After
    public void onTearDown() throws IOException {
        Files.deleteIfExists(Paths.get("tessera-config.json"));

        Files.walk(dataDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void getType() {
        assertThat(instance.getType()).isEqualTo(CliType.CONFIG_MIGRATION);
    }

    @Test
    public void withoutCliArgsAllConfigIsSetFromTomlFile() throws Exception {
        dataDirectory = Paths.get("data");
        Files.createDirectory(dataDirectory);

        Path alwaysSendTo1 = Files.createFile(dataDirectory.resolve("alwayssendto1"));
        Files.write(
                alwaysSendTo1,
                ("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=\n" + "jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=")
                        .getBytes());

        Path alwaysSendTo2 = Files.createFile(dataDirectory.resolve("alwayssendto2"));
        Files.write(alwaysSendTo2, "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=".getBytes());

        Path sampleFile = Paths.get(getClass().getResource("/sample-toml-no-nulls.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("alwaysSendToPath1", "alwayssendto1");
        params.put("alwaysSendToPath2", "alwayssendto2");

        String data = ElUtil.process(new String(Files.readAllBytes(sampleFile)), params);

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        CliResult result = CliDelegate.instance().execute("--tomlfile", configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        final Config config = result.getConfig().get();

        assertThat(config.getServer().getHostName()).isEqualTo("http://127.0.0.1");
        assertThat(config.getServer().getPort()).isEqualTo(9001);
        assertThat(config.getServer().getBindingAddress()).isEqualTo("http://127.0.0.1:9001");
        assertThat(config.getUnixSocketFile().toString()).isEqualTo("data/constellation.ipc");
        assertThat(config.getPeers().size()).isEqualTo(2);
        assertThat(config.getPeers().get(0).getUrl()).isEqualTo("http://127.0.0.1:9001/");
        assertThat(config.getPeers().get(1).getUrl()).isEqualTo("http://127.0.0.1:9002/");
        assertThat(config.getKeys().getKeyData().size()).isEqualTo(2);
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("data/foo.pub"));
        assertThat(config.getKeys().getKeyData().get(0))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("data/foo.key"));
        assertThat(config.getKeys().getKeyData().get(1))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("data/foo2.pub"));
        assertThat(config.getKeys().getKeyData().get(1))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("data/foo2.key"));
        assertThat(config.getAlwaysSendTo().size()).isEqualTo(3);
        assertThat(config.getAlwaysSendTo().get(0)).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getAlwaysSendTo().get(1)).isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(config.getAlwaysSendTo().get(2)).isEqualTo("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=");
        assertThat(config.getKeys().getPasswordFile().toString()).isEqualTo("data/passwords");
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("jdbc:h2:mem:tessera");
        assertThat(config.isUseWhiteList()).isTrue();
        assertThat(config.getServer().getSslConfig().getTls()).isEqualByComparingTo(SslAuthenticationMode.STRICT);
        assertThat(config.getServer().getSslConfig().getServerTlsCertificatePath().toString())
                .isEqualTo("data/tls-server-cert.pem");
        assertThat(config.getServer().getSslConfig().getServerTrustCertificates().size()).isEqualTo(2);
        assertThat(config.getServer().getSslConfig().getServerTrustCertificates().get(0).toString())
                .isEqualTo("data/chain1");
        assertThat(config.getServer().getSslConfig().getServerTrustCertificates().get(1).toString())
                .isEqualTo("data/chain2");
        assertThat(config.getServer().getSslConfig().getServerTlsKeyPath().toString())
                .isEqualTo("data/tls-server-key.pem");
        assertThat(config.getServer().getSslConfig().getServerTrustMode()).isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(config.getServer().getSslConfig().getKnownClientsFile().toString())
                .isEqualTo("data/tls-known-clients");
        assertThat(config.getServer().getSslConfig().getClientTlsCertificatePath().toString())
                .isEqualTo("data/tls-client-cert.pem");
        assertThat(config.getServer().getSslConfig().getClientTrustCertificates().size()).isEqualTo(2);
        assertThat(config.getServer().getSslConfig().getClientTrustCertificates().get(0).toString())
                .isEqualTo("data/clientchain1");
        assertThat(config.getServer().getSslConfig().getClientTrustCertificates().get(1).toString())
                .isEqualTo("data/clientchain2");
        assertThat(config.getServer().getSslConfig().getClientTlsKeyPath().toString())
                .isEqualTo("data/tls-client-key.pem");
        assertThat(config.getServer().getSslConfig().getClientTrustMode())
                .isEqualByComparingTo(SslTrustMode.CA_OR_TOFU);
        assertThat(config.getServer().getSslConfig().getKnownServersFile().toString())
                .isEqualTo("data/tls-known-servers");
    }

    @Test
    public void providingCliArgsOverridesTomlFileConfig() throws Exception {

        Path sampleFile = Paths.get(getClass().getResource("/sample.conf").toURI());

        String data = Files.readAllLines(sampleFile).stream().collect(Collectors.joining(System.lineSeparator()));

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        Path workdir = Paths.get("override");

        if (Files.exists(workdir)) {
            Files.walk(workdir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Files.createDirectory(workdir);
        Files.createFile(workdir.resolve("new.pub"));
        Files.createFile(workdir.resolve("new.key"));
        Path alwaysSendToFile = Files.createFile(workdir.resolve("alwayssendto"));
        Files.write(
                alwaysSendToFile,
                ("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=\n" + "jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=")
                        .getBytes());

        String[] args = {
            "--tomlfile",
            configFile.toString(),
            "--url",
            "http://override",
            "--port",
            "1111",
            "--workdir",
            "override",
            "--socket",
            "cli.ipc",
            "--othernodes",
            "http://others",
            "--publickeys",
            "new.pub",
            "--privatekeys",
            "new.key",
            "--alwayssendto",
            "alwayssendto",
            "--passwords",
            "pw.txt",
            "--storage",
            "jdbc:test",
            "--ipwhitelist",
            "--tls",
            "off",
            "--tlsservercert",
            "over-server-cert.pem",
            "--tlsserverchain",
            "serverchain.file",
            "--tlsserverkey",
            "over-server-key.pem",
            "--tlsservertrust",
            "whitelist",
            "--tlsknownclients",
            "over-known-clients",
            "--tlsclientcert",
            "over-client-cert.pem",
            "--tlsclientchain",
            "clientchain.file",
            "--tlsclientkey",
            "over-client-key.pem",
            "--tlsclienttrust",
            "tofu",
            "--tlsknownservers",
            "over-known-servers"
        };

        CliResult result = CliDelegate.instance().execute(args);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        assertThat(result.getConfig().get().getServer().getHostName()).isEqualTo("http://override");
        assertThat(result.getConfig().get().getServer().getPort()).isEqualTo(1111);
        assertThat(result.getConfig().get().getServer().getBindingAddress()).isEqualTo("http://override:1111");
        assertThat(result.getConfig().get().getUnixSocketFile().toString()).isEqualTo("override/cli.ipc");
        assertThat(result.getConfig().get().getPeers().size()).isEqualTo(1);
        assertThat(result.getConfig().get().getPeers().get(0).getUrl()).isEqualTo("http://others");
        assertThat(result.getConfig().get().getKeys().getKeyData().size()).isEqualTo(1);
        assertThat(result.getConfig().get().getKeys().getKeyData().get(0))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("override/new.pub"));
        assertThat(result.getConfig().get().getKeys().getKeyData().get(0))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("override/new.key"));
        assertThat(result.getConfig().get().getAlwaysSendTo().size()).isEqualTo(2);
        assertThat(result.getConfig().get().getAlwaysSendTo().get(0))
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(result.getConfig().get().getAlwaysSendTo().get(1))
                .isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(result.getConfig().get().getKeys().getPasswordFile().toString()).isEqualTo("override/pw.txt");
        assertThat(result.getConfig().get().getJdbcConfig().getUrl()).isEqualTo("jdbc:test");
        assertThat(result.getConfig().get().isUseWhiteList()).isTrue();
        assertThat(result.getConfig().get().getServer().getSslConfig().getTls())
                .isEqualByComparingTo(SslAuthenticationMode.OFF);
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTlsCertificatePath().toString())
                .isEqualTo("override/over-server-cert.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustCertificates().size())
                .isEqualTo(1);
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustCertificates().get(0).toString())
                .isEqualTo("override/serverchain.file");
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTlsKeyPath().toString())
                .isEqualTo("override/over-server-key.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustMode())
                .isEqualByComparingTo(SslTrustMode.WHITELIST);
        assertThat(result.getConfig().get().getServer().getSslConfig().getKnownClientsFile().toString())
                .isEqualTo("override/over-known-clients");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTlsCertificatePath().toString())
                .isEqualTo("override/over-client-cert.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustCertificates().size())
                .isEqualTo(1);
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustCertificates().get(0).toString())
                .isEqualTo("override/clientchain.file");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTlsKeyPath().toString())
                .isEqualTo("override/over-client-key.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustMode())
                .isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(result.getConfig().get().getServer().getSslConfig().getKnownServersFile().toString())
                .isEqualTo("override/over-known-servers");

        Files.walk(workdir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void ifConfigParameterIsNotSetInTomlOrCliThenDefaultIsUsed() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");
        Path keysFile = Files.createTempFile("key", ".tmp").toAbsolutePath();
        Files.write(keysFile, Collections.singletonList("SOMEDATA"));

        String[] requiredParams = {
            "--tomlfile", configFile.toString(),
            "--url", "http://127.0.0.1",
            "--port", "9001",
            "--othernodes", "localhost:1111",
            "--socket", "myipcfile.ipc",
            "--publickeys", keysFile.toString(),
            "--privatekeys", keysFile.toString()
        };

        CliResult result = CliDelegate.instance().execute(requiredParams);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        assertThat(result.getStatus()).isEqualTo(0);

        assertThat(result.getConfig().get().getUnixSocketFile().toString()).isEqualTo("myipcfile.ipc");
        // Empty List
        assertThat(Optional.ofNullable(result.getConfig().get().getKeys().getKeyData()).isPresent()).isEqualTo(true);
        assertThat(result.getConfig().get().getAlwaysSendTo().size()).isEqualTo(0);
        assertThat(result.getConfig().get().getKeys().getPasswordFile()).isNull();
        assertThat(result.getConfig().get().getJdbcConfig().getUrl()).isEqualTo("jdbc:h2:mem:tessera");
        assertThat(result.getConfig().get().isUseWhiteList()).isFalse();
        assertThat(result.getConfig().get().getServer().getSslConfig().getTls())
                .isEqualByComparingTo(SslAuthenticationMode.OFF);
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTlsCertificatePath()).isNull();
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustCertificates().size())
                .isEqualTo(0);
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTlsKeyPath()).isNull();
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustMode())
                .isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(result.getConfig().get().getServer().getSslConfig().getKnownClientsFile()).isNull();
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTlsCertificatePath()).isNull();
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustCertificates().size())
                .isEqualTo(0);
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTlsKeyPath()).isNull();
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustMode())
                .isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(result.getConfig().get().getServer().getSslConfig().getKnownServersFile()).isNull();
    }

    @Test
    public void ifWorkDirCliOverrideIsProvidedThenItIsAppliedToBothTomlAndCliSetParameters() throws Exception {

        Path workdir = Paths.get("override");

        if (Files.exists(workdir)) {
            Files.walk(workdir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Files.createDirectory(workdir);
        Files.createFile(workdir.resolve("new.pub"));
        Files.createFile(workdir.resolve("new.key"));
        Path alwaysSendToFile = Files.createFile(workdir.resolve("alwayssendto"));
        Files.write(
                alwaysSendToFile,
                ("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=\n" + "jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=")
                        .getBytes());

        Path sampleFile = Paths.get(getClass().getResource("/sample-toml-no-nulls-tls-off.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("alwaysSendToPath1", "alwayssendto");
        params.put("alwaysSendToPath2", "alwayssendto");

        String data = ElUtil.process(new String(Files.readAllBytes(sampleFile)), params);

        Path configFile = Files.createTempFile("workdiroverride", ".txt");
        Files.write(configFile, data.getBytes());

        String[] args = {
            "--tomlfile", configFile.toString(),
            "--workdir", "override",
            "--publickeys", "new.pub",
            "--privatekeys", "new.key"
        };

        CliResult result = CliDelegate.instance().execute(args);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        assertThat(result.getConfig().get().getServer().getHostName()).isEqualTo("http://127.0.0.1");
        assertThat(result.getConfig().get().getServer().getPort()).isEqualTo(9001);
        assertThat(result.getConfig().get().getServer().getBindingAddress()).isEqualTo("http://127.0.0.1:9001");
        assertThat(result.getConfig().get().getUnixSocketFile().toString()).isEqualTo("override/constellation.ipc");
        assertThat(result.getConfig().get().getPeers().size()).isEqualTo(2);
        assertThat(result.getConfig().get().getPeers().get(0).getUrl()).isEqualTo("http://127.0.0.1:9001/");
        assertThat(result.getConfig().get().getPeers().get(1).getUrl()).isEqualTo("http://127.0.0.1:9002/");
        assertThat(result.getConfig().get().getKeys().getKeyData().size()).isEqualTo(1);
        assertThat(result.getConfig().get().getKeys().getKeyData().get(0))
                .extracting("publicKeyPath")
                .containsExactly(Paths.get("override/new.pub"));
        assertThat(result.getConfig().get().getKeys().getKeyData().get(0))
                .extracting("privateKeyPath")
                .containsExactly(Paths.get("override/new.key"));
        assertThat(result.getConfig().get().getAlwaysSendTo().size()).isEqualTo(4);
        assertThat(result.getConfig().get().getAlwaysSendTo().get(0))
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(result.getConfig().get().getAlwaysSendTo().get(1))
                .isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(result.getConfig().get().getAlwaysSendTo().get(2))
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(result.getConfig().get().getAlwaysSendTo().get(3))
                .isEqualTo("jWKqelS4XjJ67JBbuKE7x9CVGFJ706wRYy/ev/OCOzk=");
        assertThat(result.getConfig().get().getKeys().getPasswordFile().toString()).isEqualTo("override/passwords");
        assertThat(result.getConfig().get().getJdbcConfig().getUrl()).isEqualTo("jdbc:h2:mem:tessera");
        assertThat(result.getConfig().get().isUseWhiteList()).isTrue();
        assertThat(result.getConfig().get().getServer().getSslConfig().getTls())
                .isEqualByComparingTo(SslAuthenticationMode.OFF);
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTlsCertificatePath().toString())
                .isEqualTo("override/tls-server-cert.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustCertificates().size())
                .isEqualTo(2);
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustCertificates().get(0).toString())
                .isEqualTo("override/chain1");
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustCertificates().get(1).toString())
                .isEqualTo("override/chain2");
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTlsKeyPath().toString())
                .isEqualTo("override/tls-server-key.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getServerTrustMode())
                .isEqualByComparingTo(SslTrustMode.TOFU);
        assertThat(result.getConfig().get().getServer().getSslConfig().getKnownClientsFile().toString())
                .isEqualTo("override/tls-known-clients");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTlsCertificatePath().toString())
                .isEqualTo("override/tls-client-cert.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustCertificates().size())
                .isEqualTo(2);
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustCertificates().get(0).toString())
                .isEqualTo("override/clientchain1");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustCertificates().get(1).toString())
                .isEqualTo("override/clientchain2");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTlsKeyPath().toString())
                .isEqualTo("override/tls-client-key.pem");
        assertThat(result.getConfig().get().getServer().getSslConfig().getClientTrustMode())
                .isEqualByComparingTo(SslTrustMode.CA_OR_TOFU);
        assertThat(result.getConfig().get().getServer().getSslConfig().getKnownServersFile().toString())
                .isEqualTo("override/tls-known-servers");
    }

    @Test
    public void urlNotSetGivesNullHostname() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");

        String[] requiredParams = {
            "--tomlfile=" + configFile.toString(), "--port=9001", "--othernodes=localhost:1111",
        };

        CliResult result = instance.execute(requiredParams);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        assertThat(result.getConfig().get().getServer().getHostName()).isNull();
    }

    @Test
    public void urlWithPortSet() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");

        String[] requiredParams = {
            "--tomlfile", configFile.toString(), "--url", "http://127.0.0.1:9001", "--port", "9001",
        };

        CliResult result = CliDelegate.instance().execute(requiredParams);

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();

        assertThat(result.getConfig().get().getServer().getHostName()).isEqualTo("http://127.0.0.1");
    }

    @Test
    public void invalidUrlProvided() throws Exception {

        Path configFile = Files.createTempFile("emptyConfig", ".txt");

        String[] requiredParams = {"--tomlfile", configFile.toString(), "--url", "htt://invalidHost", "--port", "9001"};

        final Throwable throwable = catchThrowable(() -> CliDelegate.instance().execute(requiredParams));

        assertThat(throwable)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bad server url given: unknown protocol: htt");
    }

    @Test
    public void sampleTomlFileOnly() throws Exception {

        Path serverKeyStorePath = Files.createTempFile("serverKeyStorePath", ".bog");
        Path passwordFile = Files.createTempFile("passwords", ".txt");
        Files.write(passwordFile, Arrays.asList("PASWORD1"));

        Path sampleFile = Paths.get(getClass().getResource("/sample.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("passwordFile", passwordFile);
        params.put("serverKeyStorePath", serverKeyStorePath);

        String data = Files.readAllLines(sampleFile).stream().collect(Collectors.joining(System.lineSeparator()));

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        CliResult result = instance.execute("--tomlfile=" + configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
        // TODO assert that value of config is as expected from sample config

        //        assertThat(result.getStatus()).isEqualTo(0);
    }

    @Test
    public void noOptionsWithTomlFile() throws Exception {

        Path serverKeyStorePath = Files.createTempFile("serverKeyStorePath", ".bog");
        Path passwordFile = Files.createTempFile("passwords", ".txt");
        Files.write(passwordFile, Arrays.asList("PASWORD1"));

        Path sampleFile = Paths.get(getClass().getResource("/sample-all-values.conf").toURI());
        Map<String, Object> params = new HashMap<>();
        params.put("passwordFile", passwordFile);
        params.put("serverKeyStorePath", serverKeyStorePath);

        String data = ElUtil.process(new String(Files.readAllBytes(sampleFile)), params);

        Path configFile = Files.createTempFile("noOptions", ".txt");
        Files.write(configFile, data.getBytes());

        CliResult result = instance.execute("--tomlfile=" + configFile.toString());

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isPresent();
    }

    @Test
    public void passwordOverrideProvidedButNoKeyDataOverrideProvidedThenPrintMessageToConsole() {
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream errStream = new PrintStream(errContent);

        MockSystemAdapter systemAdapter = (MockSystemAdapter) SystemAdapter.INSTANCE;
        systemAdapter.setErrPrintStream(errStream);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        final LegacyOverridesMixin mixin = new LegacyOverridesMixin();
        mixin.passwords = "override/path";
        instance.setOverrides(mixin);

        instance.applyOverrides(configBuilder, KeyDataBuilder.create());

        assertThat(errContent.toString())
                .isEqualTo(
                        "Info: Public/Private key data not provided in overrides. Overriden password file has not been added to config.\n");
    }

    @Test
    public void noPasswordOrKeyDataOverrideProvidedThenNoMessagePrintedToConsole() {
        ConfigBuilder configBuilder = ConfigBuilder.create();

        instance.applyOverrides(configBuilder, KeyDataBuilder.create());

        assertThat(systemErrRule.getLog()).isEmpty();
    }

    @Test
    public void keyDataProvidedButNoPasswordProvidedThenNoMessagePrintedToConsole() {
        final LegacyOverridesMixin mixin = new LegacyOverridesMixin();
        mixin.passwords = "override/path";
        instance.setOverrides(mixin);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        List<String> publicKeys = new ArrayList<>();
        publicKeys.add("pub");
        List<String> privateKeys = new ArrayList<>();
        privateKeys.add("priv");

        KeyDataBuilder keyDataBuilder = KeyDataBuilder.create().withPublicKeys(publicKeys).withPrivateKeys(privateKeys);

        instance.applyOverrides(configBuilder, keyDataBuilder);

        assertThat(systemErrRule.getLog()).isEmpty();
    }

    @Test
    public void passwordAndKeyDataProvidedAsOverrideThenNoMessagePrintedToConsole() {
        ConfigBuilder configBuilder = ConfigBuilder.create();

        List<String> publicKeys = new ArrayList<>();
        publicKeys.add("pub");
        List<String> privateKeys = new ArrayList<>();
        privateKeys.add("priv");

        KeyDataBuilder keyDataBuilder = KeyDataBuilder.create().withPublicKeys(publicKeys).withPrivateKeys(privateKeys);

        instance.applyOverrides(configBuilder, keyDataBuilder);

        assertThat(systemErrRule.getLog()).isEmpty();
    }

    @Test
    public void ifTomlWorkDirProvidedWithoutOverrideWorkDirThenTomlWorkDirUsedOnOverridenValues() {
        String socketFilepath = "path/to/socket.ipc";

        final LegacyOverridesMixin mixin = new LegacyOverridesMixin();
        mixin.socket = socketFilepath;
        instance.setOverrides(mixin);

        String tomlWorkDir = "toml";
        ConfigBuilder configBuilder = ConfigBuilder.create().workdir(tomlWorkDir);

        ConfigBuilder result = instance.applyOverrides(configBuilder, KeyDataBuilder.create());

        Path expected = Paths.get(tomlWorkDir, socketFilepath);

        assertThat(result.build().getUnixSocketFile()).isEqualByComparingTo(expected);
    }

    @Test
    public void ifTomlWorkDirProvidedWithOverrideWorkDirThenOverrideWorkDirUsedOnOverridenValues() {
        String overrideWorkDir = "override";
        String socketFilepath = "path/to/socket.ipc";

        final LegacyOverridesMixin mixin = new LegacyOverridesMixin();
        mixin.socket = socketFilepath;
        mixin.workdir = overrideWorkDir;
        instance.setOverrides(mixin);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        ConfigBuilder result = instance.applyOverrides(configBuilder, KeyDataBuilder.create());

        Path expected = Paths.get(overrideWorkDir, socketFilepath);

        assertThat(result.build().getUnixSocketFile()).isEqualByComparingTo(expected);
    }

    @Test
    public void ifTomlWorkDirNotProvidedWithoutOverrideWorkDirThenDefaultWorkDirUsedOnOverridenValues() {
        String socketFilepath = "path/to/socket.ipc";

        final LegacyOverridesMixin mixin = new LegacyOverridesMixin();
        mixin.socket = socketFilepath;
        instance.setOverrides(mixin);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        ConfigBuilder result = instance.applyOverrides(configBuilder, KeyDataBuilder.create());

        Path expected = Paths.get(socketFilepath);

        assertThat(result.build().getUnixSocketFile()).isEqualByComparingTo(expected);
    }

    @Test
    public void ifTomlWorkDirNotProvidedButOverrideWorkDirIsThenOverrideWorkDirUsedOnOverridenValues() {
        String overrideWorkDir = "override";
        String socketFilepath = "path/to/socket.ipc";

        final LegacyOverridesMixin mixin = new LegacyOverridesMixin();
        mixin.socket = socketFilepath;
        mixin.workdir = overrideWorkDir;
        instance.setOverrides(mixin);

        ConfigBuilder configBuilder = ConfigBuilder.create();

        ConfigBuilder result = instance.applyOverrides(configBuilder, KeyDataBuilder.create());

        Path expected = Paths.get(overrideWorkDir, socketFilepath);

        assertThat(result.build().getUnixSocketFile()).isEqualByComparingTo(expected);
    }

    @Test
    public void applyOverrides() throws Exception {
        final int portOverride = 9999;
        final String unixSocketFileOverride = "unixSocketFileOverride.ipc";
        final String workdirOverride = "workdirOverride";
        final List<Peer> overridePeers =
                Arrays.asList(new Peer("http://otherone.com:9188/other"), new Peer("http://yetanother.com:8829/other"));

        final List<Path> privateKeyPaths =
                Arrays.asList(
                        Files.createTempFile("applyOverrides1", ".txt"),
                        Files.createTempFile("applyOverrides2", ".txt"));

        final byte[] privateKeyData = FixtureUtil.createLockedPrivateKey().toString().getBytes();
        for (Path p : privateKeyPaths) {
            Files.write(p, privateKeyData);
        }

        final List<String> privateKeyPasswords = Arrays.asList("SECRET1", "SECRET2");
        final Path privateKeyPasswordFile = Files.createTempFile("applyOverridesPasswords", ".txt");
        Files.write(privateKeyPasswordFile, privateKeyPasswords);

        final LegacyOverridesMixin mixin = new LegacyOverridesMixin();
        mixin.url = "http://junit.com:8989";
        mixin.port = portOverride;
        mixin.socket = unixSocketFileOverride;
        mixin.workdir = workdirOverride;
        mixin.othernodes = overridePeers.stream().map(Peer::getUrl).collect(toList());
        mixin.publickeys = Stream.of("ONE", "TWO").collect(toList());
        mixin.storage = "sqlite:somepath";
        mixin.tlsservertrust = SslTrustMode.WHITELIST;
        mixin.tlsclienttrust = SslTrustMode.CA;
        mixin.tlsservercert = "tlsservercert.cert";
        mixin.tlsclientcert = "tlsclientcert.cert";
        mixin.tlsserverchain = Stream.of("server1.crt", "server2.crt", "server3.crt").collect(toList());
        mixin.tlsclientchain = Stream.of("client1.crt", "client2.crt", "client3.crt").collect(toList());
        mixin.tlsserverkey = "tlsserverkey.key";
        mixin.tlsclientkey = "tlsclientkey.key";
        mixin.privatekeys = privateKeyPaths.stream().map(Path::toString).collect(toList());
        mixin.passwords = privateKeyPasswordFile.toString();
        mixin.tlsknownclients = "tlsknownclients.file";
        mixin.tlsknownservers = "tlsknownservers.file";
        mixin.alwayssendto = new ArrayList<>();
        instance.setOverrides(mixin);

        final Config result = instance.applyOverrides(builderWithValidValues, KeyDataBuilder.create()).build();

        assertThat(result).isNotNull();

        final SslConfig sslConfig = result.getServer().getSslConfig();

        assertThat(result.getServer().getHostName()).isEqualTo("http://junit.com");
        assertThat(result.getServer().getPort()).isEqualTo(portOverride);
        assertThat(result.getServer().getBindingAddress()).isEqualTo("http://junit.com:" + portOverride);
        assertThat(result.getUnixSocketFile()).isEqualTo(Paths.get(workdirOverride, unixSocketFileOverride));
        assertThat(result.getPeers()).containsExactly(overridePeers.toArray(new Peer[0]));
        assertThat(result.getKeys().getKeyData()).hasSize(2);
        assertThat(result.getJdbcConfig()).isNotNull();
        assertThat(result.getJdbcConfig().getUrl()).isEqualTo("jdbc:sqlite:somepath");

        assertThat(sslConfig.getServerTrustMode()).isEqualTo(SslTrustMode.WHITELIST);
        assertThat(sslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA);
        assertThat(sslConfig.getClientTlsCertificatePath()).isEqualTo(Paths.get("workdirOverride/tlsclientcert.cert"));
        assertThat(sslConfig.getServerTlsCertificatePath()).isEqualTo(Paths.get("workdirOverride/tlsservercert.cert"));

        assertThat(sslConfig.getServerTrustCertificates())
                .containsExactly(
                        Paths.get(workdirOverride, "server1.crt"),
                        Paths.get(workdirOverride, "server2.crt"),
                        Paths.get(workdirOverride, "server3.crt"));

        assertThat(sslConfig.getClientTrustCertificates())
                .containsExactly(
                        Paths.get(workdirOverride, "client1.crt"),
                        Paths.get(workdirOverride, "client2.crt"),
                        Paths.get(workdirOverride, "client3.crt"));

        assertThat(sslConfig.getServerKeyStore()).isEqualTo(Paths.get("workdirOverride/sslServerKeyStorePath"));
        assertThat(sslConfig.getClientKeyStore()).isEqualTo(Paths.get("workdirOverride/sslClientKeyStorePath"));
        assertThat(sslConfig.getKnownServersFile()).isEqualTo(Paths.get("workdirOverride/tlsknownservers.file"));
        assertThat(sslConfig.getKnownClientsFile()).isEqualTo(Paths.get(workdirOverride, "tlsknownclients.file"));
    }

    @Test
    public void applyOverridesNullValues() {
        Config expectedValues = builderWithValidValues.build();

        Config result = instance.applyOverrides(builderWithValidValues, KeyDataBuilder.create()).build();

        assertThat(result).isNotNull();

        final DeprecatedServerConfig expectedServerConfig = expectedValues.getServer();
        final DeprecatedServerConfig realServerConfig = result.getServer();
        final SslConfig sslConfig = realServerConfig.getSslConfig();

        assertThat(realServerConfig.getHostName()).isEqualTo(expectedServerConfig.getHostName());
        assertThat(realServerConfig.getPort()).isEqualTo(expectedServerConfig.getPort());
        assertThat(realServerConfig.getBindingAddress()).isEqualTo(expectedServerConfig.getBindingAddress());

        assertThat(result.getUnixSocketFile()).isEqualTo(expectedValues.getUnixSocketFile());

        assertThat(result.getPeers()).containsOnlyElementsOf(expectedValues.getPeers());

        assertThat(result.getJdbcConfig().getUrl()).isEqualTo("jdbc:bogus");

        assertThat(sslConfig.getServerTrustMode()).isEqualTo(SslTrustMode.TOFU);
        assertThat(sslConfig.getClientTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);
        assertThat(sslConfig.getClientKeyStore()).isEqualTo(Paths.get("sslClientKeyStorePath"));
        assertThat(sslConfig.getServerKeyStore()).isEqualTo(Paths.get("sslServerKeyStorePath"));
        assertThat(sslConfig.getServerTrustCertificates()).containsExactly(Paths.get("sslServerTrustCertificates"));
        assertThat(sslConfig.getClientTrustCertificates()).containsExactly(Paths.get("sslClientTrustCertificates"));
        assertThat(sslConfig.getKnownServersFile()).isEqualTo(Paths.get("knownServersFile"));
        assertThat(sslConfig.getKnownClientsFile()).isEqualTo(Paths.get("knownClientsFile"));
    }

    @Test
    public void writeToOutputFileValidationError() throws Exception {
        Config config = mock(Config.class);

        Path outputPath = Files.createTempFile("writeToOutputFileValidationError", ".txt");

        CliResult result = LegacyCliAdapter.writeToOutputFile(config, outputPath);

        assertThat(result.getStatus()).isEqualTo(2);
    }
}
