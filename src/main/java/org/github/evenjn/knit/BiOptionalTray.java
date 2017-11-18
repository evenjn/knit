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

import org.github.evenjn.lang.BiOptional;

public class BiOptionalTray<F, B> implements
		BiOptional<F, B> {

	private boolean has_front = false;

	private boolean has_back = false;

	/**
	 * Returns {@code true} if the both slots are filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the both slots are filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasBoth( ) {
		return has_front && has_back;
	}

	/**
	 * Returns {@code true} if the front slot is filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the front slot is filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasFront( ) {
		return has_front;
	}

	/**
	 * Returns {@code true} if the back slot is filled in, {@code false}
	 * otherwise.
	 * 
	 * @return {@code true} if the back slot is filled in, {@code false}
	 *         otherwise.
	 */
	public boolean hasBack( ) {
		return has_back;
	}

	@Override
	public F front( ) {
		return front;
	}

	@Override
	public B back( ) {
		return back;
	}

	public static <F, B> BiOptionalTray<F, B> nu( F front, B back, boolean has_front,
			boolean has_back ) {
		BiOptionalTray<F, B> dp = new BiOptionalTray<>( );
		dp.front = front;
		dp.back = back;
		if ( !has_back && !has_front ) {
			throw new IllegalArgumentException( );
		}
		dp.has_back = has_back;
		dp.has_front = has_front;
		return dp;
	}

	private F front;

	private B back;

}
