package com.github.nexus.argon2;

import java.util.Arrays;

public class ArgonResult {

    private final ArgonOptions options;

    private final byte[] salt;

    private final byte[] hash;

    public ArgonResult(final ArgonOptions options, final byte[] salt, final byte[] hash) {
        this.options = options;
        this.salt = Arrays.copyOf(salt, salt.length);
        this.hash = Arrays.copyOf(hash, hash.length);
    }

    public ArgonOptions getOptions() {
        return options;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

}
