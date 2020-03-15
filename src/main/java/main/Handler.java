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
                    // �������� ID
                    Integer id = readId(sin);

                    if (id == null) {
                        continue;
                    }

                    MainFrame.getInstance().showMessage("������� " + id);
                    // �������� �
                    BigInteger e = readE(sin);
                    MainFrame.getInstance().showMessage("�������� E = " + e.toString());

                    // �������� N
                    BigInteger n = readN(sin);
                    MainFrame.getInstance().showMessage("�������� N = " + n.toString());

                    // ���� ��������� ���� � ����
                    UserInfo userInfo = findIdInFile(id);

                    if (userInfo == null) {
                        MainFrame.getInstance().showMessage("�������� ID");
                        continue;
                    }

                    // ��������� S
                    double i = Math.random();
                    String s = md4.hash(Double.toString(i));
                    MainFrame.getInstance().showMessage("������ S = " + s);

                    // ���������� S
                    byte[] rsss = RSA.encrypt(s.getBytes(), n, e);
                    sout.write(rsss);
                    sout.flush();

                    //�������� S
                    String hash = md4.hash(s);
                    MainFrame.getInstance().showMessage("��������� MD4(S) = " + hash);

                    // �������� ��� �� �������
                    byte[] d = new byte[130];
                    sin.read(d);
                    String clientHash = new String(RSA.decrypt(d, n, userInfo.secretKey));
                    MainFrame.getInstance().showMessage("MD4(S) �� ������� = " + clientHash);

                    if (clientHash.trim().equals(hash.trim())) {
                        showInfoMessage(userInfo.name + " �����������");
                    } else {
                        showWarnMessage("Hash(S) ��� " + userInfo.name + " ��������. ����������� �� ���������");
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
