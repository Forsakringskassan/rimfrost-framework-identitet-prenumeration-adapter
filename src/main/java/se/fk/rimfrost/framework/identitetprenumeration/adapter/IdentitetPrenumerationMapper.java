package se.fk.rimfrost.framework.identitetprenumeration.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.identitetprenumeration.model.CreatePrenumerationRequest;
import se.fk.rimfrost.framework.identitetprenumeration.model.Idtyp;
import java.util.List;

@ApplicationScoped
public class IdentitetPrenumerationMapper
{

   /**
    * Konverterar en domän-{@link CreatePrenumerationRequest} till API-modellen.
    *
    * @param request domänmodell
    * @return API-modell redo att skickas till tjänsten
    */
   public se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.CreatePrenumerationRequest toApiCreatePrenumerationRequest(
         CreatePrenumerationRequest request)
   {
      return new se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.CreatePrenumerationRequest(
            request.prenumerationId(),
            toApiIdentiteter(request.identiteter()),
            request.replyTo());
   }

   private List<se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.Idtyp> toApiIdentiteter(
         List<Idtyp> identiteter)
   {
      return identiteter.stream()
            .map(this::toApiIdtyp)
            .toList();
   }

   private se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.Idtyp toApiIdtyp(
         Idtyp idtyp)
   {
      return new se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.model.Idtyp(
            idtyp.typId(),
            idtyp.varde());
   }
}
