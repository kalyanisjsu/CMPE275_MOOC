package python;

import java.util.ArrayList;

import org.junit.Test;

import poke.resources.CourseIDResource;
import poke.resources.CourseListResource;
import poke.resources.SignInResource;
import poke.resources.SignUpResource;
import poke.server.Server;
import eye.Comm.GetCourse;
import eye.Comm.Header;
import eye.Comm.Payload;
import eye.Comm.Request;
import eye.Comm.RequestList;
import eye.Comm.SignIn;
import eye.Comm.Header.Routing;
import eye.Comm.SignUp;

public class PythonTest {
	
	@Test
	public void testSignUp() 
	{
		System.out.println("\n\n***************Running Sign Up Test***************");
		SignUp.Builder signUpBuilder = SignUp.newBuilder();
		signUpBuilder.setUserName("cmpesjsu");
		signUpBuilder.setPassword("cmpesjsu");
		signUpBuilder.setFullName("cmpesjsu");
		Header.Builder header = Header.newBuilder();
		header.setTag("SignUp");
		header.setRoutingId(Routing.JOBS);
		header.setOriginator("client");
		
		Payload.Builder payLoad = Payload.newBuilder();
		payLoad.setSignUp(signUpBuilder.build());
		
		Request.Builder rb = Request.newBuilder();
		rb.setBody(payLoad.build());
		rb.setHeader(header.build());					
		
		Request req = rb.build();
		
		SignUpResource signUpResource = new SignUpResource();
		Request response = signUpResource.process(req);
		System.out.println("\nSignUp Test Reply : " + response.getHeader().getReplyMsg());
	}
	
	@Test
	public void testSignIn() 
	{
		System.out.println("\n\n***************Running Sign In Test***************");
		SignIn.Builder signInBuilder = SignIn.newBuilder();
		signInBuilder.setUserName("cmpesjsu");
		signInBuilder.setPassword("cmpesjsu");
		
		Header.Builder header = Header.newBuilder();
		header.setTag("SignIn");
		header.setRoutingId(Routing.JOBS);
		header.setOriginator("client");
		
		Payload.Builder payLoad = Payload.newBuilder();
		payLoad.setSignIn(signInBuilder.build());
		
		Request.Builder rb = Request.newBuilder();
		rb.setBody(payLoad.build());
		rb.setHeader(header.build());					
		
		Request req = rb.build();
		
		SignInResource signUpResource = new SignInResource();
		Request response = signUpResource.process(req);
		System.out.println("\nSignIn Test Reply : " + response.getHeader().getReplyMsg());
	}
	
	@Test
	public void testGetCourseByID() 
	{
		System.out.println("\n\n***************Running Get Course ID Test***************");
		GetCourse.Builder getCourse = GetCourse.newBuilder();
		getCourse.setCourseId(275);
				
		Header.Builder header = Header.newBuilder();
		header.setTag("SearchCourse");
		header.setRoutingId(Routing.JOBS);
		header.setOriginator("client");
		
		Payload.Builder payLoad = Payload.newBuilder();
		payLoad.setGetCourse(getCourse.build());
		
		Request.Builder rb = Request.newBuilder();
		rb.setBody(payLoad.build());
		rb.setHeader(header.build());					
		
		Request req = rb.build();
		
		CourseIDResource getCourseResource = new CourseIDResource();
		Request response = getCourseResource.process(req);
		System.out.println("\nGetCourse Test Reply : " + response.getHeader().getReplyMsg());
		System.out.println("Course ID :" + response.getBody().getGetCourse().getCourseId());
		System.out.println("Course Name :" + response.getBody().getGetCourse().getCourseName());
		System.out.println("Course Description :" + response.getBody().getGetCourse().getCourseDescription());
				
	}
	
	@Test
	public void testGetCourseList() 
	{
		System.out.println("\n\n***************Running Get Course List Test***************");
		
		RequestList.Builder reqList = RequestList.newBuilder();
				
		Payload.Builder payLoad = Payload.newBuilder();
		payLoad.setReqList(reqList.build());
		
		Header.Builder header = Header.newBuilder();
		header.setTag("CourseList");
		header.setRoutingId(Routing.JOBS);
		header.setOriginator("client");
		
		Request.Builder rb = Request.newBuilder();
		rb.setBody(payLoad.build());
		rb.setHeader(header.build());
		
		Request req = rb.build();
		
		CourseListResource getCourseList = new CourseListResource();
		
		Request response = getCourseList.process(req);
				
		System.out.println(response.getBody());
		
		/*for (int i = 0; i < courseList.size(); i++) {
			
			GetCourse course = courseList.get(i);
			System.out.println("\nCourse ID :" + course.getCourseId());
			System.out.println("Course Name :" + course.getCourseName());
			System.out.println("Course Description :" + course.getCourseDescription());
			
		}*/
	}
	
	@Test
	public void testCourseEnroll() 
	{
		System.out.println("\n\n***************Running Course Enroll Test***************");
		
		GetCourse.Builder getCourse = GetCourse.newBuilder();
		getCourse.setCourseId(275);
		
		Header.Builder header = Header.newBuilder();
		header.setTag("Enroll");
		header.setRoutingId(Routing.JOBS);
		header.setOriginator("client");
		
		Payload.Builder payLoad = Payload.newBuilder();
		payLoad.setGetCourse(getCourse.build());
		
		Request.Builder rb = Request.newBuilder();
		rb.setBody(payLoad.build());
		rb.setHeader(header.build());					
		
		Request req = rb.build();
		
		CourseIDResource getCourseResource = new CourseIDResource();
		Request response = getCourseResource.process(req);
		
		System.out.println("\nGetCourse Test Reply : " + response.getHeader().getReplyMsg());
		System.out.println("Course ID :" + response.getBody().getGetCourse().getCourseId());
		System.out.println("Course Name :" + response.getBody().getGetCourse().getCourseName());
		System.out.println("Course Description :" + response.getBody().getGetCourse().getCourseDescription());
	}
}
