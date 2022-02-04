#Vegeta Load Testing
echo "GET http://localhost:8888/todos" | vegeta attack -workers=10 -max-workers=20 -rate=100 -duration=30s | tee results.bin | vegeta report

#Vegeta Graphing
vegeta plot -title=Attack%20Results results.bin > results.html

#MySQL SPL Command
WITH RECURSIVE category_path (id, title, path) AS
(
SELECT id, name, name as path
FROM items
WHERE parent_id IS NULL
UNION ALL
SELECT c.id, c.name, CONCAT(cp.path, ' > ', c.name)
FROM category_path AS cp JOIN items AS c
ON cp.id = c.parent_id
)
SELECT * FROM category_path
ORDER BY path;
