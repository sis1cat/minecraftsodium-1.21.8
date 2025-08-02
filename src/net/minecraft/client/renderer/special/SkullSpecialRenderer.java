package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SkullSpecialRenderer implements NoDataSpecialModelRenderer {
	private final SkullModelBase model;
	private final float animation;
	private final RenderType renderType;

	public SkullSpecialRenderer(SkullModelBase skullModelBase, float f, RenderType renderType) {
		this.model = skullModelBase;
		this.animation = f;
		this.renderType = renderType;
	}

	@Override
	public void render(ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, boolean bl) {
		SkullBlockRenderer.renderSkull(null, 180.0F, this.animation, poseStack, multiBufferSource, i, this.model, this.renderType);
	}

	@Override
	public void getExtents(Set<Vector3f> set) {
		PoseStack poseStack = new PoseStack();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.model.setupAnim(this.animation, 180.0F, 0.0F);
		this.model.root().getExtentsForGui(poseStack, set);
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(SkullBlock.Type kind, Optional<ResourceLocation> textureOverride, float animation) implements SpecialModelRenderer.Unbaked {
		public static final MapCodec<SkullSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					SkullBlock.Type.CODEC.fieldOf("kind").forGetter(SkullSpecialRenderer.Unbaked::kind),
					ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(SkullSpecialRenderer.Unbaked::textureOverride),
					Codec.FLOAT.optionalFieldOf("animation", 0.0F).forGetter(SkullSpecialRenderer.Unbaked::animation)
				)
				.apply(instance, SkullSpecialRenderer.Unbaked::new)
		);

		public Unbaked(SkullBlock.Type type) {
			this(type, Optional.empty(), 0.0F);
		}

		@Override
		public MapCodec<SkullSpecialRenderer.Unbaked> type() {
			return MAP_CODEC;
		}

		@Nullable
		@Override
		public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
			SkullModelBase skullModelBase = SkullBlockRenderer.createModel(entityModelSet, this.kind);
			ResourceLocation resourceLocation = (ResourceLocation)this.textureOverride
				.map(resourceLocationx -> resourceLocationx.withPath((UnaryOperator<String>)(string -> "textures/entity/" + string + ".png")))
				.orElse(null);
			if (skullModelBase == null) {
				return null;
			} else {
				RenderType renderType = SkullBlockRenderer.getSkullRenderType(this.kind, resourceLocation);
				return new SkullSpecialRenderer(skullModelBase, this.animation, renderType);
			}
		}
	}
}
