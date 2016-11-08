COPY MENU
FROM '/extra/rlui001/cs166/mydb/data/menu.csv'
WITH DELIMITER ';';

COPY USERS
FROM '/extra/rlui001/cs166/mydb/data/users.csv'
WITH DELIMITER ';';

COPY ORDERS
FROM '/extra/rlui001/cs166/mydb/data/orders.csv'
WITH DELIMITER ';';
ALTER SEQUENCE orders_orderid_seq RESTART 87257;

COPY ITEMSTATUS
FROM '/extra/rlui001/cs166/mydb/data/itemStatus.csv'
WITH DELIMITER ';';

