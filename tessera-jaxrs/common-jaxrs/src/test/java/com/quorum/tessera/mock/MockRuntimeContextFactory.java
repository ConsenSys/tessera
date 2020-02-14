package com.quorum.tessera.mock;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;

import static org.mockito.Mockito.mock;

public class MockRuntimeContextFactory implements RuntimeContextFactory {
    @Override
    public RuntimeContext create(Config config) {
        return mock(RuntimeContext.class);
    }
}
