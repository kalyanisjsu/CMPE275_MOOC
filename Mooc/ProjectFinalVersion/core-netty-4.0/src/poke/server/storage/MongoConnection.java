package poke.server.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import com.mongodb.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.server.resources.CourseDetails;
import poke.server.resources.LoginDetails;
import poke.server.resources.MongoConfiguration;


public class MongoConnection {

    private boolean success = false;
    private MongoClient mongoClient;
    private static DB db;
    private static DBCollection collection;
    private static DBObject course;

    private String dbHost1;
    private String dbHost2;
    private String dbHost3;
    private String dbHost4;
    private String dbHost0;

    private MongoClient mongoClientDAO;

    int dbPort1;
    int dbPort2;
    int dbPort3;
    int dbPort4;
    int dbPort0;

    String dbName;
    protected static Logger logger = LoggerFactory.getLogger("Database-->");

    public MongoConnection() throws IOException{
        logger.info("Trying to connect to Db");

        MongoConfiguration configuration = new MongoConfiguration();
        this.dbName = configuration.getDbName();

        this.dbHost0 = configuration.getNode_zero();
      //  this.dbHost1 = configuration.getNode_one();
        /*this.dbHost2 = configuration.getNode_two();
        this.dbHost3 = configuration.getNode_three();
        this.dbHost4 = configuration.getNode_four();
*/
        this.dbPort0 = configuration.getPort_zero();
       // this.dbPort1 = configuration.getPort_one();
        /*this.dbPort2 = configuration.getPort_two();
        this.dbPort3 = configuration.getPort_three();
        this.dbPort4 = configuration.getPort_four();*/

    }


    @SuppressWarnings("finally")
    public boolean obtainConnection() {
        try {
               /* mongoClient = new MongoClient(Arrays.asList(new ServerAddress(dbHost0,dbPort0),
                        new ServerAddress(dbHost1,dbPort1),new ServerAddress(dbHost1,dbPort1)));*/

                mongoClient = new MongoClient(new ServerAddress("localhost", 27017));
                logger.info("Trying to connect to Db");
                db = mongoClient.getDB("coursesDB");
                success = true;
        }
        catch (UnknownHostException e) {

            logger.info("Exception at + " + e.getMessage());

            logger.info(e.toString());
            e.printStackTrace();
        }
        catch(Exception e){
            logger.info("Exception at + " + e.getStackTrace());
        }
        finally {
            return success;
        }
    }

    public DBCollection getCollection(String collectionName){
        collection = db.getCollection(collectionName);
        return collection;
    }

    public void insertData(BasicDBObject doc)	{
        collection.insert(doc);
    }

    public void deleteData(BasicDBObject doc)	{
        collection.findAndRemove(doc);
    }

    public void updateData(BasicDBObject query, BasicDBObject update )	{
        collection.findAndModify(query, update);
    }

    public void closeConnection() {
        mongoClientDAO.close();
    }

    public DBCursor findData(BasicDBObject query) {
        return collection.find(query);
    }

    public void updateEnrollCounter(String cnt, String courseID)
    {
    	BasicDBObject newDocument = new BasicDBObject();
    	newDocument.append("$set", new BasicDBObject().append("c_enroll", cnt));
       	BasicDBObject searchQuery = new BasicDBObject().append("c_id",courseID );
       	collection.update(searchQuery, newDocument);
    }
    
    public CourseDetails getCourseID(String courseID)
	{
		return getCourse("c_id",courseID);
	}
    
        
    	
	@SuppressWarnings("finally")
    public CourseDetails getCourse(String key, String value) {
        
		CourseDetails course = null;
        BasicDBObject query = new BasicDBObject(key, value);

        DBCursor cursor = collection.find(query);

        try {
        	if(cursor.count() > 0){
        		course = new CourseDetails();
            while (cursor.hasNext()) {

                String cour = cursor.next().toString();
                JSONParser parser = new JSONParser();

                JSONObject jsonObject = (JSONObject) parser.parse(cour);
                jsonObject.get("c_enroll");
                
                String c_id = (String) jsonObject.get("c_id");
                course.setCourseId(c_id);

                String c_descp = (String) jsonObject.get("c_descp");
                course.setCourseDescp(c_descp);

                String c_name = (String) jsonObject.get("c_name");
                course.setCourseName(c_name);
                
                String c_enroll = (String) jsonObject.get("c_enroll");
                course.setCourseEnroll(c_enroll);
              }
        	}
        }
        finally {
            cursor.close();
            return course;
        }
    }
    
    public LoginDetails getUsername(String uname)
	{
		return getLogin("uname",uname);
	}
	
	public LoginDetails getPassword(String password)
	{
		return getLogin("password",password);
	}

    @SuppressWarnings("finally")
	public LoginDetails getLogin(String key, String value) {
		
		LoginDetails login = new LoginDetails();
		BasicDBObject query = new BasicDBObject(key, value);
		
		DBCursor cursor = collection.find(query);
		try 
		{
			while (cursor.hasNext()) {
					
				String cour = cursor.next().toString();
				JSONParser parser = new JSONParser();
				
				JSONObject jsonObject = (JSONObject) parser.parse(cour);

				String u_name = (String) jsonObject.get("uname");
				login.setUname(u_name);
				
				String p_word = (String)jsonObject.get("password");
				login.setPassword(p_word);
				

			}
		}
		finally {
			cursor.close();
			return login;
		}
	}
    
    @SuppressWarnings("finally")
    public ArrayList<CourseDetails> getCourseList() {
    	
    	ArrayList<CourseDetails> courses = new ArrayList<CourseDetails>();
        DBCursor cursor = collection.find();

        try {
        	
            while (cursor.hasNext()) {
            	CourseDetails co =new CourseDetails();
                String cour = cursor.next().toString();
                JSONParser parser = new JSONParser();

                JSONObject jsonObject = (JSONObject) parser.parse(cour);

                String c_id = (String) jsonObject.get("c_id");
                co.setCourseId(c_id);

                String c_descp = (String) jsonObject.get("c_descp");
                co.setCourseDescp(c_descp);

                String c_name = (String) jsonObject.get("c_name");
                co.setCourseName(c_name);
                
                courses.add(co);
            }
        }
        finally {
            cursor.close();
            return courses;
        }
    }
    
    
    
    public boolean createCollection(String collectionName) {
        if(db!= null && !db.collectionExists(collectionName))
            db.createCollection(collectionName, null);
        else
            return false;
        return true;

    }

    public boolean insertDocIntoCollection(CourseDetails courseDetails, String collectionName){

        if(db!= null && !db.collectionExists(collectionName))
            collection =  db.createCollection(collectionName, null);
        else
            return false;


        BasicDBObject basicDBObject = new BasicDBObject("c_id", courseDetails.getCourseId()).
                append("c_name", courseDetails.getCourseName()).
                append("c_descp", courseDetails.getCourseDescp());

        if(collection != null){
            collection.insert(basicDBObject);
            return true;
        }
        return false;
    }
}
