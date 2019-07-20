package me.branchpanic.mods.rtfm.impl;

import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.api.DocLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

public class ResourcePackDocLoader implements DocLoader {
    @Override
    public Optional<DocEntry> retrieve(String type, Identifier name) {
        try {
            Resource entryFile = MinecraftClient.getInstance().getResourceManager().getResource(new Identifier(name.getNamespace(), "docs" + "/" + type + "/" + name.getPath() + ".md"));

            return Optional.of(() -> {
                try {
                    return Parser.builder().build().parseReader(new InputStreamReader(entryFile.getInputStream()));
                } catch (IOException e) {
                    throw new IllegalStateException();
                }
            });
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
