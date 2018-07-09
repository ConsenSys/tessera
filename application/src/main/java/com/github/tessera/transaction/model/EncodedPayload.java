package com.github.tessera.transaction.model;


import com.github.tessera.nacl.Key;
import com.github.tessera.nacl.Nonce;
import java.util.*;

/**
 * This class contains the base data that is sent to other nodes
 * (it is wrapped further, but the main data that is needed is here)
 */
public class EncodedPayload {

    private final Key senderKey;

    private final byte[] cipherText;

    private final Nonce cipherTextNonce;

    private final List<byte[]> recipientBoxes;

    private final Nonce recipientNonce;

    public EncodedPayload(final Key senderKey,
                          final byte[] cipherText,
                          final Nonce cipherTextNonce,
                          final List<byte[]> recipientBoxes,
                          final Nonce recipientNonce) {

        this.senderKey = senderKey;
        this.cipherText = cipherText;
        this.cipherTextNonce = cipherTextNonce;
        this.recipientNonce = recipientNonce;

        final List<byte[]> recBoxes = Optional
            .ofNullable(recipientBoxes)
            .orElse(new ArrayList<>());

        this.recipientBoxes = Collections.unmodifiableList(recBoxes);
    }

    public Key getSenderKey() {
        return senderKey;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public Nonce getCipherTextNonce() {
        return cipherTextNonce;
    }

    public List<byte[]> getRecipientBoxes() {
        return recipientBoxes;
    }

    public Nonce getRecipientNonce() {
        return recipientNonce;
    }
}
