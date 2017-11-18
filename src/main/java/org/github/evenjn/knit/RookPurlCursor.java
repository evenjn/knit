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

import org.github.evenjn.lang.Rook;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorRingPurl;
import org.github.evenjn.yarn.EndOfCursorException;

class RingPurlCursor<I, O> implements
		Cursor<O> {

	private final Cursor<? extends I> cursor;

	private final CursorRingPurl<? super I, O> purl;

	private BasicAutoRook internal_rook;

	private final boolean use_internal_rook;

	RingPurlCursor(
			Cursor<I> cursor,
			CursorRingPurl<? super I, O> purl) {
		this.cursor = cursor;
		this.purl = purl;
		this.use_internal_rook = false;
	}

	RingPurlCursor(
			Rook rook,
			Cursor<I> cursor,
			CursorRingPurl<? super I, O> purl) {
		this.use_internal_rook = true;
		rook.hook( new AutoCloseable( ) {

			@Override
			public void close( ) {
				if ( internal_rook != null ) {
					internal_rook.close( );
				}
			}
		} );
		this.cursor = cursor;
		this.purl = purl;
	}

	private Cursor<O> current = null;

	private boolean end = false;

	@Override
	public O next( )
			throws EndOfCursorException {
		for ( ;; ) {
			if ( current != null ) {
				try {
					O next = current.next( );
					return next;
				}
				catch ( EndOfCursorException t ) {
					current = null;
				}
			}
			if ( end ) {
				if ( use_internal_rook && internal_rook != null ) {
					internal_rook.close( );
					internal_rook = null;
				}
				throw EndOfCursorException.neo();
			}

			try {
				I next = cursor.next( );
				if ( use_internal_rook ) {
					if (internal_rook != null) {
						internal_rook.close( );
						internal_rook = null;
					}
					internal_rook = new BasicAutoRook( );
				}
				current = purl.next( internal_rook, next );
			}
			catch ( EndOfCursorException t ) {
				end = true;
				if ( use_internal_rook ) {
					if (internal_rook != null) {
						internal_rook.close( );
						internal_rook = null;
					}
					internal_rook = new BasicAutoRook( );
				}
				current = purl.end( internal_rook );
			}
		}
	}

}
