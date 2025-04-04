DROP INDEX IF EXISTS index_OrderID;
DROP INDEX IF EXISTS index_typeOfItem;
DROP INDEX IF EXISTS index_login;
DROP INDEX IF EXISTS index_price;
DROP INDEX IF EXISTS index_orderTimestamp;
DROP INDEX IF EXISTS index_itemName;
DROP INDEX IF EXISTS index_role;

CREATE INDEX index_OrderID
ON FoodOrder
USING HASH
(OrderID);

CREATE INDEX index_typeOfItem
ON Items
USING HASH
(typeOfItem);

CREATE INDEX index_role
ON Users
USING HASH
(role);

CREATE INDEX index_login
ON Users
USING HASH
(login);

CREATE INDEX index_price
ON Items
USING BTREE
(price);

CREATE INDEX index_itemName
ON Items
USING BTREE
(itemName);

CREATE INDEX index_Timestamp
ON FoodOrder
USING BTREE
(orderTimestamp);
