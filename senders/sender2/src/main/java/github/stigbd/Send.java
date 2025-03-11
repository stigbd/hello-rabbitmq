package github.stigbd;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;

public class Send {

    private static final String QUEUE_NAME = "worker.queue";
    private static final String EXCHANGE_NAME = "worker.exchange";
    private static final String DEAD_LETTER_NAME = "deadLetter.queue";
    private static final String DEAD_LETTER_EXCHANGE_NAME =
        "deadLetter.exchange";
    private static final String ROUTING_KEY = "email";
    private static int COUNTER = 0;
    private static Connection connection;
    private static Channel channel;
    private static String username = System.getenv("RABBITMQ_DEFAULT_USER");
    private static String password = System.getenv("RABBITMQ_DEFAULT_PASS");

    private static void setupConnection() throws IOException, TimeoutException {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq");
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setRequestedHeartbeat(60);
            connection = factory.newConnection();
            channel = connection.createChannel();
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME);
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
            channel.queueDeclare(QUEUE_NAME, true, false, false, args);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
            // Declaring deadletterQueue
            channel.exchangeDeclare(DEAD_LETTER_EXCHANGE_NAME, "fanout", true);
            channel.queueDeclare(DEAD_LETTER_NAME, true, false, false, null);
            channel.queueBind(
                DEAD_LETTER_NAME,
                DEAD_LETTER_EXCHANGE_NAME,
                ROUTING_KEY
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void sendToQueue() throws IOException {
        ++COUNTER;
        try {
            String message = "ID: " + COUNTER + " -- Hello World, from java!";
            JSONObject payload = new JSONObject();
            payload.put("msg", message);
            payload.put("dataOK", true);
            channel.basicPublish(
                EXCHANGE_NAME,
                ROUTING_KEY,
                new AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .contentEncoding("UTF-8")
                    .build(),
                payload.toString().getBytes()
            );
            System.out.println(" [x] Sent '" + payload.toString() + "'");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void closeConnection() {
        try {
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        try {
            setupConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (true) {
            try {
                sendToQueue();
                TimeUnit.SECONDS.sleep(1);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                closeConnection();
                System.exit(1);
            }
        }
    }
}
