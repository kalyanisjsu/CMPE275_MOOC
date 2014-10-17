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
package poke.server.management.managers;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import com.rabbitmq.client.Channel;
import eye.Comm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.LeaderFunctions;
import poke.server.ServerHandler;
import poke.server.ServerNodeInfo;
import poke.server.VotingConnection;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.resources.ResourceFactory;

import eye.Comm.JobBid;
import eye.Comm.JobProposal;
import eye.Comm.Management;

/**
 * The job manager class is used by the system to assess and vote on a job. This
 * is used to ensure leveling of the servers take into account the diversity of
 * the network.
 * 
 * @author gash
 * 
 */
public class JobManager {
	protected static Logger logger = LoggerFactory.getLogger("management");
	protected static AtomicReference<JobManager> instance = new AtomicReference<JobManager>();

	private String nodeId;
	private int counter = 0;
	
	ServerConf conf;
    VotingConnection vc;
    String nodeIp;
    String host;

	public static JobManager getInstance(String id) {
		
		instance.compareAndSet(null, new JobManager(id));
		return instance.get();
	}

	public static JobManager getInstance() {
		return instance.get();
	}

	public JobManager(String nodeId) {
		this.nodeId = nodeId;
		this.conf = ResourceFactory.cfg;
	}
	
	public void processRequest(Management mgmtReq, JobProposal req) {
		
		logger.info("\nJob proposal received\n");
		counter = 0;
		String bidType = new String();
		JobBid jobBid = null;
		
		if (req.getNameSpace().equals("Job Proposal - Voting"))
		{
			jobBid = createJobBidVoting (req);
		}
		
		else if (req.getNameSpace().equals("Job Proposal - Request"))
		{			
			jobBid = createJobBidRequest(req);
		}
		forwardJobBid (jobBid);
	}

	private eye.Comm.JobBid createJobBidVoting(JobProposal req) {
		
		logger.info("Creating job bid for voting");
		Random randomGenerator = new Random();
		int bidValue = randomGenerator.nextInt(2);
		
		JobBid.Builder bidBuilder = eye.Comm.JobBid.newBuilder();
		bidBuilder.setNameSpace("Job Bid - Voting");
		bidBuilder.setOwnerId(ServerNodeInfo.nodeId);
		bidBuilder.setJobId(req.getJobId());
		bidBuilder.setBid(bidValue);
		bidBuilder.setCounter(counter);
		
		return bidBuilder.build();
	}
	
	private eye.Comm.JobBid createJobBidRequest(JobProposal req) {
		
		logger.info("Node " + ServerNodeInfo.nodeId  + " is creating job bid for request");
		int bidValue = calculateBidValue();
		
		JobBid.Builder bidBuilder = eye.Comm.JobBid.newBuilder();
		bidBuilder.setNameSpace("Job Bid - Request");
		bidBuilder.setOwnerId(ServerNodeInfo.nodeId);
		bidBuilder.setJobId(req.getJobId());
		bidBuilder.setBid(bidValue);
		bidBuilder.setCounter(0);
		
		return bidBuilder.build();		
	}

	private int calculateBidValue() {
		return ServerNodeInfo.totalJobWeight;	
	}

	/**
	 * a job bid for my job
	 * 
	 * @param req
	 *            The bid
	 */
	public void processRequest(Management mgmtReq, JobBid req) {		
		logger.info("\nJob bid received\n");
		
		String bidType = req.getNameSpace();
		long ownerId =0;
		
		if (bidType.equals("Job Bid - Voting")){
			forwardJobBidVoting(req);
		}
		else if (bidType.equals("Job Bid - Request")){
			forwardJobBidRequest(req);
		}
	}
	
	public void forwardJobBidVoting(JobBid req){
		logger.info("Creating job bid for voting");
		
		Random randomGenerator = new Random();
		int bidValue = randomGenerator.nextInt(2) + req.getBid();	
		
		JobBid.Builder bidBuilder = eye.Comm.JobBid.newBuilder();
		bidBuilder.setNameSpace("Job Bid - Voting");
		bidBuilder.setOwnerId(ServerNodeInfo.nodeId);
		bidBuilder.setJobId(req.getJobId());
		bidBuilder.setBid(bidValue);
		bidBuilder.setCounter(req.getCounter()+1);
		
		eye.Comm.JobBid newJobBid = bidBuilder.build();
		
		//if not leader - forward to nearest node 
		if (!ServerNodeInfo.isLeader(this.nodeId))		{
			logger.info("Node not leader. Will forward job bid");
			logger.info("**Job Bid message: " + newJobBid);
			forwardJobBid (newJobBid);
		}
		
		else	{
			logger.info("Node is leader. Will send init voting if majority is yes");
			//if (isMajority(req.getBid()))
				sendInitVotingResponse(req.getJobId());
		}			
	}
	
