/*
 * Diff
 * 
 * Copyright 2017 Marco Trevisan
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

package org.github.evenjn.knit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;

import org.github.evenjn.knit.DiffOp.Operation;
import org.github.evenjn.yarn.Equivalencer;

class Diff {
	
	static <T,Y> LinkedList<DiffOp<T,Y>> adiff_main_nc(
  		KnittingTuple<T> text1,
  		KnittingTuple<Y> text2,
  		Equivalencer<T,Y> equivalencer,
      long deadline) {
    // Check for null inputs.
    if (text1 == null || text2 == null) {
      throw new IllegalArgumentException("Null inputs. (adiff_main)");
    }

    // Check for equality (speedup).
    LinkedList<DiffOp<T,Y>> diffs;
    if (text1.equivalentTo(text2, equivalencer)) {
      diffs = new LinkedList<DiffOp<T,Y>>();
      if (text1.size() != 0) {
        diffs.add(DiffOp.equal(text1, text2));
      }
      return diffs;
    }

    // Trim off common prefix (speedup).
    int commonlength_p = text1.longestCommonPrefix(text2, equivalencer);
    KnittingTuple<T> commonprefix1 = text1.head(0, commonlength_p);
    KnittingTuple<Y> commonprefix2 = text2.head(0, commonlength_p);
    text1 = text1.headless(commonlength_p);
    text2 = text2.headless(commonlength_p);

    // Trim off common suffix (speedup).
    int commonlength_s = text1.longestCommonSuffix(text2, equivalencer);
    KnittingTuple<T> commonsuffix1 = text1.tail(0, commonlength_s);
    KnittingTuple<Y> commonsuffix2 = text2.tail(0, commonlength_s);
    text1 = text1.tailless(commonlength_s);
    text2 = text2.tailless(commonlength_s);

    // Compute the diff on the middle block.
    diffs = adiff_compute_nc(text1, text2, equivalencer, deadline);

    // Restore the prefix and suffix.
    if (commonlength_p != 0) {
      diffs.addFirst(DiffOp.equal(commonprefix1, commonprefix2));
    }
    if (commonlength_s != 0) {
      diffs.addLast(DiffOp.equal(commonsuffix1, commonsuffix2));
    }

    adiff_cleanupMerge(diffs, equivalencer);
    return diffs;
  }

	static <T,Y> LinkedList<DiffOp<T,Y>> adiff_compute_nc(
  		KnittingTuple<T> text1,
  		KnittingTuple<Y> text2,
  		Equivalencer<T,Y> equivalencer,
  		long deadline) {
		LinkedList<DiffOp<T,Y>> diffs = new LinkedList<DiffOp<T,Y>>();

    if (text1.size() == 0) {
      // Just add some text (speedup).
      diffs.add(DiffOp.insert(text2));
      return diffs;
    }

    if (text2.size() == 0) {
      // Just delete some text (speedup).
      diffs.add(DiffOp.delete(text1));
      return diffs;
    }
    
    text1 = KnittingTuple.wrap( text1.collect( new ArrayList<>() ) );
    text2 = KnittingTuple.wrap( text2.collect( new ArrayList<>() ) );

    if (text1.size() > text2.size()) {
      Optional<Integer> opt = text1.findSubtuple(text2, 0, equivalencer);
      if (opt.isPresent()) {
      	int i = opt.get();
        // Shorter text is inside the longer text (speedup).
        diffs.add(DiffOp.delete(text1.head(0, i)));	
        diffs.add(DiffOp.equal(text1.head(i, text2.size()),text2));
        diffs.add(DiffOp.delete(text1.headless(i + text2.size())));
        return diffs;
      }

      if (text2.size() == 1) {
        // Single character string.
        // After the previous speedup, the character can't be an equality.
        diffs.add(DiffOp.delete(text1));
        diffs.add(DiffOp.insert(text2));
        return diffs;
      }
    }
    else {
      Optional<Integer> opt = text2.findSubtuple(text1, 0, equivalencer.transpose( ));
      if (opt.isPresent()) {
      	int i = opt.get();
        // Shorter text is inside the longer text (speedup).
        diffs.add(DiffOp.insert(text2.head(0, i)));
        diffs.add(DiffOp.equal(text1, text2.head(i, text1.size())));
        diffs.add(DiffOp.insert(text2.headless(i + text1.size())));
        return diffs;
      }

      if (text1.size() == 1) {
        // Single character string.
        // After the previous speedup, the character can't be an equality.
        diffs.add(DiffOp.delete(text1));
        diffs.add(DiffOp.insert(text2));
        return diffs;
      }
    }

    return adiff_bisect(text1, text2, equivalencer, deadline);
  }

	static <T,Y> LinkedList<DiffOp<T,Y>> adiff_bisect(
  		KnittingTuple<T> text1,
  		KnittingTuple<Y> text2,
  		Equivalencer<T,Y> equivalencer,
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
      if (deadline >= 0 && System.currentTimeMillis() > deadline) {
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
              return adiff_bisectSplit(text1, text2, equivalencer, x1, y1, deadline);
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
               && equivalencer.equivalent(
              		 text1.get(text1_length - x2 - 1),
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
              return adiff_bisectSplit(text1, text2, equivalencer, x1, y1, deadline);
            }
          }
        }
      }
    }
    // Diff took too long and hit the deadline or
    // number of diffs equals number of characters, no commonality at all.
    LinkedList<DiffOp<T,Y>> diffs = new LinkedList<DiffOp<T,Y>>();
    diffs.add(DiffOp.delete(text1));
    diffs.add(DiffOp.insert(text2));
    return diffs;
  }

	private static <T,Y> LinkedList<DiffOp<T,Y>> adiff_bisectSplit(
  		KnittingTuple<T> text1,
  		KnittingTuple<Y> text2,
  		Equivalencer<T,Y> equivalencer,
      int x,
      int y,
      long deadline) {
  	KnittingTuple<T> text1a = text1.head(0, x);
  	KnittingTuple<Y> text2a = text2.head(0, y);
    KnittingTuple<T> text1b = text1.headless(x);
    KnittingTuple<Y> text2b = text2.headless(y);

    // Compute both diffs serially.
    LinkedList<DiffOp<T,Y>> diffs = adiff_main_nc(text1a, text2a, equivalencer, deadline);
    LinkedList<DiffOp<T,Y>> diffsb = adiff_main_nc(text1b, text2b, equivalencer, deadline);

    diffs.addAll(diffsb);
    return diffs;
  }

	static <T,Y> void adiff_cleanupMerge(
  		LinkedList<DiffOp<T,Y>> diffs,
  		Equivalencer<T,Y> equivalencer) {
  	diffs.add(DiffOp.equal(KnittingTuple.empty(),KnittingTuple.empty()));  // Add a dummy entry at the end.
  	ListIterator<DiffOp<T,Y>> pointer = diffs.listIterator();
    int count_delete = 0;
    int count_insert = 0;
    KnittingTuple<T> text_delete = KnittingTuple.empty();
    KnittingTuple<Y> text_insert = KnittingTuple.empty();
    DiffOp<T,Y> thisDiff = pointer.next();
    DiffOp<T,Y> prevEqual = null;
    int commonlength;
    while (thisDiff != null) {
      switch (thisDiff.getOperation()) {
      case INSERT:
        count_insert++;
        text_insert = text_insert.append(thisDiff.getInsertedText());
        prevEqual = null;
        break;
      case DELETE:
        count_delete++;
        text_delete = text_delete.append(thisDiff.getDeletedText());
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
            commonlength = text_insert.longestCommonPrefix(text_delete, equivalencer.transpose());
            if (commonlength != 0) {
              if (pointer.hasPrevious()) {
                thisDiff = pointer.previous();
                assert thisDiff.getOperation() == Operation.EQUAL
                       : "Previous diff should have been an equality.";
                KnittingTuple<T> t_f = thisDiff.getTextFront().append(text_delete.head(0, commonlength));
                KnittingTuple<Y> t_b = thisDiff.getTextBack().append(text_insert.head(0, commonlength));
                thisDiff.setEqualText(t_f, t_b);
                pointer.next();
              } else {
                pointer.add(DiffOp.equal(
                		text_delete.head(0, commonlength),
                    text_insert.head(0, commonlength)));
              }
              text_insert = text_insert.headless(commonlength);
              text_delete = text_delete.headless(commonlength);
            }
            // Factor out any common suffixies.
            commonlength = text_insert.longestCommonSuffix(text_delete, equivalencer.transpose());
            if (commonlength != 0) {
              thisDiff = pointer.next();
              assert thisDiff.getOperation() == Operation.EQUAL
                  : "this Diff should have been an equality.";
              KnittingTuple<T> text_1 = thisDiff.getTextFront().prepend(text_delete.tail(0, commonlength));
              KnittingTuple<Y> text_2 = thisDiff.getTextBack().prepend(text_insert.tail(0, commonlength));
              thisDiff.setEqualText(text_1, text_2);
              text_insert = text_insert.tailless(commonlength);
              text_delete = text_delete.tailless(commonlength);
              pointer.previous();
            }
          }
          // Insert the merged records.
          if (text_delete.size() != 0) {
            pointer.add(DiffOp.delete(text_delete));
          }
          if (text_insert.size() != 0) {
            pointer.add(DiffOp.insert(text_insert));
          }
          // Step forward to the equality.
          thisDiff = pointer.hasNext() ? pointer.next() : null;
        } else if (prevEqual != null) {
          // Merge this equality with the previous one.
        	KnittingTuple<T> text_1 = prevEqual.getTextFront().append(thisDiff.getTextFront());
					KnittingTuple<Y> text_2 = prevEqual.getTextBack().append(thisDiff.getTextBack());
					prevEqual.setEqualText(text_1, text_2);
          pointer.remove();
          thisDiff = pointer.previous();
          pointer.next();  // Forward direction
        }
        count_insert = 0;
        count_delete = 0;
        text_delete = KnittingTuple.empty();
        text_insert = KnittingTuple.empty();
        prevEqual = thisDiff;
        break;
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
    if (diffs.getLast().getEqualSize() == 0) {
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
    DiffOp<T,Y> prevDiff = pointer.hasNext() ? pointer.next() : null;
    thisDiff = pointer.hasNext() ? pointer.next() : null;
    DiffOp<T,Y> nextDiff = pointer.hasNext() ? pointer.next() : null;
    // Intentionally ignore the first and last element (don't need checking).
    while (nextDiff != null) {
      if (prevDiff.getOperation() == Operation.EQUAL &&
          nextDiff.getOperation() == Operation.EQUAL) {
        // This is a single edit surrounded by equalities.
        if ((thisDiff.getOperation() == Operation.DELETE
        		&& thisDiff.getDeletedText().endsWith(prevDiff.getTextFront()))
        		|| (thisDiff.getOperation() == Operation.INSERT
        		&& thisDiff.getInsertedText().endsWith(prevDiff.getTextBack()))) {
          // Shift the edit over the previous equality.
        	if (thisDiff.getOperation() == Operation.DELETE) {
            thisDiff.setDeletedText(prevDiff.getTextFront().append(
                thisDiff.getDeletedText().tailless(prevDiff.getTextFront().size())));
        	}
        	else {
            thisDiff.setInsertedText(prevDiff.getTextBack().append(
                thisDiff.getInsertedText().tailless(prevDiff.getTextBack().size())));
        	}
          KnittingTuple<T> text_1 = prevDiff.getTextFront().append(nextDiff.getTextFront());
          KnittingTuple<Y> text_2 = prevDiff.getTextBack().append(nextDiff.getTextBack());
          nextDiff.setEqualText(text_1, text_2);
          pointer.previous(); // Walk past nextDiff.
          pointer.previous(); // Walk past thisDiff.
          pointer.previous(); // Walk past prevDiff.
          pointer.remove(); // Delete prevDiff.
          pointer.next(); // Walk past thisDiff.
          thisDiff = pointer.next(); // Walk past nextDiff.
          nextDiff = pointer.hasNext() ? pointer.next() : null;
          changes = true;
        } else if ((thisDiff.getOperation() == Operation.INSERT
        					&& thisDiff.getInsertedText().startsWith(nextDiff.getTextBack()))
        					|| (thisDiff.getOperation() == Operation.DELETE
        					&& thisDiff.getDeletedText().startsWith(nextDiff.getTextFront()))) {
          // Shift the edit over the next equality.
          KnittingTuple<T> ndt_front = prevDiff.getTextFront().append(nextDiff.getTextFront());
          KnittingTuple<Y> ndt_back = prevDiff.getTextBack().append(nextDiff.getTextBack());
          prevDiff.setEqualText(ndt_front, ndt_back);
        	if (thisDiff.getOperation() == Operation.DELETE) {
            thisDiff.setDeletedText(thisDiff.getDeletedText().headless(nextDiff.getTextFront().size()).append(
                nextDiff.getTextFront()));
        	}
        	else {
            thisDiff.setInsertedText(thisDiff.getTextBack().headless(nextDiff.getTextBack().size()).append(
                nextDiff.getTextBack()));
        	}
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
      adiff_cleanupMerge(diffs, equivalencer);
    }
  }
}
