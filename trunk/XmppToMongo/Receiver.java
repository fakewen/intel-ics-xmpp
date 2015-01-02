import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.*;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.listener.*;

//Mongo's libs with java
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import java.util.Arrays;

public class Reciever{


	public static void main(String[] args) {
			String USER_NAME = "xmpp_mongo";
			String PASSWORD = "xmpp_mongo";
			String ServerAddress = "140.112.170.26";
			//String ServerAddress = "192.168.0.103";
			String ServerName = "pubsub.wukongdemac-mini.local";
			String port="5222";
		XMPP.XMPPinit(USER_NAME,PASSWORD,ServerAddress,port,ServerName);

		ItemEventListener<PayloadItem> lis=new ItemEventListener<PayloadItem>(){
				public void handlePublishedItems(ItemPublishEvent evt){
				
				System.out.println("Get one item");
				for(Object obj : evt.getItems()){
				
				PayloadItem item = (PayloadItem) obj;
				System.out.println("--:Payload=" + item.getPayload().toString());
				}
//transport to Mongo
				try{   
		 // To connect to mongodb server
         MongoClient mongoClient = new MongoClient( "140.112.170.32" , 27017 );
         // Now connect to your databases
         DB db = mongoClient.getDB( "test" );
		 System.out.println("Connect to database successfully");
//         boolean auth = db.authenticate("admin", "admin");
//		 System.out.println("Authentication: "+auth);
         DBCollection coll = db.getCollection("mycol");
         System.out.println("Collection mycol selected successfully");
//insert
         BasicDBObject doc = new BasicDBObject("title", "MongoDB").
            append("description", "database").
            append("likes", 100).
            append("url", "http://www.tutorialspoint.com/mongodb/").
            append("by", "tutorials point");
         coll.insert(doc);
         System.out.println("Document inserted successfully");

//retrieve
DBCursor cursor = coll.find();
         int i=1;
         while (cursor.hasNext()) { 
            System.out.println("Inserted Document: "+i); 
            System.out.println(cursor.next()); 
            i++;
         }
      }catch(Exception e){
	     System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	  }
	  //transport end

				}

		};
		String nodeId = "nooneknow_tomongo";
		XMPP.XMPPsubscribe(nodeId,lis);


	}


}
