package infoasset.replication;

import java.io.IOException;

/** 
 * Replication Master
 * @author Manisa
 * @since Sep 22, 2014
 */
interface Master {
	Transaction nextTransaction() throws IOException, RepException;
	String getMasterName();
}
