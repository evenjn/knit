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

/**
 * <p>
 * A {@code Bi} object holds two references to objects, referred to as the
 * <em>front</em> and the <em>back</em>. There are no restrictions on the
 * references. One or both references may be null, and both references may point
 * to the same object. The referred objects to need not be immutable or satisfy
 * any particular constraint.
 * </p>
 *
 * @param <A>
 *          The type of the object in the <em>front</em> slot.
 * @param <B>
 *          The type of the object in the <em>back</em> slot.
 * 
 * @since 1.0
 */
public final class Bi<A, B> {

	private Bi() {
	}

	/**
	 * Static factory method.
	 * 
	 * @param <A>
	 *          The type of the object in the <em>front</em> slot.
	 * @param <B>
	 *          The type of the object in the <em>back</em> slot.
	 * @param front
	 *          the element to put in the <em>front</em> slot.
	 * @param back
	 *          the element to put in the <em>back</em> slot.
	 * @return a new {@code Bi} object providing access to the argument objects.
	 * @since 1.0
	 */
	public static <A, B> Bi<A, B> nu( A front, B back ) {
		return new Bi<A, B>( ).set( front, back );
	}

	private A front;

	private B back;

	/**
	 * Replaces the objects currently held in the <em>front</em> and <em>back</em>
	 * slot with the argument objects.
	 * 
	 * @param front
	 *          the element to put in the <em>front</em> slot.
	 * @param back
	 *          the element to put in the <em>back</em> slot.
	 * @return this {@code Bi} object, modified to provide access to the argument
	 *         objects.
	 * @since 1.0
	 */
	public Bi<A, B> set( A front, B back ) {
		this.front = front;
		this.back = back;
		return this;
	}

	/**
	 * 
	 * @return the object in the <em>front</em> slot.
	 * @since 1.0
	 */
	public A front( ) {
		return front;
	}

	/**
	 * 
	 * @return the object in the <em>back</em> slot.
	 * @since 1.0
	 */
	public B back( ) {
		return back;
	}
}
