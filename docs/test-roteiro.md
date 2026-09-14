# Roteiro de testes manuais - CampusGigs

> **Importante sobre este roteiro**: estes comandos `curl` foram preparados
> para cobrir exatamente os 15 cenarios exigidos no enunciado, mas **nao
> foram executados neste ambiente** (o sandbox usado para gerar o codigo nao
> tem acesso ao Maven Central nem ao Docker, entao a aplicacao nunca chegou a
> rodar aqui - veja o aviso na mensagem de entrega). Ou seja: nenhum destes
> testes deve ser considerado "passou" ate que VOCE rode a aplicacao
> localmente (`docker compose up --build`) e execute os comandos abaixo,
> registrando o status HTTP e a resposta de cada um como evidencia para o
> professor.

Pre-requisito: aplicacao rodando em `http://localhost:8080` (via
`docker compose up --build`).

```bash
BASE=http://localhost:8080
```

## 1. Cadastro de USER (com CEP valido)

```bash
curl -i -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana Lima","email":"ana@ufu.br","password":"senha123","cep":"38400100"}'
```
Esperado: `201 Created`, corpo com `city`/`state` preenchidos automaticamente.

## 2. Cadastro de um segundo USER (para testar contratacao)

```bash
curl -i -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Bruno Melo","email":"bruno@ufu.br","password":"senha123","cep":"01310930"}'
```

## 3. Autenticacao e recebimento do JWT

```bash
curl -i -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@ufu.br","password":"senha123"}'
```
Esperado: `200 OK`, corpo com `token`. Guarde os tokens de Ana e Bruno:

```bash
TOKEN_ANA="<token retornado para ana@ufu.br>"
TOKEN_BRUNO="<token retornado para bruno@ufu.br>"
```

## 4. Chamada autenticada (endpoint protegido)

```bash
curl -i $BASE/api/users/me -H "Authorization: Bearer $TOKEN_ANA"
```
Esperado: `200 OK` com os dados de Ana.

## 5. Publicacao de servico (por Ana)

```bash
curl -i -X POST $BASE/api/gigs \
  -H "Authorization: Bearer $TOKEN_ANA" \
  -H "Content-Type: application/json" \
  -d '{"title":"Aulas de Calculo 1","description":"Reforco para a prova final","category":"Aulas","price":50.00}'
```
Esperado: `201 Created`. Anote o `id` retornado como `GIG_ID`.

## 6. Listagem de servicos

```bash
curl -i "$BASE/api/gigs?status=ATIVO"
```
Esperado: `200 OK`, sem necessidade de token (endpoint publico).

## 7. Contratacao por outro usuario (Bruno contrata o servico da Ana)

```bash
curl -i -X POST $BASE/api/hirings \
  -H "Authorization: Bearer $TOKEN_BRUNO" \
  -H "Content-Type: application/json" \
  -d "{\"gigId\": $GIG_ID}"
```
Esperado: `201 Created`.

## 8. Tentativa de contratar o proprio servico -> erro

```bash
curl -i -X POST $BASE/api/hirings \
  -H "Authorization: Bearer $TOKEN_ANA" \
  -H "Content-Type: application/json" \
  -d "{\"gigId\": $GIG_ID}"
```
Esperado: `400 Bad Request` ("Voce nao pode contratar o proprio servico").

## 9. Tentativa de contratar servico nao ativo -> erro

```bash
# Primeiro encerre o servico (ver item 10), depois tente contratar:
curl -i -X POST $BASE/api/hirings \
  -H "Authorization: Bearer $TOKEN_BRUNO" \
  -H "Content-Type: application/json" \
  -d "{\"gigId\": $GIG_ID}"
```
Esperado: `400 Bad Request` (servico nao esta ativo).

## 10. USER tentando encerrar servico de outro USER -> acesso negado (OBRIGATORIO)

```bash
curl -i -X PATCH $BASE/api/gigs/$GIG_ID/close \
  -H "Authorization: Bearer $TOKEN_BRUNO"
```
Esperado: `403 Forbidden` (Bruno nao e o dono do servico de Ana).

## 11. ADMIN encerrando servico de qualquer usuario -> permitido

```bash
# Promova um usuario a ADMIN diretamente no banco (nao ha endpoint para isso):
#   docker compose exec db psql -U campusgigs -d campusgigs \
#     -c "UPDATE users SET role='ADMIN' WHERE email='bruno@ufu.br';"
# Depois faca login novamente com bruno@ufu.br para obter um token com a nova role:
curl -i -X POST $BASE/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"bruno@ufu.br","password":"senha123"}'
TOKEN_ADMIN="<novo token de bruno, agora ADMIN>"

curl -i -X PATCH $BASE/api/gigs/$GIG_ID/close -H "Authorization: Bearer $TOKEN_ADMIN"
```
Esperado: `200 OK` (dono OU admin pode encerrar; aqui testamos o caso ADMIN
encerrando um servico que nao e dele).

## 12. Acesso a endpoint protegido sem token -> rejeitado

```bash
curl -i $BASE/api/users/me
```
Esperado: `401 Unauthorized`.

## 13. Token invalido -> rejeitado

```bash
curl -i $BASE/api/users/me -H "Authorization: Bearer token-invalido-qualquer"
```
Esperado: `401 Unauthorized`.

## 14. Cadastro com CEP valido -> cidade e UF preenchidos

Ja coberto no item 1 (verifique `city`/`state` na resposta).

## 15. CEP inexistente -> erro claro

```bash
curl -i -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Teste Cep","email":"teste.cep@ufu.br","password":"senha123","cep":"99999999"}'
```
Esperado: `400 Bad Request` ("CEP nao encontrado: 99999999"). O cadastro NAO
deve ser criado.

---

Para registrar a evidencia, cole o comando + status HTTP + corpo da resposta
de cada item (pode ser um arquivo `.md`, print do Postman/Insomnia, ou saida
de terminal) e anexe/junte ao commit do CP5.
