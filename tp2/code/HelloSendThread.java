import java.net.MulticastSocket;
import java.util.Map;
import java.net.InetAddress;
import java.util.Map;
import java.io.IOException;
import java.net.DatagramPacket;
import java.lang.Exception;


class HelloSendThread extends Thread {


    private MulticastSocket socket;
    private Map<InetAddress,No> tabela;
    private byte[] buf;

    public HelloSendThread(MulticastSocket socket,Map<InetAddress,No> tabela) {
        this.socket = socket;
        this.tabela = tabela;
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

    public String addVizinhos(String hello){
        for (No no : tabela.values()){
            if(no.getSaltos() == 1){
                hello = hello + " " + no.getIp().getHostAddress().split("\\%")[0]; //;no.getIp().getScopeId();
            }
        }
        return hello;
    }


    @Override
    public void run() {
        try {
            while(true){
                String hello = "HELLO";
                hello = addVizinhos(hello);
                multicastSend(hello);
                try {
                     Thread.sleep(3000);    //hello interval = 3 segundos
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        catch (Exception io) {

        }
    }
}
