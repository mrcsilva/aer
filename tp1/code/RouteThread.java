import java.net.MulticastSocket;
import java.net.InetAddress;
import java.util.Map;
import java.net.DatagramPacket;
import java.lang.Integer;

class RouteThread extends Thread {

    private MulticastSocket socket;
    private String[] data;
    private Map<InetAddress, No> tabela;
    private InetAddress source;
    private No addNo;

    public RouteThread(String[] data, Map<InetAddress, No> tabela, InetAddress source, No addNo) {
        this.data = data;
        this.tabela = tabela;
        this.source = source;
        this.addNo = addNo;
    }

    private String concat(String[] s) {
        String res = "";
        for(int i = 0; i < s.length; i++) {
            res += s[i]+" ";
        }
        return res;
    }

    private DatagramPacket newDatagram(String[] data, InetAddress group, int flag) {
        if(flag==1) {
            int ttl = Integer.parseInt(data[3]);
            ttl--;
            data[3] = ((Integer)ttl).toString();
        }
        String dados = concat(data);
        byte[] buf = new byte[1000];
        buf = dados.getBytes();
        DatagramPacket p = new DatagramPacket(buf, buf.length, group, 9999);
        return p;
    }

    @Override
    public void run() {
        try{
            socket = new MulticastSocket(9999);
            InetAddress group = InetAddress.getByName("FF02::1");
            socket.joinGroup(group);
            // Formato de um ROUTE_REQUEST
            // ROUTE_REQUEST IP_ORIGEM IP_DESTINO SALTOS TEMPO
            if(data[0].equals("ROUTE_REQUEST")) {
                InetAddress ip = InetAddress.getByName(data[2]);
                // Se não estiver na tabela e ainda tiver saltos para fazer
                if(!tabela.containsKey(ip) && !data[3].equals("0")) {
                    addNo.setIp(ip);
                    addNo.setIpVizinho(source);
                    tabela.put(ip, addNo);
                    RequestTimeoutThread rt = new RequestTimeoutThread(Integer.parseInt(data[4]), tabela, ip);
                    DatagramPacket p = newDatagram(data, group, 1);
                    socket.send(p);
                    rt.start();
                    // System.out.println("RRQ Sent : " + concat(data));
                }
                else if(tabela.containsKey(ip) && tabela.get(ip).getSaltos() != -1 && tabela.get(ip).getSaltos() < 3) {
                    // Se estiver na tabela e for ele proprio ou um vizinho ate nivel 2
                    String next = source.getHostAddress();
                    String resp[] = {"ROUTE_REPLY " + next + " " + tabela.get(ip).getSaltos() + " " + ip.getHostAddress()};
                    DatagramPacket p = newDatagram(resp, group, 0);
                    socket.send(p);
                    // System.out.println("RRP Sent: " + resp[0]);
                }
                else if(tabela.containsKey(ip) && tabela.get(ip).getSaltos() != -1 && tabela.get(ip).getSaltos() > 2 && !data[3].equals("0")) {
                    // Se esta na tabela e nao e vizinho
                    No n = tabela.get(ip);
                    // Se passarem mais de 10 minutos vai novamente à procura
                    if(System.currentTimeMillis() - n.getTime() > 10*60*1000) {
                        n.setSaltos(-1);
                        n.setIpVizinho(source);
                        RequestTimeoutThread rt = new RequestTimeoutThread(Integer.parseInt(data[4]), tabela, ip);
                        DatagramPacket p = newDatagram(data, group, 1);
                        socket.send(p);
                        rt.start();
                        Thread.sleep(Integer.parseInt(data[4]));
                        if(tabela.containsKey(n.getIp())) {
                            n = tabela.get(n.getIp());
                            String next = source.getHostAddress();
                            String resp[] = {"ROUTE_REPLY " + next + " " + n.getSaltos() + " " + ip.getHostAddress()};
                            p = newDatagram(resp, group, 0);
                            socket.send(p);
                        }
                    }
                    else {
                        // Se tempo de adicao < 10min responde mesmo nao sendo vizinho de nivel 2
                        String next = source.getHostAddress();
                        String resp[] = {"ROUTE_REPLY " + next + " " + n.getSaltos() + " " + ip.getHostAddress()};
                        DatagramPacket p = newDatagram(resp, group, 0);
                        socket.send(p);
                        // System.out.println("RRP Sent: " + resp[0]);
                    }
                }
            }
            else if(data[0].equals("ROUTE_REPLY")) {
                // Formato de um ROUTE_REPLY
                // ROUTE_REPLY NEXT_IP SALTOS IP_DESTINO_ROUTE_REQUEST
                InetAddress ip = InetAddress.getByName(data[3]);
                int salt = -1;
                for(No n : tabela.values()) {
                    if(n.getIp().getHostAddress().equals(data[1])) {
                        salt = n.getSaltos();
                        break;
                    }
                }
                if(salt == 0 && tabela.containsKey(ip)) {
                    int saltos = Integer.parseInt(data[2]);
                    No n = tabela.get(ip);
                    String next = n.getIpVizinho().getHostAddress();
                    String resp[] = new String[1];
                    if(n.getSaltos() == -1) {
                        n.setSaltos(saltos+1);
                        n.setIpVizinho(source);
                        n.setTime(System.currentTimeMillis());
                        resp[0] = data[0]+" "+next+" "+(saltos+1)+" "+data[3];
                        DatagramPacket p = newDatagram(resp, group, 0);
                        socket.send(p);
                        // System.out.println("RRP Sent: " + resp[0]);
                    }
                }
            }
            socket.leaveGroup(group);
        }
        catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

}
