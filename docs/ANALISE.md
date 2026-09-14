# Analise e checklist final - CampusGigs

> **Nota de revisao**: este documento foi atualizado apos uma auditoria
> externa que encontrou pontos reais a corrigir (ver secao 6). As correcoes
> foram feitas em commits novos, sem alterar o conteudo dos commits CP1-CP5
> originais - so as MENSAGENS desses 5 commits foram reescritas (via
> `git rebase`) para incluir a justificativa de 1 a 3 linhas exigida pelo
> enunciado, que antes so existia aqui neste arquivo.

## 0. Observacao sobre o ambiente em que este projeto foi gerado

O upload continha apenas o PDF do enunciado e as instrucoes em texto - **nenhum
codigo de projeto existente foi enviado**. Por isso este nao e uma "revisao"
de um projeto seu: o codigo foi construido do zero, checkpoint por checkpoint,
seguindo estritamente o enunciado.

Alem disso, o ambiente onde o codigo foi escrito **nao tem acesso ao Maven
Central e nao tem Docker/daemon disponivel** (so Java 21 e Git). Isso significa:

- O projeto **nao foi compilado** (`mvn compile`/`package`) neste ambiente.
- **Nenhum teste automatizado ou manual foi executado de fato.**
- O `docker compose up` **nao foi executado** aqui.

Tudo abaixo reflete isso: nada que dependa de execucao esta marcado como
`[OK]` so por eu ter lido o codigo. Voce PRECISA rodar `docker compose up
--build` na sua maquina e seguir `docs/test-roteiro.md` antes de entregar.

## 1. Analise tecnica

| Item | Valor |
|---|---|
| Java | 21 (decisao de implementacao - projeto novo) |
| Spring Boot | 3.3.5 |
| Build tool | Maven |
| Banco | PostgreSQL 16 (decisao de implementacao - nenhum banco estava definido) |
| Estrutura | `domain`, `repository`, `dto`, `service`, `controller`, `security`, `config`, `client`, `exception` |
| Dependencias principais | spring-boot-starter-web, -data-jpa, -security, -validation, postgresql, flyway-core, flyway-database-postgresql, jjwt-*, lombok |
| Ja pronto | Todos os requisitos funcionais e regras de negocio do enunciado (ver tabela abaixo) |
| Faltando | Compilar, rodar, testar manualmente (ver secao 3) |
| Problemas encontrados | Nenhum no enunciado; a unica limitacao foi a impossibilidade de compilar/executar neste sandbox |

## 2. Tabela de requisitos

| Requisito | Situacao atual | O que precisa ser feito | Checkpoint |
|---|---|---|---|
| Ambiente sobe via Docker | Dockerfile + docker-compose.yml escritos | Rodar `docker compose up --build` e confirmar | CP1 |
| Migration inicial (Flyway) | `V1__create_initial_schema.sql` criada (users/gigs/hirings) | Confirmar que roda sem erro ao subir o app | CP1 |
| Cadastro de usuario | `POST /api/auth/register` implementado | Testar via curl | CP2 |
| Senha nunca em texto puro | BCrypt (`PasswordEncoder`) usado no hash | Nenhuma (verificavel no codigo) | CP2 |
| E-mail unico | Constraint `UNIQUE` no banco + checagem na aplicacao (409) | Testar tentativa de e-mail duplicado | CP2 |
| Autenticacao (login) | `POST /api/auth/login` implementado | Testar via curl | CP2/CP3 |
| Emissao/validacao de JWT | `JwtService` + `JwtAuthenticationFilter` implementados | Testar token valido/invalido/ausente | CP3 |
| Endpoint protegido | `GET /api/users/me` | Testar sem token / com token | CP3 |
| Publicar servico | `POST /api/gigs` | Testar via curl | CP4 |
| Listar servicos | `GET /api/gigs` (publico) | Testar via curl | CP4 |
| Contratar servico | `POST /api/hirings` | Testar via curl | CP4 |
| Encerrar servico | `PATCH /api/gigs/{id}/close` | Testar via curl | CP4 |
| USER edita/encerra so o proprio | Checagem de propriedade em `GigService` (403) | Testar cenario negado | CP4 |
| ADMIN encerra qualquer um | Checagem de role em `GigService` (200) | Testar promovendo um usuario a ADMIN | CP4 |
| Nao contratar o proprio servico | Checagem em `HiringService` (400) | Testar via curl | CP4 |
| So contratar servico ATIVO | Checagem em `HiringService` (400) | Testar via curl | CP4 |
| 401 vs 403 diferenciados | `RestAuthenticationEntryPoint` (401) / `RestAccessDeniedHandler` + handler no `GlobalExceptionHandler` (403) | Testar os dois cenarios | CP3/CP4 |
| Tratamento centralizado de erros | `GlobalExceptionHandler` (`@RestControllerAdvice`) | Nenhuma (verificavel no codigo) | CP2-CP5 |
| Integracao de CEP via HttpExchange | `ViaCepClient` (`@HttpExchange`) + `RestClientConfig` | Testar cadastro com CEP valido | CP5 |
| Falha/timeout do servico externo | `CepLookupService` mapeia para 400 (nao encontrado) ou 502 (indisponivel) | Testar CEP inexistente; simular timeout se possivel | CP5 |
| README completo | `README.md` escrito | Nenhuma | CP5 |
| Roteiro de testes manuais | `docs/test-roteiro.md` escrito | **Executar de verdade e guardar evidencia** | CP5 |

