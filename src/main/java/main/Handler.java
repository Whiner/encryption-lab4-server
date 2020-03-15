package main;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static main.GuiUtil.showInfoMessage;
import static main.GuiUtil.showWarnMessage;

/**
 * @author Sasha
 */
public class Handler extends Thread {
    private ServerSocket serverSocket;
    private MD4 md4 = new MD4();

    public Handler() throws IOException {
        super();
        final int port = 8033;
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        do {
            try {
                Socket socket = serverSocket.accept();
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();

                do {
                    // получаем ID
                    Integer id = readId(sin);

                    if (id == null) {
                        continue;
                    }

                    MainFrame.getInstance().showMessage("Получен " + id);
                    // получаем Е
                    BigInteger e = readE(sin);
                    MainFrame.getInstance().showMessage("Получено E = " + e.toString());

                    // получаем N
                    BigInteger n = readN(sin);
                    MainFrame.getInstance().showMessage("Получено N = " + n.toString());

                    // ищем секретный ключ в базе
                    UserInfo userInfo = findIdInFile(id);

                    if (userInfo == null) {
                        MainFrame.getInstance().showMessage("Неверный ID");
                        continue;
                    }

                    // формируем S
                    double i = Math.random();
                    String s = md4.hash(Double.toString(i));
                    MainFrame.getInstance().showMessage("Строка S = " + s);

                    // Отправляем S
                    byte[] rsss = RSA.encrypt(s.getBytes(), n, e);
                    sout.write(rsss);
                    sout.flush();

                    //Хэшируем S
                    String hash = md4.hash(s);
                    MainFrame.getInstance().showMessage("Серверный MD4(S) = " + hash);

                    // Получаем хэш от клиента
                    byte[] d = new byte[130];
                    sin.read(d);
                    String clientHash = new String(RSA.decrypt(d, n, userInfo.secretKey));
                    MainFrame.getInstance().showMessage("MD4(S) от клиента = " + clientHash);

                    if (clientHash.trim().equals(hash.trim())) {
                        showInfoMessage(userInfo.name + " авторизован");
                    } else {
                        showWarnMessage("Hash(S) для " + userInfo.name + " неверный. Авторизация не выполнена");
                    }


                } while (true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } while (true);
    }

    private Integer readId(InputStream sin) throws IOException {
        byte[] data = new byte[300];
        sin.read(data);
        ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default
        int value = wrapped.getInt();
        return value > 0 ? value : null;
    }

    private BigInteger readE(InputStream sin) throws IOException {
        byte[] d = new byte[2];
        sin.read(d);
        return new BigInteger(d);
    }

    private BigInteger readN(InputStream sin) throws IOException {
        byte[] d = new byte[129];
        sin.read(d);
        return new BigInteger(d);
    }

    private UserInfo findIdInFile(int id) throws IOException {
        File file = new File("users.txt");
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(file),
                                Charset.forName("cp1251")
                        )
                )
        ) {
            String s;
            while ((s = in.readLine()) != null) {
                String[] arr;
                arr = s.split(",");
                if (id == Integer.parseInt(arr[0])) {
                    return new UserInfo(arr[1], new BigInteger(arr[2].trim()));
                }
            }
        }

        return null;
    }

    private static class UserInfo {
        private String name;
        private BigInteger secretKey;

        public UserInfo(String name, BigInteger secretKey) {
            this.name = name;
            this.secretKey = secretKey;
        }
    }
}
