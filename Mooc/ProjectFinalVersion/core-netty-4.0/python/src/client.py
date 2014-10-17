#Python Client to connect to the Server.

import socket
import comm_pb2
import struct

#Global Variables
sock = ''      
host = '' # server's ip address.
port = '' 
serverResponse = ''

def connectToServer():
	global sock
	global host
	global port
	#host = "192.168.0.93"
	#port = 5570
	host = raw_input("Enter Cluster Leader IP : ")
	port = input("Enter Cluster Leader Port : ")
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.connect((host, port))

def signIn():
	uname = raw_input("Enter User Name : ")
	password = raw_input("Enter Password : ")
	#signIn
	signin = comm_pb2.SignIn()
	signin.user_name = uname
	signin.password = password
	#payload
	payload = comm_pb2.Payload()
	payload.sign_in.CopyFrom(signin)
	#header
	header = comm_pb2.Header()
	header.routing_id = 13
	header.originator = "client"
	header.tag = "SignIn"
	#request
	request = comm_pb2.Request()
	request.header.CopyFrom(header)
	request.body.CopyFrom(payload)
	write(request.SerializeToString(),sock)
	#SignIn reponse from server
	requestResponse = comm_pb2.Request()
	headerResp = comm_pb2.Header()
	print_str1= get_message(sock, requestResponse)
	print '\n'
	print print_str1.header.reply_msg
	
			
def signUp():
	fullname = raw_input("Enter Full Name : ")
	uname = raw_input("Enter User Name : ")
	password = raw_input("Enter Password : ")
	#signUp
	signup = comm_pb2.SignUp()
	signup.full_name = fullname
	signup.user_name = uname
	signup.password = password
	#payload
	payload = comm_pb2.Payload()
	payload.sign_up.CopyFrom(signup)
	#header
	header = comm_pb2.Header()
	header.routing_id = 13
	header.originator = "client"
	header.tag = "SignUp"
	#request
	request = comm_pb2.Request()
	request.header.CopyFrom(header)
	request.body.CopyFrom(payload)
	write(request.SerializeToString(),sock)
	#SignUp response from server
	requestResponse = comm_pb2.Request()
	print_str2= get_message(sock, requestResponse)
	print '\n'
	print print_str2.header.reply_msg
		
		
def getCourseList():
	#courselist
	requestList = comm_pb2.RequestList()
	#payload
	payload = comm_pb2.Payload()
	payload.req_list.CopyFrom(requestList)
	#header
	header = comm_pb2.Header()
	header.routing_id = 13
	header.originator = "client"
	header.tag = "CourseList"
	#request
	request = comm_pb2.Request()
	request.header.CopyFrom(header)
	request.body.CopyFrom(payload)
	write(request.SerializeToString(),sock)
	#get Course List from server
	requestResponse = comm_pb2.Request()
	getCourseList = comm_pb2.GetCourse()
	print_str3= get_message(sock,requestResponse)
	#print print_str3
	list = print_str3.body.req_list.CourseList
	for course in list:
		getCourseList = course
		print "\nCourse ID : " + str(getCourseList.course_id)
		print "Course Name : " + getCourseList.course_name
		print "Course Desc : " + getCourseList.course_description
		 
	
def getCoursebyID():
	#GetCourse by ID
	courseId = input("Enter Course ID : ")
	getCourse = comm_pb2.GetCourse()
	getCourse.course_id = courseId
	#payload
	payload = comm_pb2.Payload()
	payload.get_course.CopyFrom(getCourse)
	#header
	header = comm_pb2.Header()
	header.routing_id = 13
	header.originator = "client"
	header.tag = "SearchCourse"
	#request
	request = comm_pb2.Request()
	request.header.CopyFrom(header)
	request.body.CopyFrom(payload)
	write(request.SerializeToString(),sock)
	#GetCourseID response from server
	requestResponse = comm_pb2.Request()
	print_str4 = get_message(sock, requestResponse)
	print "\nCourse ID : " + str(print_str4.body.get_course.course_id)
	print "Course Name : " + print_str4.body.get_course.course_name
	print "Course Description : " + print_str4.body.get_course.course_description
	
	
