package poke.server.resources;

public class CourseDetails {

	private String courseId;
	private String courseDescp;
	private String courseName;
	private String courseEnroll;
	
	public String getCourseEnroll() {
		return courseEnroll;
	}
	public void setCourseEnroll(String courseEnroll) {
		this.courseEnroll = courseEnroll;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getCourseDescp() {
		return courseDescp;
	}
	public void setCourseDescp(String courseDescp) {
		this.courseDescp = courseDescp;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public boolean IsEmpty(){
		if(courseName.equals(null) || courseId.equals(null) || courseDescp.equals(null))
			return true;
		return false;
	}
}
