package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** This class contains the data that is sent to other nodes */
public class EncodedPayload {

    private final PublicKey senderKey;

    private final byte[] cipherText;

    private final Nonce cipherTextNonce;

    private final List<byte[]> recipientBoxes;

    private final Nonce recipientNonce;

    private final List<PublicKey> recipientKeys;

    private final PrivacyMode privacyMode;

    private final Map<TxHash, byte[]> affectedContractTransactions;

    private final byte[] execHash;

    public EncodedPayload(
            final PublicKey senderKey,
            final byte[] cipherText,
            final Nonce cipherTextNonce,
            final List<byte[]> recipientBoxes,
            final Nonce recipientNonce,
            final List<PublicKey> recipientKeys,
            final PrivacyMode privacyMode,
            final Map<TxHash, byte[]> affectedContractTransactions,
            final byte[] execHash) {
        this.senderKey = senderKey;
        this.cipherText = cipherText;
        this.cipherTextNonce = cipherTextNonce;
        this.recipientBoxes = recipientBoxes;
        this.recipientNonce = recipientNonce;
        this.recipientKeys = recipientKeys;
        this.privacyMode = Objects.requireNonNull(privacyMode);
        this.affectedContractTransactions = affectedContractTransactions;
        this.execHash = execHash;
    }

    public PublicKey getSenderKey() {
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

    public List<PublicKey> getRecipientKeys() {
        return recipientKeys;
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }

    public byte[] getExecHash() {
        return execHash;
    }

    public Map<TxHash, byte[]> getAffectedContractTransactions() {
        return affectedContractTransactions;
    }
}
