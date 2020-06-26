package com.ls.demo.gt.common.transaction;

import lombok.Getter;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

import java.sql.Connection;

public class TransactionNodeCacheListener implements NodeCacheListener {

    @Getter
    private Connection connection;

    public TransactionNodeCacheListener(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void nodeChanged() throws Exception {

    }
}
