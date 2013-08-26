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

package net.wimpi.modbus.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.util.ThreadPool;

/**
 * Class that implements a ModbusTCPListener.<br>
 * If listening, it accepts incoming requests passing them on to be handled.
 * 
 * @author Dieter Wimberger
 * @version @version@ (@date@)
 */
public class ModbusTCPListener implements Runnable {

	private ServerSocket m_ServerSocket = null;
	private ThreadPool m_ThreadPool;
	private Thread m_Listener;
	private int m_Port = Modbus.DEFAULT_PORT;
	private int m_FloodProtection = 5;
	private final AtomicBoolean m_Listening;
	private InetAddress m_Address = null;

	/**
	 * Constructs a ModbusTCPListener instance.<br>
	 * 
	 * @param poolsize
	 *            the size of the <tt>ThreadPool</tt> used to handle incoming
	 *            requests.
	 */
	public ModbusTCPListener(int poolsize) {
		m_Listening = new AtomicBoolean(false);
		m_ThreadPool = new ThreadPool(poolsize);
		try {
			m_Address = InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			if (Modbus.debug)
				System.out.println("Couldn't get the local address: "
						+ ex.toString());
		}
	}// constructor

	/**
	 * Constructs a ModbusTCPListener instance.<br>
	 * 
	 * @param poolsize
	 *            the size of the <tt>ThreadPool</tt> used to handle incoming
	 *            requests.
	 * @param addr
	 *            the interface to use for listening.
	 */
	public ModbusTCPListener(int poolsize, InetAddress addr) {
		m_Listening = new AtomicBoolean(false);
		m_ThreadPool = new ThreadPool(poolsize);
		m_Address = addr;
	}// constructor

	/**
	 * Sets the port to be listened to.
	 * 
	 * @param port
	 *            the number of the IP port as <tt>int</tt>.
	 */
	public void setPort(int port) {
		m_Port = port;
	}// setPort

	/**
	 * Sets the address of the interface to be listened to.
	 * 
	 * @param addr
	 *            an <tt>InetAddress</tt> instance.
	 */
	public void setAddress(InetAddress addr) {
		m_Address = addr;
	}// setAddress

	/**
	 * Gets the address of the listening interface.
	 * 
	 * @return The address of the listening interface.
	 */
	public InetAddress getAddress() {
		return m_Address;
	}

	/**
	 * Starts this <tt>ModbusTCPListener</tt>.
	 */
	public void start() {
		m_Listener = new Thread(this);
		m_Listening.set(true);
		m_Listener.start();
	}// start

	/**
	 * Stops this <tt>ModbusTCPListener</tt>.
	 */
	public void stop() {
		m_Listening.set(false);
		if (m_ServerSocket != null) {
			try {
				m_ServerSocket.close();
				m_Listener.interrupt(); // Interrupting is required as well as
										// closing the socket
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}// stop

	/**
	 * Accepts incoming connections and handles then with
	 * <tt>TCPConnectionHandler</tt> instances.
	 */
	public void run() {

		/*
		 * A server socket is opened with a connectivity queue of a size
		 * specified in int floodProtection. Concurrent login handling under
		 * normal circumstances should be allright, denial of service attacks
		 * via massive parallel program logins can probably be prevented.
		 */
		try {
			m_ServerSocket = new ServerSocket(m_Port, m_FloodProtection,
					m_Address);
			if (Modbus.debug)
				System.out.println("Listenening to "
						+ m_ServerSocket.toString() + "(Port " + m_Port + ")");
		} catch (IOException e1) {
			System.err.println("Couldn't start TCP listener:");
			e1.printStackTrace();
			m_Listening.set(false);
		}
		
		Socket incoming = null;

		while (m_Listening.get()) {
			try {
				incoming = m_ServerSocket.accept();
				if (Modbus.debug)
					System.out.println("Making new connection "
							+ incoming.toString());
				if (m_Listening.get()) {
					// FIXME: Replace with object pool due to resource issues
					m_ThreadPool.execute(new TCPConnectionHandler(
							new TCPSlaveConnection(incoming)));
				}
				
				// We can get these exceptions while quitting. If so, hide the
				// error message. If the exception occurs during regular
				// operation though, print the message
			} catch (SocketException iex) {
				if (m_Listening.get()) {
					iex.printStackTrace();
				}
			} catch (IOException e) {
				if (m_Listening.get()) {
					e.printStackTrace();
				}
			}
		} //while listening
		
		if (Modbus.debug)
			System.out.println("ModbusTCPListener is quitting");
		
		if (incoming != null) {
			try {
				incoming.close();
			} catch (IOException e) {
				//Don't care.
			}
		}
		
		m_ThreadPool.killPool();
	}// run

	/**
	 * Tests if this <tt>ModbusTCPListener</tt> is listening and accepting
	 * incoming connections.
	 * 
	 * @return true if listening (and accepting incoming connections), false
	 *         otherwise.
	 */
	public boolean isListening() {
		return m_Listening.get();
	}// isListening

}// class ModbusTCPListener
