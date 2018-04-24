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
    private Map<InetAddress,No> tabela;
    private Map<InetAddress,List<Message>> messages;
    private byte[] buf;

    public MulticastReceiveThread(MulticastSocket socket,Map<InetAddress,No> tabela,Map<InetAddress,List<Message>> messages) {
        this.socket = socket;
        this.tabela = tabela;
        this.messages = messages;
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
            DatagramSocket socket2 = new DatagramSocket();
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

                        no = new No(ip, 0, 1, queueH, 0);
                        tabela.put(ip,no);

                        HelloReceiveThread h = new HelloReceiveThread(ip,tabela,1,queueH);
                        h.start();
                        List<Message> toDelete = new ArrayList<>();
                        try {
                            if(messages.containsKey(ip)){
                                toDelete.clear();
                                for ( Message ma : messages.get(ip)){
                                    buf = ma.getMess().getBytes();
                                    sendPacket = new DatagramPacket(buf,buf.length, ip, 6666);
                                    socket2.send(sendPacket);
                                    toDelete.add(ma);
                                }
                                // Remove todas as mensagens enviadas
                                messages.get(ip).removeAll(toDelete);

                                // Apenas elimina o IP da tabela de já nao houver mais mensagens para enviar
                                if(messages.get(ip).size() == 0) {
                                    messages.remove(ip);
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
                    else {
                        if(!(tabela.get(ip).getSaltos() == 0)) {
                           no = tabela.get(ip);
                           no.getQueue().offer(packet);
                        }
                    }
                }
            }
        } catch (Exception io) {
            socket2.close();
            System.out.println("EERRO " + io.getMessage());
            io.printStackTrace();
        }

    }
}
