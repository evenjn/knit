/**
 *
 * Copyright 2017 Marco Trevisan
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

import java.util.function.Function;

import org.github.evenjn.lang.Tuple;

class MapTuple<I, O> implements
		Tuple<O> {

	private Function<? super I, O> stateless_function;

	private Tuple<I> base;

	MapTuple(Tuple<I> base, Function<? super I, O> stateless_function) {
		this.base = base;
		this.stateless_function = stateless_function;
	}

	@Override
	public O get( int index ) {
		return stateless_function.apply( base.get( index ) );
	}

	@Override
	public int size( ) {
		return base.size( );
	}

}
