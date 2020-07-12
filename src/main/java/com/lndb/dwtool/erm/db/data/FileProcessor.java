package com.lndb.dwtool.erm.db.data;

import java.io.File;
import java.sql.Connection;

import com.lndb.dwtool.erm.SchemaJoinMetaData;
import com.lndb.dwtool.erm.db.TableDescriptor;

public interface FileProcessor {

    FileStatistics process(String connectionName, TableDescriptor tableDescriptor, File dataFile, boolean headerIncluded, SchemaJoinMetaData joinMetaData, File good, File bad);

    FileStatistics performDBUpdates(String connectionName, TableDescriptor tableDescriptor, File goodData);

    void performDBUpdates(FileStatistics statistics, String connectionName, TableDescriptor tableDescriptor, File goodData);

    public void process(Connection con, FileStatistics statistics, String connectionName, TableDescriptor tableDescriptor, File dataFile, boolean headerIncluded, SchemaJoinMetaData joinMetaData,
	    File good, File bad);

    public void performUncommitedUpdates(Connection con, TableDescriptor tableDescriptor, File goodData);

}