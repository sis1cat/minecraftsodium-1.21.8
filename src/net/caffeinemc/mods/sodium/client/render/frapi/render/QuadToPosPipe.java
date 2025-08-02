package net.caffeinemc.mods.sodium.client.render.frapi.render;

import java.util.function.Consumer;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class QuadToPosPipe implements Consumer<QuadView> {
   private final Consumer<Vector3fc> posConsumer;
   private final Vector3f vec;
   public Matrix4fc matrix;

   public QuadToPosPipe(Consumer<Vector3fc> posConsumer, Vector3f vec) {
      this.posConsumer = posConsumer;
      this.vec = vec;
   }

   public void accept(QuadView quad) {
      for (int i = 0; i < 4; i++) {
         quad.copyPos(i, this.vec);
         this.vec.x = MatrixHelper.transformPositionX(this.matrix, this.vec.x, this.vec.y, this.vec.z);
         this.vec.y = MatrixHelper.transformPositionY(this.matrix, this.vec.x, this.vec.y, this.vec.z);
         this.vec.z = MatrixHelper.transformPositionZ(this.matrix, this.vec.x, this.vec.y, this.vec.z);
         this.posConsumer.accept(this.vec);
      }
   }
}
