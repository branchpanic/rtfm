package me.branchpanic.mods.rtfm.api;

import org.commonmark.node.Node;

/**
 * A DocEntry is a displayable documentation element.
 */
@FunctionalInterface
public interface DocEntry {

    /**
     * Gets the root node of the Markdown document associated with this DocEntry. This might just return a
     * pre-calculated value, or might actually load and parse Markdown data.
     */
    Node node();
}
