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

import java.util.Iterator;
import java.util.LinkedList;

import org.github.evenjn.lang.BiOptional;
import org.github.evenjn.lang.Equivalencer;
import org.github.evenjn.lang.Tuple;

class DiffIterable<F, B> implements
		Iterable<BiOptional<F, B>> {

	private Tuple<F> front;

	private Tuple<B> back;

	private LinkedList<DiffOp<F, B>> diffs;

	DiffIterable(
			Tuple<F> front, Tuple<B> back,
			Equivalencer<F, B> equivalencer) {
		this.front = front;
		this.back = back;
		long deadline = ( -1 );
		diffs =
				Diff.adiff_main_nc(
						KnittingTuple.wrap( front ),
						KnittingTuple.wrap( back ),
						equivalencer,
						deadline );
	}

	@Override
	public Iterator<BiOptional<F, B>> iterator( ) {
		KnittingCursor<DiffOp<F, B>> cursor =
				KnittingCursor.wrap( diffs.iterator( ) );
		DiffIterator<F, B> diffIterator =
				new DiffIterator<F, B>( KnittingCursor.wrap( front ), KnittingCursor.wrap( back ), cursor );
		return KnittingCursor.wrap( diffIterator ).asIterator( );
	}
}
