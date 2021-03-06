/**
 *
 * Copyright 2018 Marco Trevisan
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

class NumberedImpl<K> implements Numbered<K> {

	private final K wrapped;

	private final int number;

	public NumberedImpl(K object, int number) {
		this.wrapped = object;
		this.number = number;
	}

	@Override
	public K get( ) {
		return wrapped;
	}

	@Override
	public int getNumber( ) {
		return number;
	}

	public String toString( ) {
		return number + " " + wrapped.toString( );
	}
}
