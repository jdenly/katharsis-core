package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathIds;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.ResourceFieldNotFoundException;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.ResourceResponse;
import io.katharsis.utils.Generics;
import io.katharsis.utils.parser.TypeParser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class ChildResourceGet extends RelationshipsResourceGet {

    private ResourceRegistry resourceRegistry;
    private TypeParser typeParser;

    public ChildResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser) {
        super(resourceRegistry, typeParser);
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
    }

    @Override
    public boolean isAcceptable(JsonPath jsonPath, String requestType) {
        return jsonPath.getParentResource() != null
                && jsonPath.getParentResource() instanceof ResourcePath
                && (jsonPath instanceof ResourcePath || jsonPath instanceof FieldPath)
                && HttpMethod.GET.name().equals(requestType);
    }

    @Override
    public BaseResponse handle(JsonPath jsonPath, RequestParams requestParams, RequestBody requestBody)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String resourceName = jsonPath.getResourceName();
        String parentResourceName = jsonPath.getParentResource().getResourceName();
        PathIds resourceIds = jsonPath.getParentResource().getIds();
        RegistryEntry<?> parentRegistryEntry = resourceRegistry.getEntry(parentResourceName);
        RegistryEntry<?> registryEntry = resourceRegistry.getEntry(resourceName);

        Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
        String elementName = jsonPath.getElementName();
        Field relationshipField = parentRegistryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
        if (relationshipField == null) {
            throw new ResourceFieldNotFoundException(elementName);
        }

        Class<?> baseRelationshipFieldClass = relationshipField.getType();
        Class<?> relationshipFieldClass = Generics.getResourceClass(relationshipField, baseRelationshipFieldClass);

        RelationshipRepository relationshipRepositoryForClass = parentRegistryEntry.getRelationshipRepositoryForClass(relationshipFieldClass);
        BaseResponse target;
        if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
            List dataList = new LinkedList<>();

            Iterable targetObjects = relationshipRepositoryForClass.findManyTargets(castedResourceId, elementName, requestParams);
            if (targetObjects != null) {
                for (Object targetObject : targetObjects) {
                    dataList.add(targetObject);
                }
            }
            target = new CollectionResponse(dataList, jsonPath, requestParams);
        } else {
            Object targetObject = relationshipRepositoryForClass.findOneTarget(castedResourceId, elementName, requestParams);
            if (targetObject != null) {
                target = new ResourceResponse(targetObject, jsonPath, requestParams);
            } else {
                target = new ResourceResponse(null, jsonPath, requestParams);
            }
        }

        return target;
    }

    private Serializable getResourceId(PathIds resourceIds, RegistryEntry<?> registryEntry) {
        String resourceId = resourceIds.getIds().get(0);
        Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
                .getResourceInformation()
                .getIdField()
                .getType();
        return typeParser.parse(resourceId, idClass);
    }
}
