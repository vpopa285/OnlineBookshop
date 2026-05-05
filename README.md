# OnlineBookshop

As a <b>User</b>, I want to be able to:

Register and login to the platform.
Search for a book by title, author or genre.
Preview a book and the information about it.
Pay for a book.
Rate a book and left a comment.
Access my history.


As an <b>Administrator</b>, I want to be able to:

Add, remove and view books in the whole repository.
View statistics and usage reports.

## Local database setup

### Requirements
- Docker installed

### Configure environment

Copy:

```bash
cp .env.example .env
```

Edit `.env`

### Start database

```bash
docker compose up -d
```

### Stop database

```bash
docker compose down
```

### Verify

Connect using DBeaver or similar tool
