package org.github.evenjn.knit;

/*
 * Test harness for DiffPatch.java
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
 * Adaptation of work by Neil Fraser at Google Inc.
 *
 * Copyright 2006 Google Inc.
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.github.evenjn.knit.DiffPatch.Diff;

import junit.framework.TestCase;

public class DiffTest
		extends TestCase {

	private DiffPatch<Integer> dmp;

	private DiffPatch.Operation DELETE = DiffPatch.Operation.DELETE;

	private DiffPatch.Operation EQUAL = DiffPatch.Operation.EQUAL;

	private DiffPatch.Operation INSERT = DiffPatch.Operation.INSERT;

	protected void setUp( ) {
		// Create an instance of the DiffMatchPatch object.
		dmp = new DiffPatch<Integer>( );
	}

	// DIFF TEST FUNCTIONS

	public void testDiffCommonPrefix( ) {
		// Detect any common prefix.
		assertEquals( "diff_commonPrefix: Null case.", 0,
				dmp.diff_commonPrefix( "abc", "xyz" ) );

		assertEquals( "diff_commonPrefix: Non-null case.", 4,
				dmp.diff_commonPrefix( "1234abcdef", "1234xyz" ) );

		assertEquals( "diff_commonPrefix: Whole case.", 4,
				dmp.diff_commonPrefix( "1234", "1234xyz" ) );
	}

	public void testDiffCommonSuffix( ) {
		// Detect any common suffix.
		assertEquals( "diff_commonSuffix: Null case.", 0,
				dmp.diff_commonSuffix( "abc", "xyz" ) );

		assertEquals( "diff_commonSuffix: Non-null case.", 4,
				dmp.diff_commonSuffix( "abcdef1234", "xyz1234" ) );

		assertEquals( "diff_commonSuffix: Whole case.", 4,
				dmp.diff_commonSuffix( "1234", "xyz1234" ) );
	}

	public void testDiffCommonOverlap( ) {
		// Detect any suffix/prefix overlap.
		assertEquals( "diff_commonOverlap: Null case.", 0,
				dmp.diff_commonOverlap( "", "abcd" ) );

		assertEquals( "diff_commonOverlap: Whole case.", 3,
				dmp.diff_commonOverlap( "abc", "abcd" ) );

		assertEquals( "diff_commonOverlap: No overlap.", 0,
				dmp.diff_commonOverlap( "123456", "abcd" ) );

		assertEquals( "diff_commonOverlap: Overlap.", 3,
				dmp.diff_commonOverlap( "123456xxx", "xxxabcd" ) );

		// Some overly clever languages (C#) may treat ligatures as equal to their
		// component letters. E.g. U+FB01 == 'fi'
		assertEquals( "diff_commonOverlap: Unicode.", 0,
				dmp.diff_commonOverlap( "fi", "\ufb01i" ) );
	}

	public void testDiffHalfmatch( ) {
		// Detect a halfmatch.
		dmp.Diff_Timeout = 1;
		assertNull( "diff_halfMatch: No match #1.",
				dmp.diff_halfMatch( "1234567890", "abcdef" ) );

		assertNull( "diff_halfMatch: No match #2.",
				dmp.diff_halfMatch( "12345", "23" ) );

		assertArrayEquals( "diff_halfMatch: Single Match #1.", new String[] {
				"12", "90", "a", "z", "345678"
		}, dmp.diff_halfMatch( "1234567890", "a345678z" ) );

		assertArrayEquals( "diff_halfMatch: Single Match #2.", new String[] {
				"a", "z", "12", "90", "345678"
		}, dmp.diff_halfMatch( "a345678z", "1234567890" ) );

		assertArrayEquals( "diff_halfMatch: Single Match #3.", new String[] {
				"abc", "z", "1234", "0", "56789"
		}, dmp.diff_halfMatch( "abc56789z", "1234567890" ) );

		assertArrayEquals( "diff_halfMatch: Single Match #4.", new String[] {
				"a", "xyz", "1", "7890", "23456"
		}, dmp.diff_halfMatch( "a23456xyz", "1234567890" ) );

		assertArrayEquals( "diff_halfMatch: Multiple Matches #1.", new String[] {
				"12123", "123121", "a", "z", "1234123451234"
		}, dmp.diff_halfMatch( "121231234123451234123121", "a1234123451234z" ) );

		assertArrayEquals( "diff_halfMatch: Multiple Matches #2.", new String[] {
				"", "-=-=-=-=-=", "x", "", "x-=-=-=-=-=-=-="
		}, dmp.diff_halfMatch( "x-=-=-=-=-=-=-=-=-=-=-=-=", "xx-=-=-=-=-=-=-=" ) );

		assertArrayEquals( "diff_halfMatch: Multiple Matches #3.", new String[] {
				"-=-=-=-=-=", "", "", "y", "-=-=-=-=-=-=-=y"
		}, dmp.diff_halfMatch( "-=-=-=-=-=-=-=-=-=-=-=-=y", "-=-=-=-=-=-=-=yy" ) );

		// Optimal diff would be -q+x=H-i+e=lloHe+Hu=llo-Hew+y not
		// -qHillo+x=HelloHe-w+Hulloy
		assertArrayEquals( "diff_halfMatch: Non-optimal halfmatch.", new String[] {
				"qHillo", "w", "x", "Hulloy", "HelloHe"
		}, dmp.diff_halfMatch( "qHilloHelloHew", "xHelloHeHulloy" ) );

		dmp.Diff_Timeout = 0;
		assertNull( "diff_halfMatch: Optimal no halfmatch.",
				dmp.diff_halfMatch( "qHilloHelloHew", "xHelloHeHulloy" ) );
	}

	public void testDiffCleanupMerge( ) {
		// Cleanup a messy diff.
		LinkedList<Diff<Integer>> diffs = diffList( );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Null case.", diffList( ), diffs );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( DELETE, "b" ), Diff.fromText(
						INSERT, "c" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals(
				"diff_cleanupMerge: No change case.",
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( DELETE, "b" ), Diff.fromText(
						INSERT, "c" ) ), diffs );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( EQUAL, "b" ), Diff.fromText(
						EQUAL, "c" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Merge equalities.", diffList( Diff.fromText(
				EQUAL, "abc" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "a" ), Diff.fromText( DELETE, "b" ), Diff.fromText(
						DELETE, "c" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Merge deletions.", diffList( Diff.fromText(
				DELETE, "abc" ) ), diffs );

		diffs =
				diffList( Diff.fromText( INSERT, "a" ), Diff.fromText( INSERT, "b" ), Diff.fromText(
						INSERT, "c" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Merge insertions.", diffList( Diff.fromText(
				INSERT, "abc" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "a" ), Diff.fromText( INSERT, "b" ), Diff.fromText(
						DELETE, "c" ), Diff.fromText( INSERT, "d" ), Diff.fromText( EQUAL, "e" ),
						Diff.fromText( EQUAL, "f" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals(
				"diff_cleanupMerge: Merge interweave.",
				diffList( Diff.fromText( DELETE, "ac" ), Diff.fromText( INSERT, "bd" ), Diff.fromText(
						EQUAL, "ef" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "a" ), Diff.fromText( INSERT, "abc" ), Diff.fromText(
						DELETE, "dc" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals(
				"diff_cleanupMerge: Prefix and suffix detection.",
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( DELETE, "d" ), Diff.fromText(
						INSERT, "b" ), Diff.fromText( EQUAL, "c" ) ), diffs );

		diffs =
				diffList( Diff.fromText( EQUAL, "x" ), Diff.fromText( DELETE, "a" ), Diff.fromText(
						INSERT, "abc" ), Diff.fromText( DELETE, "dc" ), Diff.fromText( EQUAL, "y" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals(
				"diff_cleanupMerge: Prefix and suffix detection with equalities.",
				diffList( Diff.fromText( EQUAL, "xa" ), Diff.fromText( DELETE, "d" ), Diff.fromText(
						INSERT, "b" ), Diff.fromText( EQUAL, "cy" ) ), diffs );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( INSERT, "ba" ), Diff.fromText(
						EQUAL, "c" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Slide edit left.",
				diffList( Diff.fromText( INSERT, "ab" ), Diff.fromText( EQUAL, "ac" ) ), diffs );

		diffs =
				diffList( Diff.fromText( EQUAL, "c" ), Diff.fromText( INSERT, "ab" ), Diff.fromText(
						EQUAL, "a" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Slide edit right.",
				diffList( Diff.fromText( EQUAL, "ca" ), Diff.fromText( INSERT, "ba" ) ), diffs );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( DELETE, "b" ), Diff.fromText(
						EQUAL, "c" ), Diff.fromText( DELETE, "ac" ), Diff.fromText( EQUAL, "x" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Slide edit left recursive.",
				diffList( Diff.fromText( DELETE, "abc" ), Diff.fromText( EQUAL, "acx" ) ), diffs );

		diffs =
				diffList( Diff.fromText( EQUAL, "x" ), Diff.fromText( DELETE, "ca" ), Diff.fromText(
						EQUAL, "c" ), Diff.fromText( DELETE, "b" ), Diff.fromText( EQUAL, "a" ) );
		dmp.diff_cleanupMerge( diffs );
		assertEquals( "diff_cleanupMerge: Slide edit right recursive.",
				diffList( Diff.fromText( EQUAL, "xca" ), Diff.fromText( DELETE, "cba" ) ), diffs );
	}

	public void testDiffCleanupSemantic( ) {
		// Cleanup semantically trivial equalities.
		LinkedList<Diff<Integer>> diffs = diffList( );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals( "diff_cleanupSemantic: Null case.", diffList( ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( INSERT, "cd" ), Diff.fromText(
						EQUAL, "12" ), Diff.fromText( DELETE, "e" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals(
				"diff_cleanupSemantic: No elimination #1.",
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( INSERT, "cd" ), Diff.fromText(
						EQUAL, "12" ), Diff.fromText( DELETE, "e" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "abc" ), Diff.fromText( INSERT, "ABC" ),
						Diff.fromText( EQUAL, "1234" ), Diff.fromText( DELETE, "wxyz" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals(
				"diff_cleanupSemantic: No elimination #2.",
				diffList( Diff.fromText( DELETE, "abc" ), Diff.fromText( INSERT, "ABC" ),
						Diff.fromText( EQUAL, "1234" ), Diff.fromText( DELETE, "wxyz" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "a" ), Diff.fromText( EQUAL, "b" ), Diff.fromText(
						DELETE, "c" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals( "diff_cleanupSemantic: Simple elimination.",
				diffList( Diff.fromText( DELETE, "abc" ), Diff.fromText( INSERT, "b" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( EQUAL, "cd" ), Diff.fromText(
						DELETE, "e" ), Diff.fromText( EQUAL, "f" ), Diff.fromText( INSERT, "g" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals( "diff_cleanupSemantic: Backpass elimination.",
				diffList( Diff.fromText( DELETE, "abcdef" ), Diff.fromText( INSERT, "cdfg" ) ),
				diffs );

		diffs =
				diffList( Diff.fromText( INSERT, "1" ), Diff.fromText( EQUAL, "A" ), Diff.fromText(
						DELETE, "B" ), Diff.fromText( INSERT, "2" ), Diff.fromText( EQUAL, "_" ),
						Diff.fromText( INSERT, "1" ), Diff.fromText( EQUAL, "A" ), Diff.fromText( DELETE,
								"B" ), Diff.fromText( INSERT, "2" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals( "diff_cleanupSemantic: Multiple elimination.",
				diffList( Diff.fromText( DELETE, "AB_AB" ), Diff.fromText( INSERT, "1A2_1A2" ) ),
				diffs );

		diffs = diffList( Diff.fromText( DELETE, "abcxx" ), Diff.fromText( INSERT, "xxdef" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals( "diff_cleanupSemantic: No overlap elimination.",
				diffList( Diff.fromText( DELETE, "abcxx" ), Diff.fromText( INSERT, "xxdef" ) ),
				diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "abcxxx" ), Diff.fromText( INSERT, "xxxdef" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals(
				"diff_cleanupSemantic: Overlap elimination.",
				diffList( Diff.fromText( DELETE, "abc" ), Diff.fromText( EQUAL, "xxx" ),
						Diff.fromText( INSERT, "def" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "xxxabc" ), Diff.fromText( INSERT, "defxxx" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals(
				"diff_cleanupSemantic: Reverse overlap elimination.",
				diffList( Diff.fromText( INSERT, "def" ), Diff.fromText( EQUAL, "xxx" ),
						Diff.fromText( DELETE, "abc" ) ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "abcd1212" ),
						Diff.fromText( INSERT, "1212efghi" ), Diff.fromText( EQUAL, "----" ),
						Diff.fromText( DELETE, "A3" ), Diff.fromText( INSERT, "3BC" ) );
		dmp.diff_cleanupSemantic( diffs );
		assertEquals(
				"diff_cleanupSemantic: Two overlap eliminations.",
				diffList( Diff.fromText( DELETE, "abcd" ), Diff.fromText( EQUAL, "1212" ),
						Diff.fromText( INSERT, "efghi" ), Diff.fromText( EQUAL, "----" ), Diff.fromText(
								DELETE, "A" ), Diff.fromText( EQUAL, "3" ), Diff.fromText( INSERT, "BC" ) ),
				diffs );
	}

	public void testDiffCleanupEfficiency( ) {
		// Cleanup operationally trivial equalities.
		dmp.Diff_EditCost = 4;
		LinkedList<Diff<Integer>> diffs = diffList( );
		dmp.diff_cleanupEfficiency( diffs );
		assertEquals( "diff_cleanupEfficiency: Null case.", diffList( ), diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( INSERT, "12" ), Diff.fromText(
						EQUAL, "wxyz" ), Diff.fromText( DELETE, "cd" ), Diff.fromText( INSERT, "34" ) );
		dmp.diff_cleanupEfficiency( diffs );
		assertEquals(
				"diff_cleanupEfficiency: No elimination.",
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( INSERT, "12" ), Diff.fromText(
						EQUAL, "wxyz" ), Diff.fromText( DELETE, "cd" ), Diff.fromText( INSERT, "34" ) ),
				diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( INSERT, "12" ), Diff.fromText(
						EQUAL, "xyz" ), Diff.fromText( DELETE, "cd" ), Diff.fromText( INSERT, "34" ) );
		dmp.diff_cleanupEfficiency( diffs );
		assertEquals(
				"diff_cleanupEfficiency: Four-edit elimination.",
				diffList( Diff.fromText( DELETE, "abxyzcd" ), Diff.fromText( INSERT, "12xyz34" ) ),
				diffs );

		diffs =
				diffList( Diff.fromText( INSERT, "12" ), Diff.fromText( EQUAL, "x" ), Diff.fromText(
						DELETE, "cd" ), Diff.fromText( INSERT, "34" ) );
		dmp.diff_cleanupEfficiency( diffs );
		assertEquals( "diff_cleanupEfficiency: Three-edit elimination.",
				diffList( Diff.fromText( DELETE, "xcd" ), Diff.fromText( INSERT, "12x34" ) ),
				diffs );

		diffs =
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( INSERT, "12" ), Diff.fromText(
						EQUAL, "xy" ), Diff.fromText( INSERT, "34" ), Diff.fromText( EQUAL, "z" ),
						Diff.fromText( DELETE, "cd" ), Diff.fromText( INSERT, "56" ) );
		dmp.diff_cleanupEfficiency( diffs );
		assertEquals(
				"diff_cleanupEfficiency: Backpass elimination.",
				diffList( Diff.fromText( DELETE, "abxyzcd" ), Diff.fromText( INSERT, "12xy34z56" ) ),
				diffs );

		dmp.Diff_EditCost = 5;
		diffs =
				diffList( Diff.fromText( DELETE, "ab" ), Diff.fromText( INSERT, "12" ), Diff.fromText(
						EQUAL, "wxyz" ), Diff.fromText( DELETE, "cd" ), Diff.fromText( INSERT, "34" ) );
		dmp.diff_cleanupEfficiency( diffs );
		assertEquals(
				"diff_cleanupEfficiency: High cost elimination.",
				diffList( Diff.fromText( DELETE, "abwxyzcd" ), Diff.fromText( INSERT, "12wxyz34" ) ),
				diffs );
		dmp.Diff_EditCost = 4;
	}


	public void testDiffText( ) {
		// Compute the source and destination texts.
		LinkedList<Diff<Integer>> diffs =
				diffList( Diff.fromText( EQUAL, "jump" ), Diff.fromText( DELETE, "s" ), Diff.fromText(
						INSERT, "ed" ), Diff.fromText( EQUAL, " over " ), Diff.fromText( DELETE,
						"the" ), Diff.fromText( INSERT, "a" ), Diff.fromText( EQUAL, " lazy" ) );
		assertEquals( "diff_text1:", "jumps over the lazy", dmp.diff_text1( diffs ) );
		assertEquals( "diff_text2:", "jumped over a lazy", dmp.diff_text2( diffs ) );
	}


	public void testDiffXIndex( ) {
		// Translate a location in text1 to text2.
		LinkedList<Diff<Integer>> diffs =
				diffList( Diff.fromText( DELETE, "a" ), Diff.fromText( INSERT, "1234" ),
						Diff.fromText( EQUAL, "xyz" ) );
		assertEquals( "diff_xIndex: Translation on equality.", 5,
				dmp.diff_xIndex( diffs, 2 ) );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( DELETE, "1234" ), Diff.fromText(
						EQUAL, "xyz" ) );
		assertEquals( "diff_xIndex: Translation on deletion.", 1,
				dmp.diff_xIndex( diffs, 3 ) );
	}

	public void testDiffLevenshtein( ) {
		LinkedList<Diff<Integer>> diffs =
				diffList( Diff.fromText( DELETE, "abc" ), Diff.fromText( INSERT, "1234" ),
						Diff.fromText( EQUAL, "xyz" ) );
		assertEquals( "Levenshtein with trailing equality.", 4,
				dmp.diff_levenshtein( diffs ) );

		diffs =
				diffList( Diff.fromText( EQUAL, "xyz" ), Diff.fromText( DELETE, "abc" ),
						Diff.fromText( INSERT, "1234" ) );
		assertEquals( "Levenshtein with leading equality.", 4,
				dmp.diff_levenshtein( diffs ) );

		diffs =
				diffList( Diff.fromText( DELETE, "abc" ), Diff.fromText( EQUAL, "xyz" ),
						Diff.fromText( INSERT, "1234" ) );
		assertEquals( "Levenshtein with middle equality.", 7,
				dmp.diff_levenshtein( diffs ) );
	}

	public void testDiffBisect( ) {
		// Normal.
		String a = "cat";
		String b = "map";
		// Since the resulting diff hasn't been normalized, it would be ok if
		// the insertion and deletion pairs are swapped.
		// If the order changes, tweak this test as required.
		LinkedList<Diff<Integer>> diffs =
				diffList( Diff.fromText( DELETE, "c" ), Diff.fromText( INSERT, "m" ), Diff.fromText(
						EQUAL, "a" ), Diff.fromText( DELETE, "t" ), Diff.fromText( INSERT, "p" ) );
		assertEquals( "diff_bisect: Normal.", diffs,
				dmp.diff_bisect( a, b, Long.MAX_VALUE ) );

		// Timeout.
		diffs = diffList( Diff.fromText( DELETE, "cat" ), Diff.fromText( INSERT, "map" ) );
		assertEquals( "diff_bisect: Timeout.", diffs, dmp.diff_bisect( a, b, 0 ) );
	}

	public void testDiffMain( ) {
		// Perform a trivial diff.
		LinkedList<Diff<Integer>> diffs = diffList( );
		assertEquals( "diff_main: Null case.", diffs, dmp.diff_main( "", "", false ) );

		diffs = diffList( Diff.fromText( EQUAL, "abc" ) );
		assertEquals( "diff_main: Equality.", diffs,
				dmp.diff_main( "abc", "abc", false ) );

		diffs =
				diffList( Diff.fromText( EQUAL, "ab" ), Diff.fromText( INSERT, "123" ), Diff.fromText(
						EQUAL, "c" ) );
		assertEquals( "diff_main: Simple insertion.", diffs,
				dmp.diff_main( "abc", "ab123c", false ) );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( DELETE, "123" ), Diff.fromText(
						EQUAL, "bc" ) );
		assertEquals( "diff_main: Simple deletion.", diffs,
				dmp.diff_main( "a123bc", "abc", false ) );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( INSERT, "123" ), Diff.fromText(
						EQUAL, "b" ), Diff.fromText( INSERT, "456" ), Diff.fromText( EQUAL, "c" ) );
		assertEquals( "diff_main: Two insertions.", diffs,
				dmp.diff_main( "abc", "a123b456c", false ) );

		diffs =
				diffList( Diff.fromText( EQUAL, "a" ), Diff.fromText( DELETE, "123" ), Diff.fromText(
						EQUAL, "b" ), Diff.fromText( DELETE, "456" ), Diff.fromText( EQUAL, "c" ) );
		assertEquals( "diff_main: Two deletions.", diffs,
				dmp.diff_main( "a123b456c", "abc", false ) );

		// Perform a real diff.
		// Switch off the timeout.
		dmp.Diff_Timeout = 0;
		diffs = diffList( Diff.fromText( DELETE, "a" ), Diff.fromText( INSERT, "b" ) );
		assertEquals( "diff_main: Simple case #1.", diffs,
				dmp.diff_main( "a", "b", false ) );

		diffs =
				diffList( Diff.fromText( DELETE, "Apple" ), Diff.fromText( INSERT, "Banana" ),
						Diff.fromText( EQUAL, "s are a" ), Diff.fromText( INSERT, "lso" ), Diff.fromText(
								EQUAL, " fruit." ) );
		
		assertEquals( "diff_main: Simple case #2.", diffs,
				dmp.diff_main( "Apples are a fruit.", "Bananas are also fruit.", false ) );

		diffs =
				diffList( Diff.fromText( DELETE, "a" ), Diff.fromText( INSERT, "\u0680" ),
						Diff.fromText( EQUAL, "x" ), Diff.fromText( DELETE, "\t" ), Diff.fromText( INSERT,
								"\000" ) );
		assertEquals( "diff_main: Simple case #3.", diffs,
				dmp.diff_main( "ax\t", "\u0680x\000", false ) );

		diffs =
				diffList( Diff.fromText( DELETE, "1" ), Diff.fromText( EQUAL, "a" ), Diff.fromText(
						DELETE, "y" ), Diff.fromText( EQUAL, "b" ), Diff.fromText( DELETE, "2" ),
						Diff.fromText( INSERT, "xab" ) );
		assertEquals( "diff_main: Overlap #1.", diffs,
				dmp.diff_main( "1ayb2", "abxab", false ) );

		diffs =
				diffList( Diff.fromText( INSERT, "xaxcx" ), Diff.fromText( EQUAL, "abc" ),
						Diff.fromText( DELETE, "y" ) );
		assertEquals( "diff_main: Overlap #2.", diffs,
				dmp.diff_main( "abcy", "xaxcxabc", false ) );

		diffs =
				diffList( Diff.fromText( DELETE, "ABCD" ), Diff.fromText( EQUAL, "a" ), Diff.fromText(
						DELETE, "=" ), Diff.fromText( INSERT, "-" ), Diff.fromText( EQUAL, "bcd" ),
						Diff.fromText( DELETE, "=" ), Diff.fromText( INSERT, "-" ), Diff.fromText( EQUAL,
								"efghijklmnopqrs" ), Diff.fromText( DELETE, "EFGHIJKLMNOefg" ) );
		assertEquals( "diff_main: Overlap #3.", diffs, dmp.diff_main(
				"ABCDa=bcd=efghijklmnopqrsEFGHIJKLMNOefg", "a-bcd-efghijklmnopqrs",
				false ) );

		diffs =
				diffList( Diff.fromText( INSERT, " " ), Diff.fromText( EQUAL, "a" ), Diff.fromText(
						INSERT, "nd" ), Diff.fromText( EQUAL, " [[Pennsylvania]]" ), Diff.fromText(
						DELETE, " and [[New" ) );
		assertEquals( "diff_main: Large equality.", diffs, dmp.diff_main(
				"a [[Pennsylvania]] and [[New", " and [[Pennsylvania]]", false ) );

		dmp.Diff_Timeout = 0.1f; // 100ms
		String a =
				"`Twas brillig, and the slithy toves\nDid gyre and gimble in the wabe:\nAll mimsy were the borogoves,\nAnd the mome raths outgrabe.\n";
		String b =
				"I am the very model of a modern major general,\nI've information vegetable, animal, and mineral,\nI know the kings of England, and I quote the fights historical,\nFrom Marathon to Waterloo, in order categorical.\n";
		// Increase the text lengths by 1024 times to ensure a timeout.
		for ( int x = 0; x < 10; x++ ) {
			a = a + a;
			b = b + b;
		}
		long startTime = System.currentTimeMillis( );
		dmp.diff_main( a, b );
		long endTime = System.currentTimeMillis( );
		// Test that we took at least the timeout period.
		assertTrue( "diff_main: Timeout min.", dmp.Diff_Timeout * 1000 <= endTime
				- startTime );
		// Test that we didn't take forever (be forgiving).
		// Theoretically this test could fail very occasionally if the
		// OS task swaps or locks up for a second at the wrong moment.
		assertTrue( "diff_main: Timeout max.",
				dmp.Diff_Timeout * 1000 * 2 > endTime - startTime );
		dmp.Diff_Timeout = 0;

		// Test null inputs.
		try {
			String null1 = null;
			dmp.diff_main( null1, null1 );
			fail( "diff_main: Null inputs." );
		}
		catch ( IllegalArgumentException ex ) {
			// Error expected.
		}
	}

	private void assertArrayEquals( String error_msg, Object[] a, Object[] b ) {
		List<Object> list_a = Arrays.asList( a );
		List<Object> list_b = Arrays.asList( b );
		assertEquals( error_msg, list_a, list_b );
	}

	// Private function for quickly building lists of diffs.
	@SafeVarargs
	private static LinkedList<Diff<Integer>> diffList( Diff<Integer> ... diffs ) {
		LinkedList<Diff<Integer>> myDiffList = new LinkedList<Diff<Integer>>( );
		for ( Diff<Integer> myDiff : diffs ) {
			myDiffList.add( myDiff );
		}
		return myDiffList;
	}
}
