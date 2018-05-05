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
      buf = new byte[1024];
  }

    @Override
    public void run() {
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while(true){
                socket.receive(packet);
                String data = new String(packet.getData(), packet.getOffset(), packet.getLength());

                // Thread auxiliar para conseguir efetuar processamento de varias mensagens em simultaneo
                HandleUnicastPacket hp = new HandleUnicastPacket(socket, tabela, messages, data);
                hp.start();
            }
        }
        catch (Exception io) {
            io.printStackTrace();
        }

    }
}

class HandleUnicastPacket extends Thread {

    private DatagramSocket socket;
    private Map<InetAddress,No> tabela;
    private Map<InetAddress,List<Message>> messages;
    private String data;
    private byte[] buf;

    public HandleUnicastPacket(DatagramSocket socket, Map<InetAddress, No> tabela, Map<InetAddress, List<Message>> messages, String data) {
        this.socket = socket;
        this.tabela = tabela;
        this.messages = messages;
        this.data = data;
    }

    @Override
    public void run() {
        try {
            String[] splited = data.split("\\s+");
            InetAddress source = InetAddress.getByName(splited[1]);
            InetAddress dest = InetAddress.getByName(splited[2]);

            // System.out.println("Recebido Unicast: " + data);

            if(splited[0].equals("GET_NEWS_FROM")) {
                // Se o pacote recebido for GET_NEWS_FROM:
                // - Verifica se o destino consta da sua tabela e trata de enviar o pacote
                // - Caso contrario adiciona-o a tabela de mensagens a serem enviadas
                if(this.tabela.containsKey(dest)) {
                    // Se constar e for ele proprio
                    if(this.tabela.get(dest).getSaltos() == 0) {
                        // Faz TCP para a porta 9999
                        Socket s = new Socket("localhost", 9999);
                        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                        out.println(data);
                        s.close();
                    }
                    else{
                        // Caso nao seja -> UDP para o no final
                        buf = data.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, dest, 6666);
                        socket.send(sendPacket);
                        // System.out.println("Sent to: " + dest.getHostAddress());
                        // System.out.println("\tPacote: " + data);
                    }
                }
                else {
                    // Adicionar as mensagens a serem enviadas
                    Message m = new Message(source, dest, "", Long.parseLong(splited[3]), true);
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
                // Para o NEWS_FOR o processo e o mesmo que no GET_NEWS_FROM
                if(this.tabela.containsKey(dest)) {
                    if(this.tabela.get(dest).getSaltos() == 0) {
                        Socket s = new Socket("localhost", 9999);
                        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                        out.println(data);
                        s.close();
                    }
                    else {
                        buf = data.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, dest, 6666);
                        socket.send(sendPacket);
                        // System.out.println("Sent to: " + dest.getHostAddress());
                        // System.out.println("\tPacote: " + data);
                    }
                }
                else {
                    String temp = "";
                    for(int i = 5; i < splited.length; i++) {
                        temp += splited[i] + " ";
                    }
                    Message m = new Message(source, dest, temp, Long.parseLong(splited[3]), false);
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
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
