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

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.NonExtendable // Should only be extended by fabric API.
public abstract class Event<T> {

	protected volatile T invoker;


	public final T invoker() {
		return invoker;
	}


	public abstract void register(T listener);


	public static final ResourceLocation DEFAULT_PHASE = new ResourceLocation("fabric", "default");


	public void register(ResourceLocation phase, T listener) {
		// This is done to keep compatibility with existing Event subclasses, but they should really not be subclassing Event.
		register(listener);
	}


	public void addPhaseOrdering(ResourceLocation firstPhase, ResourceLocation secondPhase) {
		// This is not abstract to avoid breaking existing Event subclasses, but they should really not be subclassing Event.
	}
}
