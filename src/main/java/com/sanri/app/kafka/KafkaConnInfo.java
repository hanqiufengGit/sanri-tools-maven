package com.sanri.app.kafka;

import io.netty.internal.tcnative.SSL;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.Map;

public class KafkaConnInfo {
    private String clusterName;
    private String clusterVersion;
    private String zkConnectStrings;
    private String chroot = "/";

    private String saslMechanism = "PLAIN";

    // 使用安全认证时的操作
    private String securityProtocol = "PLAINTEXT";
    /**
     * @see  AppConfigurationEntry
     */
    private String jaasConfig;
    private Ssl ssl;

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

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    public static class Ssl {

        /**
         * Password of the private key in the key store file.
         */
        private String keyPassword;

        /**
         * Location of the key store file.
         */
        private Resource keystoreLocation;

        /**
         * Store password for the key store file.
         */
        private String keystorePassword;

        /**
         * Type of the key store.
         */
        private String keyStoreType;

        /**
         * Location of the trust store file.
         */
        private Resource truststoreLocation;

        /**
         * Store password for the trust store file.
         */
        private String truststorePassword;

        /**
         * Type of the trust store.
         */
        private String trustStoreType;

        /**
         * SSL protocol to use.
         */
        private String protocol;

        public String getKeyPassword() {
            return this.keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }

        public Resource getKeystoreLocation() {
            return this.keystoreLocation;
        }

        public void setKeystoreLocation(Resource keystoreLocation) {
            this.keystoreLocation = keystoreLocation;
        }

        public String getKeystorePassword() {
            return this.keystorePassword;
        }

        public void setKeystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
        }

        public String getKeyStoreType() {
            return this.keyStoreType;
        }

        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public Resource getTruststoreLocation() {
            return this.truststoreLocation;
        }

        public void setTruststoreLocation(Resource truststoreLocation) {
            this.truststoreLocation = truststoreLocation;
        }

        public String getTruststorePassword() {
            return this.truststorePassword;
        }

        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

        public String getTrustStoreType() {
            return this.trustStoreType;
        }

        public void setTrustStoreType(String trustStoreType) {
            this.trustStoreType = trustStoreType;
        }

        public String getProtocol() {
            return this.protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

    }
}
