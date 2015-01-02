// import org.jivesoftware.smack.*;
// import org.jivesoftware.smackx.*;
// import org.jivesoftware.smackx.pubsub.PubSubManager;
// import org.jivesoftware.smackx.pubsub.*;
// import org.jivesoftware.smackx.pubsub.LeafNode; 
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TEST{

	public static void main(String[] args){
		String USER_NAME = "admin";
		String PASSWORD = "admin";
		String ServerAddress = "140.112.170.26";
		//String ServerAddress = "192.168.0.103";
		//String ServerName = "pubsub.wukong.ccc.ntu.edu.tw";
		String ServerName = "pubsub.wukongdemac-mini.local";
		String port = "5222";//The standard port for clients to connect to the server
		XMPP.XMPPinit(USER_NAME,PASSWORD,ServerAddress,port,ServerName);

		String nodeId = "nooneknow_tomongo"; //node's ID[I guess it's topic]
		String UUID="L1";
		String type="LED";
		String value="99";
		String location="kitchen";
		String confidence="0.8";
Date now = new Date();
		String timestamp=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
//		String timestamp="2014-05-19 17:37:28";
		String reference="wukong";
		//XMPP.publish("",);
		for(int i=19;i<20;i++){
 			XMPP.XMPPpublish(nodeId,String.valueOf(i),type,value,location,  confidence,timestamp ,reference);
		}
		//XMPP.XMPPsubscribe(nodeId);
	}
}
