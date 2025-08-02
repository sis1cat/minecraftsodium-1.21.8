package net.caffeinemc.mods.sodium.client.render.chunk;

import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderPredicate;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface ExtendedBlockEntityType<T extends BlockEntity> {
   BlockEntityRenderPredicate<T>[] sodium$getRenderPredicates();

   void sodium$addRenderPredicate(BlockEntityRenderPredicate<T> var1);

   boolean sodium$removeRenderPredicate(BlockEntityRenderPredicate<T> var1);

   static <T extends BlockEntity> boolean shouldRender(BlockEntityType<? extends T> type, BlockGetter blockGetter, BlockPos blockPos, T entity) {
      BlockEntityRenderPredicate<T>[] predicates = ((ExtendedBlockEntityType)type).sodium$getRenderPredicates();

      for (int i = 0; i < predicates.length; i++) {
         if (!predicates[i].shouldRender(blockGetter, blockPos, entity)) {
            return false;
         }
      }

      return true;
   }

   static <T extends BlockEntity> void addRenderPredicate(BlockEntityType<T> type, BlockEntityRenderPredicate<T> predicate) {
      ((ExtendedBlockEntityType)type).sodium$addRenderPredicate(predicate);
   }

   static <T extends BlockEntity> boolean removeRenderPredicate(BlockEntityType<T> type, BlockEntityRenderPredicate<T> predicate) {
      return ((ExtendedBlockEntityType)type).sodium$removeRenderPredicate(predicate);
   }
}
