package com.github.nexus.service.locator;

import java.util.ServiceLoader;
import java.util.Set;

public interface ServiceLocator {

    Set<Object> getServices(String filename);

    static ServiceLocator create() {
        return ServiceLoader.load(ServiceLocator.class).iterator().next();
    }

}
