package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringBootDemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.annotation.Validated;

import com.example.exception.BadRequestException;
import com.example.exception.ForbiddenRequestException;
import com.example.exception.GenericException;
import com.example.model.DossierDescriptionRequest;
import com.example.model.NuxeoPartialUpdateDossier;
import com.example.model.NuxeoPartialUpdateDossierProperties;
import com.example.service.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class PartialUpdateDossierServiceImplTest {

    @InjectMocks
    private PartialUpdateDossierServiceImpl partialUpdateDossierService;

    @Mock
    private GetDossierESHandler getDossierESHandler;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private SecurityService securityService;

    @Mock
    private PartialUpdateDossierServiceHandler partialUpdateDossierServiceHandler;

    @Mock
    private AuditService auditService;

    @Mock
    private UserAuth userAuth;

    @Mock
    private ValidateAdministration validateAdministration;

    private String consumerId;
    private String traceId;
    private String dossierId;
    private DossierDescriptionRequest descriptionRequest;
    private Map<String, Object> metadata;

    @BeforeEach
    public void setup() {
        consumerId = "testConsumerId";
        traceId = "testTraceId";
        dossierId = "testDossierId";
        descriptionRequest = new DossierDescriptionRequest();
        descriptionRequest.setDescription("Test Description");

        metadata = new HashMap<>();
        metadata.put("dossierStatus", "open");
    }

    @Test
    public void testPartialUpdateDossierByPatch_Success() throws Exception {
        String actorId = "actorId";

        // Mock behavior
        when(validateAdministration.validateUser(consumerId, traceId)).thenReturn(actorId);
        when(getDossierESHandler.verifyDossierExists(actorId, dossierId, "uniqueId", "searchDossier", traceId))
                .thenReturn(mock(SearchHit.class));
        when(resourceMapper.resourceAttributeMapper(any(), eq("DOSSIER"), eq("UPDATE_DOSSIER")))
                .thenReturn(mock(Resource.class));
        when(securityService.getAuthorizationDecision(any(), eq(traceId), eq(actorId)))
                .thenReturn(new Decision("permit", "employee"));

        // Call the method to test
        partialUpdateDossierService.partialUpdateDossierByPatch(consumerId, traceId, dossierId, descriptionRequest);

        // Verify interactions
        verify(partialUpdateDossierServiceHandler, times(1))
                .partialUpdateDossierNuxeo(any(NuxeoPartialUpdateDossier.class), eq(actorId), eq(traceId));
        verify(auditService, times(1)).auditRequest(any(), eq(actorId), eq(traceId), eq("UPDATE_DOSSIER"));
    }

    @Test
    public void testPartialUpdateDossierByPatch_DossierClosed() throws Exception {
        String actorId = "actorId";
        metadata.put("dossierStatus", "CLOSED");

        // Mock behavior
        when(validateAdministration.validateUser(consumerId, traceId)).thenReturn(actorId);
        when(getDossierESHandler.verifyDossierExists(actorId, dossierId, "uniqueId", "searchDossier", traceId))
                .thenReturn(mock(SearchHit.class));
        when(resourceMapper.resourceAttributeMapper(any(), eq("DOSSIER"), eq("UPDATE_DOSSIER")))
                .thenReturn(mock(Resource.class));
        when(securityService.getAuthorizationDecision(any(), eq(traceId), eq(actorId)))
                .thenReturn(new Decision("permit", "employee"));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> partialUpdateDossierService.partialUpdateDossierByPatch(consumerId, traceId, dossierId, descriptionRequest));

        assertEquals("DOSSIER_ALREADY_CLOSED", exception.getMessage());
    }

    @Test
    public void testPartialUpdateDossierByPatch_AccessDenied() throws Exception {
        String actorId = "actorId";

        // Mock behavior
        when(validateAdministration.validateUser(consumerId, traceId)).thenReturn(actorId);
        when(getDossierESHandler.verifyDossierExists(actorId, dossierId, "uniqueId", "searchDossier", traceId))
                .thenReturn(mock(SearchHit.class));
        when(resourceMapper.resourceAttributeMapper(any(), eq("DOSSIER"), eq("UPDATE_DOSSIER")))
                .thenReturn(mock(Resource.class));
        when(securityService.getAuthorizationDecision(any(), eq(traceId), eq(actorId)))
                .thenReturn(new Decision("deny", "employee"));

        ForbiddenRequestException exception = assertThrows(ForbiddenRequestException.class,
                () -> partialUpdateDossierService.partialUpdateDossierByPatch(consumerId, traceId, dossierId, descriptionRequest));

        assertEquals("ACCESS_DENIED", exception.getMessage());
    }

    @Test
    public void testPartialUpdateDossierByPatch_MissingSearchHit() throws Exception {
        String actorId = "actorId";

        // Mock behavior
        when(validateAdministration.validateUser(consumerId, traceId)).thenReturn(actorId);
        when(getDossierESHandler.verifyDossierExists(actorId, dossierId, "uniqueId", "searchDossier", traceId))
                .thenReturn(null);

        GenericException exception = assertThrows(GenericException.class,
                () -> partialUpdateDossierService.partialUpdateDossierByPatch(consumerId, traceId, dossierId, descriptionRequest));

        assertEquals("DOSSIER_NOT_FOUND", exception.getMessage());
    }
}




