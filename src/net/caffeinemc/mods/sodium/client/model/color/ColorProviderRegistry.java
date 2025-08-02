package net.caffeinemc.mods.sodium.client.model.color;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap.Entry;
import net.caffeinemc.mods.sodium.client.model.color.interop.BlockColorsExtension;
import net.caffeinemc.mods.sodium.client.services.FluidRendererFactory;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ColorProviderRegistry {
   private final Reference2ReferenceMap<Block, ColorProvider<BlockState>> blocks = new Reference2ReferenceOpenHashMap();
   private final Reference2ReferenceMap<Fluid, ColorProvider<FluidState>> fluids = new Reference2ReferenceOpenHashMap();
   private final ReferenceSet<Block> overridenBlocks;

   public ColorProviderRegistry(BlockColors blockColors) {
      Reference2ReferenceMap<Block, BlockColor> providers = BlockColorsExtension.getProviders(blockColors);

       for (Entry<Block, BlockColor> blockServerPlayerEntry : providers.reference2ReferenceEntrySet()) {
           Entry<Block, BlockColor> entry =  blockServerPlayerEntry;
           this.blocks.put((Block) entry.getKey(), DefaultColorProviders.adapt((BlockColor) entry.getValue()));
       }

      this.overridenBlocks = BlockColorsExtension.getOverridenVanillaBlocks(blockColors);
      this.installOverrides();
   }

   private void installOverrides() {
      this.registerBlocks(
         DefaultColorProviders.GrassColorProvider.BLOCKS,
         Blocks.GRASS_BLOCK,
         Blocks.FERN,
         Blocks.SHORT_GRASS,
         Blocks.POTTED_FERN,
         Blocks.PINK_PETALS,
         Blocks.SUGAR_CANE,
         Blocks.LARGE_FERN,
         Blocks.TALL_GRASS
      );
      this.registerBlocks(
         DefaultColorProviders.FoliageColorProvider.BLOCKS,
         Blocks.OAK_LEAVES,
         Blocks.JUNGLE_LEAVES,
         Blocks.ACACIA_LEAVES,
         Blocks.DARK_OAK_LEAVES,
         Blocks.VINE,
         Blocks.MANGROVE_LEAVES
      );
      this.registerBlocks(FluidRendererFactory.getInstance().getWaterBlockColorProvider(), Blocks.WATER, Blocks.BUBBLE_COLUMN);
      this.registerFluids(FluidRendererFactory.getInstance().getWaterColorProvider(), Fluids.WATER, Fluids.FLOWING_WATER);
   }

   private void registerBlocks(ColorProvider<BlockState> provider, Block... blocks) {
      for (Block block : blocks) {
         if (!this.overridenBlocks.contains(block)) {
            this.blocks.put(block, provider);
         }
      }
   }

   private void registerFluids(ColorProvider<FluidState> provider, Fluid... fluids) {
      for (Fluid fluid : fluids) {
         this.fluids.put(fluid, provider);
      }
   }

   public ColorProvider<BlockState> getColorProvider(Block block) {
      return (ColorProvider<BlockState>)this.blocks.get(block);
   }

   public ColorProvider<FluidState> getColorProvider(Fluid fluid) {
      return (ColorProvider<FluidState>)this.fluids.get(fluid);
   }
}
