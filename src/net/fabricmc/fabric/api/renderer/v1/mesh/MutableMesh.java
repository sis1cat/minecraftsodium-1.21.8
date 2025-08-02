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

package net.fabricmc.fabric.api.renderer.v1.mesh;

import java.util.function.Consumer;


public interface MutableMesh extends MeshView {

	QuadEmitter emitter();

	/**
	 * Access all the quads encoded in this mesh and modify them as necessary. The quad instance sent to the consumer
	 * should never be retained outside the current call to the consumer.
	 *
	 * <p>Nesting calls to this method on the same mesh is <b>not</b> allowed.
	 */
	void forEachMutable(Consumer<? super MutableQuadView> action);


	Mesh immutableCopy();

	/**
	 * Resets this mesh to an empty state with zero quads, effectively clearing all existing quads.
	 */
	void clear();
}
