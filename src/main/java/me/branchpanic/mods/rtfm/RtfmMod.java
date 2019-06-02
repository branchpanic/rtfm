package me.branchpanic.mods.rtfm;

import me.branchpanic.mods.rtfm.api.DocLoader;
import me.branchpanic.mods.rtfm.impl.ImmutableDirectoryDocLoader;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

/**
 * The RtfmMod singleton is the active instance of RTFM.
 */
public enum RtfmMod implements ClientModInitializer {
    INSTANCE;

    private Logger logger = LogManager.getLogger("rtfm");
    private DocLoader docLoader;

    public DocLoader getDocLoader() {
        return docLoader;
    }

    @Override
    public void onInitializeClient() {
        docLoader = ImmutableDirectoryDocLoader.builder()
                .root(Paths.get("."))
                .build();

        logger.info("rtfm: using documentation loader: {}", docLoader.toString());
        logger.info("rtfm: ready on client");
    }
}
