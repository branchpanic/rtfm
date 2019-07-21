package me.branchpanic.mods.rtfm.impl;

import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.api.DocRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
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
    public Optional<DocEntry> retrieve(String type, String languageCode, Identifier name) {
        Identifier localizedIdentifier = new Identifier(
                name.getNamespace(), "docs" + "/" + type + "/" + name.getPath() + "." + languageCode + ".md"
        );

        Identifier unlocalizedIdentifier = new Identifier(
                name.getNamespace(), "docs" + "/" + type + "/" + name.getPath() + ".md"
        );

        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        Identifier targetIdentifier;

        if (resourceManager.containsResource(localizedIdentifier)) {
            targetIdentifier = localizedIdentifier;
        } else if (resourceManager.containsResource(unlocalizedIdentifier)) {
            targetIdentifier = unlocalizedIdentifier;
        } else {
            return Optional.empty();
        }

        try (Resource entryFile = MinecraftClient.getInstance()
                .getResourceManager()
                .getResource(targetIdentifier)) {
            return Optional.of(() -> {
                try {
                    return Parser.builder().build().parseReader(new InputStreamReader(entryFile.getInputStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            // If we reached this point, something went wrong. We already confirmed that the resource exists so there's
            // a weird I/O issue.
            throw new RuntimeException(e);
        }
    }
}
