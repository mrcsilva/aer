import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.List;
import java.util.ArrayList;
import java.net.DatagramPacket;
import java.lang.InterruptedException;
import java.lang.Exception;

class HelloReceiveThread extends Thread {


    private InetAddress ip;
    private Map<InetAddress, No> tabela;
    private BlockingQueue<DatagramPacket> queueH = null;

    public HelloReceiveThread(InetAddress ip, Map<InetAddress, No> tabela, BlockingQueue<DatagramPacket> queueH) {

        this.ip = ip;
        this.tabela = tabela;
        this.queueH = queueH;
    }

    void removeVizinhos(InetAddress ip){

        List<InetAddress> list=new ArrayList<InetAddress>();

        for (No no : this.tabela.values()){
            if(no.getIpVizinho().equals(ip)){
                list.add(no.getIp());
            }
        }

        for(int i=0 ;i < list.size();i++){

            if(this.tabela.containsKey(list.get(i))){
                this.tabela.remove(list.get(i));
                // System.out.println("No removido: " + list.get(i));
            }
        }
    }

    @Override
    public void run() {

        try {

            List<DatagramPacket> listR = new ArrayList<DatagramPacket>();

            while (true) {
                try {
                    Thread.sleep(6000); // dead interval
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                //verificar se o servidor tem atualizado o seu estado
                //retira todos os dados da queue e adiciona-os a lista
                queueH.drainTo(listR);

                if (!listR.isEmpty()) {
                    //Remover todos os elementos da lisa
                    listR.removeAll(listR);
                } else {
                    removeVizinhos(this.ip);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception io) {

        }
    }
}
