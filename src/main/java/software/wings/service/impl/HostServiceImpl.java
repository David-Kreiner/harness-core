package software.wings.service.impl;

import static software.wings.beans.Host.HostBuilder.aHost;

import com.google.common.collect.ImmutableMap;

import software.wings.beans.Host;
import software.wings.beans.Infra;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.HostService;
import software.wings.utils.BoundedInputStream;
import software.wings.utils.HostFileHelper;

import java.io.File;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by anubhaw on 5/9/16.
 */
public class HostServiceImpl implements HostService {
  @Inject private WingsPersistence wingsPersistence;

  @Override
  public PageResponse<Host> list(PageRequest<Host> req) {
    return wingsPersistence.query(Host.class, req);
  }

  @Override
  public Host get(String appId, String infraId, String hostId) {
    return wingsPersistence.get(Host.class, hostId);
  }

  @Override
  public Host save(Host host) {
    return wingsPersistence.saveAndGet(Host.class, host);
  }

  @Override
  public Host update(Host host) {
    wingsPersistence.updateFields(Host.class, host.getUuid(),
        ImmutableMap.<String, Object>builder()
            .put("hostName", host.getHostName())
            .put("hostAttributes", host.getHostAttributes())
            .put("bastionHostAttributes", host.getBastionHostAttributes())
            .put("tags", host.getTags())
            .build());
    return wingsPersistence.saveAndGet(Host.class, host);
  }

  @Override
  public int importHosts(Host baseHost, BoundedInputStream inputStream) {
    Infra infra = wingsPersistence.get(Infra.class, baseHost.getInfraId()); // TODO: validate infra
    List<Host> hosts = HostFileHelper.parseHosts(inputStream, baseHost);
    List<String> IDs = wingsPersistence.save(hosts);
    return IDs.size();
  }

  @Override
  public File exportHosts(String appId, String infraId) {
    List<Host> hosts = wingsPersistence.createQuery(Host.class).field("infraID").equal(infraId).asList();
    return HostFileHelper.createHostsFile(hosts);
  }

  @Override
  public String getInfraId(String envId, String appId) {
    return wingsPersistence.createQuery(Infra.class).field("envId").equal(envId).get().getUuid();
  }

  @Override
  public void delete(String appId, String infraId, String hostId) {
    wingsPersistence.delete(Host.class, hostId);
  }

  @Override
  public void bulkSave(Host baseHost, List<String> hostNames) {
    hostNames.forEach(hostName -> {
      save(aHost()
               .withHostName(hostName)
               .withAppId(baseHost.getAppId())
               .withInfraId(baseHost.getInfraId())
               .withHostAttributes(baseHost.getHostAttributes())
               .withBastionHostAttributes(baseHost.getBastionHostAttributes())
               .withTags(baseHost.getTags())
               .build());
    });
  }
}