	private boolean isMajority(int finalBid){
		if (finalBid > counter/2)
			return true;
		else
			return false;
	}
	
	private void forwardJobBidRequest(JobBid req) {
		logger.info("Forward job bid for request");
		
		//if not leader - forward to nearest node 
		if (!ServerNodeInfo.isLeader(this.nodeId))
		{
			logger.info("Node not leader. Will forward job bid");
			
			int incomingBid = req.getBid();
			int bidValue = 0;
			String ownerId = new String();
			if (incomingBid < ServerNodeInfo.totalJobWeight){
				bidValue = incomingBid;
				ownerId = req.getOwnerId();
			}
			else		{
				bidValue = ServerNodeInfo.totalJobWeight;
				ownerId = ServerNodeInfo.nodeId;
			}
			
			//set owner id as node id
			JobBid.Builder bidBuilder = eye.Comm.JobBid.newBuilder();
			bidBuilder.setNameSpace("Job Bid - Request");
			bidBuilder.setOwnerId(ownerId);
			bidBuilder.setJobId(req.getJobId());
			bidBuilder.setBid(bidValue);
			bidBuilder.setCounter(0);
			eye.Comm.JobBid newJobBid = bidBuilder.build();
			
			
			forwardJobBid (newJobBid);
		}
		else
		{			
			logger.info("Node is leader. Setting node Id to send request");
			String id = req.getOwnerId();
			logger.info("Node id is " + id);
			LeaderFunctions.getInstance().setForwardNode(id);
			LeaderFunctions.getInstance().startPublisher();
		}
	}
	

	private void sendInitVotingResponse(String id) {

        Comm.Request.Builder reqClient = Comm.Request.newBuilder();
        Comm.Header.Builder headClient = Comm.Header.newBuilder();
        Comm.Payload.Builder payClient = Comm.Payload.newBuilder();

        logger.info("Create new init voting response");
		// TODO Auto-generated method stub
		String host = conf.getServer().getProperty("host");
		String port = conf.getServer().getProperty("port");

        eye.Comm.InitVoting.Builder votingResponseBuilder = eye.Comm.InitVoting.newBuilder();
		votingResponseBuilder.setVotingId(id);
		votingResponseBuilder.setHostIp(host);
		votingResponseBuilder.setPortIp(port);

        payClient.setInitVoting(votingResponseBuilder);
        headClient.setRoutingId(Comm.Header.Routing.JOBS);
        headClient.setTag("Voting");
        headClient.setOriginator("server");
        reqClient.setBody(payClient.build());
        reqClient.setHeader(headClient.build());

        Comm.Request req = reqClient.build();

        logger.info("----------->Reply with the Sent initvoting  to hosting server");
        io.netty.channel.Channel cha = ServerHandler.clientChannel.get("server");
        if(cha.isOpen()){
            logger.info("----------->Reply with the Sent initvoting  to hosting server-- sentttttt" + req);
            logger.info("THe channel -->" + cha);
            //ServerHandler.clientChannel.get("server").write(reqClient);
            String h2 = cha.remoteAddress().toString();
            String[] arr = h2.replace("/","").split(":");
            try{
            logger.info("the remote address is "+ cha.remoteAddress().toString());
            logger.info("the remote address is is "+ arr[0]);
            h2 = arr[0];
            int p = Integer.parseInt(arr[1]);
            this.vc = new VotingConnection(h2,7000);
            this.vc.sendMessage(req);
            }
            catch (Exception e){
             logger.info("Exception in JobManager" + e);
            }
        }
		
	}

	private void forwardJobBid(JobBid newJobBid) {
		
		//Send it to nearest node
		
		logger.info("\nForward Bid\n");
		
		for (NodeDesc nn : conf.getNearest().getNearestNodes().values()) 
		{
			logger.info("Nearest node is "+nn.getNodeId());
			logger.info("nearest node host is "+nn.getHost());
			logger.info("Nearest node port is "+nn.getPort() );
			this.vc=new VotingConnection(nn.getHost(),nn.getMgmtPort());
		}
		
		eye.Comm.Management.Builder managementBuilder = eye.Comm.Management.newBuilder();
		managementBuilder.setJobBid(newJobBid);
		eye.Comm.Management message = managementBuilder.build();
		try {
			logger.info("send job bid");
			
			vc.sendMessage(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