@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PartialUpdateDossierServiceImplTest {

    @InjectMocks
    private PartialUpdateDossierServiceImpl partialUpdateDossierServiceImpl;

    @Mock
    private GetDossierESHandler getDossierESHandler;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private SecurityService securityService;

    @Mock
    private PartialUpdateDossierServiceHandler partialUpdateDossierServiceHandler;

    @Mock
    private AuditService auditService;

    @Mock
    private UserAuth userAuth;

    @Mock
    private ValidateAdministration validateAdministration;

    private DossierDescriptionRequest dossierDescriptionRequest;
    private String consumerId = "drm_nl_test";
    private String traceId = "12345";
    private String dossierId = "122344567898888";

    @BeforeEach
    void setup() {
        dossierDescriptionRequest = new DossierDescriptionRequest();
        dossierDescriptionRequest.setDescription("Test CTV Dossier");
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Positive Scenario of Partial Update Dossier")
    public void testPartialUpdateDossier() throws GenericException {
        // Mock necessary objects
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("DOS_DOSSIER_STATUS", "OPEN");
        metadata.put("REPO_ID", "repo-id");
        metadata.put("BASE_UNIQUE_ID", "UID123");

        SearchHit searchHit = Mockito.mock(SearchHit.class);
        when(searchHit.getSourceAsMap()).thenReturn(metadata);

        Resource resource = Mockito.mock(Resource.class);
        Decision decision = new Decision("permit");

        // Mocks for methods
        when(validateAdministration.validateUser(anyString(), anyString())).thenReturn("actorId");
        when(getDossierESHandler.verifyDossierExists(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(searchHit);
        when(resourceMapper.resourceAttributeMapper(anyMap(), anyString(), anyString())).thenReturn(resource);
        when(securityService.getAuthorizationDecision(any(Resource.class), anyString(), anyString())).thenReturn(decision);
        when(userAuth.getAuthTokenUserId()).thenReturn("userId");

        // No exceptions expected
        doNothing().when(partialUpdateDossierServiceHandler)
                .partialUpdateDossierNuxeo(any(NuxeoPartialUpdateDossier.class), anyString(), anyString());

        doNothing().when(auditService).auditRequest(anyString(), anyString(), anyString(), anyString(), anyString());

        // Call the method
        partialUpdateDossierServiceImpl.partialUpdateDossierByPatch(consumerId, traceId, dossierId, dossierDescriptionRequest);

        // Verify expected calls
        verify(partialUpdateDossierServiceHandler, times(1))
                .partialUpdateDossierNuxeo(any(NuxeoPartialUpdateDossier.class), anyString(), anyString());
        verify(auditService, times(1))
                .auditRequest(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Negative Scenario of Partial Update Dossier - Dossier Closed")
    public void testPartialUpdateDossier_when_DossierClosed() throws GenericException {
        // Mock necessary objects
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("DOS_DOSSIER_STATUS", "CLOSED");

        SearchHit searchHit = Mockito.mock(SearchHit.class);
        when(searchHit.getSourceAsMap()).thenReturn(metadata);

        when(validateAdministration.validateUser(anyString(), anyString())).thenReturn("actorId");
        when(getDossierESHandler.verifyDossierExists(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(searchHit);

        // Assert that a BadRequestException is thrown when the dossier is closed
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            partialUpdateDossierServiceImpl.partialUpdateDossierByPatch(consumerId, traceId, dossierId, dossierDescriptionRequest);
        });

        assertEquals(DOSSIER_ALREADY_CLOSED.name(), exception.getMessage());
    }

    @Test
    @DisplayName("Negative Scenario - Access Denied")
    public void testPartialUpdateDossier_when_AccessDenied() throws GenericException {
        // Mock necessary objects
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("DOS_DOSSIER_STATUS", "OPEN");

        SearchHit searchHit = Mockito.mock(SearchHit.class);
        when(searchHit.getSourceAsMap()).thenReturn(metadata);

        Resource resource = Mockito.mock(Resource.class);
        Decision decision = new Decision("deny");

        when(validateAdministration.validateUser(anyString(), anyString())).thenReturn("actorId");
        when(getDossierESHandler.verifyDossierExists(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(searchHit);
        when(resourceMapper.resourceAttributeMapper(anyMap(), anyString(), anyString())).thenReturn(resource);
        when(securityService.getAuthorizationDecision(any(Resource.class), anyString(), anyString())).thenReturn(decision);

        // Assert that a ForbiddenRequestException is thrown
        ForbiddenRequestException exception = assertThrows(ForbiddenRequestException.class, () -> {
            partialUpdateDossierServiceImpl.partialUpdateDossierByPatch(consumerId, traceId, dossierId, dossierDescriptionRequest);
        });

        assertEquals(ACCESS_DENIED.name(), exception.getMessage());
    }
}

