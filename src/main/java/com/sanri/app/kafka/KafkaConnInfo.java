package com.sanri.app.kafka;

public class KafkaConnInfo {
    private String clusterName;
    private String clusterVersion;
    private String zkConnectStrings;
    private String chroot = "/";

    private String saslMechanism = "GSSAPI";
    private String securityProtocol = "SASL_PLAINTEXT";

    private String jaasConfig;

    public KafkaConnInfo() {
    }

    public KafkaConnInfo(String clusterName, String clusterVersion, String zkConnectStrings, String chroot, String saslMechanism) {
        this.clusterName = clusterName;
        this.clusterVersion = clusterVersion;
        this.zkConnectStrings = zkConnectStrings;
        this.chroot = chroot;
        this.saslMechanism = saslMechanism;
    }

    public KafkaConnInfo(String clusterName, String clusterVersion, String zkConnectStrings) {
        this.clusterName = clusterName;
        this.clusterVersion = clusterVersion;
        this.zkConnectStrings = zkConnectStrings;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public String getZkConnectStrings() {
        return zkConnectStrings;
    }

    public void setZkConnectStrings(String zkConnectStrings) {
        this.zkConnectStrings = zkConnectStrings;
    }

    public String getChroot() {
        return chroot;
    }

    public void setChroot(String chroot) {
        this.chroot = chroot;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getJaasConfig() {
        return jaasConfig;
    }

    public void setJaasConfig(String jaasConfig) {
        this.jaasConfig = jaasConfig;
    }
}
