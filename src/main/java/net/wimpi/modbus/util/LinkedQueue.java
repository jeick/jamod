/***
 * Copyright 2002-2010 jamod development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***/

/***
 * Copied with style from
 * Lea, Doug: "Concurrent Programming in Java: Design Principles and Patterns",
 * Second Edition, Addison-Wesley, ISBN 0-201-31009-0, November 1999
 ***/
package net.wimpi.modbus.util;

public class LinkedQueue {

	/**
	 * The head of the list.
	 **/
	protected LinkedNode m_Head;

	/**
	 * Helper monitor for managing access to the list.
	 **/
	private final Object m_PutLock = new Object();

	/**
	 * The last node of the list.
	 **/
	protected LinkedNode m_Tail;

	/**
	 * The number of threads waiting for a take. Notifications are provided in
	 * put only if greater than zero. The bookkeeping is worth it here since in
	 * reasonably balanced usages, the notifications will hardly ever be
	 * necessary, so the call overhead to notify can be eliminated.
	 **/
	protected int m_WaitingForTake = 0;

	public LinkedQueue() {
		m_Head = null;
		m_Tail = m_Head;
	}// constructor

	/** Main mechanics for put/offer **/
	protected void insert(Object x) {
		synchronized (m_PutLock) {
			LinkedNode p = new LinkedNode(x);
			if (m_Tail != null) {
				m_Tail.m_NextNode = p;
				m_Tail = p;
			} else {
				m_Head = p;
				m_Tail = p;
			}
			if (m_WaitingForTake > 0)
				m_PutLock.notify();
		}
	}// insert

	/** Main mechanics for take/poll **/
	protected Object extract() {
		synchronized (m_PutLock) {
			if (m_Head == null) {
				return null;
			}
			Object x = m_Head.m_Node;
			m_Head = m_Head.m_NextNode;
			if (m_Head == null)
				m_Tail = null;
			return x;
		}
	}// extract

	/**
	 * Adds an object to this queue
	 * 
	 * @param x
	 *            The object to add to the queue
	 * @throws InterruptedException
	 *             In the event that this thread is interrupted while waiting
	 *             for the queue's lock
	 */
	public void put(Object x) throws InterruptedException {
		if (x == null)
			throw new IllegalArgumentException();
		insert(x);
	}// put

	/**
	 * Blocks until an object is available to be taken from the queue
	 * 
	 * @return The head of the queue.
	 * @throws InterruptedException
	 *             If the thread is interrupted while waiting for something to
	 *             be put on the queue.
	 */
	public Object take() throws InterruptedException {
		Object x = extract();
		if (x != null)
			return x;
		else {
			synchronized (m_PutLock) {
				try {
					++m_WaitingForTake;
					while (true) {
						m_PutLock.wait();
						x = extract();
						if (x != null) {
							--m_WaitingForTake;
							return x;
						}
					}
				} catch (InterruptedException ex) {
					--m_WaitingForTake;
					m_PutLock.notify();
					throw ex;
				}
			}
		}
	}// take

	/**
	 * Returns the head of the queue (if it exists) without removing it from the
	 * queue.
	 * 
	 * @return The head of the queue, or null if it is empty.
	 */
	public Object peek() {
		synchronized (m_PutLock) {
			if (m_Head == null)
				return null;
			return m_Head.m_Node;
		}
	}// peek

	/**
	 * Returns true if the queue is empty, false otherwise.
	 * 
	 * @return true if the queu is empty.
	 */
	public boolean isEmpty() {
		synchronized (m_PutLock) {
			return m_Head == null;
		}
	}// isEmpty

	/**
	 * Polls the queue until an object is received or the number of milliseconds
	 * has elapsed. If msecs is negative, it is essentially an unlimted wait and
	 * behaves as {@link #take()} would. Otherwise, this method polls for the
	 * passed in number of milliseconds (which can be 0). If an object is
	 * received before the timeout, then it is returned. Otherwise, null is
	 * returned.
	 * 
	 * @param msecs
	 *            The timeout in milliseconds.
	 * @return The next object in the queue, or null if the timeout elapses
	 * @throws InterruptedException
	 *             If the thread is interrupted while waiting.
	 */
	public Object poll(long msecs) throws InterruptedException {
		if (msecs < 0)
			return take();
		Object x = extract();
		if (x != null || msecs == 0) {
			return x;
		} else {
			synchronized (m_PutLock) {
				try {
					long timeLeft = msecs;
					long start = System.currentTimeMillis();
					++m_WaitingForTake;
					x = null;
					while (timeLeft > 0) {
						m_PutLock.wait(timeLeft);
						x = extract();
						if (x != null) {
							break;
						}
						timeLeft = msecs + start - System.currentTimeMillis();
					}
					--m_WaitingForTake;
					return x;
				} catch (InterruptedException ex) {
					--m_WaitingForTake;
					m_PutLock.notify();
					throw ex;
				}
			}
		}
	}// poll

}// LinkedQueue

