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

import org.github.evenjn.yarn.BiOption;
import org.github.evenjn.yarn.Equivalencer;
import org.github.evenjn.yarn.Tuple;

class DiffIterable<F, B> implements
		Iterable<BiOption<F, B>> {

	private KnittingCursor<F> kc_front;

	private KnittingCursor<B> kc_back;

	private LinkedList<DiffOp<F, B>> diffs;

	DiffIterable(
			Tuple<F> front, Tuple<B> back,
			Equivalencer<F, B> equivalencer) {
		kc_front = KnittingCursor.wrap( front );
		kc_back = KnittingCursor.wrap( back );
		long deadline = ( -1 );
		diffs =
				Diff.adiff_main_nc(
						KnittingTuple.wrap( front ),
						KnittingTuple.wrap( back ),
						equivalencer,
						deadline );
	}

	@Override
	public Iterator<BiOption<F, B>> iterator( ) {
		KnittingCursor<DiffOp<F, B>> cursor =
				KnittingCursor.wrap( diffs.iterator( ) );
		DiffIterator<F, B> diffIterator =
				new DiffIterator<F, B>( kc_front, kc_back, cursor );
		return KnittingCursor.wrap( diffIterator ).asIterator( );
	}
}
