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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;

/*
 * Functions for diff, match and patch.
 * Computes the difference between two texts to create a patch.
 * Applies the patch onto another text, allowing for errors.
 *
 * @author fraser@google.com (Neil Fraser)
 */

/**
 * Class containing the diff, match and patch methods.
 * Also contains the behaviour settings.
 */
class DiffPatch<T> {

  // Defaults.
  // Set these on your DiffMatchPatch instance to override the defaults.

  /**
   * Number of seconds to map a diff before giving up (0 for infinity).
   */
  public float Diff_Timeout = 1.0f;
  /**
   * Cost of an empty edit operation in terms of edit characters.
   */
  public short Diff_EditCost = 4;
  /**
   * At what point is no match declared (0.0 = perfection, 1.0 = very loose).
   */
  public float Match_Threshold = 0.5f;
  /**
   * How far to search for a match (0 = exact location, 1000+ = broad match).
   * A match this many characters away from the expected location will add
   * 1.0 to the score (0.0 is a perfect match).
   */
  public int Match_Distance = 1000;
  /**
   * When deleting a large block of text (over ~64 characters), how close do
   * the contents have to be to match the expected contents. (0.0 = perfection,
   * 1.0 = very loose).  Note that Match_Threshold controls how closely the
   * end points of a delete need to match.
   */
  public float Patch_DeleteThreshold = 0.5f;
  /**
   * Chunk size for context length.
   */
  public short Patch_Margin = 4;

  private final KnittingTuple<T> the_empty_tuple = KnittingTuple.on();



  //  DIFF FUNCTIONS


  /**
   * The data structure representing a diff is a Linked list of Diff objects:
   * {Diff(Operation.DELETE, "Hello"), Diff(Operation.INSERT, "Goodbye"),
   *  Diff(Operation.EQUAL, " world.")}
   * which means: delete "Hello", add "Goodbye" and keep " world."
   */
  public enum Operation {
    DELETE, INSERT, EQUAL
  }



  public LinkedList<Diff<Integer>> diff_main(String text1, String text2, boolean useless) {
  	DiffPatch<Integer> diffMatchPatch = new DiffPatch<Integer>( );
  	diffMatchPatch.Diff_Timeout = Diff_Timeout;
  	return diffMatchPatch.diff_main( tt(text1), tt(text2), DiffPatch::equal_or_both_null);
  }

  public LinkedList<Diff<Integer>> diff_main(String text1, String text2) {
  	DiffPatch<Integer> diffMatchPatch = new DiffPatch<Integer>( );
  	diffMatchPatch.Diff_Timeout = Diff_Timeout;
  	return diffMatchPatch.diff_main( tt(text1), tt(text2), DiffPatch::equal_or_both_null);
  }
  
  public LinkedList<Diff<Integer>> diff_main(String text1, String text2,
      long deadline) {
  	DiffPatch<Integer> diffMatchPatch = new DiffPatch<Integer>( );
  	diffMatchPatch.Diff_Timeout = Diff_Timeout;
  	return diffMatchPatch.diff_main( tt(text1), tt(text2), DiffPatch::equal_or_both_null, deadline );
  }

  /**
   * Find the differences between two texts.
   * Run a faster, slightly less optimal diff.
   * This method allows the 'checklines' of diff_main() to be optional.
   * Most of the time checklines is wanted, so default to true.
   * @param text1 Old string to be diffed.
   * @param text2 New string to be diffed.
   * @return Linked List of Diff objects.
   */
  public LinkedList<Diff<T>> diff_main(KnittingTuple<T> text1, KnittingTuple<T> text2, Equivalencer<T> equivalencer) {
    // Set a deadline by which time the diff must be complete.
    long deadline;
    if (Diff_Timeout <= 0) {
      deadline = Long.MAX_VALUE;
    } else {
      deadline = System.currentTimeMillis() + (long) (Diff_Timeout * 1000);
    }
    return diff_main(text1, text2, equivalencer, deadline);
  }

  /**
   * Find the differences between two texts.  Simplifies the problem by
   * stripping any common prefix or suffix off the texts before diffing.
   * @param text1 Old string to be diffed.
   * @param text2 New string to be diffed.
   * @param deadline Time when the diff should be complete by.  Used
   *     internally for recursive calls.  Users should set DiffTimeout instead.
   * @return Linked List of Diff objects.
   */
  private LinkedList<Diff<T>> diff_main(
  		KnittingTuple<T> text1,
  		KnittingTuple<T> text2,
  		Equivalencer<T> equivalencer,
  		long deadline) {
    // Check for null inputs.
    if (text1 == null || text2 == null) {
      throw new IllegalArgumentException("Null inputs. (diff_main)");
    }

    // Check for equality (speedup).
    LinkedList<Diff<T>> diffs;
    if (text1.equivalentTo(text2, equivalencer)) {
      diffs = new LinkedList<Diff<T>>();
      if (text1.size() != 0) {
        diffs.add(new Diff<T>(Operation.EQUAL, text1, equivalencer));
      }
      return diffs;
    }

    // Trim off common prefix (speedup).
    int commonlength = diff_commonPrefix(text1, text2, equivalencer);
    KnittingTuple<T> commonprefix = text1.head(0, commonlength);
    text1 = text1.headless(commonlength);
    text2 = text2.headless(commonlength);

    // Trim off common suffix (speedup).
    commonlength = diff_commonSuffix(text1, text2, equivalencer);
    KnittingTuple<T> commonsuffix = text1.headless(text1.size() - commonlength);
    text1 = text1.head(0, text1.size() - commonlength);
    text2 = text2.head(0, text2.size() - commonlength);

    // Compute the diff on the middle block.
    diffs = diff_compute(text1, text2, equivalencer, deadline);

    // Restore the prefix and suffix.
    if (commonprefix.size() != 0) {
      diffs.addFirst(new Diff<T>(Operation.EQUAL, commonprefix, equivalencer));
    }
    if (commonsuffix.size() != 0) {
      diffs.addLast(new Diff<T>(Operation.EQUAL, commonsuffix, equivalencer));
    }

    diff_cleanupMerge(diffs, equivalencer);
    return diffs;
  }

