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

package net.wimpi.modbus.util;

import java.util.ArrayList;

/**
 * Class implementing a simple thread pool.
 *
 * @author Dieter Wimberger
 * @version @version@ (@date@)
 */
public class ThreadPool {

  //instance attributes and associations
  private LinkedQueue m_TaskPool;
	private ArrayList<PoolThread> m_Threads;
  private int m_Size = 1;

  /**
   * Constructs a new <tt>ThreadPool</tt> instance.
   *
   * @param size the size of the thread pool.
   */
  public ThreadPool(int size) {
    m_Size = size;
    m_TaskPool = new LinkedQueue();
		m_Threads = new ArrayList<PoolThread>();
    initPool();
  }//constructor

  /**
   * Execute the <tt>Runnable</tt> instance
   * through a thread in this <tt>ThreadPool</tt>.
   *
   * @param task the <tt>Runnable</tt> to be executed.
   */
  public synchronized void execute(Runnable task) {
    try {
      m_TaskPool.put(task);
    } catch (InterruptedException ex) {
      //FIXME: Handle!?
    }
  }//execute

  /**
   * Initializes the pool, populating it with
   * n started threads.
   */
  protected void initPool() {
    for (int i = m_Size; --i >= 0;) {
			PoolThread toAdd = new PoolThread().start();
			m_Threads.add(toAdd);
    }
  }//initPool

	/**
	 * Stops the pool, cleaning up the threads
	 */
	protected void killPool() {
		for (PoolTread p : m_Threads) {
			jj
		}
	}

  /**
   * Inner class implementing a thread that can be
   * run in a <tt>ThreadPool</tt>.
   *
   * @author Dieter Wimberger
   * @version @version@ (@date@)
   */
  private class PoolThread extends Thread {
		private boolean keepRunning;
		private Runnable task;

    /**
     * Runs the <tt>PoolThread</tt>.
     * <p>
     * This method will infinitely loop, picking
     * up available tasks from the <tt>LinkedQueue</tt>.
     */
    public void run() {
      //System.out.println("Running PoolThread");
			setRunning(true);
      do {
        try {
          //System.out.println(this.toString());
					synchronized(task) {
						task = (Runnable)m_TaskPool.take();
						task.run();
					}
          ((Runnable) m_TaskPool.take()).run();
        } catch (Exception ex) {
          //FIXME: Handle somehow!?
          ex.printStackTrace();
        }
      } while (isRunning());
    }
		public void setRunning(boolean run) {
			synchronized (keepRunning){
				keepRunning = run;
			}
		}
		public boolean isRunning() {
			synchronized (keepRunning) {
				return keepRunning;
			}
		}
  }//PoolThread

}//ThreadPool
