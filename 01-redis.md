# Redis

## Referência

https://redis.io/

## Conceitos

- Banco de dados chave / valor
- Persistência dos dados não é a prioridade e sim a replicação
- Tipos de valores: primitivos (string, números, lógicos), listas, conjuntos, hash e conjuntos ordenados

## Utilizando Docker

[Docker Playground](https://labs.play-with-docker.com/)

## Preparando o Ambiente

- Criar uma imagem baseada no centOS

`docker pull centos`

- Iniciar um `container`

`docker run -it -p 6379:6379 --name redis centos`

- Atualizar o repositório do `yum`

```
cd /etc/yum.repos.d/
sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*
dnf update -y
```

## Instalando Redis no Container

`yum install -y redis`

## Executando

```
redis-server --protected-mode no &

redis-cli

ping
```

## Dados Prioritariamente em Memória

- `SET msg alo`
- Parar o servidor: `redis-cli shutdown`
- Iniciar o servidor
- `GET msg`
  
## Comandos

- Criar, recuperar e remover uma chave (encerar o server e observar que a chave pode ser perdida)
```
SET SP;RJ 400Km
GET SP;RJ
KEYS *
DEL SP;RJ
```
- Outras operações
    - `INCR`: incrementa em uma unidade
    - `DECR`: decrementa em uma unidade
    - `INCRBY`: incrementa em X unidades
    - `DECRBY`: decrementa em X unidades
    - `APPEND`: concatena uma `String` ao valor (também `String`) de uma chave
    - `GETRANGE`: obtem uma substring do valor associado a uma chave

- **Exercício**: Quatro equipes de futebol de enfrentam em dois jogos simultâneos. Como controlar o placar dos jogos utilizando *Redis*? Considere também a possibilidade do cancelamento de gols.

## Expirando Chaves

- Pode-se determinar um tempo de "vida" para chaves criadas com o `EXPIRE`
```
SET MENSAGEM Oi
GET MENSAGEM
EXPIRE MENSAGEM 10
```

## Listas

- Uma única chave pode ter mais de um valor associado
- Novos valores são adicionados à direita da lista (item 1 -> item 2 -> item n) com `RPUSH` ou à esquerda com `LPUSH`
- Valores são removidos da esquerda com o `LPOP`
- Para listar todos os valores utilizar o `LRANGE`
- O índice de um valor pode ser obtido por meio do `LINDEX`
- `LTRIM` remove elementos de uma lista fora de um intervalo definido
- `RPOPLPUSH` remove um elemento da direita de uma lista e adiciona à esquerda em outra

```
RPUSH SP;RJ 400Km R$700,00
LRANGE SP;RJ 0 -1
LINDEX SP;RJ 0
```
- **Exercício**: verificar se a expressão `(10 * 2 + (3 -1)` está correta quanto à quantidade de parêntesis abertos e fechados
- **Exercício**: construir um sistema de senhas de atendimento em uma agência bancária considerando uma fila normal e também uma prioritária
- **Exercício**: controlar uma lista de compras com itens que devem ser comprados no mercado e os que já foram colocados no carrinho

## Conjuntos (Sets)

- Diferem das listas por não permitirem valores duplicados para uma mesma chave
- Principais comandos
  - `SADD`:  adiciona / cria um Set incluindo itens
  - `SMEMBERS`: retorna todos os itens de um Set
  - `SISMEMBER`: verifica se um item pertence ao Set
  - `SREM`: remove um item do Set (se existir)
  - `SCARD`: retorna o número total de itens do Set
  - `SRANDMEMBER`: retorna um item aleatório
  - `SPOP`: remove itens da esquerda
  - `SMOVE`: move itens de um set para outro
  - `SDIFF`: retorna os elementos distintos entre conjuntos
  - `SDIFFSTORE`: mesmo que anterior só que inclui itens no conjunto destino
  - `SINTER`: retorna elementos em comum entre conjuntos
  - `SINTERSTORE`: mesmo que anterior só que inclui itens no conjunto destino
  - `SUNION`: une os dois conjuntos sem repetir itens
  - `SUNIONSTORE`: mesmo que anterior só que inclui itens no conjunto destino

- **Exercício**: criar um controle de ocupação de assentos em um vôo (RDIS888) onde devem fazer parte do conjunto as identificações dos assentos já ocupados:
  - Antes de reservar um assento verificar se ele já está alocado no vôo
  - Oferecer a opção de cancelamento da reserva do assento no vôo
  - Criar um segundo vôo (RDIS999) e reservar alguns assentos
  - Verificar quais assentos são comuns e distintos entre os dois vôos
  - Transferir todos os assentos que não são comuns para um terceiro vôo (RDI000)

## Hash

- Hashes podem ser vistos como sub-chaves e comparados a registros em um banco de dados relacional ou ainda adocumentos em bancos de dados noSQL
- Principais operações:
  - `HMSET`: cria / atualiza chaves (mais de uma)
  - `HGET`: retorna o valor de uma chave
  - `HMGET`: retorna valores de chaves
  - `HDEL`: remove uma chave
  - `HLEN`: retorna a quantidade de chaves
  - `HEXISTS`: verifica se uma sub-chave existe
  - `HKEYS`: retorna todas as sub-chaves
  - `HVALS`: retorna todos os valores armazenados nas sub-chaves
  - `HINCRBY` à incrementa uma sub-chave baseado em um valor inteiro
  - `HINCRBYFLOAT` à incrementa uma sub-chave baseado em um valor decimal

```
HSET SP;BH distancia 600 preco 800.0 empresa TAM
HGETALL SP;BH
HGET SP;BH preco
HDEL SP;BH empresa
```
- **Exercício**: criar uma estrutura para armazenar o nome de um aluno, sua turma e semestre (escolher a chave apropriadamente). Atualizar o semestre atual.

## ZSet

- ZSets se assemelham aos Hashes mas as chaves (membros) são únicas e os valores (scores) devem ser apenas números
- Itens podem ser acessados de maneira ordenada
- Principais operações:
  - `ZREM`: remove membros
  - `ZCARD`: retorna o número de membros
  - `ZCOUNT`: retorna a quantidade de membros com o score entre os parâmetros informados
  - `ZRANK`: retorna a posição do membro
  - `ZSCORE`: retorna o score de um membro
  - `ZRANGEBYSCORE`: retorna os membros cujo score estejam entre determinado intervalo

- **Exercício**: armazenar a quantidade de gols marcados por um jogador em determinado campeonato.

## Coordenadas Geográficas

- É possível armazenar coordenadas geográficas (latitude / longitude)
- O comando `GEOADD` adiciona uma coordenada

```
> GEOADD bikes:rentable -122.27652 37.805186 station:1
> GEOADD bikes:rentable -122.2674626 37.8062344 station:2
> GEOADD bikes:rentable -122.2469854 37.8104049 station:3

```
- Pode-se consultar coordenadas com base em um raio de distância:
```
GEOSEARCH bikes:rentable FROMLONLAT -122.2612767 37.7936847 BYRADIUS 5 km WITHDIST
```
## Streams

```
XREAD BLOCK 0 STREAMS eventos $
XADD eventos * tipo click
```

## Redis na Cloud

- Redis pode ser acessado na Cloud [AQUI](https://app.redislabs.com/#/login)

## Use Cases

## Integração Aplicações (Nodejs)

- Criar uma pasta `single-signon`
- Acessar a pasta `cd single-signon`
- Iniciar um projeto **Nodejs** `npm init -y`
- Instalar o cliente **Redis** para **Nodejs** `npm install redis --save`

  ```javascript
  var redis = require('redis');

  async function testeConexao() {

      const cli = redis.createClient({
          password: '',
          socket: {
              host: '',
              port: 16633
          }
      });

      await cli.connect();

      cli.on("error", function (error) {
          console.error(error);
      });

      console.log('conectado', cli.isOpen);
      var ret = await cli.ping();
      console.log(ret)

  }

  testeConexao();
  ```
## Exemplo Express

- Instalar os pacotes necessários `npm install express ejs cookie-parser --save`

- Exemplo:

  ```javascript
  var express = require('express')
  var app = express()

  app.use(express.static('public'))

  app.get("/", (req, res) => {
      res.status(200).send('Ok');
  })

  app.listen(8888, () => {
      console.log('Servidor iniciado porta 8888')
  });
  ```

## Autenticação com Single-Signon

- Continuando a aplicação...
- Criar duas pastas `view` e `public`
- Criar a página `login.html` dentro de `public`

    ```html
    <html>
    
    <head>
        <title>Redis - Login</title>
    </head>
    
    <body style="font-family: Arial, Helvetica, sans-serif;">
    
        <div
            style="display: flex;border: solid; flex-direction: column; height: 100%; align-items: center; justify-content: center;">
            <div>
    
                <form action="/login" method="POST">
                    <label for="username">Username:</label>
                    <input type="text" id="username" name="username">
                    <label for="senha">Senha:</label>
                    <input type="password" id="senha" name="senha">
                    <input type="submit" value="Login">
                </form>
    
            </div>
        </div>
    </body>
    
    </html>
    ```
  - Incluir a página de login como pública

  ```javascript
  app.use(express.static('public'))
  ```

  - Criar os *endpoints*

  ```javascript
  app.get('/', (req, res) => {
  })
  app.post('/login', (req, res) => {
  })
  app.post('/perfil', (req, res) => {
  })
  app.get('/remove', (req, res) => {
    res.clearCookie("tokencookie");
    res.status(200).send('Cookie removido');
  })
  ```
- Criar a conexão com o **Redis** na *Cloud*
  ```javascript

  var redis = require('redis');

  const cli = redis.createClient({
    password: '',
    socket: {
        host: '',
        port: 16356
    }
  });

  async function start() {

    console.log('Teste conexao redis x nodejs')
    console.log('Criar o client')
    console.log('Estabelecer conexao')
    await cli.connect()
    const resultado = await cli.ping()
    console.log(resultado)

    app.listen(8888, () => {
        console.log('Servidor iniciado porta 8888')
    });
}

start();
```
- Adicionar o tratamento de *cookies*
  ```javascript
  var cookieParser = require('cookie-parser')
  app.use(cookieParser())
  ```
- Verificar se já existe o *token* armazenado na forma de *cookie*

  ```javascript
  app.get('/', (req, res) => {
  var tokenCookie = req.cookies.tokencookie
  if (tokenCookie) {
  cli.hgetall(tokenCookie, (err, data) => {
  res.render("home", {"corfrente": data.corFrente, "corfundo": data.corFundo});
  })
  } else {
  res.sendFile(__dirname + "/public/login.html");
  }
  })
  ```
- Habilitando dados enviados via formulário
  ```javascript
  app.use(express.urlencoded({ extended: true }))
  ```
- Habilitar a geraçao de **tokens**
  ```javascript
  var jwt = require('jsonwebtoken')
  ```
- Recebendo o *login* e *sennha* do formulário
  ```javascript
  app.post('/login', (req, res) => {
  var username = req.body.username
  var senha = req.body.senha
  if (username == 'teste' && senha == 'teste') {
  var token = jwt.sign({ username }, 'minhachavesecreta')
  cli.hset([token, "corFrente", "#", "corFundo", "#"], (err, res) => {
  })
  res.cookie('tokencookie',token)
  res.sendFile(__dirname + "/public/perfil.html");
  } else {
  res.sendFile(__dirname + "/public/login.html");
  }
  })
  ```
- Criar a página perfil.html dentro de `public`
  ```html
  <html>

  <head>
      <title>Redis - Perfil</title>
  </head>
  
  <body style="font-family: Arial, Helvetica, sans-serif;">

      <form action="/perfil" method="POST">
          <label for="corfrente">Cor Frente:</label>
          <select id="corfrente" name="corfrente">
              <option value="#FF0000">Vermelho</option>
              <option value="#00FF00">Verde</option>
          </select>
          <label for="corfundo">Cor Fundo:</label>
          <select id="corfundo" name="corfundo">
              <option value="#00FF00">Verde</option>
              <option value="#FF0000">Vermelho</option>
          </select>
          <input type="submit" value="Selecionar">
      </form>
  </body>

  </html>
  ```
- Habilitar o **ejs**
  ```javascript
  app.set('view engine', 'ejs')
  ```
- Criar a página `home.ejs` dentro de `views`

  ```html
  <html>

  <head>
      <title>Redis - Home</title>
  </head>

  <body style="background-color:<%= corfundo %>" text="<%= corfrente %>">
      <h1>Bem vindo!</h1>
  </body>

  </html>
  ```
## Arquivo de Configuração

- **Redis** pode ser iniciado com configurações específicas `redis-server redis.conf`

## Configurando Persistência

- Criar um arquivo `redis.conf`
- Informar o parâmetro `dir` para apontar para o diretório de *snapshot*
- Informar o parâmetro `dbfilename` para o nome do arquivo de *snapshot*
- Configurar o *snapshot* com `save 60 10`
- Pode-se forçar o *snapshot* com o comando `save`
- Outra forma é utilizando o AOF (Append Only File)
- Basta definir o parâmetro `appendonly yes` e definir a periodicidade com `appendfsync` que pode ser: `aways`, `everysec` ou `no`

## Replicação com Container

```mermaid
graph LR;
    master --> slave1 & slave2;
```
- Criar 3 instâncias do redis dentro do [Docker Playground](https://labs.play-with-docker.com/)
- Iniciar uma das instâncias (nó master - IP_MASTER)
  `redis-server --protected-mode no &`
- Iniciar as instâncias do redis nos containers (slaves):
- `redis-server --protected-mode no  --replicaof <IP_MASTER> 6379 &`
- Criar uma chave no `master`
- Consultar a chave nos `slaves`

## Sentinel

```mermaid
graph LR;
    sentinel --> master;
    master --> slave1 & slave2;
```
- O **Sentinel** permite acrescentar alta disponibilidade ao **Redis**
  `redis-sentinel -v`
- Criar uma instância do **Redis** que será o **Sentinel**
- Acessar a pasta home `cd ~`
- Criar um arquivo de configuração `touch sentinel.conf` dentro da instância do **Sentinel**
- Configurar o monitoramento dos nós no arquivo de configuração:
  ```
  port 26379
  sentinel monitor master <IP1> 6379 2
  sentinel down-after-milliseconds master 5000
  sentinel failover-timeout master 5000
  ```
- Iniciar o **Sentinel**
  `redis-sentinel sentinel.conf &`
- Informações sentinel:

  ```
  redis-cli -p 26379
  info sentinel
  sentinel get-master-addr-by-name master
  sentinel replicas master
  ```

- Nos nós slaves alterar o IP para apontar para o master
- Alterar os nomes dos nós nas demais configurações
- Iniciar o master: `redis-server /etc/redis.conf &`
- Iniciar o slave: `redis-server --replicaof redis-node-1 6379 &`
- Verificar log: `tail -f /var/log/redis/sentinel.log &`
- Iniciar o sentinel no master e slave: `redis-sentinel /etc/sentinel.conf &`
- No master, acessar o sentinel: `redis-cli -p 5000`
- Verificar o master atual: `sentinel master mymaster`
- Parar o master: `redis-cli -p 6379 DEBUG sleep 30`

## Clusters

- Criar um arquivo `redis.conf` para cada nó do cluster com a configuração abaixo:
```
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 5000
appendonly yes
```
- O cluster é criado com

```
redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 \
127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 \
--cluster-replicas 1
```
- Conectar em um dos nós do cluster para criar as chaves