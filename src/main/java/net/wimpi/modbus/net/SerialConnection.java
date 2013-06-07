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

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.util.SerialParameters;

import java.io.IOException;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Class that implements a serial connection which can be used for master and
 * slave implementations.
 * 
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Charles Hache
 * @version @version@ (@date@)
 */
public class SerialConnection {

	private SerialParameters m_Parameters;
	private ModbusSerialTransport m_Transport;
	private SerialPort m_SerialPort;
	private boolean m_Open;

	/**
	 * Creates a SerialConnection object and initilizes variables passed in as
	 * params.
	 * 
	 * @param parameters
	 *            A SerialParameters object.
	 */
	public SerialConnection(SerialParameters parameters) {
		m_Parameters = parameters;
		m_Open = false;
	}// constructor

	/**
	 * Returns the reference to the SerialPort instance.
	 * 
	 * @return a reference to the <tt>SerialPort</tt>.
	 */
	public SerialPort getSerialPort() {
		return m_SerialPort;
	}// getSerialPort

	/**
	 * Returns the <tt>ModbusTransport</tt> instance to be used for receiving
	 * and sending messages.
	 * 
	 * @return a <tt>ModbusTransport</tt> instance.
	 */
	public ModbusTransport getModbusTransport() {
		return m_Transport;
	}// getModbusTransport

	/**
	 * Opens the communication port.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void open() throws Exception {
		
		// 1. create the port
		m_SerialPort = new SerialPort(m_Parameters.getPortName());

		// 2. set the parameters, open the port
		try {
			setConnectionParameters();
			m_SerialPort.openPort();
		} catch (Exception e) {
			// ensure it is closed
			m_SerialPort.closePort();
			if (Modbus.debug)
				System.out.println(e.getMessage());
			throw e;
		}

		if (Modbus.SERIAL_ENCODING_ASCII.equals(m_Parameters.getEncoding())) {
			m_Transport = new ModbusASCIITransport();
		} else if (Modbus.SERIAL_ENCODING_RTU
				.equals(m_Parameters.getEncoding())) {
			m_Transport = new ModbusRTUTransport();
			setReceiveTimeout(m_Parameters.getReceiveTimeout());
																
		} else if (Modbus.SERIAL_ENCODING_BIN
				.equals(m_Parameters.getEncoding())) {
			m_Transport = new ModbusBINTransport();
		}
		m_Transport.setEcho(m_Parameters.isEcho());

		// Open the input and output streams for the connection. If they won't
		// open, close the port before throwing an exception.
		try {
			m_Transport.setSerialPort(m_SerialPort);
		} catch (IOException e) {
			m_SerialPort.closePort();
			if (Modbus.debug)
				System.out.println(e.getMessage());

			throw new Exception("Error opening i/o streams");
		}
		// System.out.println("i/o Streams prepared");

		m_Open = true;
	}// open

	public void setReceiveTimeout(int ms) {
		// Set receive timeout to allow breaking out of polling loop during
		// input handling.
		m_Transport.setReceiveTimeout(ms);
	}// setReceiveTimeout

	/**
	 * Sets the connection parameters to the setting in the parameters object.
	 * 
	 * @throws Exception
	 *             if the configured parameters cannot be set properly on the
	 *             port.
	 */
	public void setConnectionParameters() throws Exception {

		// Set connection parameters
		try {
			m_SerialPort.setParams(m_Parameters.getBaudRate(),
					m_Parameters.getDatabits(), m_Parameters.getStopbits(), 
					m_Parameters.getParity());
		} catch (SerialPortException e) {
			if (Modbus.debug)
				System.out.println(e.getMessage());

			throw new Exception(e);
		}

		// Set flow control.
		try {
			m_SerialPort.setFlowControlMode(m_Parameters.getFlowControlIn()
					| m_Parameters.getFlowControlOut());
		} catch (SerialPortException e) {
			if (Modbus.debug)
				System.out.println(e.getMessage());

			throw new Exception(e);
		}
	}// setConnectionParameters

	/**
	 * Close the port and clean up associated elements.
	 */
	public void close() {
		// If port is alread closed just return.
		if (!m_Open) {
			return;
		}

		// Check to make sure sPort has reference to avoid a NPE.
		if (m_SerialPort != null) {
			try {
				m_Transport.close();
			} catch (IOException e) {
				System.err.println(e);
			}
			// Close the port.
			try {
				m_SerialPort.closePort();
			} catch (SerialPortException e) {
			}
		}

		m_Open = false;
	}// close

	/**
	 * Reports the open status of the port.
	 * 
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpen() {
		return m_Open;
	}// isOpen

}// class SerialConnection
