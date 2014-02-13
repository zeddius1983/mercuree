--<TRANSFORMATION>
--<UPDATE estimated="12m" reapplyIfModified="true">
ALTER TABLE User ADD age int
--</UPDATE>

--<ROLLBACK>
ALTER TABLE User DROP COLUMN age
--</ROLLBACK>

--</TRANSFORMATION>