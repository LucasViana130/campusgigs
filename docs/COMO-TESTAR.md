# Como testar o CampusGigs - passo a passo

Este arquivo e um guia ordenado do que fazer, do zero, ate ter evidencia
real para entregar ao professor. Os comandos `curl` detalhados dos 16
cenarios ficam em [`docs/test-roteiro.md`](test-roteiro.md) - aqui e o
"mapa" de tudo em volta disso (identidade Git, subir o ambiente, guardar
evidencia, revisar antes do push).

> Nada aqui foi executado por mim (o ambiente onde o projeto foi gerado nao
> tem Docker nem acesso ao Maven Central). Siga os passos na sua maquina.

## Pre-requisitos

- Docker e Docker Compose instalados
- Um terminal
- `curl` (ou Postman/Insomnia, se preferir clicar em vez de digitar)

---

## Passo 1 - Configurar sua identidade Git (antes de tudo)

Os commits foram criados no sandbox com uma identidade generica
(`CampusGigs Dev <dev@campusgigs.local>`). Troque para a sua, e reatribua
a autoria de todo o historico:

```bash
cd campusgigs

git config user.name "Seu Nome Completo"
git config user.email "seu-email-real@dominio.com"

git filter-branch --env-filter '
export GIT_AUTHOR_NAME="Seu Nome Completo"
export GIT_AUTHOR_EMAIL="seu-email-real@dominio.com"
export GIT_COMMITTER_NAME="Seu Nome Completo"
export GIT_COMMITTER_EMAIL="seu-email-real@dominio.com"
' --tag-name-filter cat -- --branches --tags
```

Confira:

```bash
git log --format="%an <%ae> - %s"
```

Todas as linhas devem mostrar seu nome/e-mail agora.

---

## Passo 2 - Preparar as variaveis de ambiente

```bash
cp .env.example .env
```

Abra o `.env` e, se quiser, troque o `JWT_SECRET` por um seu (qualquer
string com 32+ caracteres serve; `openssl rand -base64 32` gera uma boa).
Os valores padrao ja funcionam para testar localmente.

---

## Passo 3 - Subir o ambiente

```bash
docker compose up --build
```

Isso vai: construir a imagem da aplicacao (baixando as dependencias do
Maven na primeira vez - pode demorar alguns minutos), subir o Postgres, e
rodar as migrations do Flyway antes de a aplicacao aceitar requisicoes.

**O que esperar ver no log, na ordem:**

1. O container `campusgigs-db` ficando `healthy`.
2. Linhas do Flyway parecidas com:
   ```
   Successfully validated 2 migrations
   Migrating schema "public" to version "1 - create initial schema"
   Migrating schema "public" to version "2 - alter user state to varchar"
   Successfully applied 2 migrations
   ```
3. O Spring Boot terminando de subir, algo como:
   ```
   Started CampusGigsApplication in X.XXX seconds
   ```

Se aparecer qualquer coisa como `Schema-validation: wrong column type` ou
o container reiniciando em loop, pare e me avise antes de continuar - isso
indicaria um problema que a analise estatica nao pegou.

Deixe esse terminal aberto (a aplicacao roda em primeiro plano). Abra um
**segundo terminal** para os proximos passos.

---

## Passo 4 - Confirmar que a API responde

No segundo terminal:

```bash
curl -i http://localhost:8080/api/gigs
```

Esperado: `200 OK` com um corpo `[]` (lista vazia, ainda sem servicos
publicados). Se isso funcionar, a aplicacao subiu corretamente.

---

## Passo 5 - Executar o roteiro de testes

Siga [`docs/test-roteiro.md`](test-roteiro.md) do passo 1 ao 16, **na
ordem, sem pular nenhum**. Para cada passo:

1. Rode o comando `curl` indicado.
2. Confira se o status HTTP bate com o "Esperado".
3. Copie o comando + status + corpo da resposta para um arquivo de texto
   (voce vai precisar disso no Passo 6).

Os passos usam variaveis de shell (`TOKEN_ANA`, `TOKEN_BRUNO`, `GIG_ID`,
`TOKEN_ADMIN`) que voce mesmo preenche com o que a API devolver em cada
etapa anterior - o roteiro indica exatamente onde pegar cada valor.

**Alternativa via Postman**: se preferir nao digitar `curl`, importe
[`docs/postman/CampusGigs.postman_collection.json`](postman/CampusGigs.postman_collection.json)
no Postman. Ela segue a mesma ordem, e os tokens/`gigId` sao capturados
automaticamente de uma requisicao para a outra (menos copiar/colar). Ha
UM passo manual dentro da colecao (marcado com aviso): promover o Bruno a
ADMIN direto no banco, ja que nao existe endpoint HTTP para isso. Para
guardar evidencia pelo Postman, exporte os resultados da execucao (aba
"Runner" -> "Export Results") e salve em `docs/evidencias/` no Passo 6.

---

## Passo 6 - Guardar a evidencia (em um commit novo, nao no CP5)

```bash
mkdir -p docs/evidencias
# cole os comandos + respostas dos 16 passos em um ou mais arquivos aqui,
# por exemplo docs/evidencias/roteiro-executado.md

git add docs/evidencias/
git commit -m "docs: adiciona evidencias dos testes manuais"
```

Nao invente ou resuma "por cima" - a evidencia deve ser o que realmente
saiu do terminal.

---

## Passo 7 - Parar o ambiente

```bash
docker compose down        # mantem os dados do banco
# ou
docker compose down -v     # tambem apaga o volume do Postgres
```

---

## Passo 8 - Revisao final antes de enviar ao professor

- [ ] `git log --format="%an <%ae> - %s"` mostra sua identidade real em
      todos os commits (Passo 1)
- [ ] `git log --reverse --format="%h %s%n%b"` mostra os 5 checkpoints
      (CP1-CP5), cada um com corpo/justificativa - releia e adapte essas
      justificativas com suas proprias palavras, o professor exige isso
- [ ] `docs/evidencias/` tem a saida real dos 16 testes, commitada
      separadamente (Passo 6)
- [ ] `docker compose up --build` sobe sem erro, do zero, em uma pasta
      limpa (clone o repositorio em outro lugar e teste, se quiser ter
      certeza)

## Passo 9 - Push para o GitHub

```bash
# crie um repositorio vazio no GitHub primeiro, depois:
git remote add origin <url-do-seu-repositorio>
git push -u origin master
```

Confira no GitHub que o historico de commits (com os 5 checkpoints) esta
visivel, como o enunciado pede.

---

## Se algo der errado

- **Porta 8080 ou 5432 ja em uso**: troque `APP_PORT`/`DB_PORT` no `.env`.
- **`docker compose` nao encontrado**: em versoes mais antigas do Docker, o
  comando e `docker-compose` (com hifen).
- **Erro de schema-validation na inicializacao**: nao deveria acontecer (a
  V2 corrige o unico mismatch encontrado na analise estatica), mas se
  acontecer, copie a mensagem completa antes de tentar corrigir sozinho.
- **`docker compose up --build` demora muito na primeira vez**: normal, ele
  baixa o Maven e todas as dependencias dentro do container; das proximas
  vezes fica bem mais rapido por causa do cache de camadas do Docker.
