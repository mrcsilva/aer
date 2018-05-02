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
    private List<Message> listaR = new ArrayList<Message>();

    public MessageCleanerThread(Map<InetAddress,List<Message>> messages) {
        this.messages = messages;
    }

    @Override
    public void run() {

        try {

            //Remove todas as mensagens que não foram entregues num periodo de 5 minutos
            for(List<Message> l : messages.values()){

                listaR = new ArrayList<Message>();

                for(Message m : l){

                        // Necessario alterar para ,por exemplo se passou 5 minutos apagar
                        if(m.getTime() > 60000) {

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
