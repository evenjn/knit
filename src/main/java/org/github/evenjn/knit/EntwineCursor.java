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

import java.util.function.BiFunction;

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class EntwineCursor<I, R, M> implements
		Cursor<M> {

	private final Cursor<I> front;

	private final Cursor<R> back;

	private final BiFunction<? super I, ? super R, M> bifunction;

	public EntwineCursor(
			Cursor<I> front,
			Cursor<R> back,
			BiFunction<? super I, ? super R, M> bifunction) {
		this.front = front;
		this.back = back;
		this.bifunction = bifunction;
	}

	@Override
	public M next( )
			throws EndOfCursorException {
		return bifunction.apply( front.next( ), back.next( ) );
	}

}
