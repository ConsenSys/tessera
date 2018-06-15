package com.github.nexus.api;

import com.github.nexus.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class NexusTest {

    private static final String contextName = "context";

    private ServiceLocator serviceLocator;

    private Nexus nexus;

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        nexus = new Nexus(serviceLocator, contextName);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {
        nexus.getSingletons();
        verify(serviceLocator).getServices(contextName);
    }

    @Test
    public void createWithNoServiceLocator() {

        final Throwable throwable = catchThrowable(() -> new Nexus(null, contextName));
        assertThat(throwable).isInstanceOf(NullPointerException.class);

        final Throwable throwableName = catchThrowable(() -> new Nexus(serviceLocator, null));
        assertThat(throwableName).isInstanceOf(NullPointerException.class);

    }
}