  /**
   * Find the differences between two texts.  Assumes that the texts do not
   * have any common prefix or suffix.
   * @param text1 Old string to be diffed.
   * @param text2 New string to be diffed.
   * @param deadline Time when the diff should be complete by.
   * @return Linked List of Diff objects.
   */
  private LinkedList<Diff<T>> diff_compute(
  		KnittingTuple<T> text1,
  		KnittingTuple<T> text2,
  		Equivalencer<T> equivalencer,
  		long deadline) {
    LinkedList<Diff<T>> diffs = new LinkedList<Diff<T>>();

    if (text1.size() == 0) {
      // Just add some text (speedup).
      diffs.add(new Diff<T>(Operation.INSERT, text2, equivalencer));
      return diffs;
    }

    if (text2.size() == 0) {
      // Just delete some text (speedup).
      diffs.add(new Diff<T>(Operation.DELETE, text1, equivalencer));
      return diffs;
    }

    KnittingTuple<T> longtext = text1.size() > text2.size() ? text1 : text2;
    KnittingTuple<T> shorttext = text1.size() > text2.size() ? text2 : text1;
    Optional<Integer> i = longtext.findSubtuple(shorttext, 0);
    if (i.isPresent( )) {
    	int i_value = i.get( );
      // Shorter text is inside the longer text (speedup).
      Operation op = (text1.size() > text2.size()) ?
                     Operation.DELETE : Operation.INSERT;
      diffs.add(new Diff<T>(op, longtext.head(0, i_value), equivalencer));
      diffs.add(new Diff<T>(Operation.EQUAL, shorttext, equivalencer));
      diffs.add(new Diff<T>(op, longtext.headless(i_value + shorttext.size()), equivalencer));
      return diffs;
    }

    if (shorttext.size() == 1) {
      // Single character string.
      // After the previous speedup, the character can't be an equality.
      diffs.add(new Diff<T>(Operation.DELETE, text1, equivalencer));
      diffs.add(new Diff<T>(Operation.INSERT, text2, equivalencer));
      return diffs;
    }

    // Check to see if the problem can be split in two.
    KnittingTuple<T>[] hm = diff_halfMatch(text1, text2, equivalencer);
    if (hm != null) {
      // A half-match was found, sort out the return data.
    	KnittingTuple<T> text1_a = hm[0];
    	KnittingTuple<T> text1_b = hm[1];
    	KnittingTuple<T> text2_a = hm[2];
    	KnittingTuple<T> text2_b = hm[3];
    	KnittingTuple<T> mid_common = hm[4];
      // Send both pairs off for separate processing.
      LinkedList<Diff<T>> diffs_a = diff_main(text1_a, text2_a, equivalencer,
                                           deadline);
      LinkedList<Diff<T>> diffs_b = diff_main(text1_b, text2_b, equivalencer,
                                           deadline);
      // Merge the results.
      diffs = diffs_a;
      diffs.add(new Diff<T>(Operation.EQUAL, mid_common, equivalencer));
      diffs.addAll(diffs_b);
      return diffs;
    }

    return diff_bisect(text1, text2, equivalencer, deadline);
  }


  public LinkedList<Diff<Integer>> diff_bisect(String text1, String text2,
      long deadline) {
  	return new DiffPatch<Integer>( ).diff_bisect( tt(text1), tt(text2), DiffPatch::equal_or_both_null, deadline );
  }
  
