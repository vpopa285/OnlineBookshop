# Transactions, Isolation, and Indexes

## ACID Consistency Demo

Use case: buying a book must update several relational tables consistently:

- `user_wallet.balance` is decreased.
- `orders` receives one purchase order.
- `order_items` receives the purchased book row.

The demo intentionally fails while inserting `order_items` by referencing a missing book id.

Run:

```bash
mvn -q -DskipTests compile exec:java -Dexec.mainClass=org.task.demo.TransactionConsistencyDemo
```

Expected behavior:

- Without a transaction, PostgreSQL commits each JDBC statement independently. The wallet is debited and the order is inserted, but the order item fails. The database is inconsistent from the domain point of view because the user paid for a purchase that has no purchased book row.
- With a transaction, the same failure triggers `rollback()`. The wallet, order, and order item changes are all undone.

Typical output shape:

```text
Initial state: wallet=50.00, orders=0, order_items=0
Failure without transaction: 23503 ERROR: insert or update on table "order_items" violates foreign key constraint ...
After failed purchase without transaction: wallet=25.00, orders=1, order_items=0
Failure with transaction: 23503 ERROR: insert or update on table "order_items" violates foreign key constraint ...
After failed purchase with transaction: wallet=50.00, orders=0, order_items=0
```

## Non-Default Isolation Demo

Use case: two users concurrently buy the last available copy of a book. The base schema has purchases, but no stock table, so the demo creates a small `book_inventory(book_id, available_copies)` table.

Run:

```bash
mvn -q -DskipTests compile exec:java -Dexec.mainClass=org.task.demo.IsolationLevelDemo
```

Expected behavior:

- PostgreSQL's default isolation level is `READ_COMMITTED`. Both transactions can read `available_copies = 1`, both can decide to buy, and both can commit completed orders. The final inventory says `0`, but there are `2` completed orders for one available copy.
- `SERIALIZABLE` is the correct level for this check-then-write business rule when the application does not lock the row explicitly. PostgreSQL detects the unsafe concurrent execution and aborts one transaction with SQL state `40001`.

Typical output shape:

```text
Default READ_COMMITTED:
  user 920001 read available_copies=1
  user 920002 read available_copies=1
  user 920001 committed
  user 920002 committed
After default isolation: available_copies=0, completed_orders=2
Correct SERIALIZABLE isolation:
  user 920001 read available_copies=1
  user 920002 read available_copies=1
  user 920001 committed
  user 920002 rolled back: 40001 ERROR: could not serialize access ...
After serializable isolation: available_copies=0, completed_orders=1
```

## Large Dataset

Populate benchmark data:

```bash
psql "$DATABASE_URL" -f sql/populate_large_dataset.sql
```

For the local Docker database configured by `.env`, the equivalent command is:

```bash
PGPASSWORD=admin psql -h localhost -p 5432 -U admin -d librarydb -f sql/populate_large_dataset.sql
```

The script inserts:

- `100000` generated users.
- `2000000` generated books.
- `2000000` generated reviews.

It finishes with `VACUUM ANALYZE` so PostgreSQL has fresh table statistics.

## Single-Column Index

Use case: book search by author. This query is selective because the generated data has many authors and about 200 books per author.

Run:

```bash
PGPASSWORD=admin psql -h localhost -p 5432 -U admin -d librarydb -f sql/index_benchmark.sql
```

Observation to capture from `EXPLAIN (ANALYZE, BUFFERS)`:

| Case | Expected plan | Expected effect |
| --- | --- | --- |
| Without `idx_books_author` | `Seq Scan` or parallel sequential scan on `books` | PostgreSQL reads a large part of the table to find one author. |
| With `idx_books_author` | `Bitmap Index Scan` or `Index Scan` using `idx_books_author` | PostgreSQL jumps to matching author rows and reads far fewer buffers. |

Record your measured times here after running locally:

| Case | Execution time | Buffers read/hit |
| --- | --- | --- |
| Without index |  |  |
| With index |  |  |

## Compound Index

Use case: catalog search normally filters by `author`, optionally narrows by `price`, and returns stable ordering by `price, title`. The index is:

```sql
CREATE INDEX idx_books_author_price_title ON books(author, price, title);
```

Run:

```bash
PGPASSWORD=admin psql -h localhost -p 5432 -U admin -d librarydb -f sql/compound_index_benchmark.sql
```

Observations:

- Filtering by the whole index column set, `author + price + title`, should use the compound index very efficiently.
- Filtering by the left prefix, `author`, can still use the same index.
- Filtering by the left prefix, `author + price`, can use the same index and also satisfy `ORDER BY price, title`.
- Filtering by `price + title` while omitting `author` does not match the leftmost-prefix order. PostgreSQL usually chooses a sequential scan or a much less efficient plan because the first indexed column is missing.

Record your measured plans:

| Query | Expected index usage | Actual plan | Execution time |
| --- | --- | --- | --- |
| `author + price + title` | Uses `idx_books_author_price_title` |  |  |
| `author` | Uses left prefix |  |  |
| `author + price` | Uses left prefix |  |  |
| `price + title` | Does not efficiently use this index |  |  |
