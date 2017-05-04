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

public class Accelerometer extends Capteur{
	private double x; /* x coordinate				*/
	private double y; /* y coordinate				*/
	private double z; /* z coordinate				*/

	public Accelerometer(){
		super("ACC", "ACC_1");
		x = y = z= 0;
	}

	public Accelerometer(String sender_class, String sender_name, double x,
			double y, double z) {
		super(sender_class, sender_name);
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Recupere la coordonnee x.
	 * @return Coordonnee x.
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Modifie la coordonnee x.
	 * @param x, un double.
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * Recupere la coordonnee y.
	 * @return Coordonnee y.
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Modifie la coordonnee y.
	 * @param y, un double.
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * Recupere la coordonnee z.
	 * @return Coordonnee z.
	 */
	public double getZ() {
		return z;
	}
	
	/**
	 * Modifie la coordonnee z.
	 * @param z, un double.
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * 
	 * @param sender_id, un int.
	 * @param contents, un JsonObject.
	 */
	public  void send(int sender_id,JsonObject contents){
		Socket socket;
		try {
			JsonObject msg = Json.createObjectBuilder()
					.add("type", "send")
					.add("sender_id", sender_id)
					.add("contents", contents)
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
			if ( res == 0 )
				System.out.println("Send success !");
			else
				System.out.println("Error : " + codeError(res));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		Socket socket;
		int numport=8888;
		try {
			socket = new Socket(InetAddress.getLocalHost(), numport);
			Accelerometer acc = new Accelerometer("ACC","myAcc",-1,2,3);
			acc.registerSender(acc.getSender_class(), acc.getSender_name(), socket);

			JsonObject jsonObj = Json.createObjectBuilder()
					.add("x",acc.getX()).add("y", acc.getY()).add("z", acc.getZ())
						.build();
			acc.send(acc.getSender_id(),jsonObj);

			socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
