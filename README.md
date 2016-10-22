# deck-editor
This is a Magic: the Gathering deck editor that supports custom categories for cards in decks.

Magic: the Gathering is owned by Wizards of the Coast, to which I have no affiliation.

This program depends on the following:
 - Google JSON library, GSON, which can be found at https://code.google.com/p/google-gson/
 - JIDE Common Layer, which can be found at http://www.jidesoft.com/products/oss.htm
 - Java Operator Overloading, which can be found at https://github.com/amelentev/java-oo (not a Maven dependency; I installed it into Eclipse)

This depends on MTGJSON (http://www.mtgjson.com/) for its card list; currently, MTGJSON is not allowing the application to connect.  This can be worked around by turning off automatic updates in the settings dialog, manually downloading "AllSets-x.json.zip," and extracting it into the directory this editor is running in.
