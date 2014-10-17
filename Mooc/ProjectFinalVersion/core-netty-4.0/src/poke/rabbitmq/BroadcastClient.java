package poke.rabbitmq;


import com.rabbitmq.client.AMQP;



public class BroadcastClient {
	MQueueFactory factory;

	public BroadcastClient(String host, int port, String user, String password) {
		factory = new MQueueFactory(host, port, user, password);
	}

	public void sendMessage(eye.Comm.Request message, String topic) {
		try {
			MQueuePublisher queue = factory.createPublisher("RequestQueue6");
			queue.publish(message,topic);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		System.out.println("\n Message sent to "+  topic + "\n message is : " + message);
	}
}