  /**
   * Find the 'middle snake' of a diff, split the problem in two
   * and return the recursively constructed diff.
   * See Myers 1986 paper: An O(ND) Difference Algorithm and Its Variations.
   * @param text1 Old string to be diffed.
   * @param text2 New string to be diffed.
   * @param deadline Time at which to bail if not yet complete.
   * @return LinkedList of Diff objects.
   */
  protected LinkedList<Diff<T>> diff_bisect(
  		KnittingTuple<T> text1,
  		KnittingTuple<T> text2,
  		Equivalencer<T> equivalencer,
      long deadline) {
    // Cache the text lengths to prevent multiple calls.
    int text1_length = text1.size();
    int text2_length = text2.size();
    int max_d = (text1_length + text2_length + 1) / 2;
    int v_offset = max_d;
    int v_length = 2 * max_d;
    int[] v1 = new int[v_length];
    int[] v2 = new int[v_length];
    for (int x = 0; x < v_length; x++) {
      v1[x] = -1;
      v2[x] = -1;
    }
    v1[v_offset + 1] = 0;
    v2[v_offset + 1] = 0;
    int delta = text1_length - text2_length;
    // If the total number of characters is odd, then the front path will
    // collide with the reverse path.
    boolean front = (delta % 2 != 0);
    // Offsets for start and end of k loop.
    // Prevents mapping of space beyond the grid.
    int k1start = 0;
    int k1end = 0;
    int k2start = 0;
    int k2end = 0;
    for (int d = 0; d < max_d; d++) {
      // Bail out if deadline is reached.
      if (System.currentTimeMillis() > deadline) {
        break;
      }

      // Walk the front path one step.
      for (int k1 = -d + k1start; k1 <= d - k1end; k1 += 2) {
        int k1_offset = v_offset + k1;
        int x1;
        if (k1 == -d || (k1 != d && v1[k1_offset - 1] < v1[k1_offset + 1])) {
          x1 = v1[k1_offset + 1];
        } else {
          x1 = v1[k1_offset - 1] + 1;
        }
        int y1 = x1 - k1;
        while (x1 < text1_length && y1 < text2_length
               && equivalencer.equivalent(text1.get(x1), text2.get(y1))) {
          x1++;
          y1++;
        }
        v1[k1_offset] = x1;
        if (x1 > text1_length) {
          // Ran off the right of the graph.
          k1end += 2;
        } else if (y1 > text2_length) {
          // Ran off the bottom of the graph.
          k1start += 2;
        } else if (front) {
          int k2_offset = v_offset + delta - k1;
          if (k2_offset >= 0 && k2_offset < v_length && v2[k2_offset] != -1) {
            // Mirror x2 onto top-left coordinate system.
            int x2 = text1_length - v2[k2_offset];
            if (x1 >= x2) {
              // Overlap detected.
              return diff_bisectSplit(text1, text2, equivalencer, x1, y1, deadline);
            }
          }
        }
      }

      // Walk the reverse path one step.
      for (int k2 = -d + k2start; k2 <= d - k2end; k2 += 2) {
        int k2_offset = v_offset + k2;
        int x2;
        if (k2 == -d || (k2 != d && v2[k2_offset - 1] < v2[k2_offset + 1])) {
          x2 = v2[k2_offset + 1];
        } else {
          x2 = v2[k2_offset - 1] + 1;
        }
        int y2 = x2 - k2;
        while (x2 < text1_length && y2 < text2_length
               && equivalencer.equivalent(text1.get(text1_length - x2 - 1),
               text2.get(text2_length - y2 - 1))) {
          x2++;
          y2++;
        }
        v2[k2_offset] = x2;
        if (x2 > text1_length) {
          // Ran off the left of the graph.
          k2end += 2;
        } else if (y2 > text2_length) {
          // Ran off the top of the graph.
          k2start += 2;
        } else if (!front) {
          int k1_offset = v_offset + delta - k2;
          if (k1_offset >= 0 && k1_offset < v_length && v1[k1_offset] != -1) {
            int x1 = v1[k1_offset];
            int y1 = v_offset + x1 - k1_offset;
            // Mirror x2 onto top-left coordinate system.
            x2 = text1_length - x2;
            if (x1 >= x2) {
              // Overlap detected.
              return diff_bisectSplit(text1, text2, equivalencer, x1, y1, deadline);
            }
          }
        }
      }
    }
    // Diff took too long and hit the deadline or
    // number of diffs equivalentTo number of characters, no commonality at all.
    LinkedList<Diff<T>> diffs = new LinkedList<Diff<T>>();
    diffs.add(new Diff<T>(Operation.DELETE, text1, equivalencer));
    diffs.add(new Diff<T>(Operation.INSERT, text2, equivalencer));
    return diffs;
  }

  /**
   * Given the location of the 'middle snake', split the diff in two parts
   * and recurse.
   * @param text1 Old string to be diffed.
   * @param text2 New string to be diffed.
   * @param x Index of split point in text1.
   * @param y Index of split point in text2.
   * @param deadline Time at which to bail if not yet complete.
   * @return LinkedList of Diff objects.
   */
  private LinkedList<Diff<T>> diff_bisectSplit(
  		KnittingTuple<T> text1,
  		KnittingTuple<T> text2,
  		Equivalencer<T> equivalencer,
      int x, int y, long deadline) {
  	KnittingTuple<T> text1a = text1.head(0, x);
  	KnittingTuple<T> text2a = text2.head(0, y);
  	KnittingTuple<T> text1b = text1.headless(x);
  	KnittingTuple<T> text2b = text2.headless(y);

    // Compute both diffs serially.
    LinkedList<Diff<T>> diffs = diff_main(text1a, text2a, equivalencer, deadline);
    LinkedList<Diff<T>> diffsb = diff_main(text1b, text2b, equivalencer, deadline);

    diffs.addAll(diffsb);
    return diffs;
  }

  
	private static KnittingTuple<Integer> tt( String s ) {
		if (s == null) return null;
		return KnittingTuple.wrap( KnittingCursor.wrap( s.codePoints( ).boxed( ) )
				.collect( new Vector<Integer>( ) ) );
  }


	public int diff_commonPrefix(String text1, String text2) {
  	return new DiffPatch<Integer>().diff_commonPrefix( tt(text1), tt(text2), DiffPatch::equal_or_both_null);
  }

  /**
   * Determine the common prefix of two strings
   * @param text1 First string.
   * @param text2 Second string.
   * @return The number of characters common to the start of each string.
   */
  public int diff_commonPrefix(KnittingTuple<T> text1, KnittingTuple<T> text2,
  		Equivalencer<T> equivalencer) {
    // Performance analysis: http://neil.fraser.name/news/2007/10/09/
    int n = Math.min(text1.size(), text2.size());
    for (int i = 0; i < n; i++) {
      if (! equivalencer.equivalent(text1.get(i), text2.get(i))) {
        return i;
      }
    }
    return n;
  }
  
	public int diff_commonSuffix(String text1, String text2) {
  	return new DiffPatch<Integer>().diff_commonSuffix( tt(text1), tt(text2), DiffPatch::equal_or_both_null);
  }

