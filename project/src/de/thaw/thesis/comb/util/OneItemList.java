/* encoding UTF-8
 * 
 * written 2013 by Arne Johannessen
 * 
 * This file is in the Public Domain.
 */

package de.thaw.thesis.comb.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;


/**
 * One-item implementation of the <code>List</code> interface. Implements all
 * optional list operations, except those which would change the number of list
 * items. Permits all elements, including <code>null</code>.
 * <p>
 * All operations run in constant time.
 * 
 * @author Arne Johannessen
 */
public final class OneItemList<E> extends AbstractList<E> implements RandomAccess {
	
	
	final private E item;
	
	
	/**
	 * Constructs a list with exactly the specified object as its content.
	 * @param item the single list item to be the content of this list
	 */
	public OneItemList (final E item) {
		super();
		this.item = item;
	}
	
	
	/**
	 * Since instances of this class cannot be modified in size, this method
	 * always throws an Exception.
	 * @throws UnsupportedOperationException
	 */
	public boolean add (final E e) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Returns the element at the specified position in this list.
	 * @param index index of the element to return
	 * @return the element at the specified position in this list
	 * @throws IndexOutOfBoundsException unless <code>index == 0</code>
	 */
	public E get (final int index) {
		if (index != 0) {
			throw new IndexOutOfBoundsException();
		}
		return item;
	}
	
	
	/**
	 * Returns an iterator over the one element in this list.
	 * @return an iterator over the one element in this list
	 */
	public Iterator<E> iterator () {
		return listIterator();
	}
	
	
	/**
	 * Returns an iterator over the one element in this list.
	 * @param index irst element to be returned from the list iterator (by a
	 *  call to the <code>next</code> method)
	 * @return a list iterator over the one element in this list
	 * @throws IndexOutOfBoundsException unless <code>index == 0</code>
	 */
	public ListIterator<E> listIterator (final int index) {
		if (index != 0) {
			throw new IndexOutOfBoundsException();
		}
		return new OneItemListIterator();
	}
	
	
	/**
	 * Returns <code>1</code>.
	 * @return the number of elements in this list
	 */
	public int size () {
		return 1;
	}
	
	
	/**
	 * Returns <code>false</code>.
	 * @return <code>false</code>
	 */
	public boolean isEmpty () {
		return false;
	}
	
	
	/**
	 * Returns <code>true</code> if this list contains the specified element.
	 * More formally, returns <code>true</code> if and only if this list's one
	 * element is identical to the object specified.
	 * @param o element whose presence in this list is to be tested
	 * @return <code>true</code> if this list contains the specified element
	 */
	public boolean contains (final Object o) {
		return item == o;
	}
	
	
	/**
	 * Returns an array containing the one element in this list.
	 * @return a safe array of size <code>1</code> containing exactly the one
	 *  element in this list.
	 * @see java.util.Arrays#asList(Object[])
	 */
	public Object[] toArray () {
		return new Object[]{item};
	}
	
	
	/**
	 * Returns an array containing the one element in this list; the runtime
	 * type of the returned array is that of the specified array. If the
	 * specified array is of length <code>1</code>, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the length <code>1</code>.
	 * @param a the array into which the element of this list are to be
	 *  stored, if it is big enough; otherwise, a new array of the same runtime
	 *  type is allocated for this purpose.
	 * @return an array containing the element of this list
	 * @throws ArrayStoreException if the runtime type of the specified array
	 *  is not a supertype of the runtime type of the element in this list
	 * @throws NullPointerException if the specified array is null
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray (T[] a) {
		if (a.length == 1) {
			a[0] = (T)item;
			return a;
		}
		// code adapted from java.util.AbstractCollection
		final T[] r = (T[])java.lang.reflect.Array
				  .newInstance(a.getClass().getComponentType(), 1);
		r[0] = (T)item;
		return r;
	}
	
	
	/**
	 * Since instances of this class cannot be modified in size, this method
	 * always throws an Exception.
	 * @throws UnsupportedOperationException
	 */
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Since instances of this class cannot be modified in size, this method
	 * always throws an Exception.
	 * @throws UnsupportedOperationException
	 */
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Returns a string representation of this collection. The string
	 * representation consists of the collection's element enclosed in square
	 * brackets (<code>"[]"</code>). Elements are converted to strings as by
	 * {@link String#valueOf(Object)}.
	 * @return a string representation of this collection
	 */
	public String toString() {
		return "[" + String.valueOf(item) + "]";
	}
	
	
	/**
	 * Iterator for the one-item list.
	 */
	private class OneItemListIterator implements ListIterator<E> {
		
		int cursor = 0;
		
		public void add (final E e) {
			throw new UnsupportedOperationException();
		}
		
		public boolean hasNext () {
			return cursor == 0;
		}
		
		public boolean hasPrevious () {
			return cursor == 1;
		}
		
		public E next () {
			if (cursor != 0) {
				throw new NoSuchElementException();
			}
			cursor = 1;
			return item;
		}
		
		public int nextIndex () {
			return cursor;
		}
		
		public E previous () {
			if (cursor != 1) {
				throw new NoSuchElementException();
			}
			cursor = 0;
			return item;
		}
		
		public int previousIndex () {
			return cursor - 1;
		}
		
		public void remove () {
			throw new UnsupportedOperationException();
		}
		
		public void set (final E e) {
			throw new UnsupportedOperationException();
		}
	}
	
}