## 3. Checklist final (comparando com o enunciado)

> Legenda: `[OK]` = verificavel por leitura do codigo/estrutura (nao exige execucao).
> `[PRECISA TESTAR]` = implementado no codigo, mas depende de execucao para confirmar - NAO testado por mim.
> `[PENDENTE]` = ainda nao feito / depende de uma acao sua.

- `[OK]` Estrutura de pacotes organizada (controller/service/repository/entity/dto/security/exception/config/client)
- `[OK]` Sem TODOs, mocks permanentes ou funcionalidades fictícias no codigo
- `[OK]` Senha tratada com BCrypt, nunca em texto puro, nunca retornada nas respostas (DTOs dedicados)
- `[OK]` Prestador/contratante sempre resolvidos via `@AuthenticationPrincipal` (JWT), nunca aceitos do corpo da requisicao
- `[OK]` Autorizacao (role) resolvida via `UserDetailsService` a partir do banco a cada requisicao - nao depende de claim do token (ver secao 6)
- `[OK]` HttpExchange (`@HttpExchange` no nivel da interface + `@GetExchange` no metodo, via `HttpServiceProxyFactory`) usado para o CEP - nenhum RestTemplate/chamada manual
- `[OK]` Flyway com migration versionada (`V1__...`), Hibernate em `ddl-auto: validate`
- `[OK]` Tratamento centralizado de erros (`@RestControllerAdvice`), sem stack trace/SQL exposto
- `[OK]` Cada um dos 5 commits (CP1-CP5) tem corpo com justificativa de 1 a 3 linhas (verificavel com `git log`)
- `[PRECISA TESTAR]` Compilacao do projeto (`mvn clean package`) - **nao executada neste ambiente** (sem acesso ao Maven Central)
- `[PRECISA TESTAR]` `docker compose up --build` sobe app + banco - **nao executado neste ambiente** (sem Docker)
- `[PRECISA TESTAR]` Migration realmente aplicada com sucesso contra um Postgres real - **nao executado**
- `[PRECISA TESTAR]` Todos os endpoints (registro, login, gigs, hirings, CEP) respondendo como esperado
- `[PENDENTE]` Rodar o roteiro de `docs/test-roteiro.md` (16 passos cobrindo os 15 cenarios do enunciado, incluindo o de acesso negado por papel obrigatorio) e **guardar a evidencia real** - o PDF exige essa evidencia, e ela nao existe ainda
- `[PENDENTE]` Configurar `git config user.name`/`user.email` com sua identidade real e reatribuir a autoria dos commits antes do push (ver secao 7 - **nao fiz isso por voce, de proposito**)
- `[PENDENTE]` Dar `git push` para um repositorio seu no GitHub

## 4. Os 5 checkpoints: arquivos, resumo e justificativa

> As justificativas abaixo agora sao exatamente as que estao no corpo de
> cada commit (`git log` mostra titulo + corpo). Voce pode reescreve-las com
> suas proprias palavras se quiser personalizar - o conteudo tecnico delas
> ja reflete decisoes reais do codigo, nao e mais so uma sugestao solta.

### CP1 - Ambiente Docker + migration inicial
**Arquivos**: `pom.xml`, `Dockerfile`, `docker-compose.yml`, `.env.example`, `.gitignore`, `application.yml`, `V1__create_initial_schema.sql`, `CampusGigsApplication.java`, `README.md` (stub).
**Resumo**: projeto Spring Boot minimo, Postgres via Docker Compose, schema inicial completo (users/gigs/hirings) via Flyway.
**Commit**: `CP1: ambiente Docker + primeira migration Flyway (schema inicial)`
**Justificativa (no corpo do commit)**: "Criei o schema inteiro (users, gigs, hirings) ja na primeira migration, pois todo o dominio ja estava definido no enunciado. Assim evito editar essa migration em checkpoints futuros - qualquer mudanca de schema vira uma nova migration (V2, V3...)."

