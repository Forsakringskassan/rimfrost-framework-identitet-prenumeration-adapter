package se.fk.rimfrost.framework.identitetprenumeration.model;

import org.immutables.value.Value;

/**
 * Identifierar en individ via en specifik ID-typ och ett tillhörande värde.
 */
@Value.Immutable
public interface Idtyp
{
   /** Unikt ID som identifierar vilken typ av värde som anges. */
   String typId();

   /** ID-värde som identifieras av typ. */
   String varde();
}
