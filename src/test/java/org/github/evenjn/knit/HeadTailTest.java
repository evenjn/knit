package org.github.evenjn.knit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HeadTailTest {


	@Test
	public void test( ) {
		KnittingCursable<Integer> t1 = KnittingCursable.on( 1, 2, 3, 4 );
		assertEquals( "head", KnittingCursable.on( 1, 2 ), KnittingCursable.on( 1, 2 ) );
		assertEquals( "head", t1.head( 0, 2 ), KnittingCursable.on( 1, 2 ) );
		assertEquals( "head", t1.head( 1, 3 ), KnittingCursable.on( 2, 3, 4 ) );
		assertEquals( "tail", t1.tail( 0, 2 ), KnittingCursable.on( 3, 4 ) );
		assertEquals( "tail", t1.tail( 1, 3 ), KnittingCursable.on( 1, 2, 3 ) );
		assertEquals( "headless", t1.headless( 2 ), KnittingCursable.on( 3, 4 ) );
		assertEquals( "tailless", t1.tailless( 2 ), KnittingCursable.on( 1, 2 ) );
	}
}

