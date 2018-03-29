import java.net.MulticastSocket;
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
    private byte[] buf;

    public MulticastReceiveThread(MulticastSocket socket,Map<InetAddress,No> tabela) {
      this.socket = socket;
      this.tabela = tabela;
      this.buf = new byte[256];
  }


    public void addNos(InetAddress ipVizinho, String hello) throws IOException {

        //int k=0;
        String[] splited = hello.split("\\s+");
        for(int i=1; i <splited.length;i++){
            // byte bytes[] = InetAddress.getByName(splited[i]).getAddress();
            // for(No no : this.tabela.values()){
            //
            //     Inet6Address adr = (Inet6Address) no.getIp();
            //     k = adr.getScopeId();
            //     break;
            // }

            // InetAddress ipSalto = Inet6Address.getByAddress("",bytes,k);

            InetAddress ipSalto = InetAddress.getByName(splited[i]);

            if(!tabela.containsKey(ipSalto)){
                No no = new No(ipSalto, ipVizinho, 2, null, 0);
                tabela.put(ipSalto,no);
                // System.out.println("Nó Adicionado: " + ipSalto);
            }
            else if (tabela.get(ipSalto).getSaltos() > 2){
                tabela.get(ipSalto).setIpVizinho(ipVizinho);
            }
        }
    }

    public void removeNos(InetAddress ipVizinho ,String hello){

        String[] splited = hello.split("\\s+");

        List<String> listNos= new ArrayList<String>();
        List<String> listHel= new ArrayList<String>();

        for (No no : this.tabela.values()){
            if(no.getIpVizinho().equals(ipVizinho) && !no.getIp().equals(ipVizinho) && no.getSaltos() < 3 && no.getSaltos() != -1){
                String[] splited2 = no.getIp().getHostAddress().split("\\%");
                listNos.add(splited2[0]);
            }
        }

        for(int i=1; i < splited.length;i++){
            String[] splited3 = splited[i].split("\\%");
            listHel.add(splited3[0]);
        }

        listNos.removeAll(listHel);

        for(String s : listNos){
            try{
                // System.out.println("No removido: "+s);
                this.tabela.remove(InetAddress.getByName(s));
            }
            catch(Exception e){}
        }
    }



    @Override
    public void run() {
        try {
            socket = new MulticastSocket(9999);
            InetAddress group = InetAddress.getByName("FF02::1");
            socket.joinGroup(group);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            No no;

            while(true){
                socket.receive(packet);
                InetAddress ip = InetAddress.getByName(packet.getAddress().getHostAddress().split("\\%")[0]);
                String data = new String(packet.getData(), packet.getOffset(), packet.getLength());

                String dataH = "HELLO";
                String dataRQ = "ROUTE_REQUEST";
                String dataRP  = "ROUTE_REPLY";
                String[] splited = data.split("\\s+");

                if(splited[0].equals(dataH)){
                    if(tabela.containsKey(ip) && tabela.get(ip).getSaltos() >= 2){
                        tabela.remove(ip);
                     }


                    if(!tabela.containsKey(ip)){

                        BlockingQueue<DatagramPacket> queueH = new ArrayBlockingQueue<DatagramPacket>(1000);

                        no = new No(ip, ip, 1, queueH, 0);
                        tabela.put(ip,no);

                        addNos(ip,data);
                        HelloReceiveThread h = new HelloReceiveThread(ip,tabela,queueH);
                        h.start();

                        // System.out.println("Nó Adicionado: " + ip.getHostAddress());
                    }

                    if(tabela.containsKey(ip)){
                        if(!(tabela.get(ip).getSaltos() == 0)){
                           addNos(ip,data);
                           removeNos(ip,data);
                           no = tabela.get(ip);
                           no.getQueue().offer(packet);
                        }
                    }
                }
                else if(splited[0].equals(dataRQ)) {
                    No n = new No(null, null ,-1, null, 0);
                    RouteThread rt = new RouteThread(splited, tabela, ip, n);
                    rt.start();
                }
                else if(splited[0].equals(dataRP)) {
                    RouteThread rt = new RouteThread(splited, tabela, ip, null);
                    rt.start();
                }
                else if(splited[0].equals("GET_NEWS_FROM")) {
                    NewsThread nt = new NewsThread(splited, tabela, 0);
                    nt.start();
                }
                else if(splited[0].equals("NEWS_FOR")) {
                    NewsThread nt2 = new NewsThread(splited, tabela, 1);
                    nt2.start();
                }
            }
        } catch (Exception io) {
            System.out.println("EERRO " + io.getMessage());
            io.printStackTrace();
        }

    }
}
