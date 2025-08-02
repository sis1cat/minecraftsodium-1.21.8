package net.caffeinemc.mods.sodium.client.render.frapi.render;

import net.fabricmc.fabric.api.util.TriState;

public enum AmbientOcclusionMode {
   ENABLED,
   DEFAULT,
   DISABLED;

   private static final TriState[] TRISTATES = new TriState[]{TriState.TRUE, TriState.DEFAULT, TriState.FALSE};

   public TriState toTriState() {
      return TRISTATES[this.ordinal()];
   }
}
