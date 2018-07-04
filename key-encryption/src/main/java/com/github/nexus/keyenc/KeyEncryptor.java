package com.github.nexus.keyenc;

import com.github.nexus.nacl.Key;

/**
 * Provides key encryption and decryption for private keys
 * into the representation expected by a private key file
 */
public interface KeyEncryptor {

    int SALTLENGTH = 16;

    /**
     * Encrypts a private key using the given password and returns a JSON object that can
     * be interpreted when decrypting
     *
     * @param privateKey the key to encrypt
     * @param password   the password to encrypt the key with
     * @return com.github.nexus.config.PrivateKey
     */
    KeyConfig encryptPrivateKey(Key privateKey, String password);

    /**
     * Decrypts a private key using the password and information provided by the given
     * JSON object. What information the object contains is up to the implementor.
     *
     * @param privateKey
     * @return the decrypted private key
     */
    Key decryptPrivateKey(KeyConfig privateKey);

}
