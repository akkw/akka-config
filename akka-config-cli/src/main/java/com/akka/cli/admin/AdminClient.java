package com.akka.cli.admin;/* 
    create qiangzhiwei time 2023/2/13
 */

public class AdminClient {


    private AdminNetworkClient adminNetworkClient;


    public void createNamespace(String namespace, String environment) {

    }

    public int createConfig(String namespace, String environment, String body) {
        return 0;
    }

    public boolean deleteConfig(String namespace, String environment, Integer version) {
        return false;
    }

    public String readAllConfig(String namespace, String environment) {
        return null;
    }

    public String activateConfig(String namespace, String environment, Integer version) {
        return null;
    }

    public String verifyConfig(String namespace, String environment, Integer version) {
        return null;
    }


}
