package autopiltote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

public class Accelerometer extends Capteur{
	private double x; // x coordinate
	private double y; // y coordinate
	private double z; // z coordinate
	
	public Accelerometer(){
	super("ACC", "ACC_1");
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Accelerometer(String sender_class, String sender_name, double x,
		double y, double z) {
		super(sender_class, sender_name);
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}
	
	
	public void send(int sender_id,JsonObject contents){
		// message send looks like :
		/* msg {
    "type" : "send",
    "sender_id" : 2,
    "contents" : {
            "x" : 1.25,
            "y" : 1.47,
            "z" : 0
    }
}
*/
		Socket socket;
		try {
			// message to send
			JsonObject msg = Json.createObjectBuilder()
					.add("type","send")
					.add("sender_id", sender_id)
					.add("contents",contents)
					.build();
			
			socket = new Socket(InetAddress.getLocalHost(), 2002);
			OutputStream out = socket.getOutputStream();
			JsonWriter jswr = Json.createWriter(out);
			jswr.writeObject(msg);
			InputStream in = socket.getInputStream();
			JsonReader jsonread = Json.createReader(in);
			JsonObject jsonObjrd = jsonread.readObject();
			JsonObject ack = jsonObjrd.getJsonObject("ack");			
			int res = ack.getInt("resp");
			jsonread.close();
			socket.close();
			if (res == 0){
				System.out.println("Send success !");	
			}
			else{
				System.out.println("Error : " + codeError(res));
				
			}	

		}catch (UnknownHostException e) {
			e.printStackTrace();

		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	public void main(String[] args) {

		Socket socket;
		int numport=2002;
		try {
			socket = new Socket(InetAddress.getLocalHost(), numport);
			Accelerometer acc = new Accelerometer("ACC","myAcc",-1,2,3);
			acc.registerSender(acc.getSender_class(), acc.getSender_name());
			JsonObject jsonObj = Json.createObjectBuilder()
					.add("contents", Json.createObjectBuilder().add("x",acc.getX()).add("y", acc.getY()).add("z", acc.getZ()))
						.build();
			send(acc.getSender_id(),jsonObj);
			socket.close();

		}catch (UnknownHostException e) {

			e.printStackTrace();
		}catch (IOException e) {

			e.printStackTrace();
		}
	}
	
}
