import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonObject;


public class Accelerometer extends Capteur{
	private double x;
	private double y;
	private double z;
	
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
	
	public static void main(String[] args) {

		Socket socket;
		int numport=2002;
		try {
			Accelerometer acc = new Accelerometer();
			
			 //JsonObject obj = JsonObject.crea("{\"name\" :\"capteur1\",\"ID\":\"C14\",\"data\" :\"donnee\"}");
			JsonObject jsonObj = Json.createObjectBuilder()
					.add("name", "nom1")
					.add("ID ", "nom1")
					.add("nom", "nom1")
					.add("nom", "nom1")
					.build();
			
			socket.close();

		}catch (UnknownHostException e) {

			e.printStackTrace();
		}catch (IOException e) {

			e.printStackTrace();
		}
	}
	
}
