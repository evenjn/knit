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

import java.util.LinkedList;

import org.github.evenjn.knit.DiffPatch.Diff;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Equivalencer;
import org.github.evenjn.yarn.Tuple;

class DiffIterator<K> implements
		Cursor<Bi<K, K>> {

	private final KnittingCursor<Diff<K>> kd;

	private final KnittingCursor<K> ka;

	private final KnittingCursor<K> kb;

	public DiffIterator(Tuple<K> a, Tuple<K> b, Equivalencer<K> equivalencer) {
		ka = KnittingCursor.wrap( a );
		kb = KnittingCursor.wrap( b );
		DiffPatch<K> dmp = new DiffPatch<K>( );
		LinkedList<Diff<K>> diffs =
				dmp.diff_main(
						KnittingTuple.wrap( a ).map( x -> x ),
						KnittingTuple.wrap( b ).map( x -> x ),
						equivalencer );
		kd = KnittingCursor.wrap( diffs.iterator( ) );
	}

	private Diff<K> current = null;

	private Bik<K, K> tray = Bik.nu( null, null );

	private int original_start;

	private int original_length;

	private int revised_start;

	private int revised_length;

	@Override
	public Bi<K, K> next( )
			throws EndOfCursorException {

		if ( current == null && kd.hasNext( ) ) {
			current = kd.next( );
			switch ( current.operation ) {
				case INSERT:
					revised_length = current.text.size( );
					break;
				case EQUAL:
					original_length = current.text.size( );
					revised_length = current.text.size( );
					break;
				case DELETE:
					original_length = current.text.size( );
					break;
				default:
					break;
			}
		}

		if ( current != null ) {
			switch ( current.operation ) {
				case INSERT:
					tray = Bik.nu( null, kb.next( ) );
					revised_length--;
					if ( revised_length == 0 ) {
						current = null;
					}
					revised_start++;
					break;
				case EQUAL:
					tray = Bik.nu( ka.next( ), kb.next( ) );
					original_length--;
					if ( original_length == 0 ) {
						current = null;
					}
					revised_length--;
					if ( revised_length == 0 ) {
						current = null;
					}
					original_start++;
					revised_start++;
					break;
				case DELETE:
					tray = Bik.nu( ka.next( ), null );
					original_length--;
					if ( original_length == 0 ) {
						current = null;
					}
					original_start++;
					break;
				default:
					break;
			}
			return tray;

		}
		throw EndOfCursorException.neo();
	}

}
