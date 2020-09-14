package space.devport.wertik.playtime.storage.mysql;

import lombok.Getter;

enum Query {

    CREATE_TABLE("CREATE TABLE IF NOT EXISTS `%table%` (\n"
            + "    `uuid` VARCHAR(40)  NOT NULL ,\n"
            + "    `lastKnownName` VARCHAR(16) ,\n"
            + "    `time` BIGINT NOT NULL DEFAULT 0 ,\n"
            + "    PRIMARY KEY (`uuid`), \n"
            + "    UNIQUE (`uuid`)\n"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"),

    DROP_TABLE("DROP TABLE IF EXISTS `%table%`"),

    DELETE_USER("DELETE FROM `%table%` WHERE `uuid` = ?"),

    EXIST_CHECK("SELECT uuid from `%table%` WHERE `uuid` = ?"),

    EXIST_CHECK_NAME("SELECT lastKnownName from `%table%` WHERE `lastKnownName` = ?"),

    INSERT_USER("INSERT IGNORE INTO `%table%` (uuid, lastKnownName, time) VALUES(?, ?, ?)"),

    UPDATE_USER("REPLACE INTO `%table%` (`uuid`, `lastKnownName`, `time`) VALUES (?, ?, ?)"),

    GET_USER("SELECT `time`, `lastKnownName` FROM `%table%` WHERE `uuid` = ?"),

    GET_USER_BY_NAME("SELECT `time`, `uuid` FROM `%table%` WHERE `lastKnownName` = ?"),

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