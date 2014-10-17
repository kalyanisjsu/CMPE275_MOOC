package poke.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rabbitmq.client.AMQP;

import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.Request;
import poke.rabbitmq.MQueueFactory;
import poke.rabbitmq.MQueuePublisher;

public class PublishTest {
	
	MQueuePublisher pub;
	String topic = "test";
	MQueueFactory factory= new MQueueFactory("localhost", AMQP.PROTOCOL.PORT, "test", "test"); ;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		pub = factory.createPublisher("TestRequestQueue");
		topic = "test";
		Request.Builder r = Request.newBuilder();
		eye.Comm.Payload.Builder p = Payload.newBuilder();

		r.setBody(p.build());
		eye.Comm.Header.Builder h = Header.newBuilder();
		h.setOriginator("client");		
		h.setTag("Voting");
		h.setRoutingId(eye.Comm.Header.Routing.PING);
		r.setHeader(h.build());
		eye.Comm.Request req = r.build();
		try {
			pub.publish(req,topic);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n Message sent to "+  topic + "\n message is : " + req);
		
	
	}

}