  /**
   * Determine the common suffix of two strings
   * @param text1 First string.
   * @param text2 Second string.
   * @return The number of characters common to the end of each string.
   */
  public int diff_commonSuffix(KnittingTuple<T> text1, KnittingTuple<T> text2,
  		Equivalencer<T> equivalencer) {
    // Performance analysis: http://neil.fraser.name/news/2007/10/09/
    int text1_length = text1.size();
    int text2_length = text2.size();
    int n = Math.min(text1_length, text2_length);
    for (int i = 1; i <= n; i++) {
      if (! equivalencer.equivalent(text1.get(text1_length - i), text2.get(text2_length - i))) {
        return i - 1;
      }
    }
    return n;
  }

  
	public int diff_commonOverlap(String text1, String text2) {
  	return new DiffPatch<Integer>().diff_commonOverlap( tt(text1), tt(text2), DiffPatch::equal_or_both_null);
  }
	
  /**
   * Determine if the suffix of one string is the prefix of another.
   * @param text1 First string.
   * @param text2 Second string.
   * @return The number of characters common to the end of the first
   *     string and the start of the second string.
   */
  protected int diff_commonOverlap(KnittingTuple<T> text1, KnittingTuple<T> text2, Equivalencer<T> equivalencer) {
    // Cache the text lengths to prevent multiple calls.
    int text1_length = text1.size();
    int text2_length = text2.size();
    // Eliminate the null case.
    if (text1_length == 0 || text2_length == 0) {
      return 0;
    }
    // Truncate the longer string.
    if (text1_length > text2_length) {
      text1 = text1.headless(text1_length - text2_length);
    } else if (text1_length < text2_length) {
      text2 = text2.head(0, text1_length);
    }
    int text_length = Math.min(text1_length, text2_length);
    // Quick check for the worst case.
    if (text1.equivalentTo(text2, equivalencer)) {
      return text_length;
    }

    // Start by looking for a single character match
    // and increase length until no match is found.
    // Performance analysis: http://neil.fraser.name/news/2010/11/04/
    int best = 0;
    int length = 1;
    while (true) {
      KnittingTuple<T> pattern = text1.headless(text_length - length);
      Optional<Integer> found = text2.findSubtuple(pattern, 0);
      if (!found.isPresent( )) {
        return best;
      }
      length += found.get( );
      if (found.get( ) == 0 || text1.headless(text_length - length).equivalentTo(
          text2.head(0, length), equivalencer)) {
        best = length;
        length++;
      }
    }
  }
	
	private static String codepointTupleToString( KnittingTuple<?> codePoints ) {
		StringBuilder sb = new StringBuilder( );
		for ( Object codePoint : codePoints.asIterable( ) ) {
			if ( codePoint instanceof Integer ) {
				char[] chars = Character.toChars( (Integer) codePoint );
				sb.append( chars );
			}
		}
		return sb.toString( );
	}
	
	public String[] diff_halfMatch(String text1, String text2) {
		DiffPatch<Integer> diffMatchPatch = new DiffPatch<Integer>();
		diffMatchPatch.Diff_Timeout = Diff_Timeout;
		KnittingTuple<Integer>[] diff_halfMatch = diffMatchPatch.diff_halfMatch( tt(text1), tt(text2), DiffPatch::equal_or_both_null);
		
		if (diff_halfMatch == null) return null;
		ArrayList<String> result = new ArrayList<>();
		for (int i = 0; i < diff_halfMatch.length; i++) {
			result.add(codepointTupleToString( diff_halfMatch[i] ));
		}
  	return result.toArray( new String[]{});
  }

  /**
   * Do the two texts share a substring which is at least half the length of
   * the longer text?
   * This speedup can produce non-minimal diffs.
   * @param text1 First string.
   * @param text2 Second string.
   * @return Five element String array, containing the prefix of text1, the
   *     suffix of text1, the prefix of text2, the suffix of text2 and the
   *     common middle.  Or null if there was no match.
   */
  @SuppressWarnings("unchecked")
	protected KnittingTuple<T>[] diff_halfMatch(KnittingTuple<T> text1, KnittingTuple<T> text2, Equivalencer<T> equivalencer) {
    if (Diff_Timeout <= 0) {
      // Don't risk returning a non-optimal diff if we have unlimited time.
      return null;
    }
    KnittingTuple<T> longtext = text1.size() > text2.size() ? text1 : text2;
    KnittingTuple<T> shorttext = text1.size() > text2.size() ? text2 : text1;
    if (longtext.size() < 4 || shorttext.size() * 2 < longtext.size()) {
      return null;  // Pointless.
    }

    // First check if the second quarter is the seed for a half-match.
    KnittingTuple<T>[] hm1 = diff_halfMatchI(longtext, shorttext, equivalencer,
                                   (longtext.size() + 3) / 4);
    // Check again based on the third quarter.
    KnittingTuple<T>[] hm2 = diff_halfMatchI(longtext, shorttext, equivalencer,
                                   (longtext.size() + 1) / 2);
    KnittingTuple<T>[] hm;
    if (hm1 == null && hm2 == null) {
      return null;
    } else if (hm2 == null) {
      hm = hm1;
    } else if (hm1 == null) {
      hm = hm2;
    } else {
      // Both matched.  Select the longest.
      hm = hm1[4].size() > hm2[4].size() ? hm1 : hm2;
    }

    // A half-match was found, sort out the return data.
    if (text1.size() > text2.size()) {
      return hm;
      //return new String[]{hm[0], hm[1], hm[2], hm[3], hm[4]};
    } else {
      return (KnittingTuple<T>[]) new KnittingTuple[]{hm[2], hm[3], hm[0], hm[1], hm[4]};
    }
  }
  

