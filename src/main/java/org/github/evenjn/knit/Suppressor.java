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

public class Suppressor {

	public static RuntimeException quit( Throwable t ) {
		return new RuntimeException( t );
	}
	
	public static void ask( Throwable t ) {
		t.printStackTrace( );
	}

	public static void log( Throwable t ) {
		t.printStackTrace( );
	}
	
	public static <T> T seal(T object) {
		if ( object == null ) {
			throw new IllegalArgumentException( "The argument must be not null." );
		}
		return object;
	}
}
