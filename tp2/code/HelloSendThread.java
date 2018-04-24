import java.net.MulticastSocket;
import java.util.Map;
import java.net.InetAddress;
import java.util.Map;
import java.io.IOException;
import java.net.DatagramPacket;
import java.lang.Exception;


class HelloSendThread extends Thread {


    private MulticastSocket socket;
    private byte[] buf;

    public HelloSendThread(MulticastSocket socket) {
        this.socket = socket;
        this.buf = new byte[256];
    }

    public void multicastSend(String hello) throws IOException {
        InetAddress group = InetAddress.getByName("FF02::1");
        socket.joinGroup(group);
        buf = new byte[256];
        buf = hello.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 9999);
        socket.send(packet);
        socket.leaveGroup(group);
    }

    @Override
    public void run() {
        try {
            while(true){
                String hello = "HELLO";
                multicastSend(hello);
                Thread.sleep(2000);    // hello interval = 2 segundos
            }
        }
        catch (Exception io) {
            io.printStackTrace();
        }
    }
}
