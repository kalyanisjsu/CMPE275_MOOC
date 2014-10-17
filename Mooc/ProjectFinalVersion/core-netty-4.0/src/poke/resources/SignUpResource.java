package poke.resources;

import java.io.IOException;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import eye.Comm;
import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.Request;
import eye.Comm.SignUp;
import eye.Comm.Header.Routing;
import poke.server.resources.LoginDetails;
import poke.server.resources.Resource;
import poke.server.storage.MongoConnection;

public class SignUpResource implements Resource {

	MongoConnection mongoConn;
	BasicDBObject dbObject;
	String uname, password,registeredUser,json;
		
	@Override
	public Request process(Request request) {
	
		try {
			
			mongoConn = new MongoConnection();
			uname = request.getBody().getSignUp().getUserName();
			password = request.getBody().getSignUp().getPassword();
			json = "{'uname' :" + "'" + uname +"'"+ "," + "'password' :" + "'"+password +"'"+"}";
			dbObject = (BasicDBObject)JSON.parse(json);
			
			if(mongoConn.obtainConnection())
			{
				mongoConn.getCollection("login");
				boolean validity = CheckValidity();
				if(validity)
				{
					SignUp.Builder signUpBuilder = SignUp.newBuilder();
					signUpBuilder.setUserName("");
					signUpBuilder.setPassword("");
					signUpBuilder.setFullName("");
					Header.Builder header = Header.newBuilder();
					header.setTag("SignUp");
					header.setRoutingId(Routing.JOBS);
					header.setOriginator("server");
					header.setReplyMsg("Already Registered Username. Sign Up with New Username");
                    header.setReplyCode(Comm.PokeStatus.FAILURE);
					Payload.Builder payLoad = Payload.newBuilder();
					payLoad.setSignUp(signUpBuilder.build());
					Request.Builder rb = Request.newBuilder();
					rb.setBody(payLoad.build());
					rb.setHeader(header.build());
					Request reply = rb.build();
					return reply;
				}
				else if(!validity)
				{
					mongoConn.insertData(dbObject);
					SignUp.Builder signUpBuilder = SignUp.newBuilder();
					signUpBuilder.setUserName("");
					signUpBuilder.setPassword("");
					signUpBuilder.setFullName("");
					Payload.Builder payLoad = Payload.newBuilder();
					payLoad.setSignUp(signUpBuilder.build());
					Header.Builder header = Header.newBuilder();
					header.setTag("SignUp");
					header.setRoutingId(Routing.JOBS);
					header.setOriginator("server");
					header.setReplyMsg("SignUp Successfully");
                    header.setReplyCode(Comm.PokeStatus.SUCCESS);
					Request.Builder rb = Request.newBuilder();
					rb.setBody(payLoad.build());
					rb.setHeader(header.build());
					Request reply = rb.build();
					return reply;
				}
			}
			else
				System.out.println("*****Error in DB********* ");
			
		} 
		catch (IOException e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	
	public boolean CheckValidity()
	{
		LoginDetails login = new LoginDetails();
		mongoConn.getCollection("login");
		login = mongoConn.getUsername(uname);
		registeredUser = login.getUname();
		if(login!=null)
		{
			registeredUser = login.getUname();
			if(registeredUser!= null)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	}
	
	

}
