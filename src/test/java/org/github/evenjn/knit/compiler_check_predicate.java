/**
 *
 * Copyright 2016 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package org.github.evenjn.knit;

import java.util.function.Predicate;

class Animal {
}

class Dog
		extends Animal {
}

class Collie
		extends Dog {
}

public class compiler_check_predicate {

	public static void main( String[] args ) {

		Predicate<Object> p0 = null;
		Predicate<Animal> p1 = null;
		Predicate<Dog> p2 = null;
		// Predicate<Collie> p3 = null;

		// m2(p0);
		m1( p0 );
		m1( p1 );
		m1( p2 );
		// m1(p3);
	}

	public static void m2( Predicate<Dog> p ) {

	}

	public static void m1( Predicate<? super Dog> p ) {

	}
}
