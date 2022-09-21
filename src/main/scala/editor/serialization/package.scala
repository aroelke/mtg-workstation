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
    FilterAdapter +
    ColorAdapter +
    CardAdapter +
    CardListEntrySerializer +
    AttributeAdapter +
    DeckAdapter +
    VersionAdapter +
    UpdateAdapter +
    CardLayoutSerializer +
    DeckSerializer
}