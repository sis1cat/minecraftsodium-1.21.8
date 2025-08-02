/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.event;

import java.util.function.Function;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;
import net.minecraft.resources.ResourceLocation;

/**
 * Helper for creating {@link Event} classes.
 */
public final class EventFactory {
	private static boolean profilingEnabled = true;

	private EventFactory() { }

	public static boolean isProfilingEnabled() {
		return profilingEnabled;
	}

	/**
	 * Invalidate and re-create all existing "invoker" instances across
	 * events created by this EventFactory. Use this if, for instance,
	 * the profilingEnabled field changes.
	 */
	// TODO: Turn this into an event?
	public static void invalidate() {
		EventFactoryImpl.invalidate();
	}


	public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return EventFactoryImpl.createArrayBacked(type, invokerFactory);
	}


	public static <T> Event<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
		return createArrayBacked(type, listeners -> {
			if (listeners.length == 0) {
				return emptyInvoker;
			} else if (listeners.length == 1) {
				return listeners[0];
			} else {
				return invokerFactory.apply(listeners);
			}
		});
	}


	public static <T> Event<T> createWithPhases(Class<? super T> type, Function<T[], T> invokerFactory, ResourceLocation... defaultPhases) {
		EventFactoryImpl.ensureContainsDefault(defaultPhases);
		EventFactoryImpl.ensureNoDuplicates(defaultPhases);

		Event<T> event = createArrayBacked(type, invokerFactory);

		for (int i = 1; i < defaultPhases.length; ++i) {
			event.addPhaseOrdering(defaultPhases[i-1], defaultPhases[i]);
		}

		return event;
	}

	public static String getHandlerName(Object handler) {
		return handler.getClass().getName();
	}
}
