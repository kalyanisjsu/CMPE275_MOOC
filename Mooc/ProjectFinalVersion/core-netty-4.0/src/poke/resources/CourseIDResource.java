package poke.resources;

import java.io.IOException;


import com.mongodb.BasicDBObject;

import eye.Comm;
import eye.Comm.GetCourse;
import eye.Comm.Header;
import eye.Comm.Header.Routing;
import eye.Comm.Payload;
import eye.Comm.Request;
import poke.server.resources.CourseDetails;
import poke.server.resources.Resource;
import poke.server.storage.MongoConnection;

public class CourseIDResource implements Resource
{
	MongoConnection mongoConn;
	BasicDBObject dbObject;
	int courseID;
	String courseIDStr, courseDesc, courseName, courseEnroll;
	
	@Override
	public Request process(Request request) {
		
		try {
			mongoConn = new MongoConnection();
			courseID = request.getBody().getGetCourse().getCourseId();
			if(mongoConn.obtainConnection())
			{
				mongoConn.getCollection("course");
				courseIDStr = String.valueOf(courseID);
				CourseDetails course = mongoConn.getCourseID(courseIDStr);
								
				if(course != null)
				{
					courseID = Integer.parseInt(courseIDStr);
					courseDesc = course.getCourseDescp();
					courseName = course.getCourseName();
										
					GetCourse.Builder getCourseBuilder = GetCourse.newBuilder();
					getCourseBuilder.setCourseDescription(courseDesc);
					getCourseBuilder.setCourseId(courseID);
					getCourseBuilder.setCourseName(courseName);
				
					Payload.Builder coursePayload = Payload.newBuilder();
					coursePayload.setGetCourse(getCourseBuilder.build());
					
					Header.Builder header = Header.newBuilder();
					header.setTag("SearchCourse");
					header.setRoutingId(Routing.JOBS);
					header.setOriginator("server");
                    header.setReplyCode(Comm.PokeStatus.SUCCESS);
					header.setReplyMsg("Course Found");
					if(request.getHeader().getTag().equals("Enroll"))
					{
						courseEnroll = course.getCourseEnroll();
						int enrollmentCount = Integer.parseInt(courseEnroll);
						enrollmentCount++;
						System.out.println("Enroll count: " + enrollmentCount);
						String strCnt = String.valueOf(enrollmentCount);
						header.setReplyMsg("Enrolled Succesfully");
						mongoConn.updateEnrollCounter(strCnt,courseIDStr);
					}
					
					Request.Builder rb = Request.newBuilder();
					rb.setBody(coursePayload);
					rb.setHeader(header.build());
					Request reply = rb.build();
					return reply;
				}
				else
				{
							
					GetCourse.Builder getCourseBuilder = GetCourse.newBuilder();
					getCourseBuilder.setCourseDescription("");
					getCourseBuilder.setCourseId(0);
					getCourseBuilder.setCourseName("");
				
					Payload.Builder coursePayload = Payload.newBuilder();
					coursePayload.setGetCourse(getCourseBuilder.build());
					
					Header.Builder header = Header.newBuilder();
					header.setTag("SearchCourse");
					header.setRoutingId(Routing.JOBS);
					header.setOriginator("server");
                    header.setReplyCode(Comm.PokeStatus.FAILURE);
					header.setReplyMsg("Course not found");
					Request.Builder rb = Request.newBuilder();
					rb.setBody(coursePayload);
					rb.setHeader(header.build());
					
					Request reply = rb.build();

					return reply;
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

}
