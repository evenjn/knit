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
 * A Suppressor provides methods to deal with checked exceptions by wrapping
 * them into runtime exceptions.
 * 
 *
 */
public class Suppressor {

	private static Suppressor global_suppressor = new Suppressor( );

	/**
	 * Replaces the default suppressor with a custom one.
	 * 
	 * @param custom_suppressor
	 *          the suppressor carrying out custom behavior.
	 */
	public static void setCustomSuppressor( Suppressor custom_suppressor ) {
		Suppressor.global_suppressor = custom_suppressor;
	}

	protected RuntimeException doQuit( Throwable throwable ) {
		return new RuntimeException( throwable );
	}

	protected void doLog( Throwable throwable ) {
		throwable.printStackTrace( );
	}

	/**
	 * The invoker uses this method to communicate that an exception was thrown
	 * and the computation will not continue.
	 * 
	 * @param throwable
	 *          any Throwable.
	 * @return a runtime exception wrapping the argument throwable.
	 */
	public static RuntimeException quit( Throwable throwable ) {
		return global_suppressor.doQuit( throwable );
	}

	/**
	 * The invoker uses this method to communicate that an exception was thrown
	 * but the computation will continue.
	 * 
	 * @param throwable
	 *          any Throwable.
	 */
	public static void log( Throwable throwable ) {
		global_suppressor.doLog( throwable );
	}

	@Deprecated
	public static <T> T seal( T object ) {
		if ( object == null ) {
			throw new IllegalArgumentException( "The argument must be not null." );
		}
		return object;
	}
}
