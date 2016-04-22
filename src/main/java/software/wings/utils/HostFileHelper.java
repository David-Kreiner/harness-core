package software.wings.utils;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import software.wings.beans.Host;
import software.wings.exception.WingsException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.csv.CSVFormat.DEFAULT;
import static software.wings.beans.ErrorConstants.INVALID_CSV_FILE;
import static software.wings.beans.ErrorConstants.UNKNOWN_ERROR_MEG;
import static software.wings.utils.HostFileHelper.HostFileType.CSV;

/**
 * Created by anubhaw on 4/15/16.
 */
public class HostFileHelper {
  public static final String[] CSVHeader = {"HOST", "OS", "ACCESS_TYPE", "TAGS", "CREATED_BY", "CREATED_AT"};

  public static enum HostFileType { CSV, PROPERTIES, XML }

  public static List<Host> parseHosts(InputStream inputStream, String infraID, HostFileType fileType) {
    List<Host> hosts = new ArrayList<>();
    if (fileType.equals(CSV)) { // TODO: Generalize for other types as well
      try {
        CSVParser csvParser = new CSVParser(new InputStreamReader(inputStream), DEFAULT.withHeader());
        List<CSVRecord> records = csvParser.getRecords();
        for (CSVRecord record : records) {
          String hostName = record.get("HOST");
          String OSType = record.get("OS");
          Host.AccessType accessType = Host.AccessType.valueOf(record.get("ACCESS_TYPE"));
          hosts.add(new Host(infraID, hostName, OSType, accessType));
        }
      } catch (IOException e) {
        throw new WingsException(INVALID_CSV_FILE);
      }
    }
    return hosts;
  }

  public static File createHostsFile(List<Host> hosts, HostFileType fileType) {
    File tempDir = FileUtils.createTempDirPath();
    File file = new File(tempDir, "Hosts.csv");
    if (fileType.equals(CSV)) { // TODO: Generalize for other types as well
      FileWriter fileWriter = null;
      try {
        final CSVPrinter csvPrinter = new CSVPrinter(fileWriter, DEFAULT);
        fileWriter = new FileWriter(file);
        hosts.forEach(host -> {
          List row = new ArrayList();
          row.add(host.getHostName());
          row.add(host.getOsType());
          row.add(host.getAccessType());
          row.add(host.getTagsString());
          row.add(host.getCreatedBy());
          row.add(host.getCreatedAt());
          try {
            csvPrinter.printRecord(row);
          } catch (IOException e) {
            throw new WingsException(UNKNOWN_ERROR_MEG);
          }
        });
        fileWriter.flush();
      } catch (IOException e) {
        throw new WingsException(UNKNOWN_ERROR_MEG);
      } finally {
        try {
          fileWriter.close();
        } catch (IOException ignore) {
        }
      }
    }
    return file;
  }
}
