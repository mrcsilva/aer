import java.net.MulticastSocket;
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


class MulticastReceiveThread extends Thread {

    private MulticastSocket socket;
    private DatagramSocket socket2;
    private Map<InetAddress,No> tabela;
    private Map<InetAddress,List<Message>> messages;
    private Map<Message, Integer> toSend;
    private Map<Message, List<InetAddress>> sent;
    private byte[] buf;

    public MulticastReceiveThread(MulticastSocket socket, DatagramSocket socket2, Map<InetAddress,No> tabela, Map<InetAddress,List<Message>> messages, Map<Message, Integer> toSend, Map<Message, List<InetAddress>> sent) {
        this.socket = socket;
        this.socket2 = socket2;
        this.tabela = tabela;
        this.messages = messages;
        this.toSend = toSend;
        this.sent = sent;
        this.buf = new byte[256];
    }

    @Override
    public void run() {
        try {
            socket = new MulticastSocket(9999);
            InetAddress group = InetAddress.getByName("FF02::1");
            socket.joinGroup(group);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            No no;
            DatagramPacket sendPacket;

            while(true){
                socket.receive(packet);
                InetAddress ip = InetAddress.getByName(packet.getAddress().getHostAddress().split("\\%")[0]);
                String data = new String(packet.getData(), packet.getOffset(), packet.getLength());

                String dataH = "HELLO";
                String[] splited = data.split("\\s+");

                if(splited[0].equals(dataH)){
                    if(!tabela.containsKey(ip)){

                        BlockingQueue<DatagramPacket> queueH = new ArrayBlockingQueue<DatagramPacket>(1000);

                        no = new No(ip, 1, 1, queueH, 0);
                        tabela.put(ip,no);

                        HelloReceiveThread h = new HelloReceiveThread(ip, tabela, 1, queueH);
                        h.start();
                        SendMessagesThread sm = new SendMessagesThread(socket2, messages, toSend, sent, ip);
                        sm.start();
                    }
                    else {
                        if(!(tabela.get(ip).getSaltos() == 0)) {
                           no = tabela.get(ip);
                           no.getQueue().offer(packet);
                        }
                    }
                }
            }
        } catch (Exception io) {
            io.printStackTrace();
        }

    }
}

class SendMessagesThread extends Thread {

    private DatagramSocket socket2;
    private Map<InetAddress,List<Message>> messages;
    private Map<Message, Integer> toSend;
    private Map<Message, List<InetAddress>> sent;
    private InetAddress ip;
    private byte[] buf;

    public SendMessagesThread(DatagramSocket socket2, Map<InetAddress,List<Message>> messages, Map<Message, Integer> toSend, Map<Message, List<InetAddress>> sent, InetAddress ip) {
        this.socket2 = socket2;
        this.messages = messages;
        this.toSend = toSend;
        this.sent = sent;
        this.ip = ip;
    }

    public void run() {
        List<Message> toDelete = new ArrayList<>();
        DatagramPacket sendPacket = null;
        try {
            // Envia mensagens que possa ter guardadas para o novo no
            if(messages.containsKey(ip)) {
                toDelete.clear();
                for(Message ma : messages.get(ip)) {
                    buf = ma.toString().getBytes();
                    sendPacket = new DatagramPacket(buf, buf.length, ip, 6666);
                    socket2.send(sendPacket);
                    System.out.println("Sent to: " + ip.getHostAddress());
                    System.out.println("\tMessage: " + ma.toString());
                    toDelete.add(ma);
                }
                // Remove todas as mensagens enviadas
                messages.get(ip).removeAll(toDelete);
                toDelete.clear();
                // Apenas elimina o IP da tabela se já nao houver mais mensagens para enviar
                if(messages.get(ip).size() == 0) {
                    messages.remove(ip);
                }
            }
            // Envia os novos GET_NEWS_FROM e NEWS_FOR para as novas conexoes
            int remove = 0;
            for (Map.Entry<Message, Integer> entry : toSend.entrySet()) {
                Message m = entry.getKey();
                Integer num = entry.getValue();
                if(!sent.get(m).contains(ip) && num > 0) {
                    buf = m.toString().getBytes();
                    sendPacket = new DatagramPacket(buf, buf.length, ip, 6666);
                    socket2.send(sendPacket);
                    if(ip.equals(InetAddress.getByName(m.toString().split(" ")[2]))) {
                        remove = 1;
                    }
                    else {
                        sent.get(m).add(ip);
                        num--;
                        entry.setValue(num);
                    }
                }
                if(num == 0 || remove == 1) {
                    sent.remove(m);
                    toSend.remove(m);
                    remove = 0;
                }
            }
        }
        catch(IOException io) {
            // Se ocorrer um erro remove todas as mensagens até agora enviadas da lista
            // Se todas foram enviadas remove o IP da tabela
            messages.get(ip).removeAll(toDelete);
            if(messages.get(ip).size() == 0) {
                messages.remove(ip);
            }
            io.printStackTrace();
        }

        // System.out.println("Nó Adicionado: " + ip.getHostAddress());
    }

}
