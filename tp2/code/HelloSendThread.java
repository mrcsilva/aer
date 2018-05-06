import java.net.MulticastSocket;
import java.util.Map;
import java.net.InetAddress;
import java.util.Map;
import java.io.IOException;
import java.net.DatagramPacket;
import java.lang.Exception;
import java.lang.InterruptedException;


class HelloSendThread extends Thread {


    private MulticastSocket socket;
    private byte[] buf;

    // Executa o envio de HELLO
    public HelloSendThread(MulticastSocket socket) {
        this.socket = socket;
    }

    public void multicastSend(String hello) throws IOException {
        InetAddress group = InetAddress.getByName("FF02::1");
        socket.joinGroup(group);
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
                Thread.sleep(500);    // hello interval = 0.5 segundos
            }
        }
        catch (Exception io) {
            if(!io.getClass().equals(new InterruptedException())) {
                io.printStackTrace();
            }
        }
    }
}
