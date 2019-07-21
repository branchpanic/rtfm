package me.branchpanic.mods.rtfm.impl;

import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.api.DocRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * A ResourcePackDocRetriever retrieves DocEntries from active resource packs.
 */
public class ResourcePackDocRetriever implements DocRetriever {

    @Override
    public Optional<DocEntry> retrieve(String type, Identifier name) {
        try {
            Identifier docIdentifier = new Identifier(
                    name.getNamespace(), "docs" + "/" + type + "/" + name.getPath() + ".md"
            );

            Resource entryFile = MinecraftClient.getInstance()
                    .getResourceManager()
                    .getResource(docIdentifier);

            return Optional.of(() -> {
                try {
                    return Parser.builder().build().parseReader(new InputStreamReader(entryFile.getInputStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
