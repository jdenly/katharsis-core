package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.CollectionGet;
import io.katharsis.dispatcher.controller.ResourceGet;
import io.katharsis.resource.registry.ResourceRegistry;

public class ControllerRegistryBuilder {

    public ControllerRegistry build(ResourceRegistry resourceRegistry) {
        CollectionGet collectionGet = new CollectionGet(resourceRegistry);
        ResourceGet resourceGet = new ResourceGet(resourceRegistry);
        return new ControllerRegistry(collectionGet, resourceGet);
    }
}