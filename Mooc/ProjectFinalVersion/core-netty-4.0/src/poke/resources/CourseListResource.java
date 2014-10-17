package poke.resources;

import java.io.IOException;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import eye.Comm;
import poke.server.resources.CourseDetails;
import eye.Comm.GetCourse;
import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.Request;
import eye.Comm.RequestList;
import eye.Comm.Header.Routing;
import poke.server.resources.Resource;
import poke.server.storage.MongoConnection;

public class CourseListResource implements Resource {

	MongoConnection mongoConn;
	BasicDBObject dbObject;
	
	@Override
	public Request process(Request request) {
		
		try {
			mongoConn = new MongoConnection();
			if(mongoConn.obtainConnection())
			{
				mongoConn.getCollection("course");
				mongoConn.insertData(new BasicDBObject("c_id", "275").append("c_name","Enterprise Application Development").append("c_descp","Distributed Systems").append("c_enroll", "5"));
				ArrayList<CourseDetails> courseList = mongoConn.getCourseList();
	            int courseCount= courseList.size();
	                      
	            RequestList.Builder reqCourseList = RequestList.newBuilder();
	            GetCourse.Builder getCourseBuilder = GetCourse.newBuilder();
	            for( int i =0; i<courseCount;i++)
	            {
	            	CourseDetails co = courseList.get(i);
	            	String co_id=co.getCourseId();
	            	String co_desc =co.getCourseDescp();
	            	String co_name = co.getCourseName();
	            		            		            	
	 				getCourseBuilder.setCourseDescription(co_desc);
	 				getCourseBuilder.setCourseId(Integer.parseInt(co_id));
	 				getCourseBuilder.setCourseName(co_name);
	 				reqCourseList.addCourseList(getCourseBuilder.build());
	 				//reqCourseList.setCourseList(i, getCourseBuilder.build());
	 			}
	                      
	            Payload.Builder payload = Payload.newBuilder();
	            payload.setReqList(reqCourseList.build());
	            
	            Header.Builder header = Header.newBuilder();
				header.setTag("CourseList");
				header.setRoutingId(Routing.JOBS);
				header.setOriginator("server");
				header.setReplyCode(Comm.PokeStatus.SUCCESS);
	            Request.Builder rb = Request.newBuilder();
	            rb.setBody(payload.build());
	            rb.setHeader(header.build());
	            
	            Request reply = rb.build();
	        	            
	            return reply;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}

}