### CP2 - Cadastro e autenticacao com senha protegida
**Arquivos**: `User.java`, `Role.java`, `UserRepository.java`, `SecurityConfig.java`, `CustomUserDetailsService.java`, `AuthService.java`, `AuthController.java`, DTOs (`RegisterRequest`, `LoginRequest`, `UserResponse`), `GlobalExceptionHandler.java`, `ApiErrorResponse.java`, `EmailAlreadyInUseException.java`.
**Resumo**: cadastro com hash BCrypt e e-mail unico; login validando credenciais via `AuthenticationManager` do Spring Security (ainda sem emitir token).
**Commit**: `CP2: cadastro de usuario e autenticacao com senha protegida (BCrypt)`
**Justificativa (no corpo do commit)**: "Login e cadastro usam o AuthenticationManager e o UserDetailsService do Spring Security (com BCryptPasswordEncoder), em vez de comparar a senha manualmente. Isso ja deixa a base pronta para o JWT entrar no proximo checkpoint sem reescrever essa parte."

### CP3 - Emissao/validacao de JWT
**Arquivos**: `pom.xml` (dependencia JJWT), `application.yml` (jwt.secret/expiration), `JwtService.java`, `JwtAuthenticationFilter.java`, `RestAuthenticationEntryPoint.java`, `RestAccessDeniedHandler.java`, `SecurityConfig.java` (reescrito), `AuthResponse.java`, `AuthService.login()` (atualizado), `UserController.java` (`/api/users/me`).
**Resumo**: login passa a devolver um JWT; um filtro le o header `Authorization`, valida o token e popula o `SecurityContext`; endpoint protegido para validar o fluxo.
**Commit**: `CP3: emissao e validacao de JWT, endpoint protegido (/api/users/me)`
**Justificativa (no corpo do commit)**: "O JwtService assina o token (HS256) com o e-mail como subject e inclui uma claim 'role' obtida no momento do login. A autorizacao de cada requisicao, porem, e resolvida pelo JwtAuthenticationFilter atraves do UserDetailsService, que carrega o usuario (e sua role atual) do banco a cada chamada - a claim 'role' do token nao e usada para autorizar nada hoje."
**Nota importante (arquitetura real, corrigida nesta revisao)**: a versao anterior deste documento e do comentario em `JwtService.java` afirmavam, incorretamente, que o token evitava consultar o banco para saber a role. Isso NUNCA foi verdade: o `JwtAuthenticationFilter` sempre usou `UserDetailsService.loadUserByUsername(email)` para carregar o usuario do banco a cada requisicao, e e dali (nao do token) que vem a role usada na autorizacao. A claim `"role"` chegou a existir no token, mas nunca foi lida por nenhum outro ponto do codigo - por ser uma informacao morta e potencialmente enganosa (alguem poderia presumir, ao ler o token, que a autorizacao usa aquele valor), ela foi **removida** em um commit posterior (ver secao 6), sem qualquer mudanca no comportamento de autorizacao, que sempre foi baseado no banco.

### CP4 - Dominio de servicos/contratacoes e autorizacao por papel
**Arquivos**: `Gig.java`, `GigStatus.java`, `Hiring.java`, `HiringStatus.java`, `GigRepository.java`, `HiringRepository.java`, DTOs de Gig/Hiring, `ResourceNotFoundException.java`, `BusinessRuleViolationException.java`, `GigService.java`, `HiringService.java`, `GigController.java`, `HiringController.java`, `SecurityConfig.java` (GET publico), `GlobalExceptionHandler.java` (novos handlers).
**Resumo**: publicar/listar/editar/encerrar servico e contratar, com todas as regras de propriedade e papel (USER só mexe no proprio, ADMIN encerra qualquer um, ninguem contrata o proprio servico nem um servico inativo).
**Commit**: `CP4: dominio de servicos/contratacoes e regras de autorizacao por papel`
**Justificativa (no corpo do commit)**: "As checagens de dono/ADMIN ficam na camada de service, nao em anotacao no controller, porque dependem do dado especifico (quem publicou aquele servico), nao so da rota. Prestador e contratante sao sempre obtidos do usuario autenticado (JWT), nunca do corpo da requisicao."

