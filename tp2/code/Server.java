class Server {

    private static String noticia;

    public static void main(String[] args) {
        Socket s;
        int opcao = 1;
        Scanner reader = new Scanner(System.in);
        noticia = "";
        try {
            s = new Socket("localhost", 9999);
            PrintWriter send = new PrintWriter(s.getOutputStream(), true);
            send.println("SERVER");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        while(opcao != 0) {
            System.out.println("\nOpções");
            System.out.println("1 - Modificar noticia");
            System.out.println("0 - Sair\n");
            opcao = reader.nextInt();
            reader.nextLine();
            switch (opcao) {
                case 0: System.exit(0);
                        break;
                case 1: System.out.println("Insira a noticia:");
                        noticia = reader.nextLine();
                        break;
                default: System.out.println("Opção inválida!");
            }
        }
        reader.close();
    }

}

class HandleRequest extends Thread {

    private Socket s;

    public HandleRequest(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        String temp;
        while(true) {
            temp = in.readLine();
            String[] split = temp.split(" ");
            if(split[0].equals("GET_NEWS_FROM")) {
                out.println("NEWS_FOR " + split[2] + " " + split[1] + " " + noticia);
            }
        }
    }

}
