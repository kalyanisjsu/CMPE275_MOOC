/*
 * copyright 2014, gash
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

package poke.server;


import java.text.SimpleDateFormat;
import java.util.Date;

import poke.rabbitmq.MQueueFactory;
import poke.rabbitmq.MQueueListener;
import poke.rabbitmq.MQueueSubscriber;

import com.rabbitmq.client.AMQP;


public class SubscriberTest extends MQueueListener {
	private SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
	private MQueueSubscriber sub;

	SubscriberTest() {
		String host = "localhost";
		String user = "test";
		String passwd = "test";

		MQueueFactory factory = new MQueueFactory(host, AMQP.PROTOCOL.PORT, user, passwd);
		sub = factory.createSubscriber("TestRequestQueue");
		sub.addListener(this);
	}

	@Override
	public void onMessage(eye.Comm.Request req, String topic) {
		System.out.println("MSG(" + topic + ") at " + fmt.format(new Date()) + " - " + req);
	}

	public void addBindingFilter(String v) {
		sub.addTopic(v);
	}

	public void demo() {

		try {
			sub.subscribe();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// banner
		System.out.print("** RabbitMQ pub-sub - Subscriber of ");
		for (String arg : args)
			System.out.print(arg + " ");
		System.out.println("**\n");

		SubscriberTest sa = new SubscriberTest();

		
		sa.addBindingFilter("test");

		sa.demo();
	}

}
