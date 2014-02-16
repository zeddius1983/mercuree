--<TRANSFORMATION name="/transformations/alter_table.sql">
--<UPDATE estimated="12m" reapplyIfModified="true">
ALTER TABLE User ADD nickname varchar(128)
--</UPDATE>

--<ROLLBACK>
ALTER TABLE User DROP COLUMN nickname
--</ROLLBACK>

--</TRANSFORMATION>