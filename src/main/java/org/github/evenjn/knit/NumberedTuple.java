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

import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Tuple;

class NumberedTuple<I> implements
		Tuple<Bi<I, Integer>> {

	private Tuple<I> base;

	NumberedTuple(Tuple<I> base) {
		this.base = base;
	}

	@Override
	public Bi<I, Integer> get( int index ) {
		return BiTray.nu( base.get( index ), index );
	}

	@Override
	public int size( ) {
		return base.size( );
	}

}
