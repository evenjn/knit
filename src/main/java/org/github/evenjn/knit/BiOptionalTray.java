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

/**
 * <p>
 * A {@code BiOptional} object is an object that may have empty slots. The
 * content of an empty slot is {@code null}.
 * </p>
 * 
 * <h2>Design Notes</h2>
 * 
 * <p>
 * Type {@code BiOptionalTray} is opaque, generic and non-descriptive. While it
 * is convenient to use {@code BiOptionalTray} to develop code quickly, we
 * recommend that APIs never expose {@code BiOptionalTray} or types that extends
 * or implement {@code BiOptionalTray}. Instead, we recommend that APIs define
 * their own types that provide context and explanation for the elements of the
 * pair.
 * </p>
 * 
 * <p>
 * In particular, for return types, we recommend that APIs expose a custom
 * interface (that does not extend {@code BiOptionalTray}) and implementation
 * uses a private class extending {@code BiOptionalTray} and implementing the
 * custom interface.
 * </p>
 *
 * @param <F>
 *          The type of the object in the <em>front</em> slot.
 * @param <B>
 *          The type of the object in the <em>back</em> slot.
 * @since 1.0
 */
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

	/**
	 * @return The object in the <em>front</em> slot.
	 * @since 1.0
	 */
	public F front( ) {
		return front;
	}

	/**
	 * @return The object in the <em>back</em> slot.
	 * @since 1.0
	 */
	public B back( ) {
		return back;
	}

	protected BiOptionalTray(F front, B back, boolean has_front,
			boolean has_back) {
		this.front = front;
		this.back = back;
		if ( !has_back && !has_front ) {
			throw new IllegalArgumentException( );
		}
		this.has_back = has_back;
		this.has_front = has_front;
	}

	public static <F, B> BiOptionalTray<F, B> nu( F front, B back,
			boolean has_front,
			boolean has_back ) {
		return new BiOptionalTray<F, B>( front, back, has_front, has_back );
	}

	private F front;

	private B back;

}
