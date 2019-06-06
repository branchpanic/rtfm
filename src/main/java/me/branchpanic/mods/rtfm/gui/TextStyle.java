package me.branchpanic.mods.rtfm.gui;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public interface TextStyle {
    float lineSpacingPx();

    float blockSpacingPx();

    Map<Integer, Float> headingScales();
}
