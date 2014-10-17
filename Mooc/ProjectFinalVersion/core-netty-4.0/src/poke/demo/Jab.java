/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.demo;

import poke.client.ClientCommand;
import poke.client.ClientPrintListener;
import poke.client.comm.CommListener;
import poke.server.management.managers.ClientListener;
import poke.server.management.managers.HeartbeatListener;

/**
 * DEMO: how to use the command class
 * 
 * @author gash
 * 
 */
public class Jab {
	private String tag;
	private int count;

	public Jab(String tag) {
		this.tag = tag;
	}

	public void run() {
		ClientCommand cc = new ClientCommand("192.168.0.93", 5570);
		

		for (int i = 0; i < 1; i++) {
			count++;
			cc.poke(tag, 1);
		}
	}

	public static void main(String[] args) {
		try {
			Jab jab = new Jab("jab");
			jab.run();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