def courseEnrollment():
	#print "\nAvailable Courses"
	#getCourseList()
	#GetCourse by ID
	courseId = input("\nEnter Course ID to Enroll : ")
	getCourse = comm_pb2.GetCourse()
	getCourse.course_id = courseId
	#payload
	payload = comm_pb2.Payload()
	payload.get_course.CopyFrom(getCourse)
	#header
	header = comm_pb2.Header()
	header.routing_id = 13
	header.originator = "client"
	header.tag = "Enroll"
	#request
	request = comm_pb2.Request()
	request.header.CopyFrom(header)
	request.body.CopyFrom(payload)
	write(request.SerializeToString(),sock)
	#GetCourseID response from server
	requestResponse = comm_pb2.Request()
	print_str5= get_message(sock, requestResponse)
	print "\nCourse ID : " + str(print_str5.body.get_course.course_id)
	print "Course Name : " + print_str5.body.get_course.course_name
	print "Course Description : " + print_str5.body.get_course.course_description
	print "Enrollment Status : " + print_str5.header.reply_msg

def startVoting():
    print "\Asking Cluster to Start Voting"
    #initvoting
    voting = comm_pb2.InitVoting()
    #payload
    payload = comm_pb2.Payload()
    payload.init_voting.MergeFrom(voting)
    #header
    header = comm_pb2.Header()
    header.routing_id = 13
    header.originator = "client"
    header.tag = "Voting"
	#request
    request = comm_pb2.Request()
    request.header.MergeFrom(header)
    request.body.MergeFrom(payload)
    #write
    write(request.SerializeToString(),sock)
    requestResponse = comm_pb2.Request()
    print_str6= get_message(sock, requestResponse)
    print print_str6.body.host_ip
    

def quitConnection():
	sock.close()
	print "Socket closed"
	
def main():
  connectToServer()
  cmd = '2'	
  print '\n'
  print "1) Sign In"
  print "2) Sign Up"
  print "3) Get Course List"
  print "4) Get Course by ID"
  print "5) Enrollment"
  print "6) Voting"
  print "7) Quit"
  cmd = raw_input("\nEnter option :")
  while(cmd < '7'):
	
	if cmd == '1':
		signIn()
	elif cmd == '2':
		signUp()
	elif cmd == '3':
		getCourseList()
	elif cmd == '4':
		getCoursebyID()
	elif cmd == '5':
		courseEnrollment()
	elif cmd == '6':
		startVoting()
	elif cmd == '7':
		quitConnection()
	elif cmd > '7':
		print "Please enter correct option!!!!"	
	print '\n'
	print "1) Sign In"
	print "2) Sign Up"
	print "3) Get Course List"
	print "4) Get Course by ID"
	print "5) Enrollment"
	print "6) Voting"
	print "7) Quit"
	cmd = raw_input("\nEnter option :")
		
        	
def get_message(sock, requestResponse):
    
    #msg = ''
    #msg = msgtype()
    len_buf = socket_read_n(sock, 4)
    msg_len = struct.unpack('>L', len_buf)[0]
    msg_buf = socket_read_n(sock, msg_len)
    requestResponse.ParseFromString(msg_buf)
    msg_len = ''
    msg_buf = ''
    len_buf = ''
    return requestResponse

def socket_read_n(sock, n):
   
    buf = ''
    data = ''
    while n > 0:
        data = sock.recv(n)
	#print "received"
        if data == '':
            raise RuntimeError('unexpected connection close')
        buf += data
        n -= len(data)
    return buf

def write(byteStream,sock):
    streamLen = struct.pack('>L', len(byteStream))
    framedStream = streamLen + byteStream
    try:
      sock.sendall(framedStream)
      sock.close
    except socket.error:
      sock.close()
      raise Exception("socket send fail, close")

main()



