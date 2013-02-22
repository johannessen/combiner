/* encoding UTF-8
 * 
 * written 2013 by Arne Johannessen
 * 
 * This file is in the Public Domain.
 */

package de.thaw.thesis.comb.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;


/**
 * An <code>Iterator</code> behaving like a FIFO queue. Adheres to the general
 * contract of the <code>Iterator</code> interface, but offers methods to
 * safely append items to the iterator while the iteration is ongoing.
 * <p>
 * This iterator/queue does not support <code>null</code> elements
 * (for performance reasons).
 * 
 * @author Arne Johannessen
 */
public class MutableIterator<E> implements Iterator {
	
	// :BUG: expensive; what we really need is a working iterator for a mutable queue, not a mutable iterator
	
	private final Queue<E> queue;
	
	
	/**
	 * Creates a new instance as empty FIFO queue. In this initial state,
	 * {@link #hasNext()} returns false and {@link #next()} throws an
	 * exception. Use {@link #add(Object)} to append items to the queue.
	 */
	public MutableIterator () {
		queue = new LinkedList<E>();
	}
	
	
	/**
	 * Appends the offered element to the tail of this queue.
	 * 
	 * @throws NullPointerException iff <code>item == null</code>
	 */
	public void add (final E item) {
		if (item == null) {
			/* If we supported null elements, we would have to change hasNext()
			 * to catch an exception. Because normal use of an iterator calls
			 * that method very frequently and catching excpetions is very
			 * expensive, we have to refuse allowing null elements in order to
			 * offer reasonable performance. Not accepting null elements is
			 * explicitly allowed for implementors of the Java Collections
			 * framework.
			 */
			throw new NullPointerException();
		}
		queue.add(item);
	}
	
	
	/**
	 * Appends the offered elements to the tail of this queue (in the order
	 * they are returned by their iterator).
	 * 
	 * @throws NullPointerException if any of the elements are <code>null</code>
	 * @see Collection#iterator()
	 */
	public void addAll (final Collection<? extends E> items) {
		// :BUG: expensive; write our own Queue implementation and work directly on its Entry objects
		// use this.add() instead of queue.addAll() to defend against null elements
		for (final E item : items) {
			add(item);
		}
	}
	
	
	/**
	 * Returns <code>true</code> if the iteration has more elements. (In other
	 * words, returns <code>true</code> if next would return an element rather
	 * than throwing an exception.)
	 * <p>
	 * If this method returns <code>false</code>, it means that the queue
	 * is currently empty. Even after calls to <code>hasNext()</code> or
	 * <code>next()</code> have returned <code>false</code> or thrown
	 * <code>NoSuchElementException</code>s, respectively, it is allowed to
	 * use {@link #add(Object)} and <code>addAll()</code> to append some more
	 * elements to the queue. At that point, this method will once again return
	 * <code>true</code> and calling <code>next()</code> will work as expected.
	 * 
	 * @return <code>true</code> if the iterator has more elements
	 */
	public boolean hasNext () {
		return queue.peek() != null;
	}
	
	
	/**
	 * Retrieves and removes the head of this queue.
	 * 
	 * @return the head of this queue
	 * @throws NoSuchElementException if this queue is currently empty
	 */
	public E next () {
		return queue.remove();
	}
	
	
	/**
	 * Always throws an <code>UnsupportedOperationException</code>.
	 * <p>
	 * Because of this class's queue-like character, a call to
	 * <code>next()</code> automatically removes the element returned from
	 * the backing collection. (This happens internally, the collection(s)
	 * used to initialise this instance are not affected.) The
	 * {@link Iterator#remove()} method being specified to remove
	 * the <em>last</em> element previously returned by calling
	 * <code>next()</code>, calling this method would mean to remove it twice.
	 * It throws an exception instead of just doing nothing for forward
	 * compatibility.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void remove () {
		throw new UnsupportedOperationException();
	}
	
}
