import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.*;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.listener.*;

public class Reciever{


	public static void main(String[] args) {
			String USER_NAME = "sub";
			String PASSWORD = "sub";
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
				}
				}

		};
		String nodeId = "/Device/type/profile/IR";
		XMPP.XMPPsubscribe(nodeId,lis);


	}


}
