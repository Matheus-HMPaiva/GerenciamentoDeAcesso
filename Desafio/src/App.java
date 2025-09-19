import java.io.*;
import java.net.*;
import java.util.*;

public class AccessManager {
    static Scanner in = new Scanner(System.in);
    static final String USERS_FILE = "/tmp/users.txt";
    static final String EVENTS_FILE = "/tmp/events.txt";
    static final String GPIO1 = "/tmp/gpio1";
    static final String GPIO2 = "/tmp/gpio2";

    // Configuração do servidor
    static final String SERVER_HOST = "127.0.0.1"; // ajustar se precisar
    static final int SERVER_PORT = 502; // porta Modbus/TCP

    public static void main(String[] args) throws Exception {
        while (true) {
            showMenu();
            String opt = in.nextLine().trim();
            switch (opt) {
                case "1": registerUser(); break;
                case "2": listUsers(); break;
                case "3": listEvents(); break;
                case "4": openPort(1); break;
                case "5": openPort(2); break;
                case "0": System.out.println("Saindo..."); return;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    static void showMenu() {
        System.out.println("\n--- GERENCIAMENTO DE ACESSO ---");
        System.out.println("1) Cadastrar usuário");
        System.out.println("2) Listar usuários");
        System.out.println("3) Listar eventos (admin)");
        System.out.println("4) Liberar porta 1");
        System.out.println("5) Liberar porta 2");
        System.out.println("0) Sair");
        System.out.print("Escolha: ");
    }

    static void registerUser() {
        try {
            System.out.print("Nome: "); String nome = in.nextLine();
            System.out.print("Senha: "); String senha = in.nextLine();
            System.out.print("Admin? (s/n): "); String a = in.nextLine();
            boolean isAdmin = a.toLowerCase().startsWith("s");
            String line = nome+"|"+senha+"|"+(isAdmin?"1":"0")+"\n";
            try (FileWriter fw = new FileWriter(USERS_FILE, true)) { fw.write(line); }
            logEvent("CADASTRO: "+nome);
            sendToServer(("Cadastro:"+nome).getBytes());
            System.out.println("Usuário cadastrado.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void listUsers() {
        try {
            File f = new File(USERS_FILE);
            if (!f.exists()) { System.out.println("Nenhum usuário."); return; }
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String ln; System.out.println("Usuários:");
                while ((ln=br.readLine())!=null) {
                    String[] p = ln.split("\\|");
                    System.out.println(" - "+p[0]+(p[2].equals("1")?" (admin)":""));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void listEvents() throws IOException {
        System.out.print("Senha admin: ");
        String s = in.nextLine();
        if (!isAdminPassword(s)) { System.out.println("Senha incorreta."); return; }
        File f = new File(EVENTS_FILE);
        if (!f.exists()) { System.out.println("Nenhum evento."); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ln; while ((ln=br.readLine())!=null) System.out.println(ln);
        }
    }

    static boolean isAdminPassword(String s) throws IOException {
        File f = new File(USERS_FILE); if (!f.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ln; while((ln=br.readLine())!=null) {
                String[] p = ln.split("\\|");
                if (p[2].equals("1") && p[1].equals(s)) return true;
            }
        }
        return false;
    }

    static void openPort(int p) {
        try {
            System.out.print("Usuário: "); String nome = in.nextLine();
            System.out.print("Senha: "); String senha = in.nextLine();
            if (!checkUser(nome, senha)) { System.out.println("Credenciais inválidas."); return; }

            String gpio = (p==1?GPIO1:GPIO2);
            try (FileWriter fw = new FileWriter(gpio)) { fw.write("1"); }
            logEvent("ABERTURA porta "+p+" por "+nome);

            // enviar frame Modbus RTU -> simulado em Modbus/TCP
            byte addr = 0x01;
            int reg = (p==1?0x34:0x35);
            byte value = (byte)0xFF;
            byte[] frame = buildModbusFrame(addr, reg, value);
            sendToServer(frame);

            System.out.println("Porta "+p+" liberada.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    static boolean checkUser(String nome, String senha) throws IOException {
        File f = new File(USERS_FILE); if (!f.exists()) return false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ln; while((ln=br.readLine())!=null) {
                String[] p = ln.split("\\|");
                if (p[0].equals(nome) && p[1].equals(senha)) return true;
            }
        }
        return false;
    }

    static void logEvent(String txt) {
        try (FileWriter fw = new FileWriter(EVENTS_FILE, true)) {
            fw.write(new Date()+" - "+txt+"\n");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Constrói um frame Modbus RTU simples (Write Single Register)
    static byte[] buildModbusFrame(byte addr, int reg, byte value) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(addr);           // address
        out.write(0x06);           // function code (write single register)
        out.write((reg>>8)&0xFF);  // reg hi
        out.write(reg&0xFF);       // reg lo
        out.write(0x00);           // value hi
        out.write(value & 0xFF);   // value lo
        // CRC fake (para RTU), aqui só pra completar
        out.write(0x00); out.write(0x00);
        return out.toByteArray();
    }

    // Envia dados simulando Modbus via TCP (para um servidor)
    static void sendToServer(byte[] data) {
        try (Socket s = new Socket(SERVER_HOST, SERVER_PORT)) {
            OutputStream out = s.getOutputStream();
            out.write(data);
            out.flush();
        } catch (Exception e) {
            System.out.println("Falha ao enviar para servidor: "+e.getMessage());
        }
    }
}
