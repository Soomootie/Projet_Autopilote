import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Gyroscope extends Capteur {

	private double x; // x coordinate
	private double y; // y coordinate
	private double z; // z coordinate
	
	public Gyroscope(){
	super("Gyro", "default_gyro");
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Gyroscope(String sender_class, String sender_name, double x,
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
	
	public  void send(int sender_id,JsonObject contents){
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
			
			String jsonText = msg.toString();
			
			socket = new Socket(InetAddress.getLocalHost(), 8888);
			
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedWriter wr = new BufferedWriter(out);
			
			wr.write(jsonText);
			wr.newLine();
			wr.flush();

			InputStreamReader in = new InputStreamReader(socket.getInputStream());
			BufferedReader rd = new BufferedReader(in);
			String jsonResp = rd.readLine();
			
		    JsonReader jsonReader = Json.createReader(new StringReader(jsonResp));
		    JsonObject object = jsonReader.readObject();
		    JsonObject ack = object.getJsonObject("ack");
			int res = ack.getInt("resp");
			socket.close();
			jsonReader.close();
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
	
	
	public static void main(String[] args) {

		Socket socket;
		int numport=8888;
		try {
			socket = new Socket(InetAddress.getLocalHost(), numport);
			Accelerometer gyro = new Accelerometer("Gyro","myGyro",1,-2,3);
			gyro.registerSender(gyro.getSender_class(), gyro.getSender_name(), socket);
			
			JsonObject jsonObj = Json.createObjectBuilder()
					.add("x",gyro.getX()).add("y", gyro.getY()).add("z", gyro.getZ())
						.build();
			gyro.send(gyro.getSender_id(),jsonObj);
			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {

			e.printStackTrace();
		}
	}
	
}