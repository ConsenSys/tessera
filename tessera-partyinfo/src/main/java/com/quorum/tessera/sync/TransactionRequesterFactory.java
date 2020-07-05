package com.quorum.tessera.sync;

import com.quorum.tessera.loader.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

public interface TransactionRequesterFactory {

    default TransactionRequester createTransactionRequester(Config config) {
        Enclave enclave = EnclaveFactory.create().create(config);
        ResendClient resendClient = ResendClientFactory.newFactory(config).create(config);
        return new TransactionRequesterImpl(enclave,resendClient);
    }

    static TransactionRequesterFactory newFactory() {
        return ServiceLoaderUtil.load(TransactionRequesterFactory.class)
            .orElse(new TransactionRequesterFactory() {
        });
    }

}
