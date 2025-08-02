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

package net.fabricmc.fabric.api.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;


public enum TriState {

	FALSE,
	/**
	 * Represents a value that refers to a "default" value, often as a fallback.
	 */
	DEFAULT,

	TRUE;


	public static TriState of(boolean bool) {
		return bool ? TRUE : FALSE;
	}


	public static TriState of(@Nullable Boolean bool) {
		return bool == null ? DEFAULT : of(bool.booleanValue());
	}


	public boolean get() {
		return this == TRUE;
	}


	@Nullable
	public Boolean getBoxed() {
		return this == DEFAULT ? null : this.get();
	}


	public boolean orElse(boolean value) {
		return this == DEFAULT ? value : this.get();
	}


	public boolean orElseGet(BooleanSupplier supplier) {
		return this == DEFAULT ? supplier.getAsBoolean() : this.get();
	}


	public <T> Optional<T> map(BooleanFunction<@Nullable ? extends T> mapper) {
		Objects.requireNonNull(mapper, "Mapper function cannot be null");

		if (this == DEFAULT) {
			return Optional.empty();
		}

		return Optional.ofNullable(mapper.apply(this.get()));
	}


	public <X extends Throwable> boolean orElseThrow(Supplier<X> exceptionSupplier) throws X {
		if (this != DEFAULT) {
			return this.get();
		}

		throw exceptionSupplier.get();
	}
}
