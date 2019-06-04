package me.branchpanic.mods.rtfm.impl;

import me.branchpanic.mods.rtfm.api.DocEntry;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileDocEntry implements DocEntry {
    private final Parser parser;
    private final Path path;
    private Node node;

    public FileDocEntry(Parser parser, Path path) {
        this.parser = parser;
        this.path = path;
    }

    @Override
    public Node node() {
        if (node == null) {
            try {
                node = parser.parseReader(Files.newBufferedReader(path));
            } catch (IOException e) {
                throw new RuntimeException("failed to lazily load entry associated with path " + path.toString());
            }
        }

        return node;
    }
}
