package me.branchpanic.mods.rtfm.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.api.DocRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A ResourcePackDocRetriever retrieves DocEntries from active resource packs.
 */
public class ResourcePackDocRetriever implements DocRetriever {

    // We'll need to keep this relatively low because modded Minecraft eats enough memory as-is.
    public static final int MAX_CACHE_SIZE = 25;

    private final LoadingCache<CacheKey, Optional<Node>> entryCache;

    public ResourcePackDocRetriever(LoadingCache<CacheKey, Optional<Node>> entryCache) {
        this.entryCache = entryCache;
    }

    public static ResourcePackDocRetriever newInstance() {
        return new ResourcePackDocRetriever(
                CacheBuilder.newBuilder()
                        .maximumSize(MAX_CACHE_SIZE)
                        .expireAfterAccess(15, TimeUnit.MINUTES)  // Won't compile with a Duration... strange
                        .build(new CacheLoader<CacheKey, Optional<Node>>() {
                            @Override
                            public Optional<Node> load(CacheKey key) {
                                return retrieveByKey(key);
                            }
                        })
        );
    }

    private static Optional<Node> retrieveByKey(CacheKey key) {
        String type = key.type;
        String languageCode = key.languageCode;
        Identifier name = key.name;

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
            return Optional.ofNullable(
                    Parser.builder().build().parseReader(new InputStreamReader(entryFile.getInputStream()))
            );
        } catch (IOException e) {
            // If we reached this point, something went wrong. We already confirmed that the resource exists so there's
            // a weird I/O issue.
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<DocEntry> retrieve(String type, String languageCode, Identifier name) {
        try {
            return entryCache.get(new CacheKey(type, languageCode, name)).map(n -> (() -> n));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refresh() {
        entryCache.invalidateAll();
    }

    private static final class CacheKey {
        final String type;
        final String languageCode;
        final Identifier name;

        private CacheKey(String type, String languageCode, Identifier name) {
            this.type = type;
            this.languageCode = languageCode;
            this.name = name;
        }
    }
}
