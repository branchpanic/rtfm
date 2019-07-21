package me.branchpanic.mods.rtfm.impl;

import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.api.DocRetriever;
import net.minecraft.util.Identifier;
import org.commonmark.parser.Parser;
import org.immutables.value.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * A DirectoryDocLoader loads DocEntries from a directory, independent of the game. It is intended for debugging
 * purposes and probably shouldn't be used in production.
 */
@Value.Immutable
public interface DirectoryDocRetriever extends DocRetriever {

    /**
     * Returns the root path that this DirectoryDocLoader is searching in.
     */
    Path root();

    @Override
    default Optional<DocEntry> retrieve(String type, Identifier name) {
        Path expectedPath = Paths.get(root().toString(), type, name.getNamespace(), name.getPath() + ".md");

        if (Files.exists(expectedPath)) {
            return Optional.of(new FileDocEntry(Parser.builder().build(), expectedPath));
        }

        return Optional.empty();
    }
}
