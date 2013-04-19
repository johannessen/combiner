/* encoding UTF-8
 * 
 * written 2013 by Arne Johannessen
 * 
 * This file is in the Public Domain.
 */

package de.thaw.thesis.comb.util;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;


public class MutableIterator2<E> implements Iterator<E>, Iterable<E> {
	
	private final Queue<Collection<E>> queue = new LinkedList<Collection<E>>();
	
	private Iterator<E> currentIterator = null;
	
	private SortedSet<E> toRemove = null;
	
	private E next = null;
	
	private E previous = null;
	
	
	public MutableIterator2 () {
		super();
	}
	
	
	public MutableIterator2 (final Collection<E> collection) {
		super();
		add(collection);
	}
	
	
	public void add (final Collection<E> collection) {
		if (collection == null) {
			throw new NullPointerException();
		}
		queue.add(collection);
	}
	
	
	public boolean hasNext () {
		while (next == null) {
			if (currentIterator != null && ! currentIterator.hasNext()) {
				final Collection<E> completedCollection = queue.poll();
				runRemoves(completedCollection);
				currentIterator = null;
			}
			if (currentIterator == null) {
				if (queue.peek() == null) {
					return false;
				}
				currentIterator = queue.peek().iterator();
				continue;
			}
			next = currentIterator.next();
			if (toRemove != null && toRemove.contains(next)) {
				next = null;
			}
		}
		// at this point, this.next is properly initialised
		return true;
	}
	
	
	public E next () {
		if (! hasNext()) {
			throw new NoSuchElementException();
		}
		// this.next is properly initialised as a side-effect fo hasNext()
		previous = next;
		next = null;
		return previous;
	}
	
	
	public Iterator<E> iterator () {
		// makes instances directly usable in for-each loops
		return this;
	}
	
	
	public void close () {
		for (final Collection<E> collection : queue) {
			runRemoves(collection);
		}
		queue.clear();
		toRemove = null;
		currentIterator = null;
		next = null;
		previous = null;
	}
	
	
	private void runRemoves (final Collection<E> collection) {
		// no iterators may be active when this method is called
		if (toRemove == null) {
			return;
		}
		collection.removeAll(toRemove);
		toRemove.clear();
	}
	
	
	public int size () {
		int size = 0;
		for (Collection<E> collection : queue) {
			size += collection.size();
		}
		return size;
	}
	
	
	public void remove (E item) {
		// removes from all collections at the first chance
		if (toRemove == null) {
			toRemove = new TreeSet<E>(new HashCodeComparator());
		}
		toRemove.add(item);
	}
	
	
	public void remove () {
		remove(previous);
	}
	
	
	private class HashCodeComparator implements Comparator<E> {
		public int compare (E o1, E o2) {
			return o1.hashCode() <= o2.hashCode() ? o1.hashCode() < o2.hashCode() ? -1 : 0 : 1;
		}
	}
	
	
	public static void main (String[] args) {
		LinkedList<Object> a = new LinkedList<Object>();
		MutableIterator2<Object> q = new MutableIterator2<Object>();
		assert ! q.hasNext();
		
		for (int i = 0; i < 6; i++) {
			LinkedList<Object> l = new LinkedList<Object>();
			q.add(l);
			assert (i > 0) == q.hasNext() : i;
			
			for (int j = 0; j < 10; j++) {
				Object o = new Object();
				l.add(o);
				a.add(o);
			}
		}
		
		for (int i = 0; i < 13; i++) {
			assert q.hasNext() : i;
			q.next();
		}
		
		for (int i = 0; i < 13; i++) {
			q.next();
		}
		assert q.hasNext();
		
		for (int i = 0; i < 6; i++) {
			LinkedList<Object> l = new LinkedList<Object>();
			q.add(l);
			assert q.hasNext() : i;
			
			for (int j = 0; j < 10; j++) {
				Object o = new Object();
				l.add(o);
				a.add(o);
			}
		}
		assert q.hasNext();
		
		assert q.size() == 60-13-13+60 : q.size();
		
	}
// ( ant build && ( cd build/classes && java de.thaw.thesis.comb.util.IteratorQueue ))
	
}
