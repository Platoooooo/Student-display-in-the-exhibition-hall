# SQL 初始化脚本

**权威位置**：[docker/sql/init.sql](../../docker/sql/init.sql)

Docker Compose 使用 `docker/sql/init.sql` 通过 volume mount 自动初始化数据库。
本目录不再维护独立的 copy，引用时请指向上述路径。

```bash
# 需要单独执行时：
mysql -u root -p exhibition_db < ../../docker/sql/init.sql
```