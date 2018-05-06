import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.List;
import java.util.ArrayList;
import java.net.DatagramPacket;
import java.lang.InterruptedException;
import java.lang.Exception;

class MessageCleanerThread extends Thread {

    private Map<InetAddress,List<Message>> messages;
    private Map<Message, List<InetAddress>> sent;
    private Map<Message, Integer> toSend;
    private List<Message> listaR = new ArrayList<Message>();

    public MessageCleanerThread(Map<InetAddress, List<Message>> messages, Map<Message, Integer> toSend, Map<Message, List<InetAddress>> sent) {
        this.messages = messages;
        this.toSend = toSend;
        this.sent = sent;
    }

    @Override
    public void run() {

        try {

            //Remove todas as mensagens que não foram entregues num periodo de 5 minutos
            for(List<Message> l : messages.values()){
                listaR = new ArrayList<Message>();
                for(Message m : l){
                        // Necessario alterar para, por exemplo se passou 5 minutos apagar
                        if(System.currentTimeMillis() - m.getTime() > 300000) {
                            listaR.add(m);
                        }
                }
                for(Message m : listaR){
                    if(l.contains(m)) {
                        l.remove(m);
                        System.out.println("Apagado: " + m.toString());
                    }
                }
            }
            listaR = new ArrayList<Message>();
            for(Map.Entry<Message, Integer> entry : toSend.entrySet()){
                if(System.currentTimeMillis() - entry.getKey().getTime() > 300000) {
                    listaR.add(entry.getKey());
                }
            }
            for(Message m : listaR) {
                toSend.remove(m);
                sent.remove(m);
            }

            try {
                  Thread.sleep(300000); // Clean de 5 em 5 minutos
              } catch (InterruptedException e) {
                    System.out.println(e);
                }


        }
        catch (Exception io) {

        }
    }

}
