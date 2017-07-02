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

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Equivalencer;
import org.github.evenjn.yarn.Tuple;

class DiffIterator<F, B> implements
		Cursor<DiffPair<F, B>> {

	private final KnittingCursor<DiffOp<F, B>> kd;

	private final KnittingCursor<F> ka;

	private final KnittingCursor<B> kb;

	public DiffIterator(Tuple<F> a, Tuple<B> b, Equivalencer<F, B> equivalencer) {
		ka = KnittingCursor.wrap( a );
		kb = KnittingCursor.wrap( b );
		DiffPatch<F, B> dmp = new DiffPatch<F, B>( );
		LinkedList<DiffOp<F, B>> diffs =
				dmp.diff_main(
						KnittingTuple.wrap( a ),
						KnittingTuple.wrap( b ),
						equivalencer );
		kd = KnittingCursor.wrap( diffs.iterator( ) );
	}

	private DiffOp<F, B> current = null;

	private DiffPair<F, B> tray;

	private int original_start;

	private int original_length;

	private int revised_start;

	private int revised_length;

	@Override
	public DiffPair<F, B> next( )
			throws EndOfCursorException {

		if ( current == null && kd.hasNext( ) ) {
			current = kd.next( );
			switch ( current.operation ) {
				case INSERT:
					revised_length = current.getTextBack( ).size( );
					break;
				case EQUAL:
					original_length = current.getTextFront( ).size( );
					revised_length = current.getTextBack( ).size( );
					break;
				case DELETE:
					original_length = current.getTextFront( ).size( );
					break;
				default:
					break;
			}
		}

		if ( current != null ) {
			switch ( current.operation ) {
				case INSERT:
					tray = DiffPair.nu( null, kb.next( ), false, true );
					revised_length--;
					if ( revised_length == 0 ) {
						current = null;
					}
					revised_start++;
					break;
				case EQUAL:
					tray = DiffPair.nu( ka.next( ), kb.next( ), true, true );
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
					tray = DiffPair.nu( ka.next( ), null, true, false );
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
		throw EndOfCursorException.neo( );
	}

}
