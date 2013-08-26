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
 * 
 * Original implementation by jamod development team.
 * This file modified by Charles Hache <chache@brood.ca>
 ***/

package net.wimpi.modbus.util;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class implementing a simple thread pool.
 * 
 * @author Dieter Wimberger
 * @version @version@ (@date@)
 */
public class ThreadPool {

	// instance attributes and associations
	private final LinkedQueue m_TaskPool;
	private ArrayList<PoolThread> m_Threads;
	private int m_Size = 1;

	/**
	 * Constructs a new <tt>ThreadPool</tt> instance.
	 * 
	 * @param size
	 *            the size of the thread pool.
	 */
	public ThreadPool(int size) {
		m_Size = size;
		m_TaskPool = new LinkedQueue();
		m_Threads = new ArrayList<PoolThread>();
		initPool();
	}// constructor

	/**
	 * Execute the <tt>Runnable</tt> instance through a thread in this
	 * <tt>ThreadPool</tt>.
	 * 
	 * @param task
	 *            the <tt>Runnable</tt> to be executed.
	 */
	public void execute(Runnable task) {
		try {
			synchronized(m_TaskPool) {
				m_TaskPool.put(task);
			}
		} catch (InterruptedException ex) {
			// FIXME: Handle!?
		}
	}// execute

	/**
	 * Initializes the pool, populating it with n started threads.
	 */
	protected void initPool() {
		for (int i = m_Size; --i >= 0;) {
			PoolThread toAdd = new PoolThread();
			toAdd.start();
			m_Threads.add(toAdd);
		}
	}// initPool

	/**
	 * Stops the pool, cleaning up the threads
	 */
	public void killPool() {
		for (PoolThread p : m_Threads) {
			p.setRunning(false);
			p.interrupt();
		}
	}

	/**
	 * Inner class implementing a thread that can be run in a
	 * <tt>ThreadPool</tt>.
	 * 
	 * @author Dieter Wimberger
	 * @version @version@ (@date@)
	 */
	private class PoolThread extends Thread {
		private final AtomicBoolean keepRunning = new AtomicBoolean(false);
		private Runnable task;

		/**
		 * Runs the <tt>PoolThread</tt>.
		 * <p>
		 * This method will infinitely loop, picking up available tasks from the
		 * <tt>LinkedQueue</tt>.
		 */
		public void run() {
			// System.out.println("Running PoolThread");
			setRunning(true);
			do {
				try {
					task = (Runnable) m_TaskPool.take();
					task.run();
					
				} catch (Exception e) {
					// Ignore, we were likely just interrupted. Recheck if we
					// should be running or not.
				}
			} while (isRunning());
		}

		void setRunning(boolean run) {
			keepRunning.set(run);
		}

		boolean isRunning() {
			return keepRunning.get();
		}
	}// PoolThread

}// ThreadPool
