package editor

import com.google.gson.GsonBuilder
import editor.collection.Categorization
import editor.collection.mutable.Deck
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency
import editor.filter.Filter
import editor.gui.deck.DeckSerializer
import editor.gui.settings.Settings

import java.awt.Color
import org.json4s.native.Serialization
import org.json4s.NoTypeHints
import org.json4s.Formats

/**
 * Global serialization constants and utilities.
 * @author Alec Roelke
 */
package object serialization {
  /** Serializer for saving and loading external information. */
  lazy val Serializer = (new GsonBuilder)
    .registerTypeAdapter(classOf[Settings], SettingsAdapter())
    .registerTypeAdapter(classOf[Categorization], CategoryAdapter())
    .registerTypeHierarchyAdapter(classOf[Filter], FilterAdapter())
    .registerTypeAdapter(classOf[Color], ColorAdapter())
    .registerTypeHierarchyAdapter(classOf[Card], CardAdapter())
    .registerTypeAdapter(classOf[CardAttribute[?, ?]], AttributeAdapter())
    .registerTypeAdapter(classOf[Deck], DeckAdapter())
    .registerTypeAdapter(classOf[DeckSerializer], DeckSerializer())
    .registerTypeAdapter(classOf[DatabaseVersion], VersionAdapter())
    .registerTypeAdapter(classOf[UpdateFrequency], UpdateAdapter())
    .setPrettyPrinting
    .create()

  given formats: Formats = Serialization.formats(NoTypeHints) +
    new SettingsAdapter +
    new CategoryAdapter +
    new FilterAdapter +
    new ColorAdapter +
    new CardAdapter +
    new CardListEntrySerializer +
    new AttributeAdapter +
    new DeckAdapter +
    new VersionAdapter +
    new UpdateAdapter +
    new DeckSerializer
}