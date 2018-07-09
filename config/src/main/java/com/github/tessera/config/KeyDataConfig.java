package com.github.tessera.config;

import com.github.tessera.config.adapters.PrivateKeyTypeAdapter;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyDataConfig {

    @NotNull
    @XmlElement(name = "data")
    private final PrivateKeyData privateKeyData;

    @NotNull
    @XmlAttribute
    @XmlJavaTypeAdapter(PrivateKeyTypeAdapter.class)
    private final PrivateKeyType type;

    public KeyDataConfig(PrivateKeyData privateKeyData, PrivateKeyType type) {
        this.privateKeyData = privateKeyData;
        this.type = type;
    }

    private static KeyDataConfig create() {
        return new KeyDataConfig(null, null);
    }

    public PrivateKeyType getType() {
        return type;
    }

    public PrivateKeyData getPrivateKeyData() {
        return privateKeyData;
    }

    public String getValue() {
        return privateKeyData.getValue();
    }

    public String getSnonce() {
        return privateKeyData.getSnonce();
    }

    public String getAsalt() {
        return privateKeyData.getAsalt();
    }

    public String getSbox() {
        return privateKeyData.getSbox();
    }

    public ArgonOptions getArgonOptions() {
        return privateKeyData.getArgonOptions();
    }

    public String getPassword() {
        return privateKeyData.getPassword();
    }

}
