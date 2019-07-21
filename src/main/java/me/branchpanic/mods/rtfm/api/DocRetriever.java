package me.branchpanic.mods.rtfm.api;

import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * A DocRetriever loads mod documentation from an arbitrary source.
 */
@FunctionalInterface
public interface DocRetriever {

    /**
     * Retrieves a documentation entry by name.
     *
     * @param type Type of entry to load, such as "item" or "block".
     * @param name Name of the entry to load.
     * @param languageCode Target language to load for, if possible.
     * @return An optional containing either the desired entry or nothing if it could not be loaded.
     */
    Optional<DocEntry> retrieve(String type, String languageCode, Identifier name);

    /**
     * Signals this DocRetriever that its contents may have changed. For example, if a resource pack is reloaded,
     * then there's a chance that documentation entries will be different.
     * <p>
     * If no caching is done, then there's no need to implement this.
     */
    default void refresh() {
        // NO-OP
    }
}
