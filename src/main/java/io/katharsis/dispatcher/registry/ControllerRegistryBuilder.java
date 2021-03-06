package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.parser.TypeParser;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A builder class which holds all of the Katharsis controllers, which must be placed in
 * {@link io.katharsis.dispatcher.controller} package.
 */
public class ControllerRegistryBuilder {

    public ControllerRegistry build(ResourceRegistry resourceRegistry, TypeParser typeParser) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Reflections reflections = new Reflections("io.katharsis.dispatcher.controller");

        Set<Class<? extends BaseController>> controllerClasses =
                reflections.getSubTypesOf(BaseController.class);

        List<BaseController> controllers = new LinkedList<>();
        for (Class<? extends BaseController> controllerClass : controllerClasses) {
            if ( !Modifier.isAbstract(controllerClass.getModifiers())) {
                Constructor<? extends BaseController> declaredConstructor = controllerClass.getDeclaredConstructor
                        (ResourceRegistry.class, TypeParser.class);
                controllers.add(declaredConstructor.newInstance(resourceRegistry, typeParser));
            }
        }

        return new ControllerRegistry(controllers);
    }
}
