package com.ykerit.Configs;

import org.greatfree.util.Tools;

public class RegisterServerConfig {
    public static final String REGISTER_SERVER_KEY = Tools.generateUniqueKey();
    public static final String REGISTER_SERVER_NAME = Tools.generateUniqueKey();
    public static final int REGISTER_SERVER_PORT = 8893;
    public static final int PEER_REGISTER_PORT = 8894;

    public RegisterServerConfig() {
    }
}
