-- ============================================================
-- V2: corrige o tipo da coluna users.state (CHAR -> VARCHAR)
--
-- A V1 criou "state" como CHAR(2) (bpchar no Postgres). O Hibernate mapeia
-- campos String como VARCHAR por padrao, e com
-- spring.jpa.hibernate.ddl-auto=validate isso causa falha na inicializacao:
--   Schema-validation: wrong column type encountered in column [state] in
--   table [users]; found [bpchar (Types#CHAR)], but expecting
--   [varchar(2) (Types#VARCHAR)]
--
-- A V1 nao foi editada retroativamente (ja usada em um checkpoint anterior);
-- esta e uma nova migration, como o proprio enunciado exige para qualquer
-- evolucao de schema.
-- ============================================================

ALTER TABLE users
    ALTER COLUMN state TYPE VARCHAR(2) USING state::VARCHAR(2);
