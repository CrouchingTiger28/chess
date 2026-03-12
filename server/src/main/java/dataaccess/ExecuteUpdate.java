package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class ExecuteUpdate {


    static public int execute(String statement, Object... params) throws DataAccessException, SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                PreparedStatement psWithParam = replaceParameters(ps, params);
                psWithParam.executeUpdate();

                ResultSet rs = psWithParam.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        }
    }

    static private PreparedStatement replaceParameters(PreparedStatement ps, Object... params) throws SQLException{
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            switch (param) {
                case String p -> ps.setString(i + 1, p);
                case Integer p -> ps.setInt(i + 1, p);
                case model.GameData p -> ps.setString(i + 1, p.toString());
                case null -> ps.setNull(i + 1, NULL);
                default -> {
                }
            }
        }
        return ps;
    }
}
