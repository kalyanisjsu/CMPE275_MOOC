package poke.server;

import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerNodeInfo {
	
	public static String nodeId = new String();
    public static String leaderId;
    public static long lastModifiedDate =0;
    
    public static int nodeNum = 99;
    public static int totalJobWeight = 0;

	public static String myIp;
    public static String myport;
    public static String leaderPort;
    public static String leaderIpAddress;
    public static ConcurrentMap<String,String> all_leaderMap = new ConcurrentHashMap<String,String>();
    protected static Logger logger = LoggerFactory.getLogger("server-Initializer");

    private static ServerNodeInfo instance;

    public static ServerNodeInfo getInstance() {
        //logger.info(" I m in the server node");
        //if(instance == null)
        instance = new ServerNodeInfo();
        all_leaderMap.put("1","192.168.0.94");
        all_leaderMap.put("2","192.168.0.95");
        all_leaderMap.put("3","192.168.0.21");
        all_leaderMap.put("4","192.168.0.31");
        all_leaderMap.put("5","192.168.0.41");
        all_leaderMap.put("6","192.168.0.51");
        all_leaderMap.put("7","192.168.0.61");
        all_leaderMap.put("8","192.168.0.71");
        all_leaderMap.put("9","192.168.0.81");
        all_leaderMap.put("10","192.168.0.91");
        all_leaderMap.put("11","192.168.0.101");
        return instance;
    }

    public static void ServerNodeInfo(){
        //logger.info(" I m in the server node - 2");
        //all_leaderMap = new ConcurrentHashMap<Integer,String>();
        //all_leaderMap.put("1","192.168.0.92");
        /*all_leaderMap.put(2,"192.168.0.11");
        all_leaderMap.put(3,"192.168.0.21");
        all_leaderMap.put(4,"192.168.0.31");
        all_leaderMap.put(5,"192.168.0.41");
        all_leaderMap.put(6,"192.168.0.51");
        all_leaderMap.put(7,"192.168.0.61");
        all_leaderMap.put(8,"192.168.0.71");
        all_leaderMap.put(9,"192.168.0.81");
        all_leaderMap.put(10,"192.168.0.91");
        all_leaderMap.put(11,"192.168.0.101");*/

    }

    public static String getLeaderIpAddress() {

        return leaderIpAddress;
    }

    public static String getLeaderPort() {
        return leaderPort;
    }
    public static void setNodeNum (String nodeId){
    	if (nodeId.equals("zero"))
    		nodeNum = 1;
    	else if (nodeId.equals("one"))
    		nodeNum = 1;
    	else if (nodeId.equals("two"))
    		nodeNum = 2;
    	else if (nodeId.equals("three"))
    		nodeNum = 3;
    	else if (nodeId.equals("four"))
    		nodeNum = 4;
    	else
    		nodeNum = 9;
    }
    public static boolean isLeader(String nodeIdToCheck){

        leaderId = getLeaderId();
        if(nodeIdToCheck.equals(leaderId)){
            return true;
        }
        return false;
    }
    
    public static boolean isLeader(){

        leaderId = getLeaderId();
        if(nodeId.equals(leaderId)){
            return true;
        }
        return false;
    }

    public static String getLeaderId()
    {
        try{
           long currentLastModifiedTime = getLastModifiedDate();
            //here we have currentLastModifiedTime to be the whats the current LMT of the file, and we previously store
            //lastModifiedDate and check if it has been changed.
           if(lastModifiedDate != currentLastModifiedTime){

                accessFile();
            }
           return leaderId;
            }
        catch (Exception e){
            System.out.println("Exception in getting Leader");
            return  null;
        }

    }

    private static long getLastModifiedDate(){
        File file;
        try{

        file = new File("src/leader.txt");
        return file.lastModified();
        }
        catch (Exception e){
            return 0;
        }
    }

    public static void accessFile() throws IOException {
        System.out.println("Inside Access File");
        String[] leader;
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader("src/leader.txt"));
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                leader = sCurrentLine.split(":");

                leaderId = leader[1];
                leaderIpAddress = leader[2];
                leaderPort = leader[3];

                System.out.println("Leader id is  : "+ leaderId );
            }
            lastModifiedDate = getLastModifiedDate();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if(br != null) {
                br.close();
            }

        }

    }

    public static void writeLeaderIntoFile(String winnerNode, String winnerIp, String winnerPort) throws IOException{
        if(!winnerNode.equals(leaderId)){//only when the current winnerNode from already present leaderNode we write into the file.
        BufferedWriter out = null;
        try {

            out = new BufferedWriter(new FileWriter("src/leader.txt"));
            out.write("Leader:" + winnerNode + ":" + winnerIp +":" + winnerPort);
            lastModifiedDate = getLastModifiedDate();
            leaderId = winnerNode;
            leaderIpAddress = winnerIp;
            leaderPort = winnerPort;

        } catch (IOException e) {}
        finally {
            if(out != null){
                out.close();
            }
        }
      }
    }

}
