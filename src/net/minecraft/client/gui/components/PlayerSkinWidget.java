package net.minecraft.client.gui.components;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PlayerSkinWidget extends AbstractWidget {
	private static final float MODEL_HEIGHT = 2.125F;
	private static final float FIT_SCALE = 0.97F;
	private static final float ROTATION_SENSITIVITY = 2.5F;
	private static final float DEFAULT_ROTATION_X = -5.0F;
	private static final float DEFAULT_ROTATION_Y = 30.0F;
	private static final float ROTATION_X_LIMIT = 50.0F;
	private final PlayerModel wideModel;
	private final PlayerModel slimModel;
	private final Supplier<PlayerSkin> skin;
	private float rotationX = -5.0F;
	private float rotationY = 30.0F;

	public PlayerSkinWidget(int i, int j, EntityModelSet entityModelSet, Supplier<PlayerSkin> supplier) {
		super(0, 0, i, j, CommonComponents.EMPTY);
		this.wideModel = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER), false);
		this.slimModel = new PlayerModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_SLIM), true);
		this.skin = supplier;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		float g = 0.97F * this.getHeight() / 2.125F;
		float h = -1.0625F;
		PlayerSkin playerSkin = (PlayerSkin)this.skin.get();
		PlayerModel playerModel = playerSkin.model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel;
		guiGraphics.submitSkinRenderState(
			playerModel, playerSkin.texture(), g, this.rotationX, this.rotationY, -1.0625F, this.getX(), this.getY(), this.getRight(), this.getBottom()
		);
	}

	@Override
	protected void onDrag(double d, double e, double f, double g) {
		this.rotationX = Mth.clamp(this.rotationX - (float)g * 2.5F, -50.0F, 50.0F);
		this.rotationY += (float)f * 2.5F;
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		return null;
	}
}
