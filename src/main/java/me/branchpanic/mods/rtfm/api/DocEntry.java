package me.branchpanic.mods.rtfm.api;

import org.commonmark.node.Node;

/**
 * A DocEntry is a displayable documentation element.
 */
public interface DocEntry {

    /**
     * Gets the root node of the Markdown document associated with this DocEntry.
     */
    Node node();
}
