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
