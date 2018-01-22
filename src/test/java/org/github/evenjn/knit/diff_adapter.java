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

import static org.github.evenjn.knit.Diff.adiff_bisect;
import static org.github.evenjn.knit.Diff.adiff_cleanupMerge;
import static org.github.evenjn.knit.Diff.adiff_main_nc;

import java.util.ArrayList;
import java.util.LinkedList;

import org.github.evenjn.lang.BasicEquivalencer;
import org.github.evenjn.yarn.Tuple;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

public class diff_adapter {

	public static LinkedList<Diff> diff_main_nc( String text1, String text2,
			long deadline ) {
		LinkedList<DiffOp<Integer, Integer>> diffs = adiff_main_nc( tt( text1 ),
				tt( text2 ), new BasicEquivalencer<Integer, Integer>( ), deadline );
		LinkedList<Diff> result = new LinkedList<Diff>( );
		for ( DiffOp<Integer, Integer> d : diffs ) {
			result.add( encode( d ) );
		}
		return result;
	}

	public static LinkedList<Diff> diff_bisect( String text1, String text2,
			long deadline ) {
		LinkedList<DiffOp<Integer, Integer>> diffs = adiff_bisect( tt( text1 ),
				tt( text2 ), new BasicEquivalencer<Integer, Integer>( ), deadline );
		LinkedList<Diff> result = new LinkedList<Diff>( );
		for ( DiffOp<Integer, Integer> d : diffs ) {
			result.add( encode( d ) );
		}
		return result;
	}

	private static KnittingTuple<Integer> tt( String s ) {
		if ( s == null )
			return null;
		return KnittingTuple.wrap( KnittingCursor.wrap( s.codePoints( ).boxed( ) )
				.collect( new ArrayList<Integer>( ) ) );
	}

	private static DiffOp<Integer, Integer> decode( Diff diff ) {
		switch ( diff.operation ) {
			case DELETE:
				return DiffOp.delete( tt( diff.text ) );
			case INSERT:
				return DiffOp.insert( tt( diff.text ) );
			case EQUAL:
				return DiffOp.equal( tt( diff.text ), tt( diff.text ) );
			default:
				throw new IllegalStateException( );
		}
	}

	private static Diff encode( DiffOp<Integer, Integer> diff ) {
		diff_match_patch.Operation operation;
		Tuple<Integer> tuple = null;
		switch ( diff.getOperation( ) ) {
			case DELETE:
				tuple = diff.getDeletedText( );
				operation = diff_match_patch.Operation.DELETE;
				break;
			case INSERT:
				tuple = diff.getInsertedText( );
				operation = diff_match_patch.Operation.INSERT;
				break;
			case EQUAL:
				tuple = diff.getTextFront( );
				operation = diff_match_patch.Operation.EQUAL;
				break;
			default:
				throw new IllegalStateException( );
		}
		StringBuilder sb = new StringBuilder( );
		for ( int i = 0; i < tuple.size( ); i++ ) {
			Integer cp = tuple.get( i );
			char[] chars = Character.toChars( cp );
			sb.append( chars );
		}
		String text = sb.toString( );
		Diff result = new Diff( operation, text );
		return result;
	}

	public static void diff_cleanupMerge( LinkedList<Diff> diffs ) {
		LinkedList<DiffOp<Integer, Integer>> decoded = new LinkedList<>( );
		for ( Diff d : diffs ) {
			decoded.add( decode( d ) );
		}
		adiff_cleanupMerge( decoded, new BasicEquivalencer<Integer, Integer>( ) );
		diffs.clear( );
		for ( DiffOp<Integer, Integer> d : decoded ) {
			diffs.add( encode( d ) );
		}
	}
}
