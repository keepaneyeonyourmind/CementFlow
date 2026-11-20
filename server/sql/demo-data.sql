-- Учебные товары и условные цены; повторный запуск не меняет существующие записи.
INSERT INTO products (id, name, unit, price) VALUES
  (1, 'Цемент М400 (учебный)', 'мешок', 420.00),
  (2, 'Цемент М500 (учебный)', 'мешок', 490.00),
  (3, 'Цемент М400 (учебный)', 'тонна', 7800.00)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('products', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM products), 1));
