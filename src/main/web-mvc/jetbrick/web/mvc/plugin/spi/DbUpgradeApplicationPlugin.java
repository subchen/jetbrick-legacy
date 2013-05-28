package jetbrick.web.mvc.plugin.spi;

import jetbrick.dao.schema.upgrade.DbUpgradeApplication;
import jetbrick.web.mvc.config.WebappConfig;
import jetbrick.web.mvc.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 支持数据库自动升级插件
 */
public class DbUpgradeApplicationPlugin implements Plugin {
	protected static final Logger log = LoggerFactory.getLogger(DbUpgradeApplicationPlugin.class);

	@Override
	public void init(WebappConfig config) {
		try {
			new DbUpgradeApplication().execute();
		} catch (Throwable e) {
			log.error("DB Upgrade Exception, JVM exit!!!", e);
			System.exit(1);
		}
	}

	@Override
	public void destory() {
	}

}
