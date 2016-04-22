package software.wings.core.ssh.executors;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.core.ssh.executors.SshSessionConfig.SshSessionConfigBuilder;

/**
 * Created by anubhaw on 2/8/16.
 */
public class SshSessionFactory {
  private final static Logger LOGGER = LoggerFactory.getLogger(SshSessionFactory.class);

  public static Session getSSHSessionWithJumpbox(SshSessionConfig config) {
    Session session = null;
    try {
      Session jumpboxSession = getSSHSession(config.getJumpboxConfig());
      int forwardingPort = jumpboxSession.setPortForwardingL(0, config.getHost(), config.getPort());
      LOGGER.info("portforwarding port " + forwardingPort);

      SshSessionConfig newConfig = new SshSessionConfigBuilder()
                                       .user(config.getUser())
                                       .password(config.getPassword())
                                       .keyPath(config.getKeyPath())
                                       .host("127.0.0.1")
                                       .port(forwardingPort)
                                       .build();
      session = getSSHSession(newConfig);
    } catch (JSchException e) {
      e.printStackTrace();
    }
    return session;
  }

  public static Session getSSHSession(SshSessionConfig config) throws JSchException {
    JSch jsch = new JSch();
    Session session = null;
    if ("KEY".equals(getSessionType(config))) {
      if (null == config.getKeyPassphrase()) {
        jsch.addIdentity(config.getKeyPath());
      } else {
        jsch.addIdentity(config.getKeyPath(), config.getKeyPassphrase());
      }
      session = jsch.getSession(config.getUser(), config.getHost(), config.getPort());
    } else {
      session = jsch.getSession(config.getUser(), config.getHost(), config.getPort());
      session.setPassword(config.getPassword());
    }
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect(config.getSshConnectionTimeout());
    session.setTimeout(config.getSshSessionTimeout());
    return session;
  }

  private static String getSessionType(SshSessionConfig config) {
    return config.getKeyPath() != null && config.getKeyPath().length() > 0 ? "KEY" : "PASSWORD";
  }
}
