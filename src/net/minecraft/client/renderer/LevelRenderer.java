package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.vertex.VertexConsumerUtils;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.caffeinemc.mods.sodium.client.util.FlawlessFrames;
import net.caffeinemc.mods.sodium.client.util.FogStorage;
import net.caffeinemc.mods.sodium.client.util.SodiumChunkSection;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionBuffers;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceLocation TRANSPARENCY_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("transparency");
	private static final ResourceLocation ENTITY_OUTLINE_POST_CHAIN_ID = ResourceLocation.withDefaultNamespace("entity_outline");
	public static final int SECTION_SIZE = 16;
	public static final int HALF_SECTION_SIZE = 8;
	public static final int NEARBY_SECTION_DISTANCE_IN_BLOCKS = 32;
	private static final int MINIMUM_TRANSPARENT_SORT_COUNT = 15;
	private static final Comparator<Entity> ENTITY_COMPARATOR = Comparator.comparing(entity -> entity.getType().hashCode());
	private final Minecraft minecraft;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	private final RenderBuffers renderBuffers;
	private final SkyRenderer skyRenderer = new SkyRenderer();
	private final CloudRenderer cloudRenderer = new CloudRenderer();
	private final WorldBorderRenderer worldBorderRenderer = new WorldBorderRenderer();
	private final WeatherEffectRenderer weatherEffectRenderer = new WeatherEffectRenderer();
	@Nullable
	private ClientLevel level;
	private final SectionOcclusionGraph sectionOcclusionGraph = new SectionOcclusionGraph();
	private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);
	private final ObjectArrayList<SectionRenderDispatcher.RenderSection> nearbyVisibleSections = new ObjectArrayList<>(50);
	@Nullable
	private ViewArea viewArea;
	private int ticks;
	private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
	private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
	@Nullable
	private RenderTarget entityOutlineTarget;
	private final LevelTargetBundle targets = new LevelTargetBundle();
	private int lastCameraSectionX = Integer.MIN_VALUE;
	private int lastCameraSectionY = Integer.MIN_VALUE;
	private int lastCameraSectionZ = Integer.MIN_VALUE;
	private double prevCamX = Double.MIN_VALUE;
	private double prevCamY = Double.MIN_VALUE;
	private double prevCamZ = Double.MIN_VALUE;
	private double prevCamRotX = Double.MIN_VALUE;
	private double prevCamRotY = Double.MIN_VALUE;
	@Nullable
	private SectionRenderDispatcher sectionRenderDispatcher;
	private int lastViewDistance = -1;
	private final List<Entity> visibleEntities = new ArrayList();
	private int visibleEntityCount;
	private Frustum cullingFrustum;
	private boolean captureFrustum;
	@Nullable
	private Frustum capturedFrustum;
	@Nullable
	private BlockPos lastTranslucentSortBlockPos;
	private int translucencyResortIterationIndex;
	private static final EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> STATIC_MAP = new EnumMap<>(ChunkSectionLayer.class);
	private SodiumWorldRenderer renderer;
	private ChunkRenderMatrices matrices;

	public LevelRenderer(
		Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher, RenderBuffers renderBuffers
	) {
		this.minecraft = minecraft;
		this.entityRenderDispatcher = entityRenderDispatcher;
		this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
		this.renderBuffers = renderBuffers;
		this.renderer = new SodiumWorldRenderer(minecraft);
	}

	public void tickParticles(Camera camera) {
		this.weatherEffectRenderer.tickRainParticles(this.minecraft.level, camera, this.ticks, this.minecraft.options.particles().get());
	}

	public void close() {
		if (this.entityOutlineTarget != null) {
			this.entityOutlineTarget.destroyBuffers();
		}

		this.skyRenderer.close();
		this.cloudRenderer.close();
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.initOutline();
	}

	public void initOutline() {
		if (this.entityOutlineTarget != null) {
			this.entityOutlineTarget.destroyBuffers();
		}

		this.entityOutlineTarget = new TextureTarget("Entity Outline", this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), true);
	}

	@Nullable
	private PostChain getTransparencyChain() {
		if (!Minecraft.useShaderTransparency()) {
			return null;
		} else {
			PostChain postChain = this.minecraft.getShaderManager().getPostChain(TRANSPARENCY_POST_CHAIN_ID, LevelTargetBundle.SORTING_TARGETS);
			if (postChain == null) {
				this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
				this.minecraft.options.save();
			}

			return postChain;
		}
	}

	public void doEntityOutline() {
		if (this.shouldShowEntityOutlines()) {
			this.entityOutlineTarget.blitAndBlendToTexture(this.minecraft.getMainRenderTarget().getColorTextureView());
		}
	}

	protected boolean shouldShowEntityOutlines() {
		return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityOutlineTarget != null && this.minecraft.player != null;
	}

	public void setLevel(@Nullable ClientLevel clientLevel) {
		this.lastCameraSectionX = Integer.MIN_VALUE;
		this.lastCameraSectionY = Integer.MIN_VALUE;
		this.lastCameraSectionZ = Integer.MIN_VALUE;
		this.entityRenderDispatcher.setLevel(clientLevel);
		this.level = clientLevel;
		if (clientLevel != null) {
			this.allChanged();
		} else {
			if (this.viewArea != null) {
				this.viewArea.releaseAllBuffers();
				this.viewArea = null;
			}

			if (this.sectionRenderDispatcher != null) {
				this.sectionRenderDispatcher.dispose();
			}

			this.sectionRenderDispatcher = null;
			this.sectionOcclusionGraph.waitAndReset(null);
			this.clearVisibleSections();
		}
		RenderDevice.enterManagedCode();

		try {
			this.renderer.setLevel(level);
		} finally {
			RenderDevice.exitManagedCode();
		}
	}

	private void clearVisibleSections() {
		this.visibleSections.clear();
		this.nearbyVisibleSections.clear();
	}

	private int nullifyBuiltChunkStorage(Options options) {
		return 0;
	}

	public void allChanged() {
		if (this.level != null) {
			this.level.clearTintCaches();
			if (this.sectionRenderDispatcher == null) {
				this.sectionRenderDispatcher = new SectionRenderDispatcher(
					this.level, this, Util.backgroundExecutor(), this.renderBuffers, this.minecraft.getBlockRenderer(), this.minecraft.getBlockEntityRenderDispatcher()
				);
			} else {
				this.sectionRenderDispatcher.setLevel(this.level);
			}

			this.cloudRenderer.markForRebuild();
			ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
			this.lastViewDistance = this.nullifyBuiltChunkStorage(minecraft.options);
			if (this.viewArea != null) {
				this.viewArea.releaseAllBuffers();
			}

			this.sectionRenderDispatcher.clearCompileQueue();
			this.viewArea = new ViewArea(this.sectionRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
			this.sectionOcclusionGraph.waitAndReset(this.viewArea);
			this.clearVisibleSections();
			Camera camera = this.minecraft.gameRenderer.getMainCamera();
			this.viewArea.repositionCamera(SectionPos.of(camera.getPosition()));
		}

		RenderDevice.enterManagedCode();

		try {
			this.renderer.reload();
		} finally {
			RenderDevice.exitManagedCode();
		}

	}

	public void resize(int i, int j) {
		this.needsUpdate();
		if (this.entityOutlineTarget != null) {
			this.entityOutlineTarget.resize(i, j);
		}
	}

	public String getSectionStatistics() {
		return this.renderer.getChunksDebugString();
		/*int i = this.viewArea.sections.length;
		int j = this.countRenderedSections();
		return String.format(
			Locale.ROOT,
			"C: %d/%d %sD: %d, %s",
			j,
			i,
			this.minecraft.smartCull ? "(s) " : "",
			this.lastViewDistance,
			this.sectionRenderDispatcher == null ? "null" : this.sectionRenderDispatcher.getStats()
		);*/
	}

	public SectionRenderDispatcher getSectionRenderDispatcher() {
		return this.sectionRenderDispatcher;
	}

	public double getTotalSections() {
		return this.viewArea.sections.length;
	}

	public double getLastViewDistance() {
		return this.lastViewDistance;
	}

	public int countRenderedSections() {
		return this.renderer.getVisibleChunkCount();
		/*int i = 0;

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			if (renderSection.getSectionMesh().hasRenderableLayers()) {
				i++;
			}
		}

		return i;*/
	}

	public String getEntityStatistics() {
		return "E: " + this.visibleEntityCount + "/" + this.level.getEntityCount() + ", SD: " + this.level.getServerSimulationDistance();
	}

	private void setupRender(Camera camera, Frustum frustum, boolean bl, boolean bl2) {
		Viewport viewport = ((ViewportProvider)frustum).sodium$createViewport();
		boolean updateChunksImmediately = FlawlessFrames.isActive();
		int sectionX = SectionPos.posToSectionCoord(camera.getPosition().x());
		int sectionY = SectionPos.posToSectionCoord(camera.getPosition().y());
		int sectionZ = SectionPos.posToSectionCoord(camera.getPosition().z());
		if (this.lastCameraSectionX != sectionX || this.lastCameraSectionY != sectionY || this.lastCameraSectionZ != sectionZ) {
			this.lastCameraSectionX = sectionX;
			this.lastCameraSectionY = sectionY;
			this.lastCameraSectionZ = sectionZ;
			this.worldBorderRenderer.invalidate();
		}

		RenderDevice.enterManagedCode();

		try {
			this.renderer
					.setupTerrain(
							camera, viewport, this.minecraft.gameRenderer.sodium$getFogParameters(), bl2, updateChunksImmediately, this.matrices
					);
		} finally {
			RenderDevice.exitManagedCode();
		}
		/*Vec3 vec3 = camera.getPosition();
		if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
			this.allChanged();
		}

		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("camera");
		int i = SectionPos.posToSectionCoord(vec3.x());
		int j = SectionPos.posToSectionCoord(vec3.y());
		int k = SectionPos.posToSectionCoord(vec3.z());
		if (this.lastCameraSectionX != i || this.lastCameraSectionY != j || this.lastCameraSectionZ != k) {
			this.lastCameraSectionX = i;
			this.lastCameraSectionY = j;
			this.lastCameraSectionZ = k;
			this.viewArea.repositionCamera(SectionPos.of(vec3));
			this.worldBorderRenderer.invalidate();
		}

		this.sectionRenderDispatcher.setCameraPosition(vec3);
		profilerFiller.popPush("cull");
		double d = Math.floor(vec3.x / 8.0);
		double e = Math.floor(vec3.y / 8.0);
		double f = Math.floor(vec3.z / 8.0);
		if (d != this.prevCamX || e != this.prevCamY || f != this.prevCamZ) {
			this.sectionOcclusionGraph.invalidate();
		}

		this.prevCamX = d;
		this.prevCamY = e;
		this.prevCamZ = f;
		profilerFiller.popPush("update");
		if (!bl) {
			boolean bl3 = this.minecraft.smartCull;
			if (bl2 && this.level.getBlockState(camera.getBlockPosition()).isSolidRender()) {
				bl3 = false;
			}

			profilerFiller.push("section_occlusion_graph");
			this.sectionOcclusionGraph.update(bl3, camera, frustum, this.visibleSections, this.level.getChunkSource().getLoadedEmptySections());
			profilerFiller.pop();
			double g = Math.floor(camera.getXRot() / 2.0F);
			double h = Math.floor(camera.getYRot() / 2.0F);
			if (this.sectionOcclusionGraph.consumeFrustumUpdate() || g != this.prevCamRotX || h != this.prevCamRotY) {
				this.applyFrustum(offsetFrustum(frustum));
				this.prevCamRotX = g;
				this.prevCamRotY = h;
			}
		}

		profilerFiller.pop();*/
	}

	public static Frustum offsetFrustum(Frustum frustum) {
		return new Frustum(frustum).offsetToFullyIncludeCameraCube(8);
	}

	private void applyFrustum(Frustum frustum) {
		if (!Minecraft.getInstance().isSameThread()) {
			throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
		} else {
			Profiler.get().push("apply_frustum");
			this.clearVisibleSections();
			this.sectionOcclusionGraph.addSectionsInFrustum(frustum, this.visibleSections, this.nearbyVisibleSections);
			Profiler.get().pop();
		}
	}

	public void addRecentlyCompiledSection(SectionRenderDispatcher.RenderSection renderSection) {
		this.sectionOcclusionGraph.schedulePropagationFrom(renderSection);
	}

	public void prepareCullFrustum(Vec3 vec3, Matrix4f matrix4f, Matrix4f matrix4f2) {
		this.cullingFrustum = new Frustum(matrix4f, matrix4f2);
		this.cullingFrustum.prepare(vec3.x(), vec3.y(), vec3.z());
	}

	public void renderLevel(
		GraphicsResourceAllocator graphicsResourceAllocator,
		DeltaTracker deltaTracker,
		boolean bl,
		Camera camera,
		Matrix4f matrix4f,
		Matrix4f matrix4f2,
		GpuBufferSlice gpuBufferSlice,
		Vector4f vector4f,
		boolean bl2
	) {
		float f = deltaTracker.getGameTimeDeltaPartialTick(false);
		this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
		this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
		final ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("light_update_queue");
		this.level.pollLightUpdates();
		profilerFiller.popPush("light_updates");
		this.level.getChunkSource().getLightEngine().runLightUpdates();
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double g = vec3.z();
		profilerFiller.popPush("culling");
		boolean bl3 = this.capturedFrustum != null;
		Frustum frustum = bl3 ? this.capturedFrustum : this.cullingFrustum;
		profilerFiller.popPush("captureFrustum");
		if (this.captureFrustum) {
			this.capturedFrustum = bl3 ? new Frustum(matrix4f, matrix4f2) : frustum;
			this.capturedFrustum.prepare(d, e, g);
			this.captureFrustum = false;
		}

		profilerFiller.popPush("cullEntities");
		boolean bl4 = this.collectVisibleEntities(camera, frustum, this.visibleEntities);
		this.visibleEntityCount = this.visibleEntities.size();
		profilerFiller.popPush("terrain_setup");
		this.matrices = new ChunkRenderMatrices(matrix4f2, matrix4f);
		this.setupRender(camera, frustum, bl3, this.minecraft.player.isSpectator());
		profilerFiller.popPush("compile_sections");
		this.compileSections(camera);
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		matrix4fStack.mul(matrix4f);
		FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
		this.targets.main = frameGraphBuilder.importExternal("main", this.minecraft.getMainRenderTarget());
		int i = this.minecraft.getMainRenderTarget().width;
		int j = this.minecraft.getMainRenderTarget().height;
		RenderTargetDescriptor renderTargetDescriptor = new RenderTargetDescriptor(i, j, true, 0);
		PostChain postChain = this.getTransparencyChain();
		if (postChain != null) {
			this.targets.translucent = frameGraphBuilder.createInternal("translucent", renderTargetDescriptor);
			this.targets.itemEntity = frameGraphBuilder.createInternal("item_entity", renderTargetDescriptor);
			this.targets.particles = frameGraphBuilder.createInternal("particles", renderTargetDescriptor);
			this.targets.weather = frameGraphBuilder.createInternal("weather", renderTargetDescriptor);
			this.targets.clouds = frameGraphBuilder.createInternal("clouds", renderTargetDescriptor);
		}

		if (this.entityOutlineTarget != null) {
			this.targets.entityOutline = frameGraphBuilder.importExternal("entity_outline", this.entityOutlineTarget);
		}

		FramePass framePass = frameGraphBuilder.addPass("clear");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		framePass.executes(
			() -> {
				RenderTarget renderTarget = this.minecraft.getMainRenderTarget();
				RenderSystem.getDevice()
					.createCommandEncoder()
					.clearColorAndDepthTextures(
						renderTarget.getColorTexture(), ARGB.colorFromFloat(0.0F, vector4f.x, vector4f.y, vector4f.z), renderTarget.getDepthTexture(), 1.0
					);
			}
		);
		if (bl2) {
			this.addSkyPass(frameGraphBuilder, camera, f, gpuBufferSlice);
		}

		this.addMainPass(frameGraphBuilder, frustum, camera, matrix4f, gpuBufferSlice, bl, bl4, deltaTracker, profilerFiller);
		PostChain postChain2 = this.minecraft.getShaderManager().getPostChain(ENTITY_OUTLINE_POST_CHAIN_ID, LevelTargetBundle.OUTLINE_TARGETS);
		if (bl4 && postChain2 != null) {
			postChain2.addToFrame(frameGraphBuilder, i, j, this.targets);
		}

		this.addParticlesPass(frameGraphBuilder, camera, f, gpuBufferSlice);
		CloudStatus cloudStatus = this.minecraft.options.getCloudsType();
		if (cloudStatus != CloudStatus.OFF) {
			Optional<Integer> optional = this.level.dimensionType().cloudHeight();
			if (optional.isPresent()) {
				float h = this.ticks + f;
				int k = this.level.getCloudColor(f);
				this.addCloudsPass(frameGraphBuilder, cloudStatus, camera.getPosition(), h, k, ((Integer)optional.get()).intValue() + 0.33F);
			}
		}

		this.addWeatherPass(frameGraphBuilder, camera.getPosition(), f, gpuBufferSlice);
		if (postChain != null) {
			postChain.addToFrame(frameGraphBuilder, i, j, this.targets);
		}

		this.addLateDebugPass(frameGraphBuilder, vec3, gpuBufferSlice);
		profilerFiller.popPush("framegraph");
		frameGraphBuilder.execute(graphicsResourceAllocator, new FrameGraphBuilder.Inspector() {
			@Override
			public void beforeExecutePass(String string) {
				profilerFiller.push(string);
			}

			@Override
			public void afterExecutePass(String string) {
				profilerFiller.pop();
			}
		});
		this.visibleEntities.clear();
		this.targets.clear();
		matrix4fStack.popMatrix();
		profilerFiller.pop();
	}

	private void addMainPass(
		FrameGraphBuilder frameGraphBuilder,
		Frustum frustum,
		Camera camera,
		Matrix4f matrix4f,
		GpuBufferSlice gpuBufferSlice,
		boolean bl,
		boolean bl2,
		DeltaTracker deltaTracker,
		ProfilerFiller profilerFiller
	) {
		FramePass framePass = frameGraphBuilder.addPass("main");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		if (this.targets.translucent != null) {
			this.targets.translucent = framePass.readsAndWrites(this.targets.translucent);
		}

		if (this.targets.itemEntity != null) {
			this.targets.itemEntity = framePass.readsAndWrites(this.targets.itemEntity);
		}

		if (this.targets.weather != null) {
			this.targets.weather = framePass.readsAndWrites(this.targets.weather);
		}

		if (bl2 && this.targets.entityOutline != null) {
			this.targets.entityOutline = framePass.readsAndWrites(this.targets.entityOutline);
		}

		ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
		ResourceHandle<RenderTarget> resourceHandle2 = this.targets.translucent;
		ResourceHandle<RenderTarget> resourceHandle3 = this.targets.itemEntity;
		ResourceHandle<RenderTarget> resourceHandle4 = this.targets.entityOutline;
		framePass.executes(() -> {
			RenderSystem.setShaderFog(gpuBufferSlice);
			float f = deltaTracker.getGameTimeDeltaPartialTick(false);
			Vec3 vec3 = camera.getPosition();
			double d = vec3.x();
			double e = vec3.y();
			double g = vec3.z();
			profilerFiller.push("terrain");
			ChunkSectionsToRender chunkSectionsToRender = this.prepareChunkRenders(matrix4f, d, e, g);
			chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.OPAQUE);
			this.minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);
			if (resourceHandle3 != null) {
				resourceHandle3.get().copyDepthFrom(this.minecraft.getMainRenderTarget());
			}

			if (this.shouldShowEntityOutlines() && resourceHandle4 != null) {
				RenderTarget renderTarget = resourceHandle4.get();
				RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1.0);
			}

			PoseStack poseStack = new PoseStack();
			MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
			MultiBufferSource.BufferSource bufferSource2 = this.renderBuffers.crumblingBufferSource();
			profilerFiller.popPush("entities");
			this.visibleEntities.sort(ENTITY_COMPARATOR);
			this.renderEntities(poseStack, bufferSource, camera, deltaTracker, this.visibleEntities);
			bufferSource.endLastBatch();
			this.checkPoseStack(poseStack);
			profilerFiller.popPush("blockentities");
			this.renderBlockEntities(poseStack, bufferSource, bufferSource2, camera, f);
			bufferSource.endLastBatch();
			this.checkPoseStack(poseStack);
			bufferSource.endBatch(RenderType.solid());
			bufferSource.endBatch(RenderType.endPortal());
			bufferSource.endBatch(RenderType.endGateway());
			bufferSource.endBatch(Sheets.solidBlockSheet());
			bufferSource.endBatch(Sheets.cutoutBlockSheet());
			bufferSource.endBatch(Sheets.bedSheet());
			bufferSource.endBatch(Sheets.shulkerBoxSheet());
			bufferSource.endBatch(Sheets.signSheet());
			bufferSource.endBatch(Sheets.hangingSignSheet());
			bufferSource.endBatch(Sheets.chestSheet());
			this.renderBuffers.outlineBufferSource().endOutlineBatch();
			if (bl) {
				this.renderBlockOutline(camera, bufferSource, poseStack, false);
			}

			profilerFiller.popPush("debug");
			this.minecraft.debugRenderer.render(poseStack, frustum, bufferSource, d, e, g);
			bufferSource.endLastBatch();
			this.checkPoseStack(poseStack);
			bufferSource.endBatch(Sheets.translucentItemSheet());
			bufferSource.endBatch(Sheets.bannerSheet());
			bufferSource.endBatch(Sheets.shieldSheet());
			bufferSource.endBatch(RenderType.armorEntityGlint());
			bufferSource.endBatch(RenderType.glint());
			bufferSource.endBatch(RenderType.glintTranslucent());
			bufferSource.endBatch(RenderType.entityGlint());
			profilerFiller.popPush("destroyProgress");
			this.renderBlockDestroyAnimation(poseStack, camera, bufferSource2);
			bufferSource2.endBatch();
			this.checkPoseStack(poseStack);
			bufferSource.endBatch(RenderType.waterMask());
			bufferSource.endBatch();
			if (resourceHandle2 != null) {
				resourceHandle2.get().copyDepthFrom(resourceHandle.get());
			}

			profilerFiller.popPush("translucent");
			chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.TRANSLUCENT);
			profilerFiller.popPush("string");
			chunkSectionsToRender.renderGroup(ChunkSectionLayerGroup.TRIPWIRE);
			if (bl) {
				this.renderBlockOutline(camera, bufferSource, poseStack, true);
			}

			bufferSource.endBatch();
			profilerFiller.pop();
		});
	}

	private void addParticlesPass(FrameGraphBuilder frameGraphBuilder, Camera camera, float f, GpuBufferSlice gpuBufferSlice) {
		FramePass framePass = frameGraphBuilder.addPass("particles");
		if (this.targets.particles != null) {
			this.targets.particles = framePass.readsAndWrites(this.targets.particles);
			framePass.reads(this.targets.main);
		} else {
			this.targets.main = framePass.readsAndWrites(this.targets.main);
		}

		ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
		ResourceHandle<RenderTarget> resourceHandle2 = this.targets.particles;
		framePass.executes(() -> {
			RenderSystem.setShaderFog(gpuBufferSlice);
			if (resourceHandle2 != null) {
				resourceHandle2.get().copyDepthFrom(resourceHandle.get());
			}

			this.minecraft.particleEngine.render(camera, f, this.renderBuffers.bufferSource());
		});
	}

	private void addCloudsPass(FrameGraphBuilder frameGraphBuilder, CloudStatus cloudStatus, Vec3 vec3, float f, int i, float g) {
		FramePass framePass = frameGraphBuilder.addPass("clouds");
		if (this.targets.clouds != null) {
			this.targets.clouds = framePass.readsAndWrites(this.targets.clouds);
		} else {
			this.targets.main = framePass.readsAndWrites(this.targets.main);
		}

		framePass.executes(() -> this.cloudRenderer.render(i, cloudStatus, g, vec3, f));
	}

	private void addWeatherPass(FrameGraphBuilder frameGraphBuilder, Vec3 vec3, float f, GpuBufferSlice gpuBufferSlice) {
		int i = this.minecraft.options.getEffectiveRenderDistance() * 16;
		float g = this.minecraft.gameRenderer.getDepthFar();
		FramePass framePass = frameGraphBuilder.addPass("weather");
		if (this.targets.weather != null) {
			this.targets.weather = framePass.readsAndWrites(this.targets.weather);
		} else {
			this.targets.main = framePass.readsAndWrites(this.targets.main);
		}

		framePass.executes(() -> {
			RenderSystem.setShaderFog(gpuBufferSlice);
			MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
			this.weatherEffectRenderer.render(this.minecraft.level, bufferSource, this.ticks, f, vec3);
			this.worldBorderRenderer.render(this.level.getWorldBorder(), vec3, i, g);
			bufferSource.endBatch();
		});
	}

	private void addLateDebugPass(FrameGraphBuilder frameGraphBuilder, Vec3 vec3, GpuBufferSlice gpuBufferSlice) {
		FramePass framePass = frameGraphBuilder.addPass("late_debug");
		this.targets.main = framePass.readsAndWrites(this.targets.main);
		if (this.targets.itemEntity != null) {
			this.targets.itemEntity = framePass.readsAndWrites(this.targets.itemEntity);
		}

		ResourceHandle<RenderTarget> resourceHandle = this.targets.main;
		framePass.executes(() -> {
			RenderSystem.setShaderFog(gpuBufferSlice);
			PoseStack poseStack = new PoseStack();
			MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
			this.minecraft.debugRenderer.renderAfterTranslucents(poseStack, bufferSource, vec3.x, vec3.y, vec3.z);
			bufferSource.endLastBatch();
			this.checkPoseStack(poseStack);
		});
	}

	private boolean collectVisibleEntities(Camera camera, Frustum frustum, List<Entity> list) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();
		boolean bl = false;
		boolean bl2 = this.shouldShowEntityOutlines();
		Entity.setViewScale(Mth.clamp(this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * this.minecraft.options.entityDistanceScaling().get());

		for (Entity entity : this.level.entitiesForRendering()) {
			if (this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, f) || entity.hasIndirectPassenger(this.minecraft.player)) {
				BlockPos blockPos = entity.blockPosition();
				if ((this.level.isOutsideBuildHeight(blockPos.getY()) || this.isSectionCompiled(blockPos))
					&& (entity != camera.getEntity() || camera.isDetached() || camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping())
					&& (!(entity instanceof LocalPlayer) || camera.getEntity() == entity)) {
					list.add(entity);
					if (bl2 && this.minecraft.shouldEntityAppearGlowing(entity)) {
						bl = true;
					}
				}
			}
		}

		return bl;
	}

	private void renderEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> list) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();
		TickRateManager tickRateManager = this.minecraft.level.tickRateManager();
		boolean bl = this.shouldShowEntityOutlines();

		for (Entity entity : list) {
			if (entity.tickCount == 0) {
				entity.xOld = entity.getX();
				entity.yOld = entity.getY();
				entity.zOld = entity.getZ();
			}

			MultiBufferSource multiBufferSource;
			if (bl && this.minecraft.shouldEntityAppearGlowing(entity)) {
				OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
				multiBufferSource = outlineBufferSource;
				int i = entity.getTeamColor();
				outlineBufferSource.setColor(ARGB.red(i), ARGB.green(i), ARGB.blue(i), 255);
			} else {
				multiBufferSource = bufferSource;
			}

			float g = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
			this.renderEntity(entity, d, e, f, g, poseStack, multiBufferSource);
		}
	}

	private void renderBlockEntities(
		PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource bufferSource2, Camera camera, float f
	) {
		this.renderer.renderBlockEntities(new PoseStack(), this.renderBuffers, this.destructionProgress, camera, f, null);
		/*Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double g = vec3.z();

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			List<BlockEntity> list = renderSection.getSectionMesh().getRenderableBlockEntities();
			if (!list.isEmpty()) {
				for (BlockEntity blockEntity : list) {
					BlockPos blockPos = blockEntity.getBlockPos();
					MultiBufferSource multiBufferSource = bufferSource;
					poseStack.pushPose();
					poseStack.translate(blockPos.getX() - d, blockPos.getY() - e, blockPos.getZ() - g);
					SortedSet<BlockDestructionProgress> sortedSet = this.destructionProgress.get(blockPos.asLong());
					if (sortedSet != null && !sortedSet.isEmpty()) {
						int i = ((BlockDestructionProgress)sortedSet.last()).getProgress();
						if (i >= 0) {
							PoseStack.Pose pose = poseStack.last();
							VertexConsumer vertexConsumer = new SheetedDecalTextureGenerator(bufferSource2.getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(i)), pose, 1.0F);
							multiBufferSource = renderType -> {
								VertexConsumer vertexConsumer2 = bufferSource.getBuffer(renderType);
								return renderType.affectsCrumbling() ? VertexMultiConsumer.create(vertexConsumer, vertexConsumer2) : vertexConsumer2;
							};
						}
					}

					this.blockEntityRenderDispatcher.render(blockEntity, f, poseStack, multiBufferSource);
					poseStack.popPose();
				}
			}
		}

		Iterator<BlockEntity> iterator = this.level.getGloballyRenderedBlockEntities().iterator();

		while (iterator.hasNext()) {
			BlockEntity blockEntity2 = (BlockEntity)iterator.next();
			if (blockEntity2.isRemoved()) {
				iterator.remove();
			} else {
				BlockPos blockPos2 = blockEntity2.getBlockPos();
				poseStack.pushPose();
				poseStack.translate(blockPos2.getX() - d, blockPos2.getY() - e, blockPos2.getZ() - g);
				this.blockEntityRenderDispatcher.render(blockEntity2, f, poseStack, bufferSource);
				poseStack.popPose();
			}
		}*/
	}

	private void renderBlockDestroyAnimation(PoseStack poseStack, Camera camera, MultiBufferSource.BufferSource bufferSource) {
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double f = vec3.z();

		for (Entry<SortedSet<BlockDestructionProgress>> entry : this.destructionProgress.long2ObjectEntrySet()) {
			BlockPos blockPos = BlockPos.of(entry.getLongKey());
			if (!(blockPos.distToCenterSqr(d, e, f) > 1024.0)) {
				SortedSet<BlockDestructionProgress> sortedSet = (SortedSet<BlockDestructionProgress>)entry.getValue();
				if (sortedSet != null && !sortedSet.isEmpty()) {
					int i = ((BlockDestructionProgress)sortedSet.last()).getProgress();
					poseStack.pushPose();
					poseStack.translate(blockPos.getX() - d, blockPos.getY() - e, blockPos.getZ() - f);
					PoseStack.Pose pose = poseStack.last();
					VertexConsumer vertexConsumer = new SheetedDecalTextureGenerator(bufferSource.getBuffer((RenderType)ModelBakery.DESTROY_TYPES.get(i)), pose, 1.0F);
					this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockPos), blockPos, this.level, poseStack, vertexConsumer);
					poseStack.popPose();
				}
			}
		}
	}

	private void renderBlockOutline(Camera camera, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean bl) {
		if (this.minecraft.hitResult instanceof BlockHitResult blockHitResult) {
			if (blockHitResult.getType() != HitResult.Type.MISS) {
				BlockPos blockPos = blockHitResult.getBlockPos();
				BlockState blockState = this.level.getBlockState(blockPos);
				if (!blockState.isAir() && this.level.getWorldBorder().isWithinBounds(blockPos)) {
					boolean bl2 = ItemBlockRenderTypes.getChunkRenderType(blockState).sortOnUpload();
					if (bl2 != bl) {
						return;
					}

					Vec3 vec3 = camera.getPosition();
					Boolean boolean_ = this.minecraft.options.highContrastBlockOutline().get();
					if (boolean_) {
						VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.secondaryBlockOutline());
						this.renderHitOutline(poseStack, vertexConsumer, camera.getEntity(), vec3.x, vec3.y, vec3.z, blockPos, blockState, -16777216);
					}

					VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
					int i = boolean_ ? -11010079 : ARGB.color(102, -16777216);
					this.renderHitOutline(poseStack, vertexConsumer, camera.getEntity(), vec3.x, vec3.y, vec3.z, blockPos, blockState, i);
					bufferSource.endLastBatch();
				}
			}
		}
	}

	private void checkPoseStack(PoseStack poseStack) {
		if (!poseStack.isEmpty()) {
			throw new IllegalStateException("Pose stack not empty");
		}
	}

	private void renderEntity(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		double h = Mth.lerp((double)g, entity.xOld, entity.getX());
		double i = Mth.lerp((double)g, entity.yOld, entity.getY());
		double j = Mth.lerp((double)g, entity.zOld, entity.getZ());
		this.entityRenderDispatcher.render(entity, h - d, i - e, j - f, g, poseStack, multiBufferSource, this.entityRenderDispatcher.getPackedLightCoords(entity, g));
	}

	private void scheduleTranslucentSectionResort(Vec3 vec3) {
		if (!this.visibleSections.isEmpty()) {
			BlockPos blockPos = BlockPos.containing(vec3);
			boolean bl = !blockPos.equals(this.lastTranslucentSortBlockPos);
			Profiler.get().push("translucent_sort");
			TranslucencyPointOfView translucencyPointOfView = new TranslucencyPointOfView();

			for (SectionRenderDispatcher.RenderSection renderSection : this.nearbyVisibleSections) {
				this.scheduleResort(renderSection, translucencyPointOfView, vec3, bl, true);
			}

			this.translucencyResortIterationIndex = this.translucencyResortIterationIndex % this.visibleSections.size();
			int i = Math.max(this.visibleSections.size() / 8, 15);

			while (i-- > 0) {
				int j = this.translucencyResortIterationIndex++ % this.visibleSections.size();
				this.scheduleResort(this.visibleSections.get(j), translucencyPointOfView, vec3, bl, false);
			}

			this.lastTranslucentSortBlockPos = blockPos;
			Profiler.get().pop();
		}
	}

	private void scheduleResort(
		SectionRenderDispatcher.RenderSection renderSection, TranslucencyPointOfView translucencyPointOfView, Vec3 vec3, boolean bl, boolean bl2
	) {
		translucencyPointOfView.set(vec3, renderSection.getSectionNode());
		boolean bl3 = renderSection.getSectionMesh().isDifferentPointOfView(translucencyPointOfView);
		boolean bl4 = bl && (translucencyPointOfView.isAxisAligned() || bl2);
		if ((bl4 || bl3) && !renderSection.transparencyResortingScheduled() && renderSection.hasTranslucentGeometry()) {
			renderSection.resortTransparency(this.sectionRenderDispatcher);
		}
	}

	private ChunkSectionsToRender prepareChunkRenders(Matrix4fc p_407733_, double p_409433_, double p_409487_, double p_408168_) {
		ChunkSectionsToRender chunkSectionsToRender = new ChunkSectionsToRender(STATIC_MAP, -1, new GpuBufferSlice[0]);
		((SodiumChunkSection)chunkSectionsToRender).sodium$setRendering(this.renderer, this.matrices, p_409433_, p_409487_, p_408168_);
		return chunkSectionsToRender;
		/*ObjectListIterator<SectionRenderDispatcher.RenderSection> objectlistiterator = this.visibleSections.listIterator(0);
		EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> enummap = new EnumMap<>(ChunkSectionLayer.class);
		int i = 0;

		for (ChunkSectionLayer chunksectionlayer : ChunkSectionLayer.values()) {
			enummap.put(chunksectionlayer, new ArrayList<>());
		}

		List<DynamicUniforms.Transform> list = new ArrayList<>();
		Vector4f vector4f = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix4f = new Matrix4f();

		while (objectlistiterator.hasNext()) {
			SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = objectlistiterator.next();
			SectionMesh sectionmesh = sectionrenderdispatcher$rendersection.getSectionMesh();

			for (ChunkSectionLayer chunksectionlayer1 : ChunkSectionLayer.values()) {
				SectionBuffers sectionbuffers = sectionmesh.getBuffers(chunksectionlayer1);
				if (sectionbuffers != null) {
					GpuBuffer gpubuffer;
					VertexFormat.IndexType vertexformat$indextype;
					if (sectionbuffers.getIndexBuffer() == null) {
						if (sectionbuffers.getIndexCount() > i) {
							i = sectionbuffers.getIndexCount();
						}

						gpubuffer = null;
						vertexformat$indextype = null;
					} else {
						gpubuffer = sectionbuffers.getIndexBuffer();
						vertexformat$indextype = sectionbuffers.getIndexType();
					}

					BlockPos blockpos = sectionrenderdispatcher$rendersection.getRenderOrigin();
					int j = list.size();
					list.add(
							new DynamicUniforms.Transform(
									p_407733_,
									vector4f,
									new Vector3f(
											(float)(blockpos.getX() - p_409433_), (float)(blockpos.getY() - p_409487_), (float)(blockpos.getZ() - p_408168_)
									),
									matrix4f,
									1.0F
							)
					);
					enummap.get(chunksectionlayer1)
							.add(
									new RenderPass.Draw<>(
											0,
											sectionbuffers.getVertexBuffer(),
											gpubuffer,
											vertexformat$indextype,
											0,
											sectionbuffers.getIndexCount(),
											(p_404906_, p_404907_) -> p_404907_.upload("DynamicTransforms", p_404906_[j])
									)
							);
				}
			}
		}

		GpuBufferSlice[] agpubufferslice = RenderSystem.getDynamicUniforms().writeTransforms(list.toArray(new DynamicUniforms.Transform[0]));
		return new ChunkSectionsToRender(enummap, i, agpubufferslice);*/
	}


	public void endFrame() {
		this.cloudRenderer.endFrame();
	}

	public void captureFrustum() {
		this.captureFrustum = true;
	}

	public void killFrustum() {
		this.capturedFrustum = null;
	}

	public void tick() {
		if (this.level.tickRateManager().runsNormally()) {
			this.ticks++;
		}

		if (this.ticks % 20 == 0) {
			Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

			while (iterator.hasNext()) {
				BlockDestructionProgress blockDestructionProgress = (BlockDestructionProgress)iterator.next();
				int i = blockDestructionProgress.getUpdatedRenderTick();
				if (this.ticks - i > 400) {
					iterator.remove();
					this.removeProgress(blockDestructionProgress);
				}
			}
		}
	}

	private void removeProgress(BlockDestructionProgress blockDestructionProgress) {
		long l = blockDestructionProgress.getPos().asLong();
		Set<BlockDestructionProgress> set = (Set<BlockDestructionProgress>)this.destructionProgress.get(l);
		set.remove(blockDestructionProgress);
		if (set.isEmpty()) {
			this.destructionProgress.remove(l);
		}
	}

	private void addSkyPass(FrameGraphBuilder frameGraphBuilder, Camera camera, float f, GpuBufferSlice gpuBufferSlice) {
		FogType fogType = camera.getFluidInCamera();
		if (fogType != FogType.POWDER_SNOW && fogType != FogType.LAVA && !this.doesMobEffectBlockSky(camera)) {
			DimensionSpecialEffects dimensionSpecialEffects = this.level.effects();
			DimensionSpecialEffects.SkyType skyType = dimensionSpecialEffects.skyType();
			if (skyType != DimensionSpecialEffects.SkyType.NONE) {
				FramePass framePass = frameGraphBuilder.addPass("sky");
				this.targets.main = framePass.readsAndWrites(this.targets.main);
				framePass.executes(() -> {
					if (Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE
							|| this.doesMobEffectBlockSky(Minecraft.getInstance().gameRenderer.getMainCamera())) {
						return;
					}
					RenderSystem.setShaderFog(gpuBufferSlice);
					if (skyType == DimensionSpecialEffects.SkyType.END) {
						this.skyRenderer.renderEndSky();
					} else {
						PoseStack poseStack = new PoseStack();
						float g = this.level.getSunAngle(f);
						float h = this.level.getTimeOfDay(f);
						float i = 1.0F - this.level.getRainLevel(f);
						float j = this.level.getStarBrightness(f) * i;
						int k = dimensionSpecialEffects.getSunriseOrSunsetColor(h);
						int l = this.level.getMoonPhase();
						int m = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), f);
						float n = ARGB.redFloat(m);
						float o = ARGB.greenFloat(m);
						float p = ARGB.blueFloat(m);
						this.skyRenderer.renderSkyDisc(n, o, p);
						MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
						if (dimensionSpecialEffects.isSunriseOrSunset(h)) {
							this.skyRenderer.renderSunriseAndSunset(poseStack, bufferSource, g, k);
						}

						this.skyRenderer.renderSunMoonAndStars(poseStack, bufferSource, h, l, i, j);
						bufferSource.endBatch();
						if (this.shouldRenderDarkDisc(f)) {
							this.skyRenderer.renderDarkDisc();
						}
					}
				});
			}
		}
	}

	private boolean shouldRenderDarkDisc(float f) {
		return this.minecraft.player.getEyePosition(f).y - this.level.getLevelData().getHorizonHeight(this.level) < 0.0;
	}

	private boolean doesMobEffectBlockSky(Camera camera) {
		return !(camera.getEntity() instanceof LivingEntity livingEntity)
			? false
			: livingEntity.hasEffect(MobEffects.BLINDNESS) || livingEntity.hasEffect(MobEffects.DARKNESS);
	}

	private void compileSections(Camera camera) {
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("populate_sections_to_compile");
		RenderRegionCache renderRegionCache = new RenderRegionCache();
		BlockPos blockPos = camera.getBlockPosition();
		List<SectionRenderDispatcher.RenderSection> list = Lists.<SectionRenderDispatcher.RenderSection>newArrayList();

		for (SectionRenderDispatcher.RenderSection renderSection : this.visibleSections) {
			if (renderSection.isDirty() && (renderSection.getSectionMesh() != CompiledSectionMesh.UNCOMPILED || renderSection.hasAllNeighbors())) {
				boolean bl = false;
				if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
					BlockPos blockPos2 = SectionPos.of(renderSection.getSectionNode()).center();
					bl = blockPos2.distSqr(blockPos) < 768.0 || renderSection.isDirtyFromPlayer();
				} else if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
					bl = renderSection.isDirtyFromPlayer();
				}

				if (bl) {
					profilerFiller.push("build_near_sync");
					this.sectionRenderDispatcher.rebuildSectionSync(renderSection, renderRegionCache);
					renderSection.setNotDirty();
					profilerFiller.pop();
				} else {
					list.add(renderSection);
				}
			}
		}

		profilerFiller.popPush("upload");
		this.sectionRenderDispatcher.uploadAllPendingUploads();
		profilerFiller.popPush("schedule_async_compile");

		for (SectionRenderDispatcher.RenderSection renderSectionx : list) {
			renderSectionx.rebuildSectionAsync(renderRegionCache);
			renderSectionx.setNotDirty();
		}

		profilerFiller.pop();
		this.scheduleTranslucentSectionResort(camera.getPosition());
	}

	private void renderHitOutline(
		PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, int i
	) {
		ShapeRenderer.renderShape(
			poseStack,
			vertexConsumer,
			blockState.getShape(this.level, blockPos, CollisionContext.of(entity)),
			blockPos.getX() - d,
			blockPos.getY() - e,
			blockPos.getZ() - f,
			i
		);
	}

	public void blockChanged(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockState blockState2, int i) {
		this.setBlockDirty(blockPos, (i & 8) != 0);
	}

	private void setBlockDirty(BlockPos pos, boolean important) {
		this.renderer
				.scheduleRebuildForBlockArea(
						pos.getX() - 1,
						pos.getY() - 1,
						pos.getZ() - 1,
						pos.getX() + 1,
						pos.getY() + 1,
						pos.getZ() + 1,
						important
				);
		/*for (int i = blockPos.getZ() - 1; i <= blockPos.getZ() + 1; i++) {
			for (int j = blockPos.getX() - 1; j <= blockPos.getX() + 1; j++) {
				for (int k = blockPos.getY() - 1; k <= blockPos.getY() + 1; k++) {
					this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), bl);
				}
			}
		}*/
	}

	public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.renderer.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, false);
		/*for (int o = k - 1; o <= n + 1; o++) {
			for (int p = i - 1; p <= l + 1; p++) {
				for (int q = j - 1; q <= m + 1; q++) {
					this.setSectionDirty(SectionPos.blockToSectionCoord(p), SectionPos.blockToSectionCoord(q), SectionPos.blockToSectionCoord(o));
				}
			}
		}*/
	}

	public void setBlockDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
		if (this.minecraft.getModelManager().requiresRender(blockState, blockState2)) {
			this.setBlocksDirty(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}
	}

	public void setSectionDirtyWithNeighbors(int x, int y, int z) {
		this.renderer.scheduleRebuildForChunks(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1, false);
		//this.setSectionRangeDirty(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
	}

	public void setSectionRangeDirty(int i, int j, int k, int l, int m, int n) {
		for (int o = k; o <= n; o++) {
			for (int p = i; p <= l; p++) {
				for (int q = j; q <= m; q++) {
					this.setSectionDirty(p, q, o);
				}
			}
		}
	}

	public void setSectionDirty(int i, int j, int k) {
		this.setSectionDirty(i, j, k, false);
	}

	private void setSectionDirty(int x, int y, int z, boolean important) {
		//this.viewArea.setDirty(i, j, k, bl);
		this.renderer.scheduleRebuildForChunk(x, y, z, important);
	}

	public void onSectionBecomingNonEmpty(long l) {
		SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSection(l);
		if (renderSection != null) {
			this.sectionOcclusionGraph.schedulePropagationFrom(renderSection);
		}
	}

	public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
		this.addParticle(particleOptions, bl, false, d, e, f, g, h, i);
	}

	public void addParticle(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double e, double f, double g, double h, double i) {
		try {
			this.addParticleInternal(particleOptions, bl, bl2, d, e, f, g, h, i);
		} catch (Throwable var19) {
			CrashReport crashReport = CrashReport.forThrowable(var19, "Exception while adding particle");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being added");
			crashReportCategory.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType()));
			crashReportCategory.setDetail(
				"Parameters",
				(CrashReportDetail<String>)(() -> ParticleTypes.CODEC
					.encodeStart(this.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), particleOptions)
					.toString())
			);
			crashReportCategory.setDetail("Position", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this.level, d, e, f)));
			throw new ReportedException(crashReport);
		}
	}

	public <T extends ParticleOptions> void addParticle(T particleOptions, double d, double e, double f, double g, double h, double i) {
		this.addParticle(particleOptions, particleOptions.getType().getOverrideLimiter(), d, e, f, g, h, i);
	}

	@Nullable
	Particle addParticleInternal(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
		return this.addParticleInternal(particleOptions, bl, false, d, e, f, g, h, i);
	}

	@Nullable
	private Particle addParticleInternal(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double e, double f, double g, double h, double i) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		ParticleStatus particleStatus = this.calculateParticleLevel(bl2);
		if (bl) {
			return this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
		} else if (camera.getPosition().distanceToSqr(d, e, f) > 1024.0) {
			return null;
		} else {
			return particleStatus == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(particleOptions, d, e, f, g, h, i);
		}
	}

	private ParticleStatus calculateParticleLevel(boolean bl) {
		ParticleStatus particleStatus = this.minecraft.options.particles().get();
		if (bl && particleStatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
			particleStatus = ParticleStatus.DECREASED;
		}

		if (particleStatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
			particleStatus = ParticleStatus.MINIMAL;
		}

		return particleStatus;
	}

	public void destroyBlockProgress(int i, BlockPos blockPos, int j) {
		if (j >= 0 && j < 10) {
			BlockDestructionProgress blockDestructionProgress = this.destroyingBlocks.get(i);
			if (blockDestructionProgress != null) {
				this.removeProgress(blockDestructionProgress);
			}

			if (blockDestructionProgress == null
				|| blockDestructionProgress.getPos().getX() != blockPos.getX()
				|| blockDestructionProgress.getPos().getY() != blockPos.getY()
				|| blockDestructionProgress.getPos().getZ() != blockPos.getZ()) {
				blockDestructionProgress = new BlockDestructionProgress(i, blockPos);
				this.destroyingBlocks.put(i, blockDestructionProgress);
			}

			blockDestructionProgress.setProgress(j);
			blockDestructionProgress.updateTick(this.ticks);
			this.destructionProgress
				.computeIfAbsent(
					blockDestructionProgress.getPos().asLong(),
					(Long2ObjectFunction<? extends SortedSet<BlockDestructionProgress>>)(l -> Sets.<BlockDestructionProgress>newTreeSet())
				)
				.add(blockDestructionProgress);
		} else {
			BlockDestructionProgress blockDestructionProgressx = this.destroyingBlocks.remove(i);
			if (blockDestructionProgressx != null) {
				this.removeProgress(blockDestructionProgressx);
			}
		}
	}

	public boolean hasRenderedAllSections() {
		return this.renderer.isTerrainRenderComplete();
		/*return this.sectionRenderDispatcher.isQueueEmpty();*/
	}

	public void onChunkReadyToRender(ChunkPos chunkPos) {
		this.sectionOcclusionGraph.onChunkReadyToRender(chunkPos);
	}

	public void needsUpdate() {
		this.renderer.scheduleTerrainUpdate();
		/*this.sectionOcclusionGraph.invalidate();
		this.cloudRenderer.markForRebuild();*/
	}

	public static int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
		return getLightColor(LevelRenderer.BrightnessGetter.DEFAULT, blockAndTintGetter, blockAndTintGetter.getBlockState(blockPos), blockPos);
	}

	public static int getLightColor(
		LevelRenderer.BrightnessGetter brightnessGetter, BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos
	) {
		if (blockState.emissiveRendering(blockAndTintGetter, blockPos)) {
			return 15728880;
		} else {
			int i = brightnessGetter.packedBrightness(blockAndTintGetter, blockPos);
			int j = LightTexture.block(i);
			int k = blockState.getLightEmission();
			if (j < k) {
				int l = LightTexture.sky(i);
				return LightTexture.pack(k, l);
			} else {
				return i;
			}
		}
	}

	public boolean isSectionCompiled(BlockPos blockPos) {
		return this.renderer.isSectionReady(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4);
		/*SectionRenderDispatcher.RenderSection renderSection = this.viewArea.getRenderSectionAt(blockPos);
		return renderSection != null && renderSection.sectionMesh.get() != CompiledSectionMesh.UNCOMPILED;*/
	}

	@Nullable
	public RenderTarget entityOutlineTarget() {
		return this.targets.entityOutline != null ? this.targets.entityOutline.get() : null;
	}

	@Nullable
	public RenderTarget getTranslucentTarget() {
		return this.targets.translucent != null ? this.targets.translucent.get() : null;
	}

	@Nullable
	public RenderTarget getItemEntityTarget() {
		return this.targets.itemEntity != null ? this.targets.itemEntity.get() : null;
	}

	@Nullable
	public RenderTarget getParticlesTarget() {
		return this.targets.particles != null ? this.targets.particles.get() : null;
	}

	@Nullable
	public RenderTarget getWeatherTarget() {
		return this.targets.weather != null ? this.targets.weather.get() : null;
	}

	@Nullable
	public RenderTarget getCloudsTarget() {
		return this.targets.clouds != null ? this.targets.clouds.get() : null;
	}

	@VisibleForDebug
	public ObjectArrayList<SectionRenderDispatcher.RenderSection> getVisibleSections() {
		return this.visibleSections;
	}

	@VisibleForDebug
	public SectionOcclusionGraph getSectionOcclusionGraph() {
		return this.sectionOcclusionGraph;
	}

	@Nullable
	public Frustum getCapturedFrustum() {
		return this.capturedFrustum;
	}

	public CloudRenderer getCloudRenderer() {
		return this.cloudRenderer;
	}

	@Override
	public SodiumWorldRenderer sodium$getWorldRenderer() {
		return this.renderer;
	}


	@Override
	public void sodium$setMatrices(ChunkRenderMatrices matrices) {
		this.matrices = matrices;
	}

	@Override
	public ChunkRenderMatrices sodium$getMatrices() {
		return this.matrices;
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface BrightnessGetter {
		LevelRenderer.BrightnessGetter DEFAULT = (blockAndTintGetter, blockPos) -> {
			int i = blockAndTintGetter.getBrightness(LightLayer.SKY, blockPos);
			int j = blockAndTintGetter.getBrightness(LightLayer.BLOCK, blockPos);
			return Brightness.pack(j, i);
		};

		int packedBrightness(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos);
	}
}
