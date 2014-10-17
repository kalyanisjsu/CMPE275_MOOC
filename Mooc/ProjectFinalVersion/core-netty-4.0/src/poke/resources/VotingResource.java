package poke.resources;


import eye.Comm;
import eye.Comm.FindLeader;
import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.Ping;
import eye.Comm.Request;
import io.netty.bootstrap.ServerBootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.Server;
import poke.server.ServerHandler;
import poke.server.ServerNodeInfo;
import poke.server.VotingConnection;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;
import poke.server.resources.Resource;
import poke.server.resources.ResourceFactory;
import poke.server.resources.ResourceUtil;
import poke.server.storage.MongoConnection;

import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class VotingResource  implements Resource {

    protected static Logger logger = LoggerFactory.getLogger("server-VotingResource");

    public ServerConf conf;

    public ServerConf getCfg() {
        return conf;
    }

    public void setCfg(ServerConf cfg) {
        this.conf = cfg;
    }

    private String nearestHost;
    private int nearestPort;
    VotingConnection vc;


    @Override
    public Comm.Request process(Comm.Request request) {

        logger.info("In Voting Resource" + request);
        try{

        MongoConnection mongoConnection = new MongoConnection();

        if(mongoConnection.obtainConnection())
            logger.info("-------> Connection Established.");

        //This is Payload --> InitVoiting
        if(request.hasBody() && request.getBody().hasInitVoting())
                {
                    //ClusterHost ---> ClusterServe --> are you ready for Voting Competition
                    if(request.hasHeader()
                       && request.getHeader().getOriginator().equals("server"))
                       {

                           if(request.hasBody() && request.getBody().hasInitVoting()
                                  && !request.getBody().getInitVoting().hasHostIp()

                           ){

                                logger.info("<----------CLuster to Cluste INITVOTING REQ------------>");
                                //Get fields
                               logger.info("Request has voting field set");
                               //Get fields
                               String id = request.getBody().getInitVoting().getVotingId();

                               //Generate job proposal
                               Comm.JobProposal.Builder jp=eye.Comm.JobProposal.newBuilder();
                               jp.setNameSpace("Job Proposal - Voting");
                               jp.setOwnerId(ServerNodeInfo.nodeId);
                               jp.setJobId(id);
                               jp.setWeight(0);

                               eye.Comm.JobProposal jobProp= jp.build();

                               logger.info("Job Proposal is " + jobProp);


                               eye.Comm.Management.Builder m=eye.Comm.Management.newBuilder();
                               m.setJobPropose(jobProp);
                               eye.Comm.Management msg=m.build();

                               logger.info("Message -->" + msg);

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
                                   this.vc.sendMessage(msg);
                               }

                            return null;
                           }
                           //clusterB replies back with initvoting to the clusterhost.
                           else if(request.hasBody() && request.getBody().hasInitVoting()
                                   && request.getBody().getInitVoting().hasHostIp()
                                   )
                           {
                               logger.info("<----------CLuster Replies with the INITVOTING REsponse------------>");

                               io.netty.channel.Channel cha = ServerHandler.clientChannel.get("client");
                               if(cha.isOpen()){
                                   ServerHandler.clientChannel.get("client").writeAndFlush(request);
                               }

                           }

                    }
                    //Client  ---> Cluster - please ask other clusters to host a comp
                    else if(request.getHeader() != null & request.getHeader().getOriginator().equals("client"))
                    {
                        //This is when the client initiates a Voting
                        //First have to FindLeader
                        logger.info("<_---------------------Got Voting Request from Client----------------------->");
                        logger.info("<_---------------------So Find Leader Now            ----------------------->");

                       /* Comm.Request.Builder reqLeader = Comm.Request.newBuilder();
                        Comm.Header.Builder reqleaderHeader = Comm.Header.newBuilder();
                        Comm.Payload.Builder reqleaderPayload = Comm.Payload.newBuilder();

                        reqleaderHeader.setOriginator("server");
                        reqleaderHeader.setRoutingId(Comm.Header.Routing.JOBS);
                        reqleaderHeader.setTag("Voting");
                        reqLeader.setHeader(reqleaderHeader.build());

                        Comm.FindLeader.Builder find = Comm.FindLeader.newBuilder();

                        reqleaderPayload.setFindLeader(find.build());
                        reqLeader.setBody(reqleaderPayload.build());*/
                        FindLeader.Builder f = eye.Comm.FindLeader.newBuilder();
                		
                		
 
                		
//                		
                		// payload containing data
                		Request.Builder r = Request.newBuilder();
                		eye.Comm.Payload.Builder p = Payload.newBuilder();
                		
                		p.setFindLeader(f.build());
                		
                		//p.setGetCourse(gc.build());
                		r.setBody(p.build());
                		

                		// header with routing info
                		eye.Comm.Header.Builder h = Header.newBuilder();
                		h.setOriginator("server");
                		
                		h.setTag("Voting");
                		h.setTime(System.currentTimeMillis());
                		h.setRoutingId(eye.Comm.Header.Routing.JOBS);
                		r.setHeader(h.build());
                		
                		

                		eye.Comm.Request reqLeader = r.build();
                        
                        
                        logger.info("After building the find leader message");
                        logger.info("Message is "+reqLeader);

                        ServerNodeInfo obj = ServerNodeInfo.getInstance();

                        for(int i=1; i< obj.all_leaderMap.size(); i++){
                            String sendhost = obj.all_leaderMap.get(i);
                            logger.info("Sending it to" + sendhost);
                            int sendport = 7000;
                            this.vc=new VotingConnection(sendhost,sendport);
                            this.vc.sendMessage(reqLeader);
                        }

                        return null;

            }
            }
         else if(request.hasBody() && request.getBody().hasFindLeader())
            {
            //Cluster - FindLeader - Cluster
            if(!request.getBody().getFindLeader().hasLeaderIp()){
                // from a different Cluster requesting the FindLeader
                logger.info("<----------Got a FindLeader request------>>");

                logger.info("Leader ip is "+ServerNodeInfo.getLeaderIpAddress());
                logger.info("Port is "+ServerNodeInfo.getLeaderPort());
                

                //eye.Comm.Request request1 = ResourceUtil.buildVotingIsLeader(ServerNodeInfo.getLeaderIpAddress(), ServerNodeInfo.getLeaderPort());

                Request.Builder req = Request.newBuilder();
                Header.Builder header = Header.newBuilder();
                Payload.Builder payload = Payload.newBuilder();

                FindLeader.Builder f = FindLeader.newBuilder();
                f.setLeaderIp(ServerNodeInfo.getLeaderIpAddress());
                f.setLeaderPort(ServerNodeInfo.getLeaderPort());

                payload.setFindLeader(f.build());

                req.setBody(payload.build());

                header.setOriginator("server");
                header.setRoutingId(Header.Routing.JOBS);
                header.setTag("Voting");
                req.setHeader(header.build());


                io.netty.channel.Channel cha = ServerHandler.clientChannel.get("server");
                logger.info("Channel in got find leader request "+cha);
                Request rn= req.build();


                if(cha.isOpen()){
                    logger.info("Sending the leader details -->" + rn);
                    String host = cha.remoteAddress().toString();
                    String[] arr = host.replace("/","").split(":");

                    logger.info("the remote address is "+ cha.remoteAddress().toString());
                    logger.info("the remote address is is "+ arr[0]);
                    host = arr[0];

                    this.vc = new VotingConnection(host,7000);
                    this.vc.sendMessage(rn);

                    //ServerHandler.clientChannel.get("server").write(rn);
                    //168953862
                }
                
                return null;
            }
            //Cluster - FoundLeader - Cluster
            else
            {
                //Reply from the different cluster with their Leader and Save it in the HashMap where the request would be forwarded to.
                //should forward the InitVoting to the respective leaders and their port. That will create a channel.

                logger.info("<---------Got the Reply for FindLeader Hence Save it in HashMap------->>");
                String leaderid = request.getBody().getFindLeader().getLeaderIp();
                String portid = request.getBody().getFindLeader().getLeaderPort();
                logger.info("Leader id is "+leaderid);
                logger.info("Port id is "+portid);


                this.vc=new VotingConnection(leaderid,Integer.parseInt(portid));

                Comm.Request.Builder reqLeader = Comm.Request.newBuilder();
                Comm.Header.Builder reqleaderHeader = Comm.Header.newBuilder();
                Comm.Payload.Builder reqleaderPayload = Comm.Payload.newBuilder();

                reqleaderHeader.setOriginator("server");
                reqleaderHeader.setRoutingId(Comm.Header.Routing.JOBS);
                reqleaderHeader.setTag("Voting");
                reqLeader.setHeader(reqleaderHeader.build());

                Comm.InitVoting.Builder initVoting = Comm.InitVoting.newBuilder();

                reqleaderPayload.setInitVoting(initVoting);
                reqLeader.setBody(reqleaderPayload.build());

                ServerNodeInfo obj = ServerNodeInfo.getInstance();


                this.vc=new VotingConnection(leaderid, Integer.parseInt(portid));
                this.vc.sendMessage(reqLeader.build());
                //this.vc.sendMessage(reqLeader.build());
                return null;

           }
        }
        }
        catch (Exception e){
            logger.info("Exception in the VotingResource + " + e);
            e.printStackTrace();
            String statusMsg = "Unable to generate message";
            return null;
            //Comm.Request rtn = ResourceUtil.buildError(request.getHeader(), Comm.PokeStatus.NOREACHABLE, statusMsg);
            //return rtn;
        }
        //TODO REMOVE THIS
        //Comm.Request rtn = ResourceUtil.buildError(request.getHeader(), Comm.PokeStatus.NOREACHABLE, "test");
        return null;
    }


}
