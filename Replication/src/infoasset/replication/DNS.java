package infoasset.replication;

import java.util.ArrayList;

interface DNS {

   public abstract ArrayList<String> getSchemaList(String ipAddress);

   public abstract void listDNS();

}