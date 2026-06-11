-- AI Assistant Intelligence Cache
-- Stores compact summaries of past AI answers keyed by question hash + keywords.
-- Used to return faster, consistent answers for semantically similar questions.
-- Does NOT store full document content — only the generated answer and source references.

CREATE TABLE IF NOT EXISTS assistant_intelligence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_hash TEXT NOT NULL,
    keywords TEXT[] NOT NULL DEFAULT '{}',
    cached_answer TEXT NOT NULL,
    diagram_type TEXT,
    source_doc_ids TEXT[] NOT NULL DEFAULT '{}',
    source_titles TEXT[] NOT NULL DEFAULT '{}',
    hit_count INT NOT NULL DEFAULT 1,
    confidence TEXT NOT NULL DEFAULT 'medium',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_ai_intel_hash
    ON assistant_intelligence(question_hash);

CREATE INDEX IF NOT EXISTS idx_ai_intel_keywords
    ON assistant_intelligence USING GIN(keywords);

ALTER TABLE assistant_intelligence ENABLE ROW LEVEL SECURITY;
