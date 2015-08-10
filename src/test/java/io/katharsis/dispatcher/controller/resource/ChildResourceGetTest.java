package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChildResourceGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ChildResourceGet sut = new ChildResourceGet(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onNonChildRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ChildResourceGet sut = new ChildResourceGet(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestLinkChildResourceGetShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/project");
        ChildResourceGet sut = new ChildResourceGet(resourceRegistry, typeParser);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestLinkChildResourcesGetShouldHandleIt() throws Exception {
        // GIVEN
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
        ChildResourceGet sut = new ChildResourceGet(resourceRegistry, typeParser);
        RegistryEntry parentRegistryEntry = mock(RegistryEntry.class);
        RelationshipRepository relationshipRepository = mock(RelationshipRepository.class);
        Set<Project> projects = new HashSet<>();
        projects.add(new Project());
        ResourceInformationBuilder rib = new ResourceInformationBuilder();

        when(resourceRegistry.getEntry("users")).thenReturn(parentRegistryEntry);
        when(parentRegistryEntry.getResourceInformation()).thenReturn(rib.build(User.class));
        when(parentRegistryEntry.getRelationshipRepositoryForClass(Project.class)).thenReturn(relationshipRepository);
        //noinspection unchecked
        when(relationshipRepository.findManyTargets(any(), any(), any())).thenReturn(projects);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), null);

        // THEN
        Assert.assertNotNull(response);
    }
}
