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
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Tuple;

public class DiffIterator<K> implements
		Itterator<Bi<K, K>> {

	private final KnittingItterator<Diff> kd;

	private final KnittingCursor<K> ka;

	private final KnittingCursor<K> kb;

	public DiffIterator(Tuple<K> a, Tuple<K> b) {
		ka = KnittingTuple.wrap( a ).pull( );
		kb = KnittingTuple.wrap( b ).pull( );
		DiffPatch dmp = new DiffPatch( );
		LinkedList<Diff> diffs =
				dmp.diff_main( KnittingTuple.wrap( a ).map( x -> x ), KnittingTuple
						.wrap( b ).map( x -> x ) );
		kd = KnittingItterator.wrap( diffs.iterator( ) );
	}

	private Diff current = null;

	private Bi<K, K> tray = new Bi<>( );

	private int original_start;

	private int original_length;

	private int revised_start;

	private int revised_length;

	@Override
	public Bi<K, K> next( )
			throws PastTheEndException {

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
					tray.set( null, kb.next( ) );
					revised_length--;
					if ( revised_length == 0 ) {
						current = null;
					}
					revised_start++;
					break;
				case EQUAL:
					tray.set( ka.next( ), kb.next( ) );
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
					tray.set( ka.next( ), null );
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
		throw PastTheEndException.neo;
	}

}
