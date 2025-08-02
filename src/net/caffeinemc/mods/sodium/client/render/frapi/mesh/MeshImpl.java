package net.caffeinemc.mods.sodium.client.render.frapi.mesh;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;

public class MeshImpl extends MeshViewImpl implements Mesh {
   private static final ThreadLocal<ObjectArrayList<QuadViewImpl>> CURSOR_POOLS = ThreadLocal.withInitial(ObjectArrayList::new);

   MeshImpl(int[] data) {
      this.data = data;
      this.limit = data.length;
   }

   MeshImpl() {
   }
}
