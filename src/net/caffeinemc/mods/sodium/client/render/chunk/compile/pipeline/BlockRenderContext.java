package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos.MutableBlockPos;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BlockRenderContext {
   private final LevelSlice slice;
   public final TranslucentGeometryCollector collector;
   private final MutableBlockPos pos = new MutableBlockPos();
   private final Vector3f origin = new Vector3f();
   private BlockState state;
   private DirectStateAccess model;
   private long seed;

   public BlockRenderContext(LevelSlice slice, TranslucentGeometryCollector collector) {
      this.slice = slice;
      this.collector = collector;
   }

   public void update(BlockPos pos, BlockPos origin, BlockState state, DirectStateAccess model, long seed) {
      this.pos.set(pos);
      this.origin.set(origin.getX(), origin.getY(), origin.getZ());
      this.state = state;
      this.model = model;
      this.seed = seed;
   }

   public TranslucentGeometryCollector collector() {
      return this.collector;
   }

   public BlockPos pos() {
      return this.pos;
   }

   public LevelSlice slice() {
      return this.slice;
   }

   public BlockState state() {
      return this.state;
   }

   public DirectStateAccess model() {
      return this.model;
   }

   public Vector3fc origin() {
      return this.origin;
   }

   public long seed() {
      return this.seed;
   }
}
