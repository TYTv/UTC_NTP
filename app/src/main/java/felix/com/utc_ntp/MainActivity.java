package felix.com.utc_ntp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        new Thread() {
            @Override
            public void run() {
                super.run();

                runNtp();

            }
        }.start();


    }

    public void runNtp() {

        int retry = 2;
        int port = 123;
        int timeout = 3000;

        // get the address and NTP address request
        //
        InetAddress ipv4Addr = null;
        try {
            ipv4Addr = InetAddress.getByName("time.stdtime.gov.tw");//更多NTP时间服务器参考附注
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        int serviceStatus = -1;
        DatagramSocket socket = null;
        long responseTime = -1;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout); // will force the
            // InterruptedIOException

            for (int attempts = 0; attempts <= retry && serviceStatus != 1; attempts++) {
                try {
                    // Send NTP request
                    //
                    byte[] data = new NtpMessage().toByteArray();
                    DatagramPacket outgoing = new DatagramPacket(data, data.length, ipv4Addr, port);
                    long sentTime = System.currentTimeMillis();
                    socket.send(outgoing);

                    // Get NTP Response
                    //
                    // byte[] buffer = new byte[512];
                    DatagramPacket incoming = new DatagramPacket(data, data.length);
                    socket.receive(incoming);
                    responseTime = System.currentTimeMillis() - sentTime;
                    double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
                    //这里要加2208988800，是因为获得到的时间是格林尼治时间，所以要变成东八区的时间，否则会与与北京时间有8小时的时差

                    // Validate NTP Response
                    // IOException thrown if packet does not decode as expected.
                    final NtpMessage msg = new NtpMessage(incoming.getData());
                    final double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;

                    System.out.println("poll: valid NTP request received the local clock offset is " + localClockOffset + ", responseTime= " + responseTime + "ms");
                    System.out.println("poll: NTP message : " + msg.toString());

                    final long finalResponseTime = responseTime;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder sb = new StringBuilder();

                            sb.append("poll : " + "\n");
                            sb.append("valid NTP request received the local clock offset is " + "\n");
                            sb.append(localClockOffset + "\n");
                            sb.append("\n");

                            sb.append("responseTime : " + "\n");
                            sb.append(finalResponseTime + "ms" + "\n");
                            sb.append("\n");

                            sb.append("poll : " + "\n");
                            sb.append("NTP message : " + "\n");
                            sb.append(msg.toString() + "\n");
                            sb.append("\n");

                            TextView tv = (TextView) findViewById(R.id.textViewShow);
                            tv.setText(sb);
                        }
                    });


                    serviceStatus = 1;
                } catch (InterruptedIOException ex) {
                    // Ignore, no response received.
                }
            }

        } catch (NoRouteToHostException e) {
            System.out.println("No route to host exception for address: " + ipv4Addr);
        } catch (ConnectException e) {
            // Connection refused. Continue to retry.
            e.fillInStackTrace();
            System.out.println("Connection exception for address: " + ipv4Addr);
        } catch (IOException ex) {
            ex.fillInStackTrace();
            System.out.println("IOException while polling address: " + ipv4Addr);
        } finally {
            if (socket != null)
                socket.close();
        }

        // Store response time if available
        //
        if (serviceStatus == 1) {
            System.out.println("responsetime==" + responseTime);
        }

    }


}
