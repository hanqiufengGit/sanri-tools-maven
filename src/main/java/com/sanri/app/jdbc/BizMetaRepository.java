package com.sanri.app.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据表业务信息存储库
 */
public class BizMetaRepository {
    private Map<String, Map<String, Set<BizEntry>>> bizEntryMap = new HashMap<>();

    public class BizEntry{
        private String columnName;
        private BizColumnType bizColumnType;
        private Object extra;
    }

    public enum BizColumnType{
        KEY,     // 业务主键,如订单编号,设备编码等
        STATUS,  // 状态标志,如删除标志,订单状态等

    }
}
