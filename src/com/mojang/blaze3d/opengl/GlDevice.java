package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class GlDevice implements GpuDevice {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected static boolean USE_GL_ARB_vertex_attrib_binding = true;
	protected static boolean USE_GL_KHR_debug = true;
	protected static boolean USE_GL_EXT_debug_label = true;
	protected static boolean USE_GL_ARB_debug_output = true;
	protected static boolean USE_GL_ARB_direct_state_access = true;
	protected static boolean USE_GL_ARB_buffer_storage = true;
	private final CommandEncoder encoder;
	@Nullable
	private final GlDebug debugLog;
	private final GlDebugLabel debugLabels;
	private final int maxSupportedTextureSize;
	private final DirectStateAccess directStateAccess;
	private final BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource;
	private final Map<RenderPipeline, GlRenderPipeline> pipelineCache = new IdentityHashMap();
	private final Map<GlDevice.ShaderCompilationKey, GlShaderModule> shaderCache = new HashMap();
	private final VertexArrayCache vertexArrayCache;
	private final BufferStorage bufferStorage;
	private final Set<String> enabledExtensions = new HashSet();
	private final int uniformOffsetAlignment;

	public GlDevice(long l, int i, boolean bl, BiFunction<ResourceLocation, ShaderType, String> biFunction, boolean bl2) {
		GLFW.glfwMakeContextCurrent(l);
		GLCapabilities gLCapabilities = GL.createCapabilities();
		int j = getMaxSupportedTextureSize();
		GLFW.glfwSetWindowSizeLimits(l, -1, -1, j, j);
		this.debugLog = GlDebug.enableDebugCallback(i, bl, this.enabledExtensions);
		this.debugLabels = GlDebugLabel.create(gLCapabilities, bl2, this.enabledExtensions);
		this.vertexArrayCache = VertexArrayCache.create(gLCapabilities, this.debugLabels, this.enabledExtensions);
		this.bufferStorage = BufferStorage.create(gLCapabilities, this.enabledExtensions);
		this.directStateAccess = DirectStateAccess.create(gLCapabilities, this.enabledExtensions);
		this.maxSupportedTextureSize = j;
		this.defaultShaderSource = biFunction;
		this.encoder = new GlCommandEncoder(this);
		this.uniformOffsetAlignment = GL11.glGetInteger(35380);
		GL11.glEnable(34895);
	}

	public GlDebugLabel debugLabels() {
		return this.debugLabels;
	}

	@Override
	public CommandEncoder createCommandEncoder() {
		return this.encoder;
	}

	@Override
	public GpuTexture createTexture(@Nullable Supplier<String> supplier, int i, TextureFormat textureFormat, int j, int k, int l, int m) {
		return this.createTexture(this.debugLabels.exists() && supplier != null ? (String)supplier.get() : null, i, textureFormat, j, k, l, m);
	}

	@Override
	public GpuTexture createTexture(@Nullable String string, int i, TextureFormat textureFormat, int j, int k, int l, int m) {
		if (m < 1) {
			throw new IllegalArgumentException("mipLevels must be at least 1");
		} else if (l < 1) {
			throw new IllegalArgumentException("depthOrLayers must be at least 1");
		} else {
			boolean bl = (i & 16) != 0;
			if (bl) {
				if (j != k) {
					throw new IllegalArgumentException("Cubemap compatible textures must be square, but size is " + j + "x" + k);
				}

				if (l % 6 != 0) {
					throw new IllegalArgumentException("Cubemap compatible textures must have a layer count with a multiple of 6, was " + l);
				}

				if (l > 6) {
					throw new UnsupportedOperationException("Array textures are not yet supported");
				}
			} else if (l > 1) {
				throw new UnsupportedOperationException("Array or 3D textures are not yet supported");
			}

			GlStateManager.clearGlErrors();
			int n = GlStateManager._genTexture();
			if (string == null) {
				string = String.valueOf(n);
			}

			int o;
			if (bl) {
				GL11.glBindTexture(34067, n);
				o = 34067;
			} else {
				GlStateManager._bindTexture(n);
				o = 3553;
			}

			GlStateManager._texParameter(o, 33085, m - 1);
			GlStateManager._texParameter(o, 33082, 0);
			GlStateManager._texParameter(o, 33083, m - 1);
			if (textureFormat.hasDepthAspect()) {
				GlStateManager._texParameter(o, 34892, 0);
			}

			if (bl) {
				for (int p : GlConst.CUBEMAP_TARGETS) {
					for (int q = 0; q < m; q++) {
						GlStateManager._texImage2D(
							p, q, GlConst.toGlInternalId(textureFormat), j >> q, k >> q, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null
						);
					}
				}
			} else {
				for (int r = 0; r < m; r++) {
					GlStateManager._texImage2D(
						o, r, GlConst.toGlInternalId(textureFormat), j >> r, k >> r, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null
					);
				}
			}

			int r = GlStateManager._getError();
			if (r == 1285) {
				throw new GpuOutOfMemoryException("Could not allocate texture of " + j + "x" + k + " for " + string);
			} else if (r != 0) {
				throw new IllegalStateException("OpenGL error " + r);
			} else {
				GlTexture glTexture = new GlTexture(i, string, textureFormat, j, k, l, m, n);
				this.debugLabels.applyLabel(glTexture);
				return glTexture;
			}
		}
	}

	@Override
	public GpuTextureView createTextureView(GpuTexture gpuTexture) {
		return this.createTextureView(gpuTexture, 0, gpuTexture.getMipLevels());
	}

	@Override
	public GpuTextureView createTextureView(GpuTexture gpuTexture, int i, int j) {
		if (gpuTexture.isClosed()) {
			throw new IllegalArgumentException("Can't create texture view with closed texture");
		} else if (i >= 0 && i + j <= gpuTexture.getMipLevels()) {
			return new GlTextureView((GlTexture)gpuTexture, i, j);
		} else {
			throw new IllegalArgumentException(
				j + " mip levels starting from " + i + " would be out of range for texture with only " + gpuTexture.getMipLevels() + " mip levels"
			);
		}
	}

	@Override
	public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int i, int j) {
		if (j <= 0) {
			throw new IllegalArgumentException("Buffer size must be greater than zero");
		} else {
			GlStateManager.clearGlErrors();
			GlBuffer glBuffer = this.bufferStorage.createBuffer(this.directStateAccess, supplier, i, j);
			int k = GlStateManager._getError();
			if (k == 1285) {
				throw new GpuOutOfMemoryException("Could not allocate buffer of " + j + " for " + supplier);
			} else if (k != 0) {
				throw new IllegalStateException("OpenGL error " + k);
			} else {
				this.debugLabels.applyLabel(glBuffer);
				return glBuffer;
			}
		}
	}

	@Override
	public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int i, ByteBuffer byteBuffer) {
		if (!byteBuffer.hasRemaining()) {
			throw new IllegalArgumentException("Buffer source must not be empty");
		} else {
			GlStateManager.clearGlErrors();
			long l = byteBuffer.remaining();
			GlBuffer glBuffer = this.bufferStorage.createBuffer(this.directStateAccess, supplier, i, byteBuffer);
			int j = GlStateManager._getError();
			if (j == 1285) {
				throw new GpuOutOfMemoryException("Could not allocate buffer of " + l + " for " + supplier);
			} else if (j != 0) {
				throw new IllegalStateException("OpenGL error " + j);
			} else {
				this.debugLabels.applyLabel(glBuffer);
				return glBuffer;
			}
		}
	}

	@Override
	public String getImplementationInformation() {
		return GLFW.glfwGetCurrentContext() == 0L
			? "NO CONTEXT"
			: GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
	}

	@Override
	public List<String> getLastDebugMessages() {
		return this.debugLog == null ? Collections.emptyList() : this.debugLog.getLastOpenGlDebugMessages();
	}

	@Override
	public boolean isDebuggingEnabled() {
		return this.debugLog != null;
	}

	@Override
	public String getRenderer() {
		return GlStateManager._getString(7937);
	}

	@Override
	public String getVendor() {
		return GlStateManager._getString(7936);
	}

	@Override
	public String getBackendName() {
		return "OpenGL";
	}

	@Override
	public String getVersion() {
		return GlStateManager._getString(7938);
	}

	private static int getMaxSupportedTextureSize() {
		int i = GlStateManager._getInteger(3379);

		for (int j = Math.max(32768, i); j >= 1024; j >>= 1) {
			GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, null);
			int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
			if (k != 0) {
				return j;
			}
		}

		int jx = Math.max(i, 1024);
		LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", jx);
		return jx;
	}

	@Override
	public int getMaxTextureSize() {
		return this.maxSupportedTextureSize;
	}

	@Override
	public int getUniformOffsetAlignment() {
		return this.uniformOffsetAlignment;
	}

	@Override
	public void clearPipelineCache() {
		for (GlRenderPipeline glRenderPipeline : this.pipelineCache.values()) {
			if (glRenderPipeline.program() != GlProgram.INVALID_PROGRAM) {
				glRenderPipeline.program().close();
			}
		}

		this.pipelineCache.clear();

		for (GlShaderModule glShaderModule : this.shaderCache.values()) {
			if (glShaderModule != GlShaderModule.INVALID_SHADER) {
				glShaderModule.close();
			}
		}

		this.shaderCache.clear();
		String string = GlStateManager._getString(7937);
		if (string.contains("AMD")) {
			amdDummyShaderWorkaround();
		}
	}

	private static void amdDummyShaderWorkaround() {
		int i = GlStateManager.glCreateShader(35633);
		GlStateManager.glShaderSource(i, "#version 150\nvoid main() {\n    gl_Position = vec4(0.0);\n}\n");
		GlStateManager.glCompileShader(i);
		int j = GlStateManager.glCreateShader(35632);
		GlStateManager.glShaderSource(
			j, "#version 150\nlayout(std140) uniform Dummy {\n    float Value;\n};\nout vec4 fragColor;\nvoid main() {\n    fragColor = vec4(0.0);\n}\n"
		);
		GlStateManager.glCompileShader(j);
		int k = GlStateManager.glCreateProgram();
		GlStateManager.glAttachShader(k, i);
		GlStateManager.glAttachShader(k, j);
		GlStateManager.glLinkProgram(k);
		GL31.glGetUniformBlockIndex(k, "Dummy");
		GlStateManager.glDeleteShader(i);
		GlStateManager.glDeleteShader(j);
		GlStateManager.glDeleteProgram(k);
	}

	@Override
	public List<String> getEnabledExtensions() {
		return new ArrayList(this.enabledExtensions);
	}

	@Override
	public void close() {
		this.clearPipelineCache();
	}

	public DirectStateAccess directStateAccess() {
		return this.directStateAccess;
	}

	protected GlRenderPipeline getOrCompilePipeline(RenderPipeline renderPipeline) {
		return (GlRenderPipeline)this.pipelineCache
			.computeIfAbsent(renderPipeline, renderPipeline2 -> this.compilePipeline(renderPipeline, this.defaultShaderSource));
	}

	protected GlShaderModule getOrCompileShader(
		ResourceLocation resourceLocation, ShaderType shaderType, ShaderDefines shaderDefines, BiFunction<ResourceLocation, ShaderType, String> biFunction
	) {
		GlDevice.ShaderCompilationKey shaderCompilationKey = new GlDevice.ShaderCompilationKey(resourceLocation, shaderType, shaderDefines);
		return (GlShaderModule)this.shaderCache.computeIfAbsent(shaderCompilationKey, shaderCompilationKey2 -> this.compileShader(shaderCompilationKey, biFunction));
	}

	public GlRenderPipeline precompilePipeline(RenderPipeline renderPipeline, @Nullable BiFunction<ResourceLocation, ShaderType, String> biFunction) {
		BiFunction<ResourceLocation, ShaderType, String> biFunction2 = biFunction == null ? this.defaultShaderSource : biFunction;
		return (GlRenderPipeline)this.pipelineCache.computeIfAbsent(renderPipeline, renderPipeline2 -> this.compilePipeline(renderPipeline, biFunction2));
	}

	private GlShaderModule compileShader(GlDevice.ShaderCompilationKey shaderCompilationKey, BiFunction<ResourceLocation, ShaderType, String> biFunction) {
		String string = (String)biFunction.apply(shaderCompilationKey.id, shaderCompilationKey.type);
		if (string == null) {
			LOGGER.error("Couldn't find source for {} shader ({})", shaderCompilationKey.type, shaderCompilationKey.id);
			return GlShaderModule.INVALID_SHADER;
		} else {
			String string2 = GlslPreprocessor.injectDefines(string, shaderCompilationKey.defines);
			int i = GlStateManager.glCreateShader(GlConst.toGl(shaderCompilationKey.type));
			GlStateManager.glShaderSource(i, string2);
			GlStateManager.glCompileShader(i);
			if (GlStateManager.glGetShaderi(i, 35713) == 0) {
				String string3 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
				LOGGER.error("Couldn't compile {} shader ({}): {}", shaderCompilationKey.type.getName(), shaderCompilationKey.id, string3);
				return GlShaderModule.INVALID_SHADER;
			} else {
				GlShaderModule glShaderModule = new GlShaderModule(i, shaderCompilationKey.id, shaderCompilationKey.type);
				this.debugLabels.applyLabel(glShaderModule);
				return glShaderModule;
			}
		}
	}

	private GlRenderPipeline compilePipeline(RenderPipeline renderPipeline, BiFunction<ResourceLocation, ShaderType, String> biFunction) {
		GlShaderModule glShaderModule = this.getOrCompileShader(renderPipeline.getVertexShader(), ShaderType.VERTEX, renderPipeline.getShaderDefines(), biFunction);
		GlShaderModule glShaderModule2 = this.getOrCompileShader(
			renderPipeline.getFragmentShader(), ShaderType.FRAGMENT, renderPipeline.getShaderDefines(), biFunction
		);
		if (glShaderModule == GlShaderModule.INVALID_SHADER) {
			LOGGER.error("Couldn't compile pipeline {}: vertex shader {} was invalid", renderPipeline.getLocation(), renderPipeline.getVertexShader());
			return new GlRenderPipeline(renderPipeline, GlProgram.INVALID_PROGRAM);
		} else if (glShaderModule2 == GlShaderModule.INVALID_SHADER) {
			LOGGER.error("Couldn't compile pipeline {}: fragment shader {} was invalid", renderPipeline.getLocation(), renderPipeline.getFragmentShader());
			return new GlRenderPipeline(renderPipeline, GlProgram.INVALID_PROGRAM);
		} else {
			GlProgram glProgram;
			try {
				glProgram = GlProgram.link(glShaderModule, glShaderModule2, renderPipeline.getVertexFormat(), renderPipeline.getLocation().toString());
			} catch (ShaderManager.CompilationException var7) {
				LOGGER.error("Couldn't compile program for pipeline {}: {}", renderPipeline.getLocation(), var7);
				return new GlRenderPipeline(renderPipeline, GlProgram.INVALID_PROGRAM);
			}

			glProgram.setupUniforms(renderPipeline.getUniforms(), renderPipeline.getSamplers());
			this.debugLabels.applyLabel(glProgram);
			return new GlRenderPipeline(renderPipeline, glProgram);
		}
	}

	public VertexArrayCache vertexArrayCache() {
		return this.vertexArrayCache;
	}

	public BufferStorage getBufferStorage() {
		return this.bufferStorage;
	}

	@Environment(EnvType.CLIENT)
	record ShaderCompilationKey(ResourceLocation id, ShaderType type, ShaderDefines defines) {

		public String toString() {
			String string = this.id + " (" + this.type + ")";
			return !this.defines.isEmpty() ? string + " with " + this.defines : string;
		}
	}
}
