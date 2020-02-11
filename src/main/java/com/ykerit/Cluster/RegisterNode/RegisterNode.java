package com.ykerit.Cluster.RegisterNode;

import org.greatfree.cluster.root.container.ClusterPeerContainer;

public class RegisterNode {
    private ClusterPeerContainer clusterPeer;
    private final static RegisterNode instance = new RegisterNode();

    private RegisterNode() {
    }

    public RegisterNode CS() {
        if (instance == null) {
            return new RegisterNode();
        }
        return instance;
    }

    public void start() {
    }
}
