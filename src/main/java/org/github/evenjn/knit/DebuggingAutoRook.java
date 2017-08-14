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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;

import org.github.evenjn.yarn.AutoRook;


/**
 * An implementation of {@link org.github.evenjn.yarn.AutoRook AutoRook} that
 * prints a warning on standard error when a resource leak is detected.
 *
 * @since 1.0
 */
public final class DebuggingAutoRook implements
		AutoRook {

	private boolean closed = false;

	private final LinkedList<AutoCloseable> objects_to_close =
			new LinkedList<AutoCloseable>( );

	private final IllegalStateException ise;

	/**
	 * Constructor.
	 */
	public DebuggingAutoRook() {
		try {
			throw new IllegalStateException(
					"This exception may help to identify the owner of this rook." );
		}
		catch ( IllegalStateException ise ) {
			this.ise = ise;
		}
	}

	@Override
	public void close( ) {
		if ( closed ) {
			StringBuilder sb = new StringBuilder( );
			sb.append( "This rook must be closed only once. No more, no less.\n" );
			sb.append(
					"The following stacktrace may help to find where this rook was created.\n" );
			throw new IllegalStateException( sb.toString( ), ise );
		}
		closed = true;
		Collections.reverse( objects_to_close );
		for ( AutoCloseable c : objects_to_close ) {
			try {
				c.close( );
			}
			catch ( Exception e ) {
				throw new IllegalStateException( e );
			}
		}
		objects_to_close.clear( );
	}

	@Override
	public void finalize( ) {
		if ( !closed ) {
			StringBuilder sb = new StringBuilder( );
			sb.append(
					"This rook is undergoing garbage-collection but it has never been closed. \n" );
			sb.append( "This may result in a resource leak.\n" );
			sb.append(
					"The following stacktrace may help to find where this rook was created.\n" );
			ise.printStackTrace( new PrintStream( new OutputStream( ) {

				@Override
				public void write( int b ) {
					sb.append( (char) b );
				}
			} ) );
			System.err.println( sb.toString( ) );
		}
	}

	@Override
	public <T extends AutoCloseable> T hook( T auto_closeable ) {
		objects_to_close.add( auto_closeable );
		return auto_closeable;
	}
}