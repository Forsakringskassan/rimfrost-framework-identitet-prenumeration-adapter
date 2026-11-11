package se.fk.rimfrost.framework.identitetprenumeration;

import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.identitetprenumeration.adapter.IdentitetPrenumerationMapper;
import se.fk.rimfrost.framework.identitetprenumeration.model.ImmutableCreatePrenumerationRequest;
import se.fk.rimfrost.framework.identitetprenumeration.model.ImmutableIdtyp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusComponentTest
public class IdentitetPrenumerationMapperTest
{

   @Inject
   IdentitetPrenumerationMapper mapper;

   @Test
   @DisplayName("toApiCreatePrenumerationRequest: alla fält mappas korrekt")
   public void should_map_all_fields_in_create_prenumeration_request()
   {
      var prenumerationId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
      var idtyp = ImmutableIdtyp.builder()
            .typId("4c34906c-03d9-425f-9a1a-062ef6eb88c7")
            .varde("19901010-1234")
            .build();
      var request = ImmutableCreatePrenumerationRequest.builder()
            .prenumerationId(prenumerationId)
            .addIdentiteter(idtyp)
            .replyTo("test-reply-topic")
            .build();

      var apiRequest = mapper.toApiCreatePrenumerationRequest(request);

      assertEquals(prenumerationId, apiRequest.getPrenumerationId());
      assertEquals("test-reply-topic", apiRequest.getReplyTo());
      assertEquals(1, apiRequest.getIdentiteter().size());
      assertEquals("4c34906c-03d9-425f-9a1a-062ef6eb88c7", apiRequest.getIdentiteter().get(0).getTypId());
      assertEquals("19901010-1234", apiRequest.getIdentiteter().get(0).getVarde());
   }

   @Test
   @DisplayName("toApiCreatePrenumerationRequest: flera identiteter mappas korrekt")
   public void should_map_multiple_identiteter()
   {
      var request = ImmutableCreatePrenumerationRequest.builder()
            .prenumerationId(UUID.randomUUID())
            .addIdentiteter(ImmutableIdtyp.builder().typId("typ-1").varde("varde-1").build())
            .addIdentiteter(ImmutableIdtyp.builder().typId("typ-2").varde("varde-2").build())
            .replyTo("some-topic")
            .build();

      var apiRequest = mapper.toApiCreatePrenumerationRequest(request);

      assertEquals(2, apiRequest.getIdentiteter().size());
      assertEquals("typ-1", apiRequest.getIdentiteter().get(0).getTypId());
      assertEquals("varde-1", apiRequest.getIdentiteter().get(0).getVarde());
      assertEquals("typ-2", apiRequest.getIdentiteter().get(1).getTypId());
      assertEquals("varde-2", apiRequest.getIdentiteter().get(1).getVarde());
   }
}
