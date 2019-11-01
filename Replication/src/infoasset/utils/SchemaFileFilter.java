package infoasset.utils;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FilenameUtils;

public class SchemaFileFilter implements FilenameFilter {
	private static final SchemaFileFilter INSTANCE = new SchemaFileFilter();
	public static SchemaFileFilter getInstance() {
		return INSTANCE;
	}
	private SchemaFileFilter() {
		
	}
	@Override
	public boolean accept(File dir, String name) {
		return FilenameUtils.getExtension(name).equals("sch");
	}

}
