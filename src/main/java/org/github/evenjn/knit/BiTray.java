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
 */
package org.github.evenjn.knit;

import org.github.evenjn.lang.Bi;

/**
 * An implementation of {@link org.github.evenjn.yarn.Bi Bi} that provides a
 * method to {@link #set(Object, Object) set} elements.
 *
 * <h2>Design Notes</h2>
 * 
 * <p>
 * Type {@code BiTray} is opaque, generic and non-descriptive. While it is
 * convenient to use {@code BiTray} to develop code quickly, we recommend that
 * APIs never expose {@code BiTray} or types that extends or implement
 * {@code BiTray}. Instead, we recommend that APIs define their own types that
 * provide context and explanation for the elements of the pair.
 * </p>
 * 
 * <p>
 * In particular, for return types, we recommend that APIs expose a custom
 * interface (that does not extend {@code BiTray}) and implementation
 * uses a private class extending {@code BiTray} and implementing the
 * custom interface.
 * </p>
 * 
 * @param <F>
 *          The type of the object in the <em>front</em> slot.
 * @param <B>
 *          The type of the object in the <em>back</em> slot..
 * @since 1.0
 */
public class BiTray<F, B> implements
		Bi<F, B> {

	protected BiTray(F front, B back) {
		this.front = front;
		this.back = back;
	}

	/**
	 * Static factory method.
	 * 
	 * @param <F>
	 *          The type of the object in the <em>front</em> slot.
	 * @param <B>
	 *          The type of the object in the <em>back</em> slot.
	 * @param front
	 *          the element to put in the <em>front</em> slot.
	 * @param back
	 *          the element to put in the <em>back</em> slot.
	 * @return a new {@code BiTray} object providing access to the argument
	 *         objects.
	 * @since 1.0
	 */
	public static <F, B> BiTray<F, B> nu( F front, B back ) {
		return new BiTray<F, B>( front, back );
	}

	private F front;

	private B back;

	/**
	 * Replaces the objects currently held in the <em>front</em> and <em>back</em>
	 * slot with the argument objects.
	 * 
	 * @param front
	 *          the element to put in the <em>front</em> slot.
	 * @param back
	 *          the element to put in the <em>back</em> slot.
	 * @return this {@code BiTray} object, modified to provide access to the
	 *         argument objects.
	 * @since 1.0
	 */
	public BiTray<F, B> set( F front, B back ) {
		this.front = front;
		this.back = back;
		return this;
	}

	/**
	 * @return the object in the <em>front</em> slot.
	 * @since 1.0
	 */
	@Override
	public F front( ) {
		return front;
	}

	/**
	 * @return the object in the <em>back</em> slot.
	 * @since 1.0
	 */
	@Override
	public B back( ) {
		return back;
	}

	/**
	 * Returns a view of this object as a {@link org.github.evenjn.yarn.Bi Bi}.
	 * 
	 * @return a view of this object as a {@link org.github.evenjn.yarn.Bi Bi}.
	 * @since 1.0
	 */
	public Bi<F, B> asBi( ) {
		return this;
	}
}
