package jetbrick.dao.schema.upgrade.task;

import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.schema.data.SimpleDaoHelper;
import jetbrick.dao.schema.data.jdbc.JdbcEntity;
import jetbrick.dao.schema.upgrade.UpgradeLogger;

public abstract class UpgradeTask {
    protected final SimpleDaoHelper dao;
    protected final Dialect dialect;
    protected final UpgradeLogger fileLog;

    public UpgradeTask(UpgradeLogger fileLog) {
        this.fileLog = fileLog;
        this.dao = JdbcEntity.DAOHelper;
        this.dialect = dao.getDialect();
    }

    protected void executeSQLWithFileLog(String sql, Object... args) {
        dao.execute(sql, args);
        fileLog.println(sql);
    }

    public abstract void initialize();

    public abstract boolean isRequired();

    public abstract void execute();

    public abstract void destory();
}
