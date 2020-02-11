package com.ykerit.Cluster.RegisterNode;

import com.ykerit.Configs.RegisterServerConfig;
import org.greatfree.server.PeerRegistry;
import org.greatfree.util.Tools;

import java.net.SocketException;

public class Register {
    public static void register() throws SocketException {
        String localIP = Tools.getLocalIP();
        // register  register_server
        PeerRegistry.SYSTEM().register(
                RegisterServerConfig.REGISTER_SERVER_KEY,
                RegisterServerConfig.REGISTER_SERVER_NAME,
                localIP,
                RegisterServerConfig.PEER_REGISTER_PORT
        );
        // register others node
        PeerRegistry.SYSTEM().registeOthers(
                RegisterServerConfig.REGISTER_SERVER_KEY,
                RegisterServerConfig.REGISTER_SERVER_NAME,
                localIP,
                RegisterServerConfig.REGISTER_SERVER_PORT);
    }

    public static void unregister() {
        PeerRegistry.SYSTEM().dispose();
    }
}
