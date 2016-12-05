/**
 *
 * Copyright 2016 Marco Trevisan
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
 * 
 */
package org.github.evenjn.knit;

import java.io.Serializable;

import org.github.evenjn.yarn.Di;

/*
 * Bis objects are invariant.
 */
public final class Bis<A extends Serializable, B extends Serializable>
		implements
		Di<A, B> {

	private static final long serialVersionUID = -7055068816119952085L;

	private Bis(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public static <A extends Serializable, B extends Serializable> Bis<A, B>
			nu( A first,
					B second ) {
		return new Bis<A, B>( first, second );
	}

	private final A first;

	private final B second;

	@Override
	public A front( ) {
		return first;
	}

	@Override
	public B back( ) {
		return second;
	}
}
