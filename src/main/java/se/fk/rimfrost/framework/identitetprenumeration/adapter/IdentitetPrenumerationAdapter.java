package se.fk.rimfrost.framework.identitetprenumeration.adapter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import se.fk.rimfrost.framework.identitetprenumeration.exception.IdentitetPrenumerationException;
import se.fk.rimfrost.framework.identitetprenumeration.model.CreatePrenumerationRequest;
import se.fk.rimfrost.service.identitet.prenumeration.jaxrsspec.controllers.generatedsource.PrenumerationControllerApi;
import java.util.UUID;

/**
 * Adapter för kommunikation med identitet-prenumeration-tjänsten.
 */
@ApplicationScoped
public class IdentitetPrenumerationAdapter
{

   @ConfigProperty(name = "identitetprenumeration.api.base-url")
   String baseUrl;

   private PrenumerationControllerApi prenumerationClient;

   private Client client;

   @Inject
   IdentitetPrenumerationMapper mapper;

   /** Initierar Jersey-proxyklienten mot tjänstens bas-URL. */
   @PostConstruct
   void init()
   {
      ClientConfig clientConfig = new ClientConfig();
      clientConfig.connectorProvider(new Apache5ConnectorProvider());
      this.client = JerseyClientBuilder.createClient(clientConfig);
      this.prenumerationClient = WebResourceFactory.newResource(
            PrenumerationControllerApi.class,
            client.target(this.baseUrl));
   }

   /** Stänger Jersey-klienten och frigör resurser. */
   @PreDestroy
   void destroy()
   {
      this.prenumerationClient = null;
      if (this.client != null)
      {
         this.client.close();
         this.client = null;
      }
   }

   /**
    * Registrerar en prenumeration på identitetsförändringar.
    *
    * @param request prenumerationsförfrågan med identiteter och replyTo-topic
    * @throws IdentitetPrenumerationException vid kommunikationsfel eller ogiltiga indata
    */
   public void createPrenumeration(CreatePrenumerationRequest request) throws IdentitetPrenumerationException
   {
      var apiRequest = mapper.toApiCreatePrenumerationRequest(request);
      try
      {
         prenumerationClient.createPrenumeration(apiRequest);
      }
      catch (ProcessingException e)
      {
         throw new IdentitetPrenumerationException(
               IdentitetPrenumerationException.ErrorType.SERVICE_UNAVAILABLE,
               "Kunde inte nå identitet-prenumeration-tjänsten vid skapande av prenumeration", e);
      }
      catch (WebApplicationException e)
      {
         var status = e.getResponse().getStatus();
         switch (status)
         {
            case 400:
               throw new IdentitetPrenumerationException(
                     IdentitetPrenumerationException.ErrorType.BAD_REQUEST,
                     "Felaktig förfrågan vid skapande av prenumeration för prenumerationId: "
                           + request.prenumerationId(),
                     e);
            default:
               throw new IdentitetPrenumerationException(
                     IdentitetPrenumerationException.ErrorType.UNEXPECTED_ERROR,
                     "Oväntat fel vid skapande av prenumeration för prenumerationId: "
                           + request.prenumerationId() + ", status: " + status,
                     e);
         }
      }
   }

   /**
    * Avregistrerar en befintlig prenumeration.
    *
    * @param prenumerationId ID för prenumerationen som ska avregistreras
    * @throws IdentitetPrenumerationException vid kommunikationsfel eller om prenumerationen inte hittas
    */
   public void deletePrenumeration(UUID prenumerationId) throws IdentitetPrenumerationException
   {
      try
      {
         prenumerationClient.deletePrenumeration(prenumerationId);
      }
      catch (ProcessingException e)
      {
         throw new IdentitetPrenumerationException(
               IdentitetPrenumerationException.ErrorType.SERVICE_UNAVAILABLE,
               "Kunde inte nå identitet-prenumeration-tjänsten vid borttagning av prenumeration", e);
      }
      catch (WebApplicationException e)
      {
         var status = e.getResponse().getStatus();
         switch (status)
         {
            case 404:
               throw new IdentitetPrenumerationException(
                     IdentitetPrenumerationException.ErrorType.NOT_FOUND,
                     "Hittade ingen prenumeration med id: " + prenumerationId, e);
            case 400:
               throw new IdentitetPrenumerationException(
                     IdentitetPrenumerationException.ErrorType.BAD_REQUEST,
                     "Felaktig förfrågan vid borttagning av prenumeration med id: " + prenumerationId, e);
            default:
               throw new IdentitetPrenumerationException(
                     IdentitetPrenumerationException.ErrorType.UNEXPECTED_ERROR,
                     "Oväntat fel vid borttagning av prenumeration med id: " + prenumerationId
                           + ", status: " + status,
                     e);
         }
      }
   }
}
