CREATE TABLE IF NOT EXISTS Webpages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    url TEXT NOT NULL,
    title TEXT,
    content TEXT
);

CREATE TABLE IF NOT EXISTS ForwardIndex (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    page_id INTEGER NOT NULL,
    word TEXT NOT NULL,
    frequency INTEGER NOT NULL,
    FOREIGN KEY (page_id) REFERENCES Webpages(id)
);

CREATE INDEX idx_word_forward ON ForwardIndex(word);
CREATE INDEX idx_page_id_forward ON ForwardIndex(page_id);

CREATE TABLE IF NOT EXISTS InvertedIndex (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word TEXT NOT NULL,
    page_id INTEGER NOT NULL,
    FOREIGN KEY (page_id) REFERENCES Webpages(id)
);

CREATE INDEX idx_word_inverted ON InvertedIndex(word);
CREATE INDEX idx_page_id_inverted ON InvertedIndex(page_id);