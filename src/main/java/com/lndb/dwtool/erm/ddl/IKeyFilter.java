package com.lndb.dwtool.erm.ddl;

import com.lndb.dwtool.erm.ForeignKey;

public interface IKeyFilter {
    boolean ignoreKey(ForeignKey foreignKey);
}
