package jetbrick.dao.schema.upgrade;

import jetbrick.dao.schema.upgrade.task.*;

public class DbUpgradeApplication {

    public void execute() {
        execute(null);
    }

    public void execute(SchemaHook schemaHook) {
        UpgradeLogger fileLog = new UpgradeLogger();
        try {
            doExecuteTask(new SchemaTableUpgradeTask(fileLog, schemaHook));
            doExecuteTask(new SchemaEnumUpgradeTask(fileLog));
            doExecuteTask(new SchemaBulkUpgradeTask(fileLog));
        } finally {
            fileLog.close();
        }
    }

    private void doExecuteTask(UpgradeTask task) {
        task.initialize();
        if (task.isRequired()) {
            task.execute();
        }
        task.destory();
    }
}
