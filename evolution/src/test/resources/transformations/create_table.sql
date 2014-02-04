--<TRANSFORMATION name="JIRA-1222">
--<UPDATE estimated="12m" reapplyIfModified="true">
CREATE TABLE User (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    fullname varchar(255) NOT NULL,
    isAdmin boolean NOT NULL,
    PRIMARY KEY (id)
);
--</UPDATE>

--<ROLLBACK>
DROP TABLE User;
--</ROLLBACK>

--</TRANSFORMATION>