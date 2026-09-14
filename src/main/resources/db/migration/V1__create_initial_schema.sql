-- ============================================================
-- V1: schema inicial do CampusGigs
-- Cria as estruturas de usuarios, servicos (gigs) e contratacoes
-- (hirings), com chaves primarias, chaves estrangeiras,
-- constraints de unicidade e de dominio (enum via CHECK).
-- ============================================================

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120)        NOT NULL,
    email       VARCHAR(180)        NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    cep         VARCHAR(8)          NOT NULL,
    city        VARCHAR(120),
    state       CHAR(2),
    role        VARCHAR(20)         NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP           NOT NULL DEFAULT now(),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE gigs (
    id           BIGSERIAL PRIMARY KEY,
    provider_id  BIGINT              NOT NULL,
    title        VARCHAR(150)        NOT NULL,
    description  TEXT                NOT NULL,
    category     VARCHAR(80)         NOT NULL,
    price        NUMERIC(10,2)       NOT NULL,
    status       VARCHAR(20)         NOT NULL DEFAULT 'ATIVO',
    created_at   TIMESTAMP           NOT NULL DEFAULT now(),

    CONSTRAINT fk_gigs_provider FOREIGN KEY (provider_id) REFERENCES users (id),
    CONSTRAINT ck_gigs_status CHECK (status IN ('ATIVO', 'PAUSADO', 'ENCERRADO')),
    CONSTRAINT ck_gigs_price_positive CHECK (price > 0)
);

CREATE TABLE hirings (
    id          BIGSERIAL PRIMARY KEY,
    gig_id      BIGINT              NOT NULL,
    hirer_id    BIGINT              NOT NULL,
    status      VARCHAR(20)         NOT NULL DEFAULT 'SOLICITADA',
    created_at  TIMESTAMP           NOT NULL DEFAULT now(),

    CONSTRAINT fk_hirings_gig FOREIGN KEY (gig_id) REFERENCES gigs (id),
    CONSTRAINT fk_hirings_hirer FOREIGN KEY (hirer_id) REFERENCES users (id),
    CONSTRAINT ck_hirings_status CHECK (status IN ('SOLICITADA', 'ACEITA', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX idx_gigs_provider_id ON gigs (provider_id);
CREATE INDEX idx_gigs_status ON gigs (status);
CREATE INDEX idx_hirings_gig_id ON hirings (gig_id);
CREATE INDEX idx_hirings_hirer_id ON hirings (hirer_id);