  /**
   * Does a substring of shorttext exist within longtext such that the
   * substring is at least half the length of longtext?
   * @param longtext Longer string.
   * @param shorttext Shorter string.
   * @param i Start index of quarter length substring within longtext.
   * @return Five element String array, containing the prefix of longtext, the
   *     suffix of longtext, the prefix of shorttext, the suffix of shorttext
   *     and the common middle.  Or null if there was no match.
   */
  @SuppressWarnings("unchecked")
	private KnittingTuple<T>[] diff_halfMatchI(
			KnittingTuple<T> longtext,
			KnittingTuple<T> shorttext,
  		Equivalencer<T> equivalencer,
			int i) {
    // Start with a 1/4 length substring at position i as a seed.
  	KnittingTuple<T> seed = longtext.head(i, (i + longtext.size( ) / 4) - i);
    int j = -1;
    KnittingTuple<T> best_common = the_empty_tuple;
    KnittingTuple<T> best_longtext_a = the_empty_tuple, best_longtext_b = the_empty_tuple;
    KnittingTuple<T> best_shorttext_a = the_empty_tuple, best_shorttext_b = the_empty_tuple;
    while ((j = shorttext.findSubtuple(seed, j + 1).orElse( -1 )) != -1) {
      int prefixLength = diff_commonPrefix(longtext.headless(i),
                                           shorttext.headless(j), equivalencer);
      int suffixLength = diff_commonSuffix(longtext.head(0, i),
                                           shorttext.head(0, j), equivalencer);
      if (best_common.size() < suffixLength + prefixLength) {
        best_common = shorttext.head(j - suffixLength, (j) - (j - suffixLength)).chain(
            shorttext.head(j, (j + prefixLength) - (j)));
        best_longtext_a = longtext.head(0, i - suffixLength);
        best_longtext_b = longtext.headless(i + prefixLength);
        best_shorttext_a = shorttext.head(0, j - suffixLength);
        best_shorttext_b = shorttext.headless(j + prefixLength);
      }
    }
    if (best_common.size() * 2 >= longtext.size()) {
      return (KnittingTuple<T>[]) new KnittingTuple[]{best_longtext_a, best_longtext_b,
                          best_shorttext_a, best_shorttext_b, best_common};
    } else {
      return null;
    }
  }

