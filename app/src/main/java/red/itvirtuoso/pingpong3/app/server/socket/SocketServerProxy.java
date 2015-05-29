package red.itvirtuoso.pingpong3.app.server.socket;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import red.itvirtuoso.pingpong3.app.server.Packet;
import red.itvirtuoso.pingpong3.app.server.PacketType;
import red.itvirtuoso.pingpong3.app.server.ServerProxy;

/**
 * Created by kenji on 15/05/07.
 */
public class SocketServerProxy extends ServerProxy implements Runnable {
    private static final String TAG = SocketServerProxy.class.getName();

    private String mHost;
    private int mPort;
    private Socket mSocket;

    public SocketServerProxy(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    @Override
    public void connect() throws IOException {
        Log.d(TAG, "execute " + new Object(){}.getClass().getEnclosingMethod().getName());
        Log.d(TAG, "host = " + mHost + ", port = " + mPort);
        InetAddress address = InetAddress.getByName(mHost);
        mSocket = new Socket(address, mPort);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(this);
        service.shutdown();
    }

    @Override
    public void send(Packet packet) throws IOException {
        mSocket.getOutputStream().write(packet.getType().getId());
    }

    @Override
    public void run() {
        while (true) {
            int data = 0;
            try {
                data = mSocket.getInputStream().read();
            } catch (IOException e) {
                disconnect();
                break;
            }
            if (data < 0) {
                disconnect();
                break;
            }
            Packet packet = new Packet(PacketType.valueOf(data));
            add(packet);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            Log.w(TAG, "ソケットのクローズに失敗しました", e);
        }
    }
}
