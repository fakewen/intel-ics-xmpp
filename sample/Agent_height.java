import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.*;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.listener.*;

//XML
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

//XML2
import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import java.net.*;
//determine how long it could stand
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedWriter;

public class Agent_height{

   public static Document loadXMLFromString(String xml) throws Exception
{
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(xml));
    return builder.parse(is);
}

	public static String logic_python(int a,int b,int c,int d){

		return "1,0,1";
	}

	public static void main(String[] args)throws IOException {
			String USER_NAME = "agent_height_test_xml";
			String PASSWORD = "agent_height_test_xml";
			String ServerAddress = "wukong.ccc.ntu.edu.tw";
			String ServerName = "pubsub.wukong.ccc.ntu.edu.tw";
			String port="5222";
		XMPP.XMPPinit(USER_NAME,PASSWORD,ServerAddress,port,ServerName);

		ItemEventListener<PayloadItem> lis=new ItemEventListener<PayloadItem>(){
				public void handlePublishedItems(ItemPublishEvent evt){
				int i=0;
				for(Object obj : evt.getItems()){
				System.out.println("chek num="+i++);
				PayloadItem item = (PayloadItem) obj;
				
				System.out.println("--:Payload=" + item.getPayload().toString());
				


				try{

//LOG timestamp(+8) appending
				Date now = new Date();
		String timestamp=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("myfile.txt", true)))) {
    out.println(timestamp);
}catch (IOException e) {
    //exception handling left as an exercise for the reader
}
				}
				catch(Exception e)
    {
      e.printStackTrace();
    }

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
  int[] input=new int[4];
 for (int temp = 0; temp < nList.getLength(); temp++) {
 
  Node nNode = nList.item(temp);
 
  System.out.println("\nCurrent Element :" + nNode.getNodeName());
 
  if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
   Element eElement = (Element) nNode;
 
   System.out.println("UUID :" + eElement.getElementsByTagName("UUID").item(0).getTextContent());
   System.out.println("VALUE :" + eElement.getElementsByTagName("value").item(0).getTextContent());
 String[] each = eElement.getElementsByTagName("value").item(0).getTextContent().split(",");
    for(int j=0;j<4;j++){
      System.out.println("VALUE"+j +":" + each[j]);
      input[j]=Integer.valueOf(each[j]);
    }
  }
 }


	

 //call Prof. wu's logic
String output= logic_python(input[0],input[1],input[2],input[3]);

					if(output!="0,0,0"){
						XMPP.XMPPpublish("/Device/type/profile/feedback","UUID_processID_agent_height","3", output,"5","6","7","N/A");
					}
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
		String nodeId = "/Device/type/profile/IR";
		XMPP.XMPPsubscribe(nodeId,lis);


	}


}
