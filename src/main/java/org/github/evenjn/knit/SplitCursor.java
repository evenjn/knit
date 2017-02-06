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

import java.util.function.Predicate;

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

class SplitCursor<K> implements
		Cursor<KnittingCursor<K>> {

	private final Predicate<K> predicate;

	private final KnittingCursor<K> kc;

	private KnittingCursor<K> mono = null;

	private SegmentCursor<K> sc = null;

	public SplitCursor(Cursor<K> cursor, Predicate<K> predicate) {
		this.predicate = predicate;
		this.kc = KnittingCursor.wrap( cursor );
	}

	@Override
	public KnittingCursor<K> next( )
			throws EndOfCursorException {
		if ( mono != null ) {
			while ( mono.hasNext( ) ) {
				mono.next( );
			}
			boolean es = sc.endOnSeparator( );
			mono = null;
			sc = null;
			if ( !es && !kc.hasNext( ) ) {
				throw EndOfCursorException.neo();
			}
		}
		sc = new SegmentCursor<>( kc, predicate );
		mono = KnittingCursor.wrap( sc );
		return mono;
	}
}

class SegmentCursor<K> implements
		Cursor<K> {

	private final Predicate<K> predicate;

	private KnittingCursor<K> kc;

	private boolean end_on_separator = false;

	public SegmentCursor(KnittingCursor<K> cursor, Predicate<K> predicate) {
		this.predicate = predicate;
		if ( cursor == null )
			throw new IllegalArgumentException( );
		if ( predicate == null )
			throw new IllegalArgumentException( );
		this.kc = cursor;
	}

	@Override
	public K next( )
			throws EndOfCursorException {
		if ( kc == null || !kc.hasNext( ) ) {
			kc = null;
			throw EndOfCursorException.neo();
		}
		K next = kc.next( );
		boolean is_separator = predicate.test( next );
		if ( is_separator ) {
			kc = null;
			end_on_separator = true;
			throw EndOfCursorException.neo();
		}
		return next;
	}

	public boolean endOnSeparator( ) {
		return end_on_separator;
	}
}
