package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;

public record StorageValue(ResourceLocation storage, NbtPathArgument.NbtPath path) implements NumberProvider {
	public static final MapCodec<StorageValue> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("storage").forGetter(StorageValue::storage), NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(StorageValue::path)
			)
			.apply(instance, StorageValue::new)
	);

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.STORAGE;
	}

	private Number getNumericTag(LootContext lootContext, Number number) {
		CompoundTag compoundTag = lootContext.getLevel().getServer().getCommandStorage().get(this.storage);

		try {
			List<Tag> list = this.path.get(compoundTag);
			if (list.size() == 1 && list.getFirst() instanceof NumericTag numericTag) {
				return numericTag.box();
			}
		} catch (CommandSyntaxException var7) {
		}

		return number;
	}

	@Override
	public float getFloat(LootContext lootContext) {
		return this.getNumericTag(lootContext, 0.0F).floatValue();
	}

	@Override
	public int getInt(LootContext lootContext) {
		return this.getNumericTag(lootContext, 0).intValue();
	}
}
