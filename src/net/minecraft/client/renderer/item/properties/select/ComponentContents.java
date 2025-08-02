package net.minecraft.client.renderer.item.properties.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public record ComponentContents<T>(DataComponentType<T> componentType) implements SelectItemModelProperty<T> {
	private static final SelectItemModelProperty.Type<? extends ComponentContents<?>, ?> TYPE = createType();

	private static <T> SelectItemModelProperty.Type<ComponentContents<T>, T> createType() {
		Codec<DataComponentType<T>> codec = ((net.minecraft.core.Registry<DataComponentType<T>>)(Object)BuiltInRegistries.DATA_COMPONENT_TYPE)
				.byNameCodec()
				.validate(p_392308_ -> p_392308_.isTransient() ? DataResult.error(() -> "Component can't be serialized") : DataResult.success(p_392308_));
		MapCodec<SelectItemModel.UnbakedSwitch<ComponentContents<T>, T>> mapcodec = codec.dispatchMap(
				"component",
				p_391524_ -> p_391524_.property().componentType,
				p_393420_ -> SelectItemModelProperty.Type.createCasesFieldCodec(p_393420_.codecOrThrow())
						.xmap(
								p_393375_ -> new SelectItemModel.UnbakedSwitch<>(
										new ComponentContents<>(p_393420_), p_393375_
								),
								SelectItemModel.UnbakedSwitch::cases
						)
		);
		return new SelectItemModelProperty.Type<>(mapcodec);
	}


	public static <T> SelectItemModelProperty.Type<ComponentContents<T>, T> castType() {
		return (SelectItemModelProperty.Type<ComponentContents<T>, T>)TYPE;
	}

	@Nullable
	@Override
	public T get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext) {
		return itemStack.get(this.componentType);
	}

	@Override
	public SelectItemModelProperty.Type<ComponentContents<T>, T> type() {
		return castType();
	}

	@Override
	public Codec<T> valueCodec() {
		return this.componentType.codecOrThrow();
	}
}
