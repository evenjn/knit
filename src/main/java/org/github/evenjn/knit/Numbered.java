package org.github.evenjn.knit;

/**
 * A {@code Numbered} object wraps an object together with an integer label
 * associated to it.
 *
 * @param <K>
 *          The type of wrapped element.
 * @since 1.0
 */
public interface Numbered<K> {

	/**
	 * <p>
	 * Returns the wrapped object.
	 * </p>
	 * 
	 * @return The wrapped object.
	 * @since 1.0
	 */
	public K get( );

	/**
	 * <p>
	 * Returns the integer label associated with the wrapped object.
	 * </p>
	 * 
	 * @return The integer label associated with the wrapped object.
	 * @since 1.0
	 */
	public int getNumber( );

}
