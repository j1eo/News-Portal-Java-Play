package dataBase;

import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class DB_Connection {
    private final Database db;

    @Inject
    public DB_Connection(Database db) {
        this.db = db;
    }

    public Connection getConnection() throws SQLException {
        return db.getConnection();
    }
}
