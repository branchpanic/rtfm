package me.branchpanic.mods.rtfm.api;

import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * A DocLoader loads mod documentation from an arbitrary source.
 */
public interface DocLoader {

    /**
     * Retrieves a documentation entry by name.
     *
     * @param name Name of the entry to load.
     * @return An optional containing either the desired entry or nothing if it could not be loaded.
     */
    Optional<DocEntry> retrieve(Identifier name);
}
