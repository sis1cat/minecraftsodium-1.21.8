package net.minecraft.server.dialog.action;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;

public record StaticAction(ClickEvent value) implements Action {
	public static final Map<ClickEvent.Action, MapCodec<StaticAction>> WRAPPED_CODECS = Util.make(() -> {
		Map<ClickEvent.Action, MapCodec<StaticAction>> map = new EnumMap(ClickEvent.Action.class);

		for (ClickEvent.Action action : ClickEvent.Action.class.getEnumConstants()) {
			if (action.isAllowedFromServer()) {
				MapCodec<ClickEvent> mapCodec = (MapCodec<ClickEvent>) action.valueCodec();
				map.put(action, mapCodec.xmap(StaticAction::new, StaticAction::value));
			}
		}

		return Collections.unmodifiableMap(map);
	});

	@Override
	public MapCodec<StaticAction> codec() {
		return (MapCodec<StaticAction>)WRAPPED_CODECS.get(this.value.action());
	}

	@Override
	public Optional<ClickEvent> createAction(Map<String, Action.ValueGetter> map) {
		return Optional.of(this.value);
	}
}
