import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.*;

import javax.json.*;

public class Capteur {
	String sender_class;
	String sender_name;
	
public Capteur(String sender_class, String sender_name) {
		this.sender_class = sender_class;
		this.sender_name = sender_name;
	}
	
public String getSender_class() {
	return sender_class;
}

public void setSender_class(String sender_class) {
	this.sender_class = sender_class;
}

public String getSender_name() {
	return sender_name;
}

public void setSender_name(String sender_name) {
	this.sender_name = sender_name;
}

public int registerSender(String senderClass, String senderName){
			return 0;
			
	}
}
