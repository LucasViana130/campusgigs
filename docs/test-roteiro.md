# Roteiro de testes manuais - CampusGigs

> **Importante sobre este roteiro**: estes comandos `curl` foram preparados
> para cobrir exatamente os 15 cenarios exigidos no enunciado, mas **nao
> foram executados neste ambiente** (o sandbox usado para gerar o codigo nao
> tem acesso ao Maven Central nem ao Docker, entao a aplicacao nunca chegou a
> rodar aqui). Ou seja: nenhum destes testes deve ser considerado "passou"
> ate que VOCE rode a aplicacao localmente (`docker compose up --build`) e
> execute os comandos abaixo, registrando o status HTTP e a resposta de cada
> um como evidencia para o professor.
>
> A ordem abaixo foi revisada para ser executavel **sequencialmente, de cima
> para baixo, sem nenhum passo depender de uma acao que só acontece depois**
> (ela nao segue mais a mesma ordem/numeracao da lista de 15 itens do
> enunciado - cada passo abaixo indica, entre colchetes, a qual item da
> lista original ele corresponde).

Pre-requisito: aplicacao rodando em `http://localhost:8080` (via
`docker compose up --build`).

```bash
BASE=http://localhost:8080
```

## 1. Cadastro de USER com CEP valido [item 1 e 14]

```bash
curl -i -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana Lima","email":"ana@ufu.br","password":"senha123","cep":"38400100"}'
```
Esperado: `201 Created`, corpo com `city`/`state` preenchidos automaticamente.

## 2. Cadastro de um segundo USER (para testar contratacao) [apoio do item 1]

```bash
curl -i -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Bruno Melo","email":"bruno@ufu.br","password":"senha123","cep":"01310930"}'
```

## 3. Autenticacao e recebimento do JWT - Ana [item 2 e 3]

```bash
curl -i -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@ufu.br","password":"senha123"}'
```
Esperado: `200 OK`, corpo com `token`.
```bash
TOKEN_ANA="<token retornado para ana@ufu.br>"
```

## 4. Autenticacao e recebimento do JWT - Bruno [apoio do item 2 e 3]

```bash
curl -i -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bruno@ufu.br","password":"senha123"}'
```
```bash
TOKEN_BRUNO="<token retornado para bruno@ufu.br>"
```

## 5. Chamada autenticada (endpoint protegido) [item 4]

```bash
curl -i $BASE/api/users/me -H "Authorization: Bearer $TOKEN_ANA"
```
Esperado: `200 OK` com os dados de Ana.

## 6. Publicacao de servico por Ana [item 5]

```bash
curl -i -X POST $BASE/api/gigs \
  -H "Authorization: Bearer $TOKEN_ANA" \
  -H "Content-Type: application/json" \
  -d '{"title":"Aulas de Calculo 1","description":"Reforco para a prova final","category":"Aulas","price":50.00}'
```
Esperado: `201 Created`. Anote o `id` retornado:
```bash
GIG_ID="<id retornado>"
```

## 7. Listagem de servicos [item 6]

```bash
curl -i "$BASE/api/gigs?status=ATIVO"
```
Esperado: `200 OK`, sem necessidade de token (endpoint publico). O servico
publicado no passo 6 deve aparecer na lista.

## 8. Contratacao por outro usuario - Bruno contrata o servico da Ana [item 7]

```bash
curl -i -X POST $BASE/api/hirings \
  -H "Authorization: Bearer $TOKEN_BRUNO" \
  -H "Content-Type: application/json" \
  -d "{\"gigId\": $GIG_ID}"
```
Esperado: `201 Created`.

## 9. Tentativa de Ana contratar o proprio servico -> erro [item 8]

```bash
curl -i -X POST $BASE/api/hirings \
  -H "Authorization: Bearer $TOKEN_ANA" \
  -H "Content-Type: application/json" \
  -d "{\"gigId\": $GIG_ID}"
```
Esperado: `400 Bad Request` ("Voce nao pode contratar o proprio servico").

## 10. USER tentando encerrar servico de outro USER -> acesso negado (OBRIGATORIO) [item 10]

O servico da Ana ainda esta `ATIVO` neste ponto (ninguem o encerrou ainda),
entao este teste e valido exatamente aqui:

```bash
curl -i -X PATCH $BASE/api/gigs/$GIG_ID/close \
  -H "Authorization: Bearer $TOKEN_BRUNO"
```
Esperado: `403 Forbidden` (Bruno nao e o dono do servico de Ana).

## 11. Promover Bruno a ADMIN e obter um token com a nova role [apoio do item 11]

Nao ha endpoint para isso (fora do escopo do enunciado); promova direto no banco:

```bash
docker compose exec db psql -U campusgigs -d campusgigs \
  -c "UPDATE users SET role='ADMIN' WHERE email='bruno@ufu.br';"
```

Como a role e resolvida a partir do banco a cada requisicao (nao a partir
do JWT), nem seria necessario gerar um token novo para a mudanca ter
efeito - mas como o login tambem serve de evidencia de que o novo papel
foi aplicado, refaca o login mesmo assim:

```bash
curl -i -X POST $BASE/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"bruno@ufu.br","password":"senha123"}'
```
```bash
TOKEN_ADMIN="<token de bruno, agora ADMIN>"
```

## 12. ADMIN encerrando servico de qualquer usuario -> permitido [item 11]

```bash
curl -i -X PATCH $BASE/api/gigs/$GIG_ID/close -H "Authorization: Bearer $TOKEN_ADMIN"
```
Esperado: `200 OK`, `status` do servico agora `ENCERRADO` (Bruno/ADMIN
encerrando um servico que nao e dele).

## 13. Tentativa de contratar servico nao ativo -> erro [item 9]

Agora que o servico foi encerrado no passo 12, este teste finalmente tem
como ser executado de verdade (na ordem original do enunciado ele vinha
antes de o servico ser encerrado, o que o tornava impossivel de rodar):

```bash
curl -i -X POST $BASE/api/hirings \
  -H "Authorization: Bearer $TOKEN_BRUNO" \
  -H "Content-Type: application/json" \
  -d "{\"gigId\": $GIG_ID}"
```
Esperado: `400 Bad Request` (situacao atual: `ENCERRADO`).

## 14. Acesso a endpoint protegido sem token -> rejeitado [item 12]

```bash
curl -i $BASE/api/users/me
```
Esperado: `401 Unauthorized`.

## 15. Token invalido -> rejeitado [item 13]

```bash
curl -i $BASE/api/users/me -H "Authorization: Bearer token-invalido-qualquer"
```
Esperado: `401 Unauthorized`.

## 16. CEP inexistente -> erro claro [item 15]

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
