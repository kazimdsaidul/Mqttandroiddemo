package com.example.hello.mqttandroiddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);
        String clientId = MqttClient.generateClientId();

        //The URL of the Mosquitto Broker is 192.168.9.100:1883
        final  MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), Constants.MQTT_BROKER_URL, clientId);

        client.setCallback(new MqttCallbackHandler(client));//This is here for when a message is received

        MqttConnectOptions options = new MqttConnectOptions();

        try {
            IMqttToken token = client.connect(options);
            options.setUserName("fred");
            options.setPassword("1234".toCharArray());

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("mqtt", "onSuccess");
//-----------------------------------------------------------------------------------------------
                    //PUBLISH THE MESSAGE
                    MqttMessage message = new MqttMessage("Hello, I am an Android Mqtt Client.".getBytes());
                    message.setQos(2);
                    message.setRetained(false);

                    String topic = "AndroidPhone";

                    try {
                        client.publish(topic, message);
                        Log.i("mqtt", "Message published");

                        // client.disconnect();
                        //Log.i("mqtt", "client disconnected");

                    } catch (MqttPersistenceException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    } catch (MqttException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
//-----------------------------------------------------------------------------------------------

                    String subtopic = "tester";
                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(subtopic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // The message was published
                                Log.i("mqtt", "subscription success");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Log.i("mqtt", "subscription failed");

                            }
                        });



                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

//---------------------------------------------------------------------------

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("mqtt", "onFailure");

                }

            });


        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


}//End of Activity class

//-----------------------------------------------------------------------------

class MqttCallbackHandler implements MqttCallbackExtended {

    private final MqttAndroidClient client;

    public MqttCallbackHandler (MqttAndroidClient client)
    {
        this.client=client;
    }

    @Override
    public void connectComplete(boolean b, String s) {
        Log.w("mqtt", s);
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    public void AlarmActivatedMessageReceived()
    {
        MqttMessage msg= new MqttMessage("Hello, the Mosquitto Broker got your message saying that the Alarm is Activated.".getBytes());
        try {
            this.client.publish("Fitlet", msg);
            Log.i("mqtt", "Message published");

        } catch (MqttPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.w("mqtt", mqttMessage.toString());

        if (mqttMessage.toString().contains("Alarm Activated"))
        {
            AlarmActivatedMessageReceived();
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}