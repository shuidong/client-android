package red.itvirtuoso.pingpong3.app.server;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kenji on 15/05/03.
 */
public abstract class ServerProxy {
    private ArrayList<Packet> packets;

    public ServerProxy() {
        packets = new ArrayList<>();
    }

    protected final void add(Packet packet) {
        synchronized (packets) {
            packets.add(packet);
        }
    }

    public abstract void connect() throws IOException;
    public abstract void send(Packet packet) throws IOException;

    public final Packet receive() {
        synchronized (packets) {
            return packets.size() > 0 ? packets.remove(0) : null;
        }
    }

    public abstract void disconnect();
}
