import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.*;
import org.jivesoftware.smackx.pubsub.LeafNode; 
import org.jivesoftware.smackx.pubsub.listener.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
//XML
import java.io.*; 
import java.util.*; 
import org.w3c.dom.*; 
import javax.xml.parsers.*; 
//XML2
import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
//net socket
import javax.net.SocketFactory;
import java.net.InetSocketAddress;
import java.net.Socket;

public class XMPP{


	static ConnectionConfiguration config;
	static Connection connection;
	static PubSubManager manager;

	public static void XMPPinit(String USER_NAME,String PASSWORD, String ServerAddress, String port, String ServerName){
		//		SASLAuthentication.supportSASLMechanism("PLAIN");//new in
		//		XMPPConnection.DEBUG_ENABLED = true;//new in
		config = new ConnectionConfiguration(ServerAddress,Integer.parseInt(port));
		connection= new XMPPConnection(config);
		try{
			//SASLAuthentication.supportSASLMechanism("PLAIN", 0);
			connection.connect();
			connection.login(USER_NAME,PASSWORD);			
			manager = new PubSubManager(connection,ServerName); //new a manager & server's name
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public static void XMPPsubscribe(String nodeId,ItemEventListener<PayloadItem> lis){
		try{
		org.jivesoftware.smackx.pubsub.Node eventNode = manager.getNode(nodeId); 
		eventNode.addItemEventListener(lis);//add lis
		eventNode.subscribe(connection.getUser());
		while(true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
static int i=-1;

	public static void XMPPpublish(String nodeId,String UUID,String type,String value,String location,String confidence,String timestamp,String reference){
		try{

			//LeafNode[msg]			
			LeafNode myNode = null;
			//don't know why another try[maybe fun's restict]
			//1.target a node[ID=topic=topic1]
			try{
				myNode = manager.getNode(nodeId);
			}catch(Exception e){
				e.printStackTrace();
			}
			//creatNode
			if(myNode == null){
				myNode = manager.createNode(nodeId);
				ConfigureForm form = new ConfigureForm(FormType.submit);
      form.setAccessModel(AccessModel.open);
      form.setDeliverPayloads(true);
      form.setNotifyRetract(true);
      form.setPersistentItems(true);
      form.setPublishModel(PublishModel.open);
      //LeafNode leaf = mgr.createNode("testNode", form);
				myNode.sendConfigurationForm(form);
			}

			String msg ="\n<context>\n\t <UUID>"+UUID+"</UUID>\n\t <type>"+type+"</type>\n\t <value>"+value+"</value>\n\t <location>"+location+"</location>\n\t <confidence>"+confidence+"</confidence>\n\t <timestamp>"+timestamp+"</timestamp>\n\t <reference>"+reference+"</reference>\n </context>\n";
			//2.payload,[put payload as an item] we should check the parameter
			SimplePayload payload = new SimplePayload("message","pubsub:test:message","<message xmlns='pubsub:test:message'><body>"+msg+"</body></message>");
			//What is 5?5 times?
			PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>("5",payload);//5=id,item ID 
			//3.node.publish(item)
			myNode.publish(item);
			System.out.println("[Publish]----publish----"+msg);

		}catch(Exception E){
			E.printStackTrace();
		}
	}


}

