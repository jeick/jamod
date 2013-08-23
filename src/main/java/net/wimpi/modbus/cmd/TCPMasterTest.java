/***
 * Copyright 2002-2013 jamod development team
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

package net.wimpi.modbus.cmd;

import java.net.InetAddress;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;


/**
 * Sample of a Modbus/TCP master. Intended to connect
 * to the TCPSlaveTest.  Just runs for a bit and makes
 * a few requests, then exits.
 * 
 * @author Charles Hache
 * @version @version@ (@date@)
 */
public class TCPMasterTest {

	private static int requestNumber = 0;
	
	public static void main(String[] args) {
		int port = Modbus.DEFAULT_PORT;
		
		try {
			if (args != null && args.length == 1) {
				port = Integer.parseInt(args[0]);
			}
			TCPMasterConnection connection = new TCPMasterConnection(InetAddress.getLocalHost());
			connection.setTimeout(3000);
			connection.setPort(port);
			connection.connect();
			
			ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
			
			ModbusRequest request;
			while ((request = getNextRequest()) != null) {
				transaction.setRequest(request);
				transaction.execute();
				ModbusResponse response = transaction.getResponse();
				gotResponse(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void gotResponse(ModbusResponse response) {
		System.out.println("Got response: "+response.toString());
	}

	private static ModbusRequest getNextRequest() {
		//Note: simple process image uses 0-based register addresses
		switch (requestNumber++) {
		case 0:
			return new WriteCoilRequest(0,true);
		case 1:
			return new ReadCoilsRequest(0, 2);
		case 2:
			return new ReadInputDiscretesRequest(0,4);
		case 3:
			return new ReadInputRegistersRequest(0,1);
		case 4:
			return new ReadMultipleRegistersRequest(0,1);
		case 5:
			Register r = ModbusCoupler.getReference().getProcessImageFactory().createRegister();
			r.setValue(420);
			return new WriteSingleRegisterRequest(0,r);
		case 6:
			return new ReadMultipleRegistersRequest(0,1);
		default:
			return null;
		}
	}
	
}
