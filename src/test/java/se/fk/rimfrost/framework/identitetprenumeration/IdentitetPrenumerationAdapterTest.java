package se.fk.rimfrost.framework.identitetprenumeration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import io.quarkus.test.InjectMock;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.identitetprenumeration.adapter.IdentitetPrenumerationAdapter;
import se.fk.rimfrost.framework.identitetprenumeration.adapter.IdentitetPrenumerationMapper;
import se.fk.rimfrost.framework.identitetprenumeration.exception.IdentitetPrenumerationException;
import se.fk.rimfrost.framework.identitetprenumeration.model.ImmutableCreatePrenumerationRequest;
import se.fk.rimfrost.framework.identitetprenumeration.model.ImmutableIdtyp;
import se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.CreatePrenumerationRequest;
import se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.Idtyp;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusComponentTest(useSystemConfigSources = true)
public class IdentitetPrenumerationAdapterTest
{

   private static WireMockServer server;

   @Inject
   IdentitetPrenumerationAdapter adapter;

   @InjectMock
   IdentitetPrenumerationMapper mapper;

   @BeforeAll
   public static void setup()
   {
      server = new WireMockServer(options().dynamicPort());
      server.start();
      System.setProperty("identitetprenumeration.api.base-url", server.baseUrl());
   }

   @AfterAll
   public static void teardown()
   {
      if (server != null)
      {
         server.stop();
      }
   }

   @BeforeEach
   void resetStubs()
   {
      server.resetToDefaultMappings();
   }

