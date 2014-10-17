package poke.server;

import java.util.concurrent.atomic.AtomicReference;

import com.rabbitmq.client.AMQP;

import poke.rabbitmq.BroadcastClient;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.management.managers.HeartbeatConnector;
import poke.server.resources.RabbitMQIP;
import poke.server.resources.ResourceFactory;
import eye.Comm.JobProposal;
import eye.Comm.Request;

public class LeaderFunctions
{
	protected static AtomicReference<LeaderFunctions> instance = new AtomicReference<LeaderFunctions>();
	ServerInfo serverInfo;
	VotingConnection vc;
	String forwardNodeId;
	Request request; 
	
	public static LeaderFunctions getInstance() {
		instance.compareAndSet(null, new LeaderFunctions());
		return instance.get();
	}
	
	public void setForwardNode (String nodeId){
			this.forwardNodeId = nodeId;
	}
	
	public LeaderFunctions()
	{
	}
	
	public JobProposal generateJobProposal(eye.Comm.Request req){
		
		//create job proposal and forward to nearest node
		int jobWeight = 0;
		String id = "1";
		
		if (req.getHeader().getTag().equals("SignIn"))
			ServerNodeInfo.totalJobWeight += 1;
		else if (req.getHeader().getTag().equals("SignUp"))
			ServerNodeInfo.totalJobWeight += 2;
		else if (req.getHeader().getTag().equals("SearchCourse"))
			ServerNodeInfo.totalJobWeight += 3;
		else if (req.getHeader().getTag().equals("Enroll"))
			ServerNodeInfo.totalJobWeight += 4;
		else if (req.getHeader().getTag().equals("CourseList"))
			ServerNodeInfo.totalJobWeight += 5;
		
		JobProposal.Builder jp=eye.Comm.JobProposal.newBuilder();
		jp.setNameSpace("Job Proposal - Request");
		jp.setOwnerId(ServerNodeInfo.nodeId);
		jp.setJobId(id);
		jp.setWeight(jobWeight);
		
		return jp.build();
	}
	
	public void forwardRequest (eye.Comm.Request req){

		//serverInfo.initializeNodeInfoTable();
		
//		for (String key : serverInfo.getNodeInfoTable().keySet() ){
//			if (serverInfo.getNodeInfoTable().get(key).equalsIgnoreCase("available")){
//				forwardNodeId = new String (key);
//				break;
//			}
//		}
		//determing forward node id based on job bids
		//create job proposal
		
		this.request = req;
		JobProposal jobProposal = generateJobProposal(req);
		
		eye.Comm.Management.Builder m=eye.Comm.Management.newBuilder();
		m.setJobPropose(jobProposal);
		eye.Comm.Management msg=m.build();
		
		ServerConf conf = ResourceFactory.cfg;
		for (NodeDesc nn : conf.getNearest().getNearestNodes().values()) 
		{
//			logger.info("Nearest node is "+nn.getNodeId());
//			logger.info("nearest node host is "+nn.getHost());
//			logger.info("Nearest node port is "+nn.getPort() );
			
			
			VotingConnection.genMsg=msg;
			System.out.println("In leader functions - Message saved as "+VotingConnection.genMsg);
			this.vc=new VotingConnection(nn.getHost(),nn.getMgmtPort());
		}
		try {
			this.vc.sendMessage(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void startPublisher (){
		Request req = this.request;
		Request.Builder r2 = req.toBuilder();
		r2.setBody(req.getBody());
		eye.Comm.Header h2 = req.getHeader();	
		
		eye.Comm.Header.Builder hb2 = h2.newBuilder();		
		hb2.setOriginator(h2.getOriginator());
		hb2.setTag(h2.getTag());
		hb2.setTime(h2.getTime());
		hb2.setRoutingId(h2.getRoutingId());
		hb2.setToNode(forwardNodeId);		
		
		r2.setHeader(hb2.build());
		Request newReq = r2.build();
		 /* ***********************/
		
		System.out.println("***************");	
		
		String host = RabbitMQIP.rabbitMQIP;
		String user = "test";
		String passwd = "test";
		BroadcastClient client = new BroadcastClient(host, AMQP.PROTOCOL.PORT, user, passwd);
		client.sendMessage(newReq, forwardNodeId);
	}
	
	public void getNodeResponse (){
		String responseNodeId = new String();
		serverInfo.setNodeAvailable(responseNodeId);
	}
}