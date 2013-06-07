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

package net.wimpi.modbus.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>WriteSingleRegisterResponse</tt>. The implementation
 * directly correlates with the class 0 function <i>write single register (FC
 * 6)</i>. It encapsulates the corresponding response message.
 * 
 * @author Dieter Wimberger
 * @version @version@ (@date@)
 */
public final class WriteSingleRegisterResponse extends ModbusResponse {

	// instance attributes
	private int m_RegisterValue;

	/**
	 * Constructs a new <tt>WriteSingleRegisterResponse</tt> instance.
	 */
	public WriteSingleRegisterResponse() {
		super();
		setDataLength(4);
	}// constructor

	/**
	 * Constructs a new <tt>WriteSingleRegisterResponse</tt> instance.
	 * 
	 * @param reference
	 *            the offset of the register written.
	 * @param value
	 *            the value of the register.
	 */
	public WriteSingleRegisterResponse(int reference, int value) {
		super();
		setReference(reference);
		setRegisterValue(value);
		setDataLength(4);
	}// constructor

	/**
	 * Returns the value that has been returned in this
	 * <tt>WriteSingleRegisterResponse</tt>.
	 * <p>
	 * 
	 * @return the value of the register.
	 */
	public int getRegisterValue() {
		return m_RegisterValue;
	}// getValue

	/**
	 * Sets the value that has been returned in the response message.
	 * <p>
	 * 
	 * @param value
	 *            the returned register value.
	 */
	private void setRegisterValue(int value) {
		m_RegisterValue = value;
	}// setRegisterValue

	public void writeData(DataOutput dout) throws IOException {
		dout.writeShort(getReference());
		dout.writeShort(getRegisterValue());
	}// writeData

	public void readData(DataInput din) throws IOException {
		setReference(din.readUnsignedShort());
		setRegisterValue(din.readUnsignedShort());
		// update data length
		setDataLength(4);
	}// readData

	public String toString() {
		return "WriteSingleRegisterResponse - Ref: " + getReference()
				+ " Value: " + m_RegisterValue;
	}

	/*
	 * protected void assembleData() throws IOException {
	 * m_DataOut.writeShort(getReference());
	 * m_DataOut.writeShort(getRegisterValue()); }//assembleData
	 * 
	 * protected void readData(DataInputStream in) throws EOFException,
	 * IOException {
	 * 
	 * setReference(in.readUnsignedShort());
	 * setRegisterValue(in.readUnsignedShort()); //update data length
	 * setDataLength(4); }//readData
	 */

}// class WriteSingleRegisterResponse
