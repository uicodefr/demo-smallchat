/** GLOBAL **/

CREATE TABLE IF NOT EXISTS global_parameter (
    name varchar(256) PRIMARY KEY,
    value varchar(1024) NOT NULL,
    client_view boolean DEFAULT false
);

INSERT OR IGNORE INTO global_parameter (name, value, client_view) VALUES
    ('general.status', 'true', false),
    ('like.max', '1000', false);

CREATE TABLE IF NOT EXISTS global_like (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    insert_date timestamp NOT NULL,
    client_ip varchar(256)
);