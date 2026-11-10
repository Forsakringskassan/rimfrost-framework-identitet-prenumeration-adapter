package se.fk.rimfrost.framework.identitetprenumeration.exception;

/**
 * Fel som uppstår vid kommunikation med identitet-prenumeration-tjänsten.
 */
public class IdentitetPrenumerationException extends Exception
{

   /** Kategoriserar felets natur för att underlätta felhantering hos anroparen. */
   public enum ErrorType
   {
      BAD_REQUEST, NOT_FOUND, SERVICE_UNAVAILABLE, UNEXPECTED_ERROR
   }

   private final ErrorType errorType;

   /**
    * Skapar ett undantag med angiven feltyp och meddelande.
    *
    * @param errorType felkategori
    * @param message beskrivande felmeddelande
    */
   public IdentitetPrenumerationException(ErrorType errorType, String message)
   {
      super(message);
      this.errorType = errorType;
   }

   /**
    * Skapar ett undantag med angiven feltyp, meddelande och bakomliggande orsak.
    *
    * @param errorType felkategori
    * @param message beskrivande felmeddelande
    * @param cause bakomliggande undantag
    */
   public IdentitetPrenumerationException(ErrorType errorType, String message, Throwable cause)
   {
      super(message, cause);
      this.errorType = errorType;
   }

   /**
    * Returnerar felkategorin för detta undantag.
    *
    * @return feltyp
    */
   public ErrorType getErrorType()
   {
      return errorType;
   }
}
