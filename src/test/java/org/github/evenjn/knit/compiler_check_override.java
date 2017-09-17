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

import java.util.function.Function;
import java.util.function.Predicate;

import org.github.evenjn.yarn.AutoRook;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.CursorMap;
import org.github.evenjn.yarn.CursorRookMap;
import org.github.evenjn.yarn.Rook;
import org.github.evenjn.yarn.RookConsumer;

public class compiler_check_override {

	private static final CursorMap<String, Integer> cursormap =
			new CursorMap<String, Integer>( ) {

				@Override
				public Cursor<Integer> get( String input ) {
					return KnittingCursor.on( 1 );
				}
			};

	private static final CursorRookMap<String, Integer> cursorrookmap =
			new CursorRookMap<String, Integer>( ) {

				@Override
				public Cursor<Integer> get( Rook rook, String input ) {
					return KnittingCursor.on( 1 );
				}
			};

	private static final Predicate<String> predicate =
			new Predicate<String>( ) {

				@Override
				public boolean test( String t ) {
					return false;
				}
			};

	private static final Function<String, Integer> function =
			new Function<String, Integer>( ) {

				@Override
				public Integer apply( String t ) {
					return 0;
				}
			};

	public static void main( String[] args ) {
		// Iterable<String> strings = new Vector<String>();
		KnittingCursable<String> cursable = KnittingCursable.on( );

		cursable.map( function );
		cursable.filter( predicate );
		// cursable.flatmap( x->args );
		// cursable.flatmap( x->strings );

		cursable.map( x -> x.substring( 1 ) );
		cursable.filter( x -> x.isEmpty( ) );
		
		cursable.flatmapCursor( cursormap );
		cursable.flatmapCursor( cursorrookmap );

		cursable.flatmapCursor( x -> KnittingCursor.on( 1 ) );
		cursable.flatmapCursor( ( h, x ) -> KnittingCursor.on( 1 ) );
		
		RookConsumer<String> ff1 = null;
		RookConsumer<Object> ff2 = null;

		try ( AutoRook rook = new BasicAutoRook( ) ) {
			KnittingCursor<String> pull =
					KnittingCursor.wrap( cursable.pull( rook ) );
			pull.peek( System.out::println ).roll( );
			pull.consume( ff1 );
			pull.consume( ff2 );
		}

	}

}
