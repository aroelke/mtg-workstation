package editor

import editor.collection.Categorization
import editor.collection.mutable.Deck
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency
import editor.filter.Filter
import editor.gui.deck.DeckSerializer
import editor.gui.settings.Settings
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.native.Serialization

import java.awt.Color

/**
 * Global serialization constants and utilities.
 * @author Alec Roelke
 */
package object serialization {
  /** Serializer for saving and loading external information. */
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
    new CardLayoutSerializer +
    new DeckSerializer
}