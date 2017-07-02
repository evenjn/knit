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
 * 
 * Adaptation of work by Neil Fraser at Google Inc.
 * 
 * Copyright 2006 Google Inc.
 *  
 * http://code.google.com/p/google-diff-match-patch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.github.evenjn.knit;

import org.github.evenjn.yarn.Bi;

public class DiffPair<T,Y> implements
		Bi<T, Y> {

	private EmptySlots missing;

	/**
	 * A code describing whether the front slot is empty, the back slot is empty,
	 * or none is empty.
	 */
	private enum EmptySlots {
		FRONTONLY,
		BACKONLY,
		NONE
	}

	/**
	 * Returns {@code true} if the both slots are filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the both slots are filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasBoth( ) {
		return missing == EmptySlots.NONE;
	}

	/**
	 * Returns {@code true} if the front slot is filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the front slot is filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasFront( ) {
		return missing != EmptySlots.FRONTONLY;
	}

	/**
	 * Returns {@code true} if the back slot is filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the back slot is filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasBack( ) {
		return missing != EmptySlots.BACKONLY;
	}

	@Override
	public T front( ) {
		return front;
	}

	@Override
	public Y back( ) {
		return back;
	}

	public static <T,Y> DiffPair<T,Y> nu( T front, Y back, boolean has_front,
			boolean has_back ) {
		DiffPair<T,Y> dp = new DiffPair<>( );
		dp.front = front;
		dp.back = back;
		if ( !has_back ) {
			dp.missing = EmptySlots.BACKONLY;
		}
		if ( !has_front ) {
			dp.missing = EmptySlots.FRONTONLY;
		}
		if ( has_back && has_front ) {
			dp.missing = EmptySlots.NONE;
		}
		return dp;
	}

	private T front;

	private Y back;

}
