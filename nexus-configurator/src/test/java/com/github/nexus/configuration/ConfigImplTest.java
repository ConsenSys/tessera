package com.github.nexus.configuration;

import com.github.nexus.configuration.model.KeyData;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigImplTest {

    @Test
    public void gettersManipulateProperties() {

        final Properties configProperties = new Properties();

        configProperties.setProperty("keygenBasePath", "basepath");
        configProperties.setProperty("url", "http://url.com");
        configProperties.setProperty("port", "2000");
        configProperties.setProperty("othernodes", "node1.com,node2.com:10000");
        configProperties.setProperty("generatekeys", "newkey1,newkey2");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.keygenBasePath()).isEqualTo(Paths.get("basepath").toAbsolutePath());
        assertThat(configuration.url()).isEqualTo("http://url.com");
        assertThat(configuration.port()).isEqualTo(2000);
        assertThat(configuration.othernodes()).hasSize(2).containsExactly("node1.com", "node2.com:10000");
        assertThat(configuration.generatekeys()).hasSize(2).containsExactly("newkey1", "newkey2");

    }

    @Test
    public void keysProperties() {

        final JsonObject[] privateKeys = new JsonObject[]{
            Json.createObjectBuilder().add("key", "priv1").build(),
            Json.createObjectBuilder().add("key", "priv2").build(),
            Json.createObjectBuilder().add("key", "priv3").build()
        };

        final Properties configProperties = new Properties();

        configProperties.setProperty("publicKeys", "key1,key2,key3");
        configProperties.setProperty("privateKeys", "{\"key\": \"priv1\"},{\"key\": \"priv2\"},{\"key\": \"priv3\"}");
        configProperties.setProperty("passwords", "p1,,p2");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.publicKeys()).hasSize(3).containsExactly("key1", "key2", "key3");
        assertThat(configuration.privateKeys()).hasSize(3).containsExactlyInAnyOrder(privateKeys);
        assertThat(configuration.passwords()).hasSize(3).containsExactly("p1", "", "p2");
        assertThat(configuration.keyData())
            .hasSize(3)
            .containsExactlyInAnyOrder(
                new KeyData("key1", privateKeys[0], "p1"),
                new KeyData("key2", privateKeys[1], ""),
                new KeyData("key3", privateKeys[2], "p2")
            );

    }

    @Test
    public void errorThrownIfSameNumberOfKeysAndPasswordsNotProvided() {

        final Properties configProperties = new Properties();

        configProperties.setProperty("publicKeys", "key1,key2,key3");
        configProperties.setProperty("privateKeys", "{\"key\": \"priv1\"},{\"key\": \"priv2\"},{\"key\": \"priv3\"}");
        configProperties.setProperty("passwords", "p1,");

        final Throwable throwable = catchThrowable(() -> new ConfigurationImpl(configProperties).keyData());

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Public, private keys and passwords must match up");

    }

    @Test
    public void emptyKeysAtEndReturnsCorrectAmount() {

        final Properties configProperties = new Properties();
        configProperties.put("passwords", "p1,p2,,");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.passwords()).hasSize(4);

    }

    @Test
    public void emptyKeynamesGetFiltered() {
        final Properties configProperties = new Properties();
        configProperties.put("generatekeys", "p1,,p2");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.generatekeys()).hasSize(2);
    }

}
