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
package poke.server;

import eye.Comm;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;










import com.google.protobuf.GeneratedMessage;

import eye.Comm.JobProposal;
import poke.server.conf.NodeDesc;
import poke.server.conf.NodeDescription;
import poke.server.conf.ServerConf;
import poke.server.management.managers.HeartbeatData;
import poke.server.management.managers.HeartbeatManager;
import poke.server.queue.ChannelQueue;
import poke.server.queue.QueueFactory;
import poke.server.resources.ResourceFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * As implemented, this server handler does not share queues or worker threads
 * between connections. A new instance of this class is created for each socket
 * connection.
 * 
 * This approach allows clients to have the potential of an immediate response
 * from the server (no backlog of items in the queue); within the limitations of
 * the VM's thread scheduling. This approach is best suited for a low/fixed
 * number of clients (e.g., infrastructure).
 * 
 * Limitations of this approach is the ability to support many connections. For
 * a design where many connections (short-lived) are needed a shared queue and
 * worker threads is advised (not shown).
 * 
 * @author gash
 * 
 */
public class ServerHandler extends SimpleChannelInboundHandler<eye.Comm.Request> {
	protected static Logger logger = LoggerFactory.getLogger("server-ServerHandler");

	private ChannelQueue queue;
    protected ConcurrentMap<String, Listner> listeners = new ConcurrentHashMap<String, Listner>();
    
    private String nearestHost;
    private int nearestPort;
    VotingConnection vc;
    ServerConf conf;
    static Channel responseChannel;
    public static HashMap<String ,Channel> clientChannel = new HashMap<String, Channel>();


	public ServerHandler() {
		logger.info("** ServerHandler created **");
		//init();
	}
	
	public void init()
	{
		
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, eye.Comm.Request req)  {
		// processing is deferred to the worker threads
		responseChannel = ctx.channel();

		try
        {
            logger.info("------>Message received is "+req);
            logger.info("--------------->Channel Details" + responseChannel);
			logger.info("------>Before check - Leader is "+ServerNodeInfo.getLeaderId());

            if(req.hasHeader() && !req.getHeader().getOriginator().equals("server")){
                if(!clientChannel.containsKey("client"))
                clientChannel.put("client",responseChannel);
            }
            else{
                if(!clientChannel.containsKey("server"))
                clientChannel.put("server",responseChannel);
            }


			/*if( req.getBody().hasInitVoting() && ServerNodeInfo.isLeader(ServerNodeInfo.nodeId))
			{
				
				logger.info("Request has voting field set");
				//Get fields 
				String id = req.getBody().getInitVoting().getVotingId();
				
				//Generate job proposal
				JobProposal.Builder jp=eye.Comm.JobProposal.newBuilder();
				jp.setNameSpace("Job Proposal - Voting");
				jp.setOwnerId(ServerNodeInfo.nodeId);
				jp.setJobId(id);
				jp.setWeight(0);
				
				eye.Comm.JobProposal jobProp=jp.build();
				logger.info("Job Proposal is "+jobProp);
				
				
				eye.Comm.Management.Builder m=eye.Comm.Management.newBuilder();
				m.setJobPropose(jobProp);
				eye.Comm.Management msg=m.build();
				
				VotingConnection.count=1;
				//Send it to nearest node
				this.conf = ResourceFactory.cfg;
				for (NodeDesc nn : conf.getNearest().getNearestNodes().values()) 
				{
					logger.info("Nearest node is "+nn.getNodeId());
					logger.info("nearest node host is "+nn.getHost());
					logger.info("Nearest node port is "+nn.getPort() );
					this.nearestHost=nn.getHost();
					this.nearestPort=nn.getPort();
					VotingConnection.genMsg=msg;
					logger.info("Saving message "+VotingConnection.genMsg);
					this.vc=new VotingConnection(nearestHost,nn.getMgmtPort());
				}
				this.vc.sendMessage(msg);
			}
			
			else if (!req.getBody().hasInitVoting() && ServerNodeInfo.isLeader(ServerNodeInfo.nodeId)){
				//not voting competition
				logger.info("######request received . ");*/
				queueInstance(ctx.channel()).enqueueRequest(req, ctx.channel());
			//}
		
		}
		catch(Exception e)
		{
			logger.info("***** - Exception in server handler read! ****");
			e.printStackTrace();
		}
	}
	
	
	public boolean send(Channel ch, GeneratedMessage msg)
	{
		
		logger.info("Message is " + msg);
		if(ch==null)
		{
			logger.info("Channel is null");
		}
		
		ChannelFuture chFuture=ch.writeAndFlush(msg);
		logger.info("Channel is "+ch);
		logger.info("wrote message to channel");
		
		
		if (chFuture.isDone() && !chFuture.isSuccess()) {
			logger.info("Failed!!!");
			logger.error("failed to poke!");
			return false;
		}
		logger.info("message sent");

		return true;
	}
	
	
	


    public void addListener(Listner listener) {
        if (listener == null)
            return;
        listeners.putIfAbsent(listener.getListenerID(), listener);
    }

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Server Unexpected exception from downstream.", cause  );
		ctx.close();
	}

	/**
	 * Isolate how the server finds the queue. Note this cannot return null.
	 * 
	 * @param channel
	 * @return
	 */
	private ChannelQueue queueInstance(Channel channel) {
		// if a single queue is needed, this is where we would obtain a
		// handle to it.

		if (queue != null)
			return queue;
		else {
			queue = QueueFactory.getInstance(channel);

			// on close remove from queue
			channel.closeFuture().addListener(new ConnectionClosedListener(queue));
		}
		return queue;
	}

	public static class ConnectionClosedListener implements ChannelFutureListener {
		private ChannelQueue sq;

		public ConnectionClosedListener(ChannelQueue sq) {
			this.sq = sq;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			// Note re-connecting to clients cannot be initiated by the server
			// therefore, the server should remove all pending (queued) tasks. A
			// more optimistic approach would be to suspend these tasks and move
			// them into a separate bucket for possible client re-connection
			// otherwise discard after some set period. This is a weakness of a
			// connection-required communication design.

			if (sq != null)
				sq.shutdown(true);
			sq = null;
		}

		
	}
	
	public void handleResponse(eye.Comm.Request request){	
//		if (responseChannel != null){
//			queueInstance(responseChannel).enqueueResponse(request, responseChannel);
//		}
	}
}
