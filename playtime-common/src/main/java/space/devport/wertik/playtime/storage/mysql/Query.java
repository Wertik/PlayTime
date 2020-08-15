package space.devport.wertik.playtime.storage.mysql;

import lombok.Getter;

enum Query {

    CREATE_TABLE("CREATE TABLE IF NOT EXISTS `%table%` (\n"
            + "    `uuid` VARCHAR(40)  NOT NULL ,\n"
            + "    `lastKnownName` VARCHAR(16) ,\n"
            + "    `time` varchar(32)  NOT NULL ,\n"
            + "    PRIMARY KEY (`uuid`), \n"
            + "    UNIQUE (`uuid`)\n"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"),

    INSERT_USER("INSERT IGNORE INTO `%table%` (uuid, time) VALUES(?, ?)"),

    EXIST_CHECK("SELECT uuid from `%table%` WHERE uuid=?"),

    UPDATE_USER("UPDATE '%table%' set time=? WHERE uuid=?"),

    GET_TIME("SELECT `time` FROM `%table%` WHERE uuid=?"),

    GET_TOP_TEN("SELECT * FROM `%table%` ORDER BY `time` DESC LIMIT 10");

    @Getter
    private final String statement;

    Query(String statement) {
        this.statement = statement;
    }

    /**
     * Get the statement relative to a table.
     */
    public String get(String tableName) {
        return statement.replaceAll("(?i)%table%", tableName);
    }
}