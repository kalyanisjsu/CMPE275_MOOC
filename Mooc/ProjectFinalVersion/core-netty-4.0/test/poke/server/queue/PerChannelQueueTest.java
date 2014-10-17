package poke.server.queue;

import static org.junit.Assert.*;
import io.netty.channel.Channel;

import java.util.concurrent.LinkedBlockingDeque;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.protobuf.GeneratedMessage;

import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.Request;
import poke.server.VotingConnection;
import poke.server.queue.PerChannelQueue.InboundWorker;


public class PerChannelQueueTest {
	
	
	private PerChannelQueue perchannelQueue=new PerChannelQueue();
	private Channel channel;
	private LinkedBlockingDeque<com.google.protobuf.GeneratedMessage> inbound;
	
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
		Request.Builder req=Request.newBuilder();
		
		Header.Builder h=Header.newBuilder();
		h.setOriginator("server");
		h.setTag("Voting");
		h.setRoutingId(eye.Comm.Header.Routing.PING);
		req.setHeader(h.build());
		
		Payload.Builder p=Payload.newBuilder();
		req.setBody(p.build());
		
		perchannelQueue.enqueueRequest(req.build(), channel);
		inbound=perchannelQueue.getInboundQueue();
		
		try {
			GeneratedMessage msg=inbound.take();
			System.out.println("Generated Message is "+msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
