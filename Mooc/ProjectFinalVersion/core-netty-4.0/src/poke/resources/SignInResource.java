package poke.resources;

import java.io.IOException;

import eye.Comm;
import poke.server.resources.LoginDetails;
import poke.server.resources.Resource;
import poke.server.storage.MongoConnection;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import eye.Comm.Header;
import eye.Comm.Header.Routing;
import eye.Comm.Payload;
import eye.Comm.Request;
import eye.Comm.SignIn;
import eye.Comm.SignUp;

public class SignInResource implements Resource{

	MongoConnection mongoConn;
	BasicDBObject dbObject;
	String uname, password,registeredUser,registeredPassword,json;
	Boolean passwordFlag = false;
	
	@Override
	public Request process(Request request) {		
		
		try {
			
			mongoConn = new MongoConnection();
			uname = request.getBody().getSignIn().getUserName();
			password = request.getBody().getSignIn().getPassword();
			json = "{'uname' :" + "'" + uname +"'"+ "," + "'password' :" + "'"+password +"'"+"}";
			dbObject = (BasicDBObject)JSON.parse(json);
				
			if(mongoConn.obtainConnection())
			{
				mongoConn.getCollection("login");
				boolean validity = CheckValidity();
				
				if(validity)
				{
					SignIn.Builder signInBuilder = SignIn.newBuilder();
					signInBuilder.setUserName("");
					Header.Builder header = Header.newBuilder();
					header.setTag("SignIn");
					header.setRoutingId(Routing.JOBS);
					header.setOriginator("server");
					if(passwordFlag)
					{
						signInBuilder.setPassword("");
						header.setReplyMsg("Wrong Password");
                        header.setReplyCode(Comm.PokeStatus.FAILURE);
					}
					else
					{
						signInBuilder.setPassword("");
						header.setReplyMsg("SignIn Succesfully");
                        header.setReplyCode(Comm.PokeStatus.SUCCESS);
					}
					Payload.Builder payLoad = Payload.newBuilder();
					payLoad.setSignIn(signInBuilder.build());
					Request.Builder rb = Request.newBuilder();
					rb.setBody(payLoad.build());
					rb.setHeader(header.build());
					Request reply = rb.build();
					return reply;
				}
				else if(!validity)
				{
					//mongoConn.insertData(dbObject);
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
					header.setReplyMsg("Wrong Values Entered");
                    header.setReplyCode(Comm.PokeStatus.FAILURE);
					Request.Builder rb = Request.newBuilder();
					rb.setBody(payLoad.build());
					rb.setHeader(header.build());
					Request reply = rb.build();
					return reply;
				}
			}
			else
				System.out.println("*****Error in DB********* ");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SignIn.Builder signBuilder = SignIn.newBuilder();
		signBuilder.setUserName("***UserName***");
		signBuilder.setPassword("***Correct Password***");
		Payload.Builder payLoad = Payload.newBuilder();
		payLoad.setSignIn(signBuilder.build());
		Header.Builder header = Header.newBuilder();
		header.setTag("SignIn");
		header.setRoutingId(Routing.PING);
		header.setOriginator("python client");
		
		Request.Builder rb = Request.newBuilder();
		rb.setBody(payLoad.build());
		rb.setHeader(header.build());
		Request reply = rb.build();
		return reply;
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
			registeredPassword = login.getPassword();
			if(registeredUser!= null)
			{
				if(!registeredPassword.equals(password))
				{   
					passwordFlag = true;
					System.out.println("Wrong Password");
				}
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