### CP5 - Integracao de CEP (HttpExchange) e revisao final
**Arquivos**: `ViaCepClient.java`, `ViaCepResponse.java`, `CepLookupService.java`, `CepLookupResult.java`, `RestClientConfig.java`, `CepNotFoundException.java`, `CepServiceUnavailableException.java`, `UpdateCepRequest.java`, `UserService.java`, `UserController.java` (endpoint de CEP), `AuthService.register()` (atualizado), `application.yml` (config de CEP), `GlobalExceptionHandler.java` (novos handlers), `README.md` (final), `docs/test-roteiro.md`.
**Resumo**: cadastro e atualizacao de CEP passam a consultar o ViaCEP via cliente declarativo `@HttpExchange`/`@GetExchange`; falhas sao tratadas sem deixar o cadastro incompleto.
**Commit**: `CP5: integracao de CEP via HttpExchange, tratamento de falha/timeout, README e roteiro de testes`
**Justificativa (no corpo do commit)**: "Separei os dois tipos de falha do CEP: formato valido mas inexistente e erro do cliente (400) e nao deixa o cadastro incompleto; timeout ou indisponibilidade do ViaCEP e erro do servico externo (502), sem expor o motivo tecnico ao usuario final."

## 5. Resumo final

- **Implementado**: todos os requisitos funcionais, regras de negocio, autenticacao/autorizacao, Flyway, Docker, HttpExchange e tratamento de erros do enunciado, ate onde e verificavel por leitura de codigo.
- **Testado de fato**: nada (ambiente sem Maven Central/Docker) - **isso e uma limitacao do ambiente em que o codigo foi gerado, nao uma etapa pulada por escolha**.
- **O que ficou pendente para voce**: compilar/subir (`docker compose up --build`), rodar o roteiro completo em `docs/test-roteiro.md` e **produzir a evidencia real** exigida pelo PDF, configurar sua identidade Git (secao 7) antes do push.
- **Os 7 commits** ja estao no historico do Git deste projeto, com titulo + justificativa no corpo de cada um dos 5 checkpoints (`git log` para conferir).
- **Comandos para rodar**: `cp .env.example .env && docker compose up --build`.
- **Pontos do PDF nao atendidos**: no nivel de codigo, nenhum requisito funcional/tecnico ficou sem implementacao correspondente. **Porem**, o PDF exige explicitamente evidencia real de testes manuais, e essa evidencia **ainda nao existe** (nada foi executado) - por isso o item "evidencia de testes manuais" permanece `[PENDENTE]` na secao 3, e a entrega **nao deve ser considerada pronta** ate que voce gere essa evidencia de verdade.

## 6. Correcoes feitas nesta auditoria (commits apos o CP5)

Alem de reescrever as mensagens dos 5 commits de checkpoint (adicionando o
corpo com a justificativa, sem alterar nenhum conteudo de arquivo), esta
rodada de revisao corrigiu os seguintes problemas reais encontrados:

1. **Documentacao do JWT incorreta** - tanto aqui quanto no comentario de
   `JwtService.java` afirmavam que o token evitava consultar o banco para
   saber a role. Isso nunca refletiu o codigo real (`JwtAuthenticationFilter`
   sempre usou `UserDetailsService` para carregar o usuario do banco a cada
   requisicao). Corrigido em ambos os lugares.
2. **Claim `"role"` no JWT removida** - ela existia no token mas nunca era
   lida por nenhum outro trecho do codigo (autorizacao morta/nao usada).
   Como nao havia motivo tecnico para mante-la e ela induzia a leitura
   incorreta da arquitetura, foi removida. **O comportamento de autorizacao
   nao mudou em nada** - continua vindo do banco, via `UserDetailsService`.
3. **`docs/test-roteiro.md` tinha uma dependencia impossivel** - o passo que
   testava "contratar servico nao ativo" vinha antes do servico ser
   efetivamente encerrado (o passo anterior a ele resultava em `403`, entao
   o servico continuava `ATIVO`). Roteiro reordenado para ser executavel
   sequencialmente do inicio ao fim; tambem foi adicionado o `login` de
   Bruno que faltava antes de usar `TOKEN_BRUNO`.
4. **`@HttpExchange` tornado explicito** - `ViaCepClient` ja usava
   `@GetExchange` (que e um atalho valido de `@HttpExchange` para GET), mas
   agora tambem declara `@HttpExchange` no nivel da interface (definindo o
   `Accept` padrao), deixando a tecnologia usada explicita para quem for
   avaliar.

## 7. Autoria dos commits - acao obrigatoria antes do push

Todos os commits deste repositorio foram criados no sandbox com uma
identidade generica (`CampusGigs Dev <dev@campusgigs.local>`). **Isso nao
deve ser enviado ao professor.** Configure sua identidade real e reatribua
a autoria de TODOS os commits antes de dar push - os comandos exatos estao
na mensagem de entrega (fora deste arquivo), pois dependem do seu nome e
e-mail reais, que eu nao tenho e nao devo inventar.
