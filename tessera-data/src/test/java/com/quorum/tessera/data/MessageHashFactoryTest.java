package com.quorum.tessera.data;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageHashFactoryTest {

    @Test
    public void createFromCiperText() {
        MessageHashFactory messageHashFactory = new MessageHashFactoryImpl();
        String cipherText = "cipherText";
        MessageHash messageHash = messageHashFactory.createFromCipherText(cipherText.getBytes());

        assertThat(messageHash).isNotNull();
    }

    @Test
    public void create() {
        assertThat(MessageHashFactory.create()).isNotNull()
            .isExactlyInstanceOf(MessageHashFactoryImpl.class);
    }
}
