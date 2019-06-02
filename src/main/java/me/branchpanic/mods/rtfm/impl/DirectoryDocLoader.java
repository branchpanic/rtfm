package me.branchpanic.mods.rtfm.impl;

import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.api.DocLoader;
import net.minecraft.util.Identifier;
import org.immutables.value.Value;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A DirectoryDocLoader loads DocEntries from a directory, independent of the game. It is intended for debugging
 * purposes and probably shouldn't be used in production.
 */
@Value.Immutable
public interface DirectoryDocLoader extends DocLoader {

    /**
     * Returns the root path that this DirectoryDocLoader is searching in.
     */
    Path root();

    @Override
    default Optional<DocEntry> retrieve(Identifier name) {
        return Optional.empty();
    }
}
