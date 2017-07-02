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
import java.util.LinkedList;

import junit.framework.TestCase;

public class DiffTest
		extends TestCase {

	private DiffPatch<Integer,Integer> dmp;

	protected void setUp( ) {
		// Create an instance of the DiffMatchPatch object.
		dmp = new DiffPatch<Integer,Integer>( );
	}

	// DIFF TEST FUNCTIONS

	public void testDiffCommonPrefix( ) {
		// Detect any common prefix.
		assertEquals( "diff_commonPrefix: Null case.", 0,
				DiffPatch.diff_commonPrefix( "abc", "xyz" ) );

		assertEquals( "diff_commonPrefix: Non-null case.", 4,
				DiffPatch.diff_commonPrefix( "1234abcdef", "1234xyz" ) );

		assertEquals( "diff_commonPrefix: Whole case.", 4,
				DiffPatch.diff_commonPrefix( "1234", "1234xyz" ) );
	}

	public void testDiffCommonSuffix( ) {
		// Detect any common suffix.
		assertEquals( "diff_commonSuffix: Null case.", 0,
				DiffPatch.diff_commonSuffix( "abc", "xyz" ) );

		assertEquals( "diff_commonSuffix: Non-null case.", 4,
				DiffPatch.diff_commonSuffix( "abcdef1234", "xyz1234" ) );

		assertEquals( "diff_commonSuffix: Whole case.", 4,
				DiffPatch.diff_commonSuffix( "1234", "xyz1234" ) );
	}

	public void testDiffCommonOverlap( ) {
		// Detect any suffix/prefix overlap.
		assertEquals( "diff_commonOverlap: Null case.", 0,
				DiffPatch.diff_commonOverlap( "", "abcd" ) );

		assertEquals( "diff_commonOverlap: Whole case.", 3,
				DiffPatch.diff_commonOverlap( "abc", "abcd" ) );

		assertEquals( "diff_commonOverlap: No overlap.", 0,
				DiffPatch.diff_commonOverlap( "123456", "abcd" ) );

		assertEquals( "diff_commonOverlap: Overlap.", 3,
				DiffPatch.diff_commonOverlap( "123456xxx", "xxxabcd" ) );

		// Some overly clever languages (C#) may treat ligatures as equal to their
		// component letters. E.g. U+FB01 == 'fi'
		assertEquals( "diff_commonOverlap: Unicode.", 0,
				DiffPatch.diff_commonOverlap( "fi", "\ufb01i" ) );
	}

	public void testDiffCleanupMerge( ) {
		// Cleanup a messy diff.
		LinkedList<DiffOp<Integer,Integer>> diffs = diffList( );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Null case.", diffList( ), diffs );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "b" ), DiffOpInt.insertFromText( "c" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupMerge: No change case.",
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "b" ), DiffOpInt.insertFromText( "c" ) ), diffs );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.equalFromText( "b" ), DiffOpInt.equalFromText( "c" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Merge equalities.", diffList( DiffOpInt.equalFromText( "abc" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "a" ), DiffOpInt.deleteFromText( "b" ), DiffOpInt.deleteFromText( "c" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Merge deletions.", diffList( DiffOpInt.deleteFromText( "abc" ) ), diffs );

		diffs =
				diffList( DiffOpInt.insertFromText( "a" ), DiffOpInt.insertFromText( "b" ), DiffOpInt.insertFromText( "c" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Merge insertions.", diffList( DiffOpInt.insertFromText( "abc" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "a" ), DiffOpInt.insertFromText( "b" ), DiffOpInt.deleteFromText( "c" ), DiffOpInt.insertFromText( "d" ), DiffOpInt.equalFromText( "e" ),
						DiffOpInt.equalFromText( "f" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupMerge: Merge interweave.",
				diffList( DiffOpInt.deleteFromText( "ac" ), DiffOpInt.insertFromText( "bd" ), DiffOpInt.equalFromText( "ef" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "a" ), DiffOpInt.insertFromText( "abc" ), DiffOpInt.deleteFromText( "dc" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupMerge: Prefix and suffix detection.",
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "d" ), DiffOpInt.insertFromText( "b" ), DiffOpInt.equalFromText( "c" ) ), diffs );

		diffs =
				diffList( DiffOpInt.equalFromText( "x" ), DiffOpInt.deleteFromText( "a" ), DiffOpInt.insertFromText( "abc" ), DiffOpInt.deleteFromText( "dc" ), DiffOpInt.equalFromText( "y" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupMerge: Prefix and suffix detection with equalities.",
				diffList( DiffOpInt.equalFromText( "xa" ), DiffOpInt.deleteFromText( "d" ), DiffOpInt.insertFromText( "b" ), DiffOpInt.equalFromText( "cy" ) ), diffs );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.insertFromText( "ba" ), DiffOpInt.equalFromText( "c" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Slide edit left.",
				diffList( DiffOpInt.insertFromText( "ab" ), DiffOpInt.equalFromText( "ac" ) ), diffs );

		diffs =
				diffList( DiffOpInt.equalFromText( "c" ), DiffOpInt.insertFromText( "ab" ), DiffOpInt.equalFromText( "a" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Slide edit right.",
				diffList( DiffOpInt.equalFromText( "ca" ), DiffOpInt.insertFromText( "ba" ) ), diffs );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "b" ), DiffOpInt.equalFromText( "c" ), DiffOpInt.deleteFromText( "ac" ), DiffOpInt.equalFromText( "x" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Slide edit left recursive.",
				diffList( DiffOpInt.deleteFromText( "abc" ), DiffOpInt.equalFromText( "acx" ) ), diffs );

		diffs =
				diffList( DiffOpInt.equalFromText( "x" ), DiffOpInt.deleteFromText( "ca" ), DiffOpInt.equalFromText( "c" ), DiffOpInt.deleteFromText( "b" ), DiffOpInt.equalFromText( "a" ) );
		dmp.diff_cleanupMerge( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupMerge: Slide edit right recursive.",
				diffList( DiffOpInt.equalFromText( "xca" ), DiffOpInt.deleteFromText( "cba" ) ), diffs );
	}

	public void testDiffCleanupSemantic( ) {
		// Cleanup semantically trivial equalities.
		LinkedList<DiffOp<Integer,Integer>> diffs = diffList( );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupSemantic: Null case.", diffList( ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.insertFromText( "cd" ), DiffOpInt.equalFromText( "12" ), DiffOpInt.deleteFromText( "e" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupSemantic: No elimination #1.",
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.insertFromText( "cd" ), DiffOpInt.equalFromText( "12" ), DiffOpInt.deleteFromText( "e" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "abc" ), DiffOpInt.insertFromText( "ABC" ),
						DiffOpInt.equalFromText( "1234" ), DiffOpInt.deleteFromText( "wxyz" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupSemantic: No elimination #2.",
				diffList( DiffOpInt.deleteFromText( "abc" ), DiffOpInt.insertFromText( "ABC" ),
						DiffOpInt.equalFromText( "1234" ), DiffOpInt.deleteFromText( "wxyz" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "a" ), DiffOpInt.equalFromText( "b" ), DiffOpInt.deleteFromText( "c" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupSemantic: Simple elimination.",
				diffList( DiffOpInt.deleteFromText( "abc" ), DiffOpInt.insertFromText( "b" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.equalFromText( "cd" ), DiffOpInt.deleteFromText( "e" ), DiffOpInt.equalFromText( "f" ), DiffOpInt.insertFromText( "g" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupSemantic: Backpass elimination.",
				diffList( DiffOpInt.deleteFromText( "abcdef" ), DiffOpInt.insertFromText( "cdfg" ) ),
				diffs );

		diffs =
				diffList( DiffOpInt.insertFromText( "1" ), DiffOpInt.equalFromText( "A" ), DiffOpInt.deleteFromText( "B" ), DiffOpInt.insertFromText( "2" ), DiffOpInt.equalFromText( "_" ),
						DiffOpInt.insertFromText( "1" ), DiffOpInt.equalFromText( "A" ), DiffOpInt.deleteFromText(
								"B" ), DiffOpInt.insertFromText( "2" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupSemantic: Multiple elimination.",
				diffList( DiffOpInt.deleteFromText( "AB_AB" ), DiffOpInt.insertFromText( "1A2_1A2" ) ),
				diffs );

		diffs = diffList( DiffOpInt.deleteFromText( "abcxx" ), DiffOpInt.insertFromText( "xxdef" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupSemantic: No overlap elimination.",
				diffList( DiffOpInt.deleteFromText( "abcxx" ), DiffOpInt.insertFromText( "xxdef" ) ),
				diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "abcxxx" ), DiffOpInt.insertFromText( "xxxdef" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupSemantic: Overlap elimination.",
				diffList( DiffOpInt.deleteFromText( "abc" ), DiffOpInt.equalFromText( "xxx" ),
						DiffOpInt.insertFromText( "def" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "xxxabc" ), DiffOpInt.insertFromText( "defxxx" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupSemantic: Reverse overlap elimination.",
				diffList( DiffOpInt.insertFromText( "def" ), DiffOpInt.equalFromText( "xxx" ),
						DiffOpInt.deleteFromText( "abc" ) ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "abcd1212" ),
						DiffOpInt.insertFromText( "1212efghi" ), DiffOpInt.equalFromText( "----" ),
						DiffOpInt.deleteFromText( "A3" ), DiffOpInt.insertFromText( "3BC" ) );
		dmp.diff_cleanupSemantic( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupSemantic: Two overlap eliminations.",
				diffList( DiffOpInt.deleteFromText( "abcd" ), DiffOpInt.equalFromText( "1212" ),
						DiffOpInt.insertFromText( "efghi" ), DiffOpInt.equalFromText( "----" ), DiffOpInt.deleteFromText( "A" ), DiffOpInt.equalFromText( "3" ), DiffOpInt.insertFromText( "BC" ) ),
				diffs );
	}

	public void testDiffCleanupEfficiency( ) {
		// Cleanup operationally trivial equalities.
		dmp.Diff_EditCost = 4;
		LinkedList<DiffOp<Integer,Integer>> diffs = diffList( );
		dmp.diff_cleanupEfficiency( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupEfficiency: Null case.", diffList( ), diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.insertFromText( "12" ), DiffOpInt.equalFromText( "wxyz" ), DiffOpInt.deleteFromText( "cd" ), DiffOpInt.insertFromText( "34" ) );
		dmp.diff_cleanupEfficiency( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupEfficiency: No elimination.",
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.insertFromText( "12" ), DiffOpInt.equalFromText( "wxyz" ), DiffOpInt.deleteFromText( "cd" ), DiffOpInt.insertFromText( "34" ) ),
				diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.insertFromText( "12" ), DiffOpInt.equalFromText( "xyz" ), DiffOpInt.deleteFromText( "cd" ), DiffOpInt.insertFromText( "34" ) );
		dmp.diff_cleanupEfficiency( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupEfficiency: Four-edit elimination.",
				diffList( DiffOpInt.deleteFromText( "abxyzcd" ), DiffOpInt.insertFromText( "12xyz34" ) ),
				diffs );

		diffs =
				diffList( DiffOpInt.insertFromText( "12" ), DiffOpInt.equalFromText( "x" ), DiffOpInt.deleteFromText( "cd" ), DiffOpInt.insertFromText( "34" ) );
		dmp.diff_cleanupEfficiency( diffs, DiffPatch::equal_or_both_null );
		assertEquals( "diff_cleanupEfficiency: Three-edit elimination.",
				diffList( DiffOpInt.deleteFromText( "xcd" ), DiffOpInt.insertFromText( "12x34" ) ),
				diffs );

		diffs =
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.insertFromText( "12" ), DiffOpInt.equalFromText( "xy" ), DiffOpInt.insertFromText( "34" ), DiffOpInt.equalFromText( "z" ),
						DiffOpInt.deleteFromText( "cd" ), DiffOpInt.insertFromText( "56" ) );
		dmp.diff_cleanupEfficiency( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupEfficiency: Backpass elimination.",
				diffList( DiffOpInt.deleteFromText( "abxyzcd" ), DiffOpInt.insertFromText( "12xy34z56" ) ),
				diffs );

		dmp.Diff_EditCost = 5;
		diffs =
				diffList( DiffOpInt.deleteFromText( "ab" ), DiffOpInt.insertFromText( "12" ), DiffOpInt.equalFromText( "wxyz" ), DiffOpInt.deleteFromText( "cd" ), DiffOpInt.insertFromText( "34" ) );
		dmp.diff_cleanupEfficiency( diffs, DiffPatch::equal_or_both_null );
		assertEquals(
				"diff_cleanupEfficiency: High cost elimination.",
				diffList( DiffOpInt.deleteFromText( "abwxyzcd" ), DiffOpInt.insertFromText( "12wxyz34" ) ),
				diffs );
		dmp.Diff_EditCost = 4;
	}


	public void testDiffText( ) {
		// Compute the source and destination texts.
		LinkedList<DiffOp<Integer,Integer>> diffs =
				diffList( DiffOpInt.equalFromText( "jump" ), DiffOpInt.deleteFromText( "s" ), DiffOpInt.insertFromText( "ed" ), DiffOpInt.equalFromText( " over " ), DiffOpInt.deleteFromText(
						"the" ), DiffOpInt.insertFromText( "a" ), DiffOpInt.equalFromText( " lazy" ) );
		assertEquals( "diff_text1:", "jumps over the lazy", dmp.diff_text1( diffs ) );
		assertEquals( "diff_text2:", "jumped over a lazy", dmp.diff_text2( diffs ) );
	}


	public void testDiffXIndex( ) {
		// Translate a location in text1 to text2.
		LinkedList<DiffOp<Integer,Integer>> diffs =
				diffList( DiffOpInt.deleteFromText( "a" ), DiffOpInt.insertFromText( "1234" ),
						DiffOpInt.equalFromText( "xyz" ) );
		assertEquals( "diff_xIndex: Translation on equality.", 5,
				dmp.diff_xIndex( diffs, 2 ) );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "1234" ), DiffOpInt.equalFromText( "xyz" ) );
		assertEquals( "diff_xIndex: Translation on deletion.", 1,
				dmp.diff_xIndex( diffs, 3 ) );
	}

	public void testDiffLevenshtein( ) {
		LinkedList<DiffOp<Integer,Integer>> diffs =
				diffList( DiffOpInt.deleteFromText( "abc" ), DiffOpInt.insertFromText( "1234" ),
						DiffOpInt.equalFromText( "xyz" ) );
		assertEquals( "Levenshtein with trailing equality.", 4,
				dmp.diff_levenshtein( diffs ) );

		diffs =
				diffList( DiffOpInt.equalFromText( "xyz" ), DiffOpInt.deleteFromText( "abc" ),
						DiffOpInt.insertFromText( "1234" ) );
		assertEquals( "Levenshtein with leading equality.", 4,
				dmp.diff_levenshtein( diffs ) );

		diffs =
				diffList( DiffOpInt.deleteFromText( "abc" ), DiffOpInt.equalFromText( "xyz" ),
						DiffOpInt.insertFromText( "1234" ) );
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
		LinkedList<DiffOp<Integer,Integer>> diffs =
				diffList( DiffOpInt.deleteFromText( "c" ), DiffOpInt.insertFromText( "m" ), DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "t" ), DiffOpInt.insertFromText( "p" ) );
		assertEquals( "diff_bisect: Normal.", diffs,
				dmp.diff_bisect( a, b, Long.MAX_VALUE ) );

		// Timeout.
		diffs = diffList( DiffOpInt.deleteFromText( "cat" ), DiffOpInt.insertFromText( "map" ) );
		assertEquals( "diff_bisect: Timeout.", diffs, dmp.diff_bisect( a, b, 0 ) );
	}

	public void testDiffMain( ) {
		// Perform a trivial diff.
		LinkedList<DiffOp<Integer,Integer>> diffs = diffList( );
		assertEquals( "diff_main: Null case.", diffs, dmp.diff_main( "", "", false ) );

		diffs = diffList( DiffOpInt.equalFromText( "abc" ) );
		assertEquals( "diff_main: Equality.", diffs,
				dmp.diff_main( "abc", "abc", false ) );

		diffs =
				diffList( DiffOpInt.equalFromText( "ab" ), DiffOpInt.insertFromText( "123" ), DiffOpInt.equalFromText( "c" ) );
		assertEquals( "diff_main: Simple insertion.", diffs,
				dmp.diff_main( "abc", "ab123c", false ) );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "123" ), DiffOpInt.equalFromText( "bc" ) );
		assertEquals( "diff_main: Simple deletion.", diffs,
				dmp.diff_main( "a123bc", "abc", false ) );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.insertFromText( "123" ), DiffOpInt.equalFromText( "b" ), DiffOpInt.insertFromText( "456" ), DiffOpInt.equalFromText( "c" ) );
		assertEquals( "diff_main: Two insertions.", diffs,
				dmp.diff_main( "abc", "a123b456c", false ) );

		diffs =
				diffList( DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "123" ), DiffOpInt.equalFromText( "b" ), DiffOpInt.deleteFromText( "456" ), DiffOpInt.equalFromText( "c" ) );
		assertEquals( "diff_main: Two deletions.", diffs,
				dmp.diff_main( "a123b456c", "abc", false ) );

		// Perform a real diff.
		// Switch off the timeout.
		dmp.Diff_Timeout = 0;
		diffs = diffList( DiffOpInt.deleteFromText( "a" ), DiffOpInt.insertFromText( "b" ) );
		assertEquals( "diff_main: Simple case #1.", diffs,
				dmp.diff_main( "a", "b", false ) );

		diffs =
				diffList( DiffOpInt.deleteFromText( "Apple" ), DiffOpInt.insertFromText( "Banana" ),
						DiffOpInt.equalFromText( "s are a" ), DiffOpInt.insertFromText( "lso" ), DiffOpInt.equalFromText( " fruit." ) );
		
		assertEquals( "diff_main: Simple case #2.", diffs,
				dmp.diff_main( "Apples are a fruit.", "Bananas are also fruit.", false ) );

		diffs =
				diffList( DiffOpInt.deleteFromText( "a" ), DiffOpInt.insertFromText( "\u0680" ),
						DiffOpInt.equalFromText( "x" ), DiffOpInt.deleteFromText( "\t" ), DiffOpInt.insertFromText(
								"\000" ) );
		assertEquals( "diff_main: Simple case #3.", diffs,
				dmp.diff_main( "ax\t", "\u0680x\000", false ) );

		diffs =
				diffList( DiffOpInt.deleteFromText( "1" ), DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "y" ), DiffOpInt.equalFromText( "b" ), DiffOpInt.deleteFromText( "2" ),
						DiffOpInt.insertFromText( "xab" ) );
		assertEquals( "diff_main: Overlap #1.", diffs,
				dmp.diff_main( "1ayb2", "abxab", false ) );

		diffs =
				diffList( DiffOpInt.insertFromText( "xaxcx" ), DiffOpInt.equalFromText( "abc" ),
						DiffOpInt.deleteFromText( "y" ) );
		assertEquals( "diff_main: Overlap #2.", diffs,
				dmp.diff_main( "abcy", "xaxcxabc", false ) );

		diffs =
				diffList( DiffOpInt.deleteFromText( "ABCD" ), DiffOpInt.equalFromText( "a" ), DiffOpInt.deleteFromText( "=" ), DiffOpInt.insertFromText( "-" ), DiffOpInt.equalFromText( "bcd" ),
						DiffOpInt.deleteFromText( "=" ), DiffOpInt.insertFromText( "-" ), DiffOpInt.equalFromText(
								"efghijklmnopqrs" ), DiffOpInt.deleteFromText( "EFGHIJKLMNOefg" ) );
		assertEquals( "diff_main: Overlap #3.", diffs, dmp.diff_main(
				"ABCDa=bcd=efghijklmnopqrsEFGHIJKLMNOefg", "a-bcd-efghijklmnopqrs",
				false ) );

		diffs =
				diffList( DiffOpInt.insertFromText( " " ), DiffOpInt.equalFromText( "a" ), DiffOpInt.insertFromText( "nd" ), DiffOpInt.equalFromText( " [[Pennsylvania]]" ), DiffOpInt.deleteFromText( " and [[New" ) );
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

	// Private function for quickly building lists of diffs.
	@SafeVarargs
	private static LinkedList<DiffOp<Integer,Integer>> diffList( DiffOp<Integer,Integer> ... diffs ) {
		LinkedList<DiffOp<Integer,Integer>> myDiffList = new LinkedList<DiffOp<Integer,Integer>>( );
		for ( DiffOp<Integer,Integer> myDiff : diffs ) {
			myDiffList.add( myDiff );
		}
		return myDiffList;
	}
}
