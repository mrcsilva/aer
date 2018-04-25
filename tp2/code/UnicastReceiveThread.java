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


class UnicastReceiveThread extends Thread {

    private DatagramSocket socket;
    private Map<InetAddress,No> tabela;
    private Map<InetAddress,List<Message>> messages;
    private Map<Message, Integer> toSend;
    private Map<Message, List<InetAddress>> sent;
    private byte[] buf;

    public UnicastReceiveThread(DatagramSocket socket, Map<InetAddress,No> tabela, Map<InetAddress, List<Message>> messages, Map<Message, Integer> toSend, Map<Message, List<InetAddress>> sent) {
      this.socket = socket;
      this.tabela = tabela;
      this.messages = messages;
      this.toSend = toSend;
      this.sent = sent;
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


                // Tem de se ver se pode ficar:
                // if(splited[0].equals("GET_NEWS_FROM") || splited[0].equals("NEWS_FOR"))

                //Trata de mensagens Get News recebidas pelos casos :
                // È o recetor final
                // Não é o recetor final mas tem na tabela registo do destino
                // Não está na tabela mas tem já mensagens para o destino
                // Não está na tabela nem existem mensagens para o destino

                if(splited[0].equals("GET_NEWS_FROM")) {
                  if(this.tabela.containsKey(dest)) {
                    if(this.tabela.get(dest).getSaltos() == 0) {
                        // TCP para ele proprio

                    }
                    else{
                        // UDP para o no final
                        buf = data.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, dest, 6666);
                        socket.send(sendPacket);
                    }
                  }
                  else {
                    Message m = new Message(source, dest, data, System.currentTimeMillis(), true);
                    if(messages.containsKey(dest)) {
                        this.messages.get(dest).add(m);
                    }
                    else {
                        List message = new ArrayList<Message>();
                        message.add(m);
                        this.messages.put(dest,message);
                    }
                  }
                }
                else if(splited[0].equals("NEWS_FOR")) {
                    if(this.tabela.containsKey(dest)) {
                        if(this.tabela.get(dest).getSaltos() == 0) {
                            // TCP para ele proprio

                        }
                        else {
                            // UDP para o no final
                            buf = data.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, dest, 6666);
                            socket.send(sendPacket);
                        }
                    }
                    else {
                        Message m = new Message(source, dest, data, System.currentTimeMillis(), false);
                        if(messages.containsKey(dest)) {
                            this.messages.get(dest).add(m);
                        }
                        else {
                            List message = new ArrayList<Message>();
                            message.add(m);
                            this.messages.put(dest,message);
                        }
                    }
                }
            }
        } catch (Exception io) {
            System.out.println("EERRO " + io.getMessage());
            io.printStackTrace();
        }

    }
}
