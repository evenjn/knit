package org.github.evenjn.knit;

/**
 * <p>
 * A {@code DiffPair} presents the result of a diff between two sequences of
 * objects.
 * </p>
 * 
 * <p>
 * A {@code DiffPair} holds two references to objects, referred to as the
 * <em>front</em> and the <em>back</em>. There are no restrictions on the
 * references. One or both references may be {@code null}, and both references
 * may point to the same object. The referred objects need not be immutable or
 * satisfy any particular constraint.
 * </p>
 * 
 * <p>
 * A {@code DiffPair} may have empty slots. The content of an empty slot is
 * {@code null}.
 * </p>
 * 
 * <p>
 * This class is part of package {@link org.github.evenjn.knit Knit}.
 * </p>
 *
 * @param <F>
 *          The type of the object in the <em>front</em> slot.
 * @param <B>
 *          The type of the object in the <em>back</em> slot.
 * @since 1.0
 */
public interface DiffPair<F, B> {

	/**
	 * @return The object in the <em>front</em> slot.
	 * @since 1.0
	 */
	F front( );

	/**
	 * @return The object in the <em>back</em> slot.
	 * @since 1.0
	 */
	B back( );

	/**
	 * @return {@code true} when the <em>back</em> slot is not empty.
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	boolean hasFront( );

	/**
	 * @return {@code true} when the <em>front</em> slot is not empty.
	 *         {@code false} otherwise.
	 * @since 1.0
	 */
	boolean hasBack( );

	/**
	 * @return {@code true} when both the <em>front</em> slot and the
	 *         <em>back</em> slot are not empty. {@code false} otherwise.
	 * @since 1.0
	 */
	boolean hasBoth( );
}
