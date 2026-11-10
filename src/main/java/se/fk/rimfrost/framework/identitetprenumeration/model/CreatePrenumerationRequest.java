package se.fk.rimfrost.framework.identitetprenumeration.model;

import org.immutables.value.Value;
import java.util.List;
import java.util.UUID;

/**
 * Förfrågan om att registrera en prenumeration på identitetsförändringar.
 */
@Value.Immutable
public interface CreatePrenumerationRequest
{
   /** Klient-valt ID som identifierar prenumerationen, t.ex. handlaggningId. */
   UUID prenumerationId();

   /** Lista över identiteter vars förändringar prenumeranten vill bevaka. */
   List<Idtyp> identiteter();

   /** Kafka-topic dit IdentitetUppdateradEvent skickas när en identitet förändras. */
   String replyTo();
}
