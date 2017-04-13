import javax.json.JsonObject;

public class SenderbyCapteur {
	private int sender_id;
	static int maxtaille=150;
	private JsonObject[] tabid =new JsonObject[maxtaille] ;
	private int msgid=0;
	public SenderbyCapteur(int sender_id, int msg_id, JsonObject[] tabid) {
		super();
		this.sender_id = sender_id;
		this.tabid = tabid;
	}

	public int getSender_id() {
		return sender_id;
	}

	public void setSender_id(int sender_id) {
		this.sender_id = sender_id;
	}
	
	public JsonObject getTabid(int indice) {
		return this.tabid[indice];
	}
	public int getmsgid(){
		return msgid;
		
	}
	public void setTabid(JsonObject tabid) {
		this.tabid[msgid] = tabid;
		msgid++;
	}

}
