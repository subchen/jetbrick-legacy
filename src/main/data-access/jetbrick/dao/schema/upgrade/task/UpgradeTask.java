package jetbrick.dao.schema.upgrade.task;

import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.schema.data.PersistentDAO;
import jetbrick.dao.schema.upgrade.UpgradeLogger;
import jetbrick.dao.utils.DbUtils;

public abstract class UpgradeTask {
	protected final PersistentDAO dao = DbUtils.dao();
	protected final Dialect dialect = DbUtils.getDialect();
	protected final UpgradeLogger fileLog;

	public UpgradeTask(UpgradeLogger fileLog) {
		this.fileLog = fileLog;
	}

	protected void executeJdbcWithFileLog(String sql, Object... args) {
		dao.update(sql, args);
		fileLog.println(sql);
	}

	public abstract void initialize();

	public abstract boolean isRequired();

	public abstract void execute();

	public abstract void destory();
}
