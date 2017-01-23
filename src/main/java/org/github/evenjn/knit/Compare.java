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

import java.util.Optional;

class Compare {

	public static Optional<Integer> compareNullFirst( Object o1, Object o2 ) {
		if ( o1 == null ) {
			if ( o2 == null )
				return Optional.of( 0 );
			return Optional.of( Integer.valueOf( -1 ) );
		}
		if ( o2 == null )
			return Optional.of( Integer.valueOf( 1 ) );
		return Optional.empty( );
	}
}
