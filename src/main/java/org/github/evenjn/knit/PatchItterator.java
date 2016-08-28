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

import java.util.LinkedList;

import org.github.evenjn.knit.DiffPatch.Diff;
import org.github.evenjn.knit.DiffPatch.Patch;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Tuple;

public class PatchItterator<K> implements
		Itterator<Bi<K, K>> {

	private final KnittingItterator<Patch> kd;

	private final KnittingCursor<K> ka;

	private final KnittingCursor<K> kb;

	public PatchItterator(Tuple<K> a, Tuple<K> b) {
		ka = KnittingTuple.wrap( a ).pull( );
		kb = KnittingTuple.wrap( b ).pull( );
		DiffPatch dmp = new DiffPatch( );
		LinkedList<Diff> diffs =
				dmp.diff_main( KnittingTuple.wrap( a ).map( x -> x ), KnittingTuple
						.wrap( b ).map( x -> x ) );
		LinkedList<Patch> patch_make = dmp.patch_make( diffs );
		kd = KnittingItterator.wrap( patch_make.iterator( ) );
	}

	private Patch current = null;

	private Bi<K, K> tray = new Bi<>( );

	private int original_start;

	private int original_length;

	private int revised_start;

	private int revised_length;

	@Override
	public Bi<K, K> next( )
			throws PastTheEndException {
		int a_pos = ka.soFar( );
		int b_pos = kb.soFar( );

		if ( current == null && kd.hasNext( ) ) {
			current = kd.next( );
			original_start = current.start1;
			original_length = current.length1;
			revised_start = current.start2;
			revised_length = current.length2;
		}

		if ( current != null ) {

			if ( a_pos == original_start && original_length > 0 ) {
				tray.first = ka.next( );
				tray.second = null;
				original_start++;
				original_length--;
				if ( original_length == 0 && revised_length == 0 )
					current = null;
				return tray;
			}

			if ( b_pos == revised_start && revised_length > 0 ) {
				tray.first = null;
				tray.second = kb.next( );
				revised_start++;
				revised_length--;
				if ( original_length == 0 && revised_length == 0 )
					current = null;
				return tray;
			}

		}
		if ( !ka.hasNext( ) && !kb.hasNext( ) )
			throw PastTheEndException.neo;
		tray.first = ka.hasNext( ) ? ka.next( ) : null;
		tray.second = kb.hasNext( ) ? kb.next( ) : null;
		return tray;
	}

}
