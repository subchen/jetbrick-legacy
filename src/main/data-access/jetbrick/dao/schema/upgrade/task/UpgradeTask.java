package jetbrick.dao.schema.upgrade.task;

import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.schema.data.EntityDaoHelper;
import jetbrick.dao.schema.data.EntityUtils;
import jetbrick.dao.schema.upgrade.UpgradeLogger;

public abstract class UpgradeTask {
    protected final EntityDaoHelper dao = EntityUtils.DAO_HELPER;
    protected final Dialect dialect = dao.getDialect();
    protected final UpgradeLogger fileLog;

    public UpgradeTask(UpgradeLogger fileLog) {
        this.fileLog = fileLog;
    }

    protected void executeJdbcWithFileLog(String sql, Object... args) {
        dao.execute(sql, args);
        fileLog.println(sql);
    }

    public abstract void initialize();

    public abstract boolean isRequired();

    public abstract void execute();

    public abstract void destory();
}
