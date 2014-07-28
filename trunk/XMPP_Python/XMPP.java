
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
//XML
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;

//XML2
import java.io.StringReader;

import java.net.*;
//net socket
import javax.net.SocketFactory;
import java.net.InetSocketAddress;
import java.net.Socket;

public class XMPP{


	static ConnectionConfiguration config;// = new ConnectionConfiguration("140.112.170.26",Integer.parseInt("5222"));
	static Connection connection;// = new XMPPConnection(config);
	static PubSubManager manager;

	public static void XMPPinit(String USER_NAME,String PASSWORD, String ServerAddress, String port, String ServerName){
		//		SASLAuthentication.supportSASLMechanism("PLAIN");//new in
		//		XMPPConnection.DEBUG_ENABLED = true;//new in
		config = new ConnectionConfiguration(ServerAddress,Integer.parseInt(port));
		connection= new XMPPConnection(config);
		try{
			SASLAuthentication.supportSASLMechanism("PLAIN", 0);
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
	//public static String output=new String("1,0,0");
	public static int[] input=new int[4];
	   public static Document loadXMLFromString(String xml) throws Exception
{
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(xml));
    return builder.parse(is);
}
	public static void XMPPsubscribe_DEMO(String nodeId){
		
		//[AGENT]
		ItemEventListener<PayloadItem> lis=new ItemEventListener<PayloadItem>(){
			public void handlePublishedItems(ItemPublishEvent evt){
				int i=0;
				for(Object obj : evt.getItems()){
					System.out.println("chek num="+i++);
					PayloadItem item = (PayloadItem) obj;

					System.out.println("--:Payload=" + item.getPayload().toString());




					//item.getPayload().toXML();
					try{

						//1.讀出來 2.切開, 3.透過logic算答案 v4.publish
						String strbuf=item.getPayload().toString();
						String[] msgs= strbuf.split("\\[");
						String[] msg= msgs[1].split("\\]");
						Document doc = loadXMLFromString(msg[0]);
						doc.getDocumentElement().normalize();

						System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

						NodeList nList = doc.getElementsByTagName("context");

						System.out.println("----------------------------"+nList.getLength());
						
						for (int temp = 0; temp < nList.getLength(); temp++) {

							Node nNode = nList.item(temp);

							System.out.println("\nCurrent Element :" + nNode.getNodeName());

							if (nNode.getNodeType() == Node.ELEMENT_NODE) {

								Element eElement = (Element) nNode;

								System.out.println("UUID :" + eElement.getElementsByTagName("UUID").item(0).getTextContent());
								System.out.println("VALUE :" + eElement.getElementsByTagName("value").item(0).getTextContent());
								//output= eElement.getElementsByTagName("value").item(0).getTextContent();
								String[] each = eElement.getElementsByTagName("value").item(0).getTextContent().split(",");
								for(int j=0;j<4;j++){
									System.out.println("VALUE"+j +":" + each[j]);
									input[j]=Integer.valueOf(each[j]);
								}
							}
						}




						//call Prof. wu's logic
						//String output= logic_python(input[0],input[1],input[2],input[3]);
/*
						if(output!="0,0,0"){
							Date now = new Date();
							String timestamp=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
							XMPP.XMPPpublish("/Device/type/profile/feedback","UUID_processID_agent_height","3", output,"5","6",timestamp,"N/A");
						}*/
						/*
						   for (int it=0;it＜ nl.getLength() ;it++){
						   System.out.print("VVVVVAAALLLUUUUEEEE:" + doc.getElementsByTagName("value").item(it).getFirstChild().getNodeValue());
						   }*/
					}catch(Exception e){
						e.printStackTrace();
					}


				}
			}

		};
		//[AGENT2]
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

			String msg ="<context><UUID>"+UUID+"</UUID><type>"+type+"</type><value>"+value+"</value><location>"+location+"</location><confidence>"+confidence+"</confidence><timestamp>"+timestamp+"</timestamp><reference>"+reference+"</reference></context>";
			//2.payload,[put payload as an item] we should check the parameter
			SimplePayload payload = new SimplePayload("message","pubsub:test:message","<message xmlns='pubsub:test:message'><body>"+msg+"</body></message>");
			//What is 5?5 times?
			PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>("5",payload);//5=id,item ID 
			//payload.toXML();
			//3.node.publish(item)
			myNode.publish(item);
			System.out.println("[Publish]----publish----"+msg);

		}catch(Exception E){
			E.printStackTrace();
		}
	}
	/*
	   public static void main(String[] args){
	   String USER_NAME = "agent_height_test";
	   String PASSWORD = "agent_height_test";
	   String ServerAddress = "192.168.0.103";
//String ServerAddress = "wukong.ccc.ntu.edu.tw";
String ServerName = "pubsub.wukong.ccc.ntu.edu.tw";
String port = "5222";//The standard port for clients to connect to the server
XMPP.XMPPinit(USER_NAME,PASSWORD,ServerAddress,port,ServerName);

String nodeId = "/Device/type/profile/IR"; //node's ID[I guess it's topic]
String UUID="L1";
String type="LED";
String value="99";
String location="kitchen";
String confidence="0.8";

String timestamp="ICS_jythone_test";
//		String timestamp="2014-05-19 17:37:28";
String reference="wukong";
for(int i=0;i<1;i++){
XMPP.XMPPpublish(nodeId,String.valueOf(i),type,value,location,  confidence,timestamp ,reference);
}
//XMPP.XMPPsubscribe(nodeId);
}*/

}
