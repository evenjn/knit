/**
 *
 * Copyright 2018 Marco Trevisan
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

import java.util.function.BiFunction;

import org.github.evenjn.yarn.Tuple;

class EntwineTuple<I, R, M> implements
		Tuple<M> {

	private final Tuple<I> front;

	private final Tuple<R> back;

	private final BiFunction<? super I, ? super R, M> bifunction;

	private final int size;

	public EntwineTuple(Tuple<I> front, Tuple<R> back,
			BiFunction<? super I, ? super R, M> bifunction) {
		this.front = front;
		this.back = back;
		this.bifunction = bifunction;
		this.size = front.size( ) <= back.size( ) ? front.size( ) : back.size( );
	}

	@Override
	public M get( int index )
			throws IllegalArgumentException {
		if ( index < 0 || index >= size ) {
			throw new IllegalArgumentException( );
		}
		return bifunction.apply( front.get( index ), back.get( index ) );
	}

	@Override
	public int size( ) {
		return size;
	}

}