   private se.fk.rimfrost.framework.identitetprenumeration.model.CreatePrenumerationRequest createDomainRequest()
   {
      return ImmutableCreatePrenumerationRequest.builder()
            .prenumerationId(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
            .addIdentiteter(ImmutableIdtyp.builder()
                  .typId("4c34906c-03d9-425f-9a1a-062ef6eb88c7")
                  .varde("19901010-1234")
                  .build())
            .replyTo("test-reply-topic")
            .build();
   }

   private CreatePrenumerationRequest createApiRequest()
   {
      var apiIdtyp = new Idtyp("4c34906c-03d9-425f-9a1a-062ef6eb88c7", "19901010-1234");
      return new CreatePrenumerationRequest(
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
            List.of(apiIdtyp),
            "test-reply-topic");
   }

   // ---------------------
   //
   // createPrenumeration
   //
   // ---------------------

   @Test
   @DisplayName("createPrenumeration: 400 ger BAD_REQUEST")
   public void should_throw_bad_request_on_400_during_create_prenumeration()
   {
      var request = createDomainRequest();
      Mockito.when(mapper.toApiCreatePrenumerationRequest(request)).thenReturn(createApiRequest());
      server.stubFor(WireMock.post(WireMock.urlPathEqualTo("/prenumerationer"))
            .willReturn(WireMock.aResponse().withStatus(400)));

      var exception = assertThrows(IdentitetPrenumerationException.class,
            () -> adapter.createPrenumeration(request));

      assertEquals(IdentitetPrenumerationException.ErrorType.BAD_REQUEST, exception.getErrorType());
   }

   @Test
   @DisplayName("createPrenumeration: 500 ger UNEXPECTED_ERROR")
   public void should_throw_unexpected_error_on_500_during_create_prenumeration()
   {
      var request = createDomainRequest();
      Mockito.when(mapper.toApiCreatePrenumerationRequest(request)).thenReturn(createApiRequest());
      server.stubFor(WireMock.post(WireMock.urlPathEqualTo("/prenumerationer"))
            .willReturn(WireMock.aResponse().withStatus(500)));

      var exception = assertThrows(IdentitetPrenumerationException.class,
            () -> adapter.createPrenumeration(request));

      assertEquals(IdentitetPrenumerationException.ErrorType.UNEXPECTED_ERROR, exception.getErrorType());
   }

   @Test
   @DisplayName("createPrenumeration: anslutningsfel ger SERVICE_UNAVAILABLE")
   public void should_throw_service_unavailable_on_connection_reset_during_create_prenumeration()
   {
      var request = createDomainRequest();
      Mockito.when(mapper.toApiCreatePrenumerationRequest(request)).thenReturn(createApiRequest());
      server.stubFor(WireMock.post(WireMock.urlPathEqualTo("/prenumerationer"))
            .willReturn(WireMock.aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

      var exception = assertThrows(IdentitetPrenumerationException.class,
            () -> adapter.createPrenumeration(request));

      assertEquals(IdentitetPrenumerationException.ErrorType.SERVICE_UNAVAILABLE, exception.getErrorType());
   }

   @Test
   @DisplayName("createPrenumeration: 201 lyckas utan undantag")
   public void should_create_prenumeration_successfully()
   {
      var request = createDomainRequest();
      Mockito.when(mapper.toApiCreatePrenumerationRequest(request)).thenReturn(createApiRequest());
      server.stubFor(WireMock.post(WireMock.urlPathEqualTo("/prenumerationer"))
            .willReturn(WireMock.aResponse().withStatus(201)));

      assertDoesNotThrow(() -> adapter.createPrenumeration(request));
   }

   // ---------------------
   //
   // deletePrenumeration
   //
   // ---------------------

   @Test
   @DisplayName("deletePrenumeration: 404 ger NOT_FOUND")
   public void should_throw_not_found_on_404_during_delete_prenumeration()
   {
      var prenumerationId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
      server.stubFor(WireMock.delete(WireMock.urlPathMatching("/prenumerationer/.+"))
            .willReturn(WireMock.aResponse().withStatus(404)));

      var exception = assertThrows(IdentitetPrenumerationException.class,
            () -> adapter.deletePrenumeration(prenumerationId));

      assertEquals(IdentitetPrenumerationException.ErrorType.NOT_FOUND, exception.getErrorType());
   }

   @Test
   @DisplayName("deletePrenumeration: 400 ger BAD_REQUEST")
   public void should_throw_bad_request_on_400_during_delete_prenumeration()
   {
      var prenumerationId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
      server.stubFor(WireMock.delete(WireMock.urlPathMatching("/prenumerationer/.+"))
            .willReturn(WireMock.aResponse().withStatus(400)));

      var exception = assertThrows(IdentitetPrenumerationException.class,
            () -> adapter.deletePrenumeration(prenumerationId));

      assertEquals(IdentitetPrenumerationException.ErrorType.BAD_REQUEST, exception.getErrorType());
   }

   @Test
   @DisplayName("deletePrenumeration: 500 ger UNEXPECTED_ERROR")
   public void should_throw_unexpected_error_on_500_during_delete_prenumeration()
   {
      var prenumerationId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
      server.stubFor(WireMock.delete(WireMock.urlPathMatching("/prenumerationer/.+"))
            .willReturn(WireMock.aResponse().withStatus(500)));

      var exception = assertThrows(IdentitetPrenumerationException.class,
            () -> adapter.deletePrenumeration(prenumerationId));

      assertEquals(IdentitetPrenumerationException.ErrorType.UNEXPECTED_ERROR, exception.getErrorType());
   }

   @Test
   @DisplayName("deletePrenumeration: anslutningsfel ger SERVICE_UNAVAILABLE")
   public void should_throw_service_unavailable_on_connection_reset_during_delete_prenumeration()
   {
      var prenumerationId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
      server.stubFor(WireMock.delete(WireMock.urlPathMatching("/prenumerationer/.+"))
            .willReturn(WireMock.aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

      var exception = assertThrows(IdentitetPrenumerationException.class,
            () -> adapter.deletePrenumeration(prenumerationId));

      assertEquals(IdentitetPrenumerationException.ErrorType.SERVICE_UNAVAILABLE, exception.getErrorType());
   }

   @Test
   @DisplayName("deletePrenumeration: 204 lyckas utan undantag")
   public void should_delete_prenumeration_successfully()
   {
      var prenumerationId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
      server.stubFor(WireMock.delete(WireMock.urlPathMatching("/prenumerationer/.+"))
            .willReturn(WireMock.aResponse().withStatus(204)));

      assertDoesNotThrow(() -> adapter.deletePrenumeration(prenumerationId));
   }
}
