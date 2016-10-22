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
import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipMap;
import org.github.evenjn.yarn.SkipMapH;

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

	private static final SkipMap<String, Integer> skipmap =
			new SkipMap<String, Integer>( ) {

				@Override
				public Integer get( String object )
						throws SkipException {
					return 1;
				}
			};

	private static final SkipMapH<String, Integer> skipmaph =
			new SkipMapH<String, Integer>( ) {

				@Override
				public Integer get( Hook hook, String object )
						throws SkipException {
					return 2;
				}
			};

	public static <K> K skip( K object )
			throws SkipException {
		throw SkipException.neo;
	}

	public static <K> K skip( Hook hook, K object )
			throws SkipException {
		throw SkipException.neo;
	}

	public static void main( String[] args ) {
		KnittingCursable<String> cursable = null;

		cursable.map( function );
		cursable.filter( predicate );
		cursable.skipmap( skipmap );
		cursable.skipmap( skipmaph );

		cursable.map( x -> x.substring( 1 ) );
		cursable.filter( x -> x.isEmpty( ) );
		cursable.skipmap( x -> skip( x ) );
		cursable.skipmap( ( h, x ) -> skip( h, x ) );

		Function<Hook, Consumer<String>> ff1 = null;
		Function<Hook, Consumer<Object>> ff2 = null;

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursor<String> pull = cursable.pull( hook );
			pull.consume( System.out::println );
			pull.consumeHook( ff1 );
			pull.consumeHook( ff2 );
		}

	}
}
