package com.quorum.tessera.context;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

public interface KeyVaultConfigValidations {

    static KeyVaultConfigValidations create() {
        return ServiceLoaderUtil.load(KeyVaultConfigValidations.class).orElse(new DefaultKeyVaultConfigValidations());
    }

    Set<ConstraintViolation<?>> validate(KeyConfiguration keys, List<ConfigKeyPair> configKeyPairs);
}
