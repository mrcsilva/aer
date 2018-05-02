import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.io.IOException;
import java.net.Inet6Address;
import java.util.List;
import java.util.ArrayList;
import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.lang.Exception;
import java.net.Socket;
import java.io.PrintWriter;


class UnicastReceiveThread extends Thread {

    private DatagramSocket socket;
    private Map<InetAddress,No> tabela;
    private Map<InetAddress,List<Message>> messages;
    private byte[] buf;

    public UnicastReceiveThread(DatagramSocket socket, Map<InetAddress,No> tabela, Map<InetAddress, List<Message>> messages) {
      this.socket = socket;
      this.tabela = tabela;
      this.messages = messages;
      this.buf = new byte[256];
  }

    @Override
    public void run() {
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while(true){
                socket.receive(packet);
                String data = new String(packet.getData(), packet.getOffset(), packet.getLength());


                String[] splited = data.split("\\s+");
                InetAddress source = InetAddress.getByName(splited[1]);
                InetAddress dest = InetAddress.getByName(splited[2]);

                // System.out.println("Recebido Unicast: " + data);

                if(splited[0].equals("GET_NEWS_FROM")) {
                  if(this.tabela.containsKey(dest)) {
                    if(this.tabela.get(dest).getSaltos() == 0) {
                        // TCP para ele proprio
                        Socket s = new Socket("localhost", 9999);
                        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                        out.println(data);
                        s.close();
                    }
                    else{
                        // UDP para o no final
                        buf = data.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, dest, 6666);
                        socket.send(sendPacket);
                        // System.out.println("Sent to: " + dest.getHostAddress());
                        // System.out.println("\tPacote: " + data);
                    }
                  }
                  else {
                    Message m = new Message(source, dest, "", System.currentTimeMillis(), true);
                    if(messages.containsKey(dest)) {
                        this.messages.get(dest).add(m);
                    }
                    else {
                        List<Message> message = new ArrayList<>();
                        message.add(m);
                        this.messages.put(dest, message);
                    }
                  }
                }
                else if(splited[0].equals("NEWS_FOR")) {
                    if(this.tabela.containsKey(dest)) {
                        if(this.tabela.get(dest).getSaltos() == 0) {
                            // TCP para ele proprio
                            Socket s = new Socket("localhost", 9999);
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            out.println(data);
                            s.close();
                        }
                        else {
                            // UDP para o no final
                            buf = data.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, dest, 6666);
                            socket.send(sendPacket);
                            // System.out.println("Sent to: " + dest.getHostAddress());
                            // System.out.println("\tPacote: " + data);
                        }
                    }
                    else {
                        String temp = "";
                        for(int i = 3; i < splited.length-1; i++) {
                            temp += splited[i] + " ";
                        }
                        Message m = new Message(source, dest, temp, System.currentTimeMillis(), false);
                        if(messages.containsKey(dest)) {
                            this.messages.get(dest).add(m);
                        }
                        else {
                            List<Message> message = new ArrayList<>();
                            message.add(m);
                            this.messages.put(dest, message);
                        }
                    }
                }
            }
        } catch (Exception io) {
            io.printStackTrace();
        }

    }
}