  /**
   * Reduce the number of edits by eliminating semantically trivial equalities.
   * @param diffs LinkedList of Diff objects.
   */
  public void diff_cleanupSemantic(LinkedList<Diff<T>> diffs, Equivalencer<T> equivalencer) {
    if (diffs.isEmpty()) {
      return;
    }
    boolean changes = false;
    Stack<Diff<T>> equalities = new Stack<Diff<T>>();  // Stack of qualities.
    KnittingTuple<T> lastequality = null; // Always equal to equalities.lastElement().text
    ListIterator<Diff<T>> pointer = diffs.listIterator();
    // Number of characters that changed prior to the equality.
    int length_insertions1 = 0;
    int length_deletions1 = 0;
    // Number of characters that changed after the equality.
    int length_insertions2 = 0;
    int length_deletions2 = 0;
    Diff<T> thisDiff = pointer.next();
    while (thisDiff != null) {
      if (thisDiff.operation == Operation.EQUAL) {
        // Equality found.
        equalities.push(thisDiff);
        length_insertions1 = length_insertions2;
        length_deletions1 = length_deletions2;
        length_insertions2 = 0;
        length_deletions2 = 0;
        lastequality = thisDiff.text;
      } else {
        // An insertion or deletion.
        if (thisDiff.operation == Operation.INSERT) {
          length_insertions2 += thisDiff.text.size();
        } else {
          length_deletions2 += thisDiff.text.size();
        }
        // Eliminate an equality that is smaller or equal to the edits on both
        // sides of it.
        if (lastequality != null && (lastequality.size()
            <= Math.max(length_insertions1, length_deletions1))
            && (lastequality.size()
                <= Math.max(length_insertions2, length_deletions2))) {
          //System.out.println("Splitting: '" + lastequality + "'");
          // Walk back to offending equality.
          while (thisDiff != equalities.lastElement()) {
            thisDiff = pointer.previous();
          }
          pointer.next();

          // Replace equality with a delete.
          pointer.set(new Diff<T>(Operation.DELETE, lastequality, equivalencer));
          // Insert a corresponding an insert.
          pointer.add(new Diff<T>(Operation.INSERT, lastequality, equivalencer));

          equalities.pop();  // Throw away the equality we just deleted.
          if (!equalities.empty()) {
            // Throw away the previous equality (it needs to be reevaluated).
            equalities.pop();
          }
          if (equalities.empty()) {
            // There are no previous equalities, walk back to the start.
            while (pointer.hasPrevious()) {
              pointer.previous();
            }
          } else {
            // There is a safe equality we can fall back to.
            thisDiff = equalities.lastElement();
            while (thisDiff != pointer.previous()) {
              // Intentionally empty loop.
            }
          }

          length_insertions1 = 0;  // Reset the counters.
          length_insertions2 = 0;
          length_deletions1 = 0;
          length_deletions2 = 0;
          lastequality = null;
          changes = true;
        }
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }

    // Normalize the diff.
    if (changes) {
      diff_cleanupMerge(diffs, equivalencer);
    }

    // Find any overlaps between deletions and insertions.
    // e.g: <del>abcxxx</del><ins>xxxdef</ins>
    //   -> <del>abc</del>xxx<ins>def</ins>
    // e.g: <del>xxxabc</del><ins>defxxx</ins>
    //   -> <ins>def</ins>xxx<del>abc</del>
    // Only extract an overlap if it is as big as the edit ahead or behind it.
    pointer = diffs.listIterator();
    Diff<T> prevDiff = null;
    thisDiff = null;
    if (pointer.hasNext()) {
      prevDiff = pointer.next();
      if (pointer.hasNext()) {
        thisDiff = pointer.next();
      }
    }
    while (thisDiff != null) {
      if (prevDiff.operation == Operation.DELETE &&
          thisDiff.operation == Operation.INSERT) {
        KnittingTuple<T> deletion = prevDiff.text;
        KnittingTuple<T> insertion = thisDiff.text;
        int overlap_length1 = this.diff_commonOverlap(deletion, insertion, equivalencer);
        int overlap_length2 = this.diff_commonOverlap(insertion, deletion, equivalencer);
        if (overlap_length1 >= overlap_length2) {
          if (overlap_length1 >= deletion.size() / 2.0 ||
              overlap_length1 >= insertion.size() / 2.0) {
            // Overlap found. Insert an equality and trim the surrounding edits.
            pointer.previous();
            pointer.add(new Diff<T>(Operation.EQUAL,
                                 insertion.head(0, overlap_length1), equivalencer));
            prevDiff.text =
                deletion.head(0, deletion.size() - overlap_length1);
            thisDiff.text = insertion.headless(overlap_length1);
            // pointer.add inserts the element before the cursor, so there is
            // no need to step past the new element.
          }
        } else {
          if (overlap_length2 >= deletion.size() / 2.0 ||
              overlap_length2 >= insertion.size() / 2.0) {
            // Reverse overlap found.
            // Insert an equality and swap and trim the surrounding edits.
            pointer.previous();
            pointer.add(new Diff<T>(Operation.EQUAL,
                                 deletion.head(0, overlap_length2), equivalencer));
            prevDiff.operation = Operation.INSERT;
            prevDiff.text =
              insertion.head(0, insertion.size() - overlap_length2);
            thisDiff.operation = Operation.DELETE;
            thisDiff.text = deletion.headless(overlap_length2);
            // pointer.add inserts the element before the cursor, so there is
            // no need to step past the new element.
          }
        }
        thisDiff = pointer.hasNext() ? pointer.next() : null;
      }
      prevDiff = thisDiff;
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
  }

  /**
   * Reduce the number of edits by eliminating operationally trivial equalities.
   * @param diffs LinkedList of Diff objects.
   */
  public void diff_cleanupEfficiency(LinkedList<Diff<T>> diffs, Equivalencer<T> equivalencer) {
    if (diffs.isEmpty()) {
      return;
    }
    boolean changes = false;
    Stack<Diff<T>> equalities = new Stack<Diff<T>>();  // Stack of equalities.
    KnittingTuple<T> lastequality = null; // Always equal to equalities.lastElement().text
    ListIterator<Diff<T>> pointer = diffs.listIterator();
    // Is there an insertion operation before the last equality.
    boolean pre_ins = false;
    // Is there a deletion operation before the last equality.
    boolean pre_del = false;
    // Is there an insertion operation after the last equality.
    boolean post_ins = false;
    // Is there a deletion operation after the last equality.
    boolean post_del = false;
    Diff<T> thisDiff = pointer.next();
    Diff<T> safeDiff = thisDiff;  // The last Diff that is known to be unsplitable.
    while (thisDiff != null) {
      if (thisDiff.operation == Operation.EQUAL) {
        // Equality found.
        if (thisDiff.text.size() < Diff_EditCost && (post_ins || post_del)) {
          // Candidate found.
          equalities.push(thisDiff);
          pre_ins = post_ins;
          pre_del = post_del;
          lastequality = thisDiff.text;
        } else {
          // Not a candidate, and can never become one.
          equalities.clear();
          lastequality = null;
          safeDiff = thisDiff;
        }
        post_ins = post_del = false;
      } else {
        // An insertion or deletion.
        if (thisDiff.operation == Operation.DELETE) {
          post_del = true;
        } else {
          post_ins = true;
        }
        /*
         * Five types to be split:
         * <ins>A</ins><del>B</del>XY<ins>C</ins><del>D</del>
         * <ins>A</ins>X<ins>C</ins><del>D</del>
         * <ins>A</ins><del>B</del>X<ins>C</ins>
         * <ins>A</del>X<ins>C</ins><del>D</del>
         * <ins>A</ins><del>B</del>X<del>C</del>
         */
        if (lastequality != null
            && ((pre_ins && pre_del && post_ins && post_del)
                || ((lastequality.size() < Diff_EditCost / 2)
                    && ((pre_ins ? 1 : 0) + (pre_del ? 1 : 0)
                        + (post_ins ? 1 : 0) + (post_del ? 1 : 0)) == 3))) {
          //System.out.println("Splitting: '" + lastequality + "'");
          // Walk back to offending equality.
          while (thisDiff != equalities.lastElement()) {
            thisDiff = pointer.previous();
          }
          pointer.next();

          // Replace equality with a delete.
          pointer.set(new Diff<T>(Operation.DELETE, lastequality, equivalencer));
          // Insert a corresponding an insert.
          pointer.add(thisDiff = new Diff<T>(Operation.INSERT, lastequality, equivalencer));

          equalities.pop();  // Throw away the equality we just deleted.
          lastequality = null;
          if (pre_ins && pre_del) {
            // No changes made which could affect previous entry, keep going.
            post_ins = post_del = true;
            equalities.clear();
            safeDiff = thisDiff;
          } else {
            if (!equalities.empty()) {
              // Throw away the previous equality (it needs to be reevaluated).
              equalities.pop();
            }
            if (equalities.empty()) {
              // There are no previous questionable equalities,
              // walk back to the last known safe diff.
              thisDiff = safeDiff;
            } else {
              // There is an equality we can fall back to.
              thisDiff = equalities.lastElement();
            }
            while (thisDiff != pointer.previous()) {
              // Intentionally empty loop.
            }
            post_ins = post_del = false;
          }

          changes = true;
        }
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }

    if (changes) {
      diff_cleanupMerge(diffs, equivalencer);
    }
  }

  /**
   * Reorder and merge like edit sections.  Merge equalities.
   * Any edit section can move as long as it doesn't cross an equality.
   * @param diffs LinkedList of Diff objects.
   */
  public void diff_cleanupMerge(LinkedList<Diff<T>> diffs, Equivalencer<T> equivalencer) {
    diffs.add(new Diff<T>(Operation.EQUAL, the_empty_tuple, equivalencer));  // Add a dummy entry at the end.
    ListIterator<Diff<T>> pointer = diffs.listIterator();
    int count_delete = 0;
    int count_insert = 0;
    KnittingTuple<T> text_delete = the_empty_tuple;
    KnittingTuple<T> text_insert = the_empty_tuple;
    Diff<T> thisDiff = pointer.next();
    Diff<T> prevEqual = null;
    int commonlength;
    while (thisDiff != null) {
      switch (thisDiff.operation) {
      case INSERT:
        count_insert++;
        text_insert = text_insert.chain( thisDiff.text);
        prevEqual = null;
        break;
      case DELETE:
        count_delete++;
        text_delete = text_delete.chain( thisDiff.text);
        prevEqual = null;
        break;
      case EQUAL:
        if (count_delete + count_insert > 1) {
          boolean both_types = count_delete != 0 && count_insert != 0;
          // Delete the offending records.
          pointer.previous();  // Reverse direction.
          while (count_delete-- > 0) {
            pointer.previous();
            pointer.remove();
          }
          while (count_insert-- > 0) {
            pointer.previous();
            pointer.remove();
          }
          if (both_types) {
            // Factor out any common prefixies.
            commonlength = diff_commonPrefix(text_insert, text_delete, equivalencer);
            if (commonlength != 0) {
              if (pointer.hasPrevious()) {
                thisDiff = pointer.previous();
                assert thisDiff.operation == Operation.EQUAL
                       : "Previous diff should have been an equality.";
                thisDiff.text = thisDiff.text.chain( text_insert.head(0, commonlength));
                pointer.next();
              } else {
                pointer.add(new Diff<T>(Operation.EQUAL,
                    text_insert.head(0, commonlength), equivalencer));
              }
              text_insert = text_insert.headless(commonlength);
              text_delete = text_delete.headless(commonlength);
            }
            // Factor out any common suffixies.
            commonlength = diff_commonSuffix(text_insert, text_delete, equivalencer);
            if (commonlength != 0) {
              thisDiff = pointer.next();
              thisDiff.text = text_insert.headless(text_insert.size()
                  - commonlength).chain( thisDiff.text );
              text_insert = text_insert.head(0, text_insert.size()
                  - commonlength);
              text_delete = text_delete.head(0, text_delete.size()
                  - commonlength);
              pointer.previous();
            }
          }
          // Insert the merged records.
          if (text_delete.size() != 0) {
            pointer.add(new Diff<T>(Operation.DELETE, text_delete, equivalencer));
          }
          if (text_insert.size() != 0) {
            pointer.add(new Diff<T>(Operation.INSERT, text_insert, equivalencer));
          }
          // Step forward to the equality.
          thisDiff = pointer.hasNext() ? pointer.next() : null;
        } else if (prevEqual != null) {
          // Merge this equality with the previous one.
          prevEqual.text = prevEqual.text.chain(thisDiff.text);
          pointer.remove();
          thisDiff = pointer.previous();
          pointer.next();  // Forward direction
        }
        count_insert = 0;
        count_delete = 0;
        text_delete = the_empty_tuple;
        text_insert = the_empty_tuple;
        prevEqual = thisDiff;
        break;
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
    if (diffs.getLast().text.size() == 0) {
      diffs.removeLast();  // Remove the dummy entry at the end.
    }

    /*
     * Second pass: look for single edits surrounded on both sides by equalities
     * which can be shifted sideways to eliminate an equality.
     * e.g: A<ins>BA</ins>C -> <ins>AB</ins>AC
     */
    boolean changes = false;
    // Create a new iterator at the start.
    // (As opposed to walking the current one back.)
    pointer = diffs.listIterator();
    Diff<T> prevDiff = pointer.hasNext() ? pointer.next() : null;
    thisDiff = pointer.hasNext() ? pointer.next() : null;
    Diff<T> nextDiff = pointer.hasNext() ? pointer.next() : null;
    // Intentionally ignore the first and last element (don't need checking).
    while (nextDiff != null) {
      if (prevDiff.operation == Operation.EQUAL &&
          nextDiff.operation == Operation.EQUAL) {
        // This is a single edit surrounded by equalities.
        if (thisDiff.text.endsWith(prevDiff.text)) {
          // Shift the edit over the previous equality.
          thisDiff.text = prevDiff.text.chain(
              thisDiff.text.head(0, thisDiff.text.size()
                                           - prevDiff.text.size()));
          nextDiff.text = prevDiff.text.chain( nextDiff.text );
          pointer.previous(); // Walk past nextDiff.
          pointer.previous(); // Walk past thisDiff.
          pointer.previous(); // Walk past prevDiff.
          pointer.remove(); // Delete prevDiff.
          pointer.next(); // Walk past thisDiff.
          thisDiff = pointer.next(); // Walk past nextDiff.
          nextDiff = pointer.hasNext() ? pointer.next() : null;
          changes = true;
        } else if (thisDiff.text.startsWith(nextDiff.text)) {
          // Shift the edit over the next equality.
          prevDiff.text = prevDiff.text.chain(nextDiff.text);
          thisDiff.text = thisDiff.text.headless(nextDiff.text.size()).chain(
              nextDiff.text );
          pointer.remove(); // Delete nextDiff.
          nextDiff = pointer.hasNext() ? pointer.next() : null;
          changes = true;
        }
      }
      prevDiff = thisDiff;
      thisDiff = nextDiff;
      nextDiff = pointer.hasNext() ? pointer.next() : null;
    }
    // If shifts were made, the diff needs reordering and another shift sweep.
    if (changes) {
      diff_cleanupMerge(diffs, equivalencer);
    }
  }

  /**
   * loc is a location in text1, compute and return the equivalent location in
   * text2.
   * e.g. "The cat" vs "The big cat", 1 goes to 1, 5 goes to 8
   * @param diffs LinkedList of Diff objects.
   * @param loc Location within text1.
   * @return Location within text2.
   */
  public int diff_xIndex(LinkedList<Diff<T>> diffs, int loc) {
    int chars1 = 0;
    int chars2 = 0;
    int last_chars1 = 0;
    int last_chars2 = 0;
    Diff<T> lastDiff = null;
    for (Diff<T> aDiff : diffs) {
      if (aDiff.operation != Operation.INSERT) {
        // Equality or deletion.
        chars1 += aDiff.text.size();
      }
      if (aDiff.operation != Operation.DELETE) {
        // Equality or insertion.
        chars2 += aDiff.text.size();
      }
      if (chars1 > loc) {
        // Overshot the location.
        lastDiff = aDiff;
        break;
      }
      last_chars1 = chars1;
      last_chars2 = chars2;
    }
    if (lastDiff != null && lastDiff.operation == Operation.DELETE) {
      // The location was deleted.
      return last_chars2;
    }
    // Add the remaining character length.
    return last_chars2 + (loc - last_chars1);
  }

  /**
   * Compute and return the source text (all equalities and deletions).
   * @param diffs LinkedList of Diff objects.
   * @return Source text.
   */
  public String diff_text1(LinkedList<Diff<T>> diffs) {
    StringBuilder text = new StringBuilder();
    for (Diff<T> aDiff : diffs) {
      if (aDiff.operation != Operation.INSERT) {
        text.append(codepointTupleToString( aDiff.text ));
      }
    }
    return text.toString();
  }

  /**
   * Compute and return the destination text (all equalities and insertions).
   * @param diffs LinkedList of Diff objects.
   * @return Destination text.
   */
  public String diff_text2(LinkedList<Diff<T>> diffs) {
    StringBuilder text = new StringBuilder();
    for (Diff<T> aDiff : diffs) {
      if (aDiff.operation != Operation.DELETE) {
        text.append(codepointTupleToString( aDiff.text ));
      }
    }
    return text.toString();
  }

  /**
   * Compute the Levenshtein distance; the number of inserted, deleted or
   * substituted characters.
   * @param diffs LinkedList of Diff objects.
   * @return Number of changes.
   */
  public int diff_levenshtein(LinkedList<Diff<T>> diffs) {
    int levenshtein = 0;
    int insertions = 0;
    int deletions = 0;
    for (Diff<T> aDiff : diffs) {
      switch (aDiff.operation) {
      case INSERT:
        insertions += aDiff.text.size();
        break;
      case DELETE:
        deletions += aDiff.text.size();
        break;
      case EQUAL:
        // A deletion and an insertion is one substitution.
        levenshtein += Math.max(insertions, deletions);
        insertions = 0;
        deletions = 0;
        break;
      }
    }
    levenshtein += Math.max(insertions, deletions);
    return levenshtein;
  }


  /**
   * Class representing one diff operation.
   */
  public static class Diff<T> {
  	
  	public static Diff<Integer> fromText(Operation operation, String text) {
  		return new Diff<Integer>(operation, tt(text), DiffPatch::equal_or_both_null);
  	}
  	
    /**
     * One of: INSERT, DELETE or EQUAL.
     */
    public Operation operation;
    /**
     * The text associated with this diff operation.
     */
    public KnittingTuple<T> text;
    
		private Equivalencer<T> equivalencer;

    /**
     * Constructor.  Initializes the diff with the provided values.
     * @param operation One of INSERT, DELETE or EQUAL.
     * @param text The text being applied.
     */
    public Diff(Operation operation, KnittingTuple<T> text, Equivalencer<T> equivalencer) {
      // Construct a diff with the specified operation and text.
      this.operation = operation;
      this.text = text;
			this.equivalencer = equivalencer;
    }

    
    /**
     * Display a human-readable version of this Diff.
     * @return text version.
     */
    public String toString() {
      String prettyText = this.text.toString( );
      return "Diff(" + this.operation + ",\"" + prettyText + "\")";
    }

    /**
     * Create a numeric hash value for a Diff.
     * This function is not used by DMP.
     * @return Hash value.
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = (operation == null) ? 0 : operation.hashCode();
      result += prime * ((text == null) ? 0 : text.hashCode());
      return result;
    }

    /**
     * Is this Diff equivalent to another Diff?
     * @param obj Another Diff to compare against.
     * @return true or false.
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Diff<?> other = (Diff<?>) obj;
      if (operation != other.operation) {
        return false;
      }
      if (text == null) {
        if (other.text != null) {
          return false;
        }
      } else if (!text.equivalentTo(other.text, equivalencer)) {
        return false;
      }
      return true;
    }
  }



  public static <T> boolean equal_or_both_null(T first, Object second) {
  	if (first == null && second == null) return true;
  	if (first == null || second == null) return false;
  	return first.equals(second);
  }

}