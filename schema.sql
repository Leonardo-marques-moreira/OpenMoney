-- ============================================================
--  OpenMoney – Schema PostgreSQL
--  JDBC puro (sem Hibernate / JPA)
-- ============================================================

-- Limpa na ordem correta (FK primeiro)
DROP TABLE IF EXISTS notificacao    CASCADE;
DROP TABLE IF EXISTS meta_economia  CASCADE;
DROP TABLE IF EXISTS transacao      CASCADE;
DROP TABLE IF EXISTS categoria      CASCADE;
DROP TABLE IF EXISTS conta          CASCADE;
DROP TABLE IF EXISTS usuario        CASCADE;

DROP TYPE IF EXISTS tipo_transacao    CASCADE;
DROP TYPE IF EXISTS tipo_notificacao  CASCADE;
DROP TYPE IF EXISTS tipo_conta        CASCADE;

-- ── Enums ────────────────────────────────────────────────────
CREATE TYPE tipo_transacao   AS ENUM ('RECEITA', 'DESPESA');
CREATE TYPE tipo_notificacao AS ENUM ('ALERTA', 'LEMBRETE', 'META_ATINGIDA');
CREATE TYPE tipo_conta       AS ENUM ('CONTA_CORRENTE', 'POUPANCA', 'CARTEIRA', 'CARTAO_CREDITO');

-- ── Usuário ──────────────────────────────────────────────────
CREATE TABLE usuario (
    id             BIGSERIAL    PRIMARY KEY,
    nome           VARCHAR(150) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    senha          VARCHAR(255) NOT NULL,          -- armazenar hash em produção
    data_cadastro  DATE         NOT NULL DEFAULT CURRENT_DATE
);

-- ── Conta ────────────────────────────────────────────────────
CREATE TABLE conta (
    id          BIGSERIAL        PRIMARY KEY,
    nome        VARCHAR(100)     NOT NULL,
    tipo        tipo_conta       NOT NULL DEFAULT 'CONTA_CORRENTE',
    cor         VARCHAR(7)       NOT NULL DEFAULT '#000000',  -- hex color
    saldo       NUMERIC(15,2)    NOT NULL DEFAULT 0.00,
    id_usuario  BIGINT           NOT NULL REFERENCES usuario(id) ON DELETE CASCADE
);

-- ── Categoria ────────────────────────────────────────────────
CREATE TABLE categoria (
    id          BIGSERIAL        PRIMARY KEY,
    nome        VARCHAR(80)      NOT NULL,
    tipo        tipo_transacao   NOT NULL,
    icone       VARCHAR(50)      NOT NULL DEFAULT 'default',
    cor         VARCHAR(7)       NOT NULL DEFAULT '#000000',
    usuario_id  BIGINT           NOT NULL REFERENCES usuario(id) ON DELETE CASCADE
);

-- ── Transação ────────────────────────────────────────────────
-- Regra de negócio: valor > 0; tipo deve bater com a categoria
CREATE TABLE transacao (
    id            BIGSERIAL       PRIMARY KEY,
    descricao     VARCHAR(200)    NOT NULL,
    valor         NUMERIC(15,2)   NOT NULL CHECK (valor > 0),
    tipo          tipo_transacao  NOT NULL,
    data          DATE            NOT NULL DEFAULT CURRENT_DATE,
    conta_id      BIGINT          NOT NULL REFERENCES conta(id)     ON DELETE CASCADE,
    categoria_id  BIGINT          NOT NULL REFERENCES categoria(id) ON DELETE RESTRICT
);

-- ── Meta de Economia ─────────────────────────────────────────
CREATE TABLE meta_economia (
    id            BIGSERIAL      PRIMARY KEY,
    descricao     VARCHAR(200)   NOT NULL,
    valor_meta    NUMERIC(15,2)  NOT NULL CHECK (valor_meta > 0),
    valor_atual   NUMERIC(15,2)  NOT NULL DEFAULT 0.00 CHECK (valor_atual >= 0),
    data_criacao  DATE           NOT NULL DEFAULT CURRENT_DATE,
    data_limite   DATE           NOT NULL,
    atingida      BOOLEAN        NOT NULL DEFAULT FALSE,
    usuario_id    BIGINT         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT chk_meta_datas CHECK (data_limite >= data_criacao)
);

-- ── Notificação ──────────────────────────────────────────────
CREATE TABLE notificacao (
    id          BIGSERIAL        PRIMARY KEY,
    mensagem    TEXT             NOT NULL,
    tipo        tipo_notificacao NOT NULL,
    data        DATE             NOT NULL DEFAULT CURRENT_DATE,
    lida        BOOLEAN          NOT NULL DEFAULT FALSE,
    usuario_id  BIGINT           NOT NULL REFERENCES usuario(id) ON DELETE CASCADE
);

-- ── Trigger: atualiza saldo da conta ao inserir transação ────
CREATE OR REPLACE FUNCTION fn_atualiza_saldo()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tipo = 'RECEITA' THEN
        UPDATE conta SET saldo = saldo + NEW.valor WHERE id = NEW.conta_id;
    ELSE
        UPDATE conta SET saldo = saldo - NEW.valor WHERE id = NEW.conta_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_atualiza_saldo
AFTER INSERT ON transacao
FOR EACH ROW EXECUTE FUNCTION fn_atualiza_saldo();

-- ── Trigger: marca meta como atingida ao depositar ───────────
CREATE OR REPLACE FUNCTION fn_verifica_meta()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.valor_atual >= NEW.valor_meta THEN
        NEW.atingida := TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_verifica_meta
BEFORE UPDATE OF valor_atual ON meta_economia
FOR EACH ROW EXECUTE FUNCTION fn_verifica_meta();

-- ── Índices ──────────────────────────────────────────────────
CREATE INDEX idx_conta_usuario         ON conta(id_usuario);
CREATE INDEX idx_categoria_usuario     ON categoria(usuario_id);
CREATE INDEX idx_transacao_conta       ON transacao(conta_id);
CREATE INDEX idx_transacao_categoria   ON transacao(categoria_id);
CREATE INDEX idx_transacao_data        ON transacao(data);
CREATE INDEX idx_meta_usuario          ON meta_economia(usuario_id);
CREATE INDEX idx_notificacao_usuario   ON notificacao(usuario_id);
CREATE INDEX idx_notificacao_lida      ON notificacao(usuario_id, lida);
