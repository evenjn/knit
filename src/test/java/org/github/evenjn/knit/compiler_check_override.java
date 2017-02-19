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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Hook;

public class compiler_check_override {

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

		Function<Hook, Consumer<String>> ff1 = null;
		Function<Hook, Consumer<Object>> ff2 = null;

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursor<String> pull =
					KnittingCursor.wrap( cursable.pull( hook ) );
			pull.peek( System.out::println ).roll( );
			pull.consume( ff1 );
			pull.consume( ff2 );
		}

	}
}
