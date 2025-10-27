# Cassandra

## Referência
https://cassandra.apache.org/
## Instalação
- [Instalação Cassandra](https://cassandra.apache.org/doc/latest/cassandra/getting_started/installing.html)
## Utilizando Docker
[Docker Playground](https://labs.play-with-docker.com/)
## Preparando o Ambiente
- Efetuar download dos pacotes necessários
```bash
mkdir cassandra
cd cassandra
wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz
wget https://archive.apache.org/dist/cassandra/5.0.0/apache-cassandra-5.0.0-bin.tar.gz
wget https://www.python.org/ftp/python/3.11.9/Python-3.11.9.tgz
```
```bash
cat <<EOF > jvm-server.options
-ea
-XX:+UseThreadPriorities
-XX:+HeapDumpOnOutOfMemoryError
-Xss256k
-XX:+AlwaysPreTouch
-XX:-UseBiasedLocking
-XX:+UseTLAB
-XX:+ResizeTLAB
-XX:+UseNUMA
-XX:+PerfDisableSharedMem
-Djava.net.preferIPv4Stack=true
-Xms1G
-Xmx1G
EOF
```
- Preparar o *Dockerfile*
```bash
cat <<EOF > Dockerfile
FROM debian
WORKDIR /cassandra
COPY *.*gz .
RUN apt-get update
RUN apt-get install -y -y build-essential libssl-dev zlib1g-dev libncurses5-dev \
libnss3-dev libreadline-dev libffi-dev make procps iputils-ping vim
RUN tar -xvf apache-cassandra*
RUN tar -xvf openjdk*
RUN tar -xvf Python-3.11*
RUN rm -f *.*gz
RUN rm apache-cassandra*/conf/jvm-server.options
COPY jvm-server.options apache-cassandra*/conf
ENV JAVA_HOME=/cassandra/jdk-11.0.2
ENV PATH=$PATH:/cassandra/jdk-11.0.2/bin:/cassandra/apache-cassandra-5.0.0/bin:/cassandra/Python-3.11.9
RUN cd Python-3.11.9 && configure --enable-optimizations
RUN cd Python-3.11.9 && make -j$(nproc)
RUN cd Python-3.11.9 && make altinstall
EOF
```
- Criar a imagem
```bash
docker build . -t cassandra_local
```
- Criar uma rede virtual na qual os nós *cassandra* irão se conectar
```bash
docker network create cassandra-net
docker network list
```
- Instanciar o *container*
```bash
docker run -d -p 9042:9042 --hostname cassandra_1 --network cassandra-net --name cassandra_1 cassandra_local tail -f /dev/null
docker ps
docker exec -it cassandra_1 /bin/bash
```
- Caso seja necessário remover o container
```bash
docker stop cassandra_1
docker rm cassandra_1
```
#### Estrutura de diretórios
- Os principais diretórios do *cassandra* são:
    - `bin`: contém os executáveis para iniciar nó (cassandra), verificar configurações (nodetool) interpretar comandos (cqlsh), etc...
    - `conf`: configurações (`cassandra.yaml`)
    - `tools`: ferramentas administrativas adicionais
    - `logs`: arquivos de log do sistema
### Configurações Mínimas Cassandra
- Acessar o diretório de cofigurações `cd apache-cassandra-5.0.0/conf`
- Mover o arquivo de configuração original `mv cassandra.yaml cassandra-original.yaml`
- Criar um novo arquivo
```bash
cat <<EOF > cassandra.yaml
cluster_name: 'MeuCluster'
num_tokens: 16
seed_provider:
  - class_name: org.apache.cassandra.locator.SimpleSeedProvider
    parameters:
      - seeds: "cassandra_1"
listen_address: cassandra_1
rpc_address: localhost
endpoint_snitch: SimpleSnitch
partitioner: org.apache.cassandra.dht.Murmur3Partitioner
data_file_directories:
  - /var/lib/cassandra/data
commitlog_directory: /var/lib/cassandra/commitlog
commitlog_sync: periodic
commitlog_sync_period: 10000ms
saved_caches_directory: /var/lib/cassandra/saved_caches
start_native_transport: true
native_transport_port: 9042
storage_port: 7000
ssl_storage_port: 7001
authenticator: AllowAllAuthenticator
authorizer: AllowAllAuthorizer
EOF
```
- Para iniciar o servidor executar `cassandra -R`
- A interface cliente é o `cqlsh`
- Para interromper o *cassandra* é necessário identificar o `pid` do processo
```bash
ps -ef
kill -9 <PID>
```
#### Container Já Pronto
- Necessário atualizar os parâmetros de inicialização do *java*
```bash
cat <<EOF > jvm-server.options
-ea
-XX:+UseThreadPriorities
-XX:+HeapDumpOnOutOfMemoryError
-Xss256k
-XX:+AlwaysPreTouch
-XX:-UseBiasedLocking
-XX:+UseTLAB
-XX:+ResizeTLAB
-XX:+UseNUMA
-XX:+PerfDisableSharedMem
-Djava.net.preferIPv4Stack=true
-Xms1G
-Xmx1G
EOF
```
- Criar uma nova imagem utilizando uma outra imagem do *cassandra* pré-configurada
```bash
cat <<EOF > Dockerfile
FROM cassandra:latest
RUN rm /opt/cassandra/conf/jvm-server.options
COPY jvm-server.options /opt/cassandra/conf
EOF
```
- Criar a imagem com base no *Dockerfile* acima
```bash
docker build . -t cassandra_local
```
- Para executar apenas um nó
```bash
docker run -d --name cassandra cassandra_local
docker logs -f cassandra
docker exec -it cassandra cqlsh
```
- Para executar mais de um nó
```bash
docker network create cassandra-net
docker run -d --name cassandra_1 --hostname cassandra_1 --network cassandra-net cassandra_local
docker run -d --name cassandra_2 --hostname cassandra_2 --network cassandra-net -e CASSANDRA_SEEDS=cassandra_1 cassandra_local
docker exec -it cassandra_1 nodetool status
```
- Configurações específicas (caso necessário)
```bash
docker run -d --name cassandra_1 --hostname cassandra_1 --network cassandra-net \
  -e CASSANDRA_CLUSTER_NAME=MeuCluster \
  -e CASSANDRA_DC=dc1 \
  -e CASSANDRA_RACK=rack1 \
  -e CASSANDRA_NUM_TOKENS=8 \
  cassandra_local
```
#### Clusters, Nós e Racks
- *Clusters* são as maiores estruturas do *cassandra*
- Cada nó dentro do *cluster* tem conhecimento dos demais dedivo ao protocolo *gossip*
- Podem ser visualizados no arquivo `cassandra.yaml`
- *Datacenters* são grupos de nós e que podem estar distribuídos geograficamente em regiões disintitas (BR, US, etc...)
- Já os *racks* podem ser configurados no arquivo `cassandra-rackdc.properties` e são agrupamentos de nós
- Para alterra o nome do *datacenter* e *rack*:
    - Alterar a propriedade `endpoint_snitch: GossipingPropertyFileSnitch` no arquivo `cassandra.yaml`
    - Definir o nome do *datacenter* e *rack* do arquivo `cassandra-rackdc.properties`
- Nós representam cada instância individual do *cassandra*
- Para visualizar informações sobre os nós
```bash
nodetool describecluster
nodetool status
nodetool info
nodetool netstats
```
#### Gossip
- Para verificar o estado dos demais nós via protocolo *gossip*
```bash
nodetool gossipinfo
```
- Algumas informações importantes:
    - `generation`: indica quanto tempo se passou (*timestamp*) desde que o nó foi iniciado
    - `heartbeat`: quantas vezes enviou um "sinal de vida"
    - `STATUS`: situação atual do nó (alguns status: `NORMAL`,`JOINING`, `LEAVING`, etc...)
#### Keyspaces
- Criar um *keyspace* utilizando a estratégia de replicação `SimpleStrategy` considerando apenas um único nó no cluster
```sql
DESCRIBE KEYSPACES;
CREATE KEYSPACE demo
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
DESCRIBE KEYSPACE demo;
USE demo;
```
- Outra forma de criar o *keyspace* distribuído entre mais de um *datacenter* (só funciona para múltiplos *data-centers*)
```sql
CREATE KEYSPACE vendas
WITH REPLICATION = {
  'class': 'NetworkTopologyStrategy',
  'DC_Brasil': 3,
  'DC_EUA': 2
};
```
- Criar uma tabela para armazenar dados de clientes (com a chave primária `id`)
```sql
CREATE TABLE cliente (
    estado text,
    nome text,
    PRIMARY KEY ((estado), nome)
) WITH CLUSTERING ORDER BY (nome ASC);
```
- Inserir alguns dados:
```sql
INSERT INTO cliente (estado, nome) VALUES ('SP', 'Joao');
INSERT INTO cliente (estado, nome) VALUES ('SP', 'Maria');
INSERT INTO cliente (estado, nome) VALUES ('MG', 'Carlos');
INSERT INTO cliente (estado, nome) VALUES ('MG', 'Ana');
INSERT INTO cliente (estado, nome) VALUES ('RJ', 'Paulo');
```
- Visualizar os *tokens* gerados para cada registro
```sql
SELECT estado, TOKEN(estado) FROM cliente;
```
- Visualizar o intervalo de nós (ele varia de - para + o valor exibido)
```sql
SELECT tokens FROM system.local;
```
- Verificar os *tokens* dentro do *ring*
```bash
nodetool ring
nodetool getendpoints demo clientes 1
```
- Para alterar o número de *tokens* distribuídos entre os nós basta editar o `cassandra.yaml` e alterar o parâmetro `num_tokens: 1` (no caso, será gerado apenas um intervalo de token por nó)
- Se necessário excluir os arquivos de dados do *cassandra*
```bash
rm -rf /var/lib/cassandra/data/*
rm -rf /var/lib/cassandra/commitlog/*
rm -rf /var/lib/cassandra/saved_caches/*
```
#### Partições
- Criar um segundo nó no *cluster*
```bash
docker run -d -p 9142:9042 --hostname cassandra_2 --network cassandra-net --name cassandra_2 cassandra_local tail -f /dev/null
docker ps
docker exec -it cassandra_2 /bin/bash
```
- Acessar o diretório de cofigurações `cd apache-cassandra-5.0.0/conf`
- Mover o arquivo de configuração original `mv cassandra.yaml cassandra-original.yaml`
- Criar um novo arquivo (repare nas configurações `listen_address` e `seeds`)
```bash
cat <<EOF > cassandra.yaml
cluster_name: 'MeuCluster'
num_tokens: 16
seed_provider:
  - class_name: org.apache.cassandra.locator.SimpleSeedProvider
    parameters:
      - seeds: "cassandra_1"
listen_address: cassandra_2
rpc_address: localhost
endpoint_snitch: SimpleSnitch
partitioner: org.apache.cassandra.dht.Murmur3Partitioner
data_file_directories:
  - /var/lib/cassandra/data
commitlog_directory: /var/lib/cassandra/commitlog
commitlog_sync: periodic
commitlog_sync_period: 10000ms
saved_caches_directory: /var/lib/cassandra/saved_caches
start_native_transport: true
native_transport_port: 9042
storage_port: 7000
ssl_storage_port: 7001
authenticator: AllowAllAuthenticator
authorizer: AllowAllAuthorizer
EOF
```
- Iniciar o segundo nó
```bash
cassandra -R
```
- Verificar o estado dos nós
```bash
nodetool status
```
***
### Exercício
- Modelar um banco de dados **Cassandra** para um cenário de compra e venda de ações na bolsa de valores considerando os seguintes atributos:
    - **Acao** (representa uma ação):
        - id_empresa - código (por exemplo, *PETR4*, *ELET3*, *VALE3*, etc...)
        - empresa - nome da empresa
    - **Cotacao** (representa as cotações de uma ação):
        - id_cotacao - código cotação (sequencial)
        - id_empresa - código da empresa (tabela *Acao*)
        - data_hora - data e hora (timestamp) do valor da ação
        - preco - valor da ação
    - **Ordem** (ordem de compra ou venda):
        - id_ordem - código cotação (sequencial)
        - id_empresa - código da empresa (tabela *Acao*)
        - data - data em que a ordem foi incluída
        - tipo - tipo da orgem (V - venda ou C - compra)
        - preco - preço máximo para compra ou preço mínimo para a venda
- Criar um *keyspace* com o nome **bolsa_de_valores** com fator de replicação 1 e utilizando o *SimpleStrategy*
- Criar as tabelas levando em consideração alguns requisitos:
    - Consultar os valores das cotações de uma ação (pelo **id_empresa**) em um determinado dia;
    - Consutlar ordens de compra por empresa, dia e tipo
***
### Tipos de Dados Nativos
- **Cassandra** suporta os seguintes tipos de dados nativos:
    - `ascii`, `text`, `varchar` para texto;
    - `bigint`, `int`, `smallint`, `tinyint`, `varint` para tipos inteiros;
    - `decimal`, `double`, `float` para tipos decimais;
    - `boolean` para *true* / *false*
    - `date`, `time`, `timestamp` para data, hora e *timestamp*
#### Alguns tipos de dados especiais
- Contadores
```sql
CREATE TABLE visitas_por_pagina (
    pagina TEXT PRIMARY KEY,
    total_visitas COUNTER
);
```
- Atualizando contadores (somente podem ser atualizados!)
```sql
UPDATE visitas_por_pagina
SET total_visitas = total_visitas + 1
WHERE pagina = 'index.html';

// ERRO!!!!
INSERT INTO visitas_por_pagina(pagina, total_visitas) VALUES ('erro.html', 10);

SELECT * FROM visitas_por_pagina;
```
- Identificadores únicos
```sql
SELECT now() AS agora FROM system.local;
```
#### Conversões Entre Tipos (Casting)
- Utilizar a expressão `cast (valor as tipo)`
```sql
CREATE KEYSPACE demo
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
USE demo;
CREATE TABLE medida (id int, largura float, altura float, PRIMARY KEY (id));
INSERT INTO medida (id, largura, altura) VALUES (1, 12.234, 15.4343);
SELECT cast(largura as int) AS largura_int, cast(altura as int) AS altura_int FROM medida;
```
#### Built-in Functions
- Obter o *timestamp*, data e hora atuais
```sql
SELECT current_timestamp() AS agora FROM system.local;
SELECT current_date() AS agora FROM system.local;
SELECT current_time() AS agora FROM system.local;
```
- Gerar identificadores únicos (`uuid`)
```sql
SELECT uuid() FROM system.local;
```
- Mascarar dados (habilitar o `dynamic_data_masking_enabled: true`)
```sql
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome TEXT,
    email TEXT MASKED WITH mask_email()
);

INSERT INTO usuarios (id, nome, email) VALUES (uuid(), 'Edson Sensato', 'edson.sensato@example.com');

SELECT nome, email FROM usuarios;

SELECT mask_null(email), mask_default(email) FROM usuarios;
```
#### Trabalhando com Data / Hora
- Existem várias formas de utilizar data / hora em *CQL*
```sql
CREATE TABLE log_acesso (
    data DATE,
    hora TIME,
    usuario TEXT,
    PRIMARY KEY (data, hora)
);

INSERT INTO log_acesso(data, hora, usuario) values ('2025-10-01', '05:15:00', 'usuario1');
INSERT INTO log_acesso(data, hora, usuario) values ('2025-10-01', '07:00:00', 'usuario2');
INSERT INTO log_acesso(data, hora, usuario) values ('2025-10-15', '05:15:00', 'usuario3');
INSERT INTO log_acesso(data, hora, usuario) values ('2025-10-16', '05:15:00', 'usuario1');
INSERT INTO log_acesso(data, hora, usuario) values ('2025-10-01', '08:00:00', 'usuario2');
INSERT INTO log_acesso(data, hora, usuario) values ('2025-10-02', '09:30:00', 'usuario3');

SELECT * FROM log_acesso WHERE data = '2025-10-01';
SELECT * FROM log_acesso WHERE data = '2025-10-01' AND hora = '05:15:00';
```
***
### Materialized Views
- São visualizações adicionais criadas a partir de consultas a tabelas já existentes
- Devem ser habilitadas no `cassandra.yaml` no parâmetro `materialized_views_enabled`
```bash
docker cp cassandra:/opt/cassandra/conf/cassandra.yaml .
```
- Editar o arquivo `cassandra.yaml` para definir `materialized_views_enabled: true`
- Copiar de volta o arquivo para o *container*    
```bash
docker cp cassandra.yaml cassandra:/opt/cassandra/conf/cassandra.yaml
```
- Reiniciar o *container*
```bash
docker stop cassandra
docker start cassandra
```
- Por exemplo, uma tabela que armazena produtos tendo como chave o `id_produto` somente permite consultas por este campo
```sql
CREATE TABLE produto (
    id UUID,
    descricao TEXT,
    fornecedor TEXT,
    cor TEXT,
    PRIMARY KEY (id)
);

INSERT INTO produto (id, descricao, fornecedor, cor) VALUES (uuid(), 'Caneta para desenho', 'Canetus Inc', 'Azul');
INSERT INTO produto (id, descricao, fornecedor, cor) VALUES (uuid(), 'Caneta para desenho', 'Canetus Inc', 'Verde');
INSERT INTO produto (id, descricao, fornecedor, cor) VALUES (uuid(), 'Pincel para pintura', 'Pincelus Inc', 'Amarelo');
INSERT INTO produto (id, descricao, fornecedor) VALUES (uuid(), 'Borracha', 'Apagus Inc');
```
- Criar uma *materialized view* que permita a consulta pela cor do produto
```sql
CREATE MATERIALIZED VIEW produto_cor AS
   SELECT * FROM produto
   WHERE cor IS NOT NULL
   AND id IS NOT NULL
   PRIMARY KEY (cor, id);

CREATE MATERIALIZED VIEW produto_fornecedor AS
   SELECT * FROM produto
   WHERE fornecedor IS NOT NULL
   AND id IS NOT NULL
   PRIMARY KEY (fornecedor, id);

SELECT * FROM produto_cor WHERE cor = 'Amarelo';

SELECT * FROM produto_fornecedor WHERE fornecedor = 'Canetus Inc';
```
***
### Triggers
- **Cassandra** permite a criação de *triggers* implementadas em *Java*
- Por exemplo, considerar uma tabela de produtos em estoque que deve ser atualizada com base nos pedidos de determinados produtos
- Criar a estrutura de tabelas:
```sql
CREATE KEYSPACE loja
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE loja;

CREATE TABLE estoque (
    id_produto TEXT PRIMARY KEY,
    total INT
);

CREATE TABLE pedido (
    id UUID PRIMARY KEY,
    id_produto TEXT,
    descricao TEXT,
    data_venda TIMESTAMP,
    quantidade INT
);
```
- Criar a classe *java* para implementar a *trigger*
```java
package org.apache.cassandra.triggers;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.partitions.Partition;
import org.apache.cassandra.db.rows.Row;
import org.apache.cassandra.db.rows.Cell;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.schema.TableMetadata;
import org.apache.cassandra.triggers.ITrigger;

import com.google.common.collect.Lists;

public class TriggerAtualizaEstoque implements ITrigger {

    @Override
    public Collection<Mutation> augment(Partition update) {
        TableMetadata meta = update.metadata();

        // Somente executa se for a tabela "pedido" do keyspace "loja"
        if (!meta.keyspace.equals("loja") || !meta.name.equals("pedido"))
            return Collections.emptyList();

        Row row = update.unfilteredIterator().next().row();

        // Obtém os valores do registro inserido
        Cell<?> cellProduto = row.getCell(meta.getColumn("id_produto"));
        Cell<?> cellQuantidade = row.getCell(meta.getColumn("quantidade"));

        if (cellProduto == null || cellQuantidade == null)
            return Collections.emptyList();

        String idProduto = UTF8Type.instance.compose(cellProduto.value());
        int quantidade = Int32Type.instance.compose(cellQuantidade.value());

        // Cria mutação para atualizar o estoque
        Mutation mut = Mutation.simpleBuilder("loja", "estoque")
                .update()
                .where("id_produto", idProduto)
                .increment("total", -quantidade) // subtrai a quantidade do estoque
                .build();

        return Collections.singletonList(mut);
    }
}
```
- A classe deve ser compilada e o `.class` gerado deve ser copiado para `$CASSANDRA_HOME/triggers/`
- Utilizar as bibliotecas do diretório `$CASSANDRA_HOME/lib/*`
```bash
javac -cp "$CASSANDRA_HOME/lib/*" TriggerAtualizaEstoque.java
```
- Criar a *trigger* na tabela `pedido`
```sql
USE loja;

CREATE TRIGGER trg_atualiza_estoque
ON pedido
USING 'org.apache.cassandra.triggers.TriggerAtualizaEstoque';
```
- Reiniciar o **Cassandra**
***
### User-Defined Functions (UDFs)
- Além das *triggers* também é possível criar funções customizadas no **Cassandra**
- Necessário habilitar a permissão `enable_user_defined_functions: true` no `$CASSANDRA_HOME/conf/cassandra.yaml`
- Por exemplo, uma função para descrever o nível de estoque dos produtos conforme o critério:
    - Menos de 100 - BAIXO
    - Entre 101 e 500 - MÉDIO
    - Acima de 500 - ALTO
- Criar a função
```sql
CREATE OR REPLACE FUNCTION nivel_estoque(qtd int)
RETURNS NULL ON NULL INPUT
RETURNS text
LANGUAGE java
AS $$
    if (qtd < 100)
        return "BAIXO";
    else if (qtd < 200)
        return "MÉDIO";
    else
        return "ALTO";
$$;
```
- Testar a função
```sql
SELECT nivel_estoque(total) AS nivel, total FROM estoque;
```
***
### Funções Agregadoras
- **Cassandra** possui algumas funções de agregação de dados como `count`, `min`, `max`, `sum` e `avg`
- Pode-se criar uma função agregadora personalizada também
```sql
CREATE OR REPLACE FUNCTION state_avg(state tuple<int, int>, val int)
CALLED ON NULL INPUT
RETURNS tuple<int, int>
LANGUAGE javascript AS
$$
    if (state == null) state = new Tuple(0, 0);
    if (val != null) {
        state.set(0, state.get(0) + val); // soma
        state.set(1, state.get(1) + 1);   // contador
    }
    state;
$$;

CREATE OR REPLACE FUNCTION final_avg(state tuple<int, int>)
CALLED ON NULL INPUT
RETURNS double
LANGUAGE javascript AS
$$
    if (state == null || state.get(1) == 0) return null;
    state.get(0) / state.get(1);
$$;

CREATE OR REPLACE AGGREGATE avg_udf(int)
SFUNC state_avg
STYPE tuple<int, int>
FINALFUNC final_avg
INITCOND (0, 0);

SELECT avg_udf(quantidade) AS media_quantidade FROM vendas;
```
***
### Integração Nodejs
- **Cassandra** pode ser facilmente integrado com projetos desenvolvidos em diversas linguagens, por exemplo, com *nodejs*
- Para isso, basta adicionar ao projeto o *driver* necessário e efetuar as chamadas ao **Cassandra** via *API*
- Instalar o *nodejs* e o *npm*
```bash
apk add nodejs npm
```bash
- Criar um projeto *nodejs* e adicionar as dependências
```bash
mkdir node-cassandra-sensor
cd node-cassandra-sensor
npm init -y
npm install express cassandra-driver
```
- Conectar-se ao *Cassandra* via *cqlsh*
```bash
docker exec -it cassandra cqlsh
```
- Configurar o banco de dados
```sql
CREATE KEYSPACE IF NOT EXISTS sensores
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

USE sensores;

CREATE TABLE IF NOT EXISTS dados (
  sensor text,
  timestamp timestamp,
  temperatura double,
  PRIMARY KEY (sensor, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);
```

- Criar a conexão com o **Cassandra** em um arquivo `cassandra.js`
```bash
touch cassandra.js
```
- Obter o *IP* do *container* executando o **Cassandra**
```bash
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' cassandra
```
- Incluir o seguinte conteúdo ao arquivo
```javascript
const cassandra = require('cassandra-driver');

const client = new cassandra.Client({
  contactPoints: ['IP_CONTAINER'], // Substituir o IP_CONTAINER!!!!
  localDataCenter: 'datacenter1', // Nome do seu data center
  keyspace: 'sensores'
});

module.exports = client;
```
- Criar a implementação do servidor
```bash
touch index.js
```
- Incluir o seguinte conteúdo ao arquivo
```javascript
const express = require('express');
const client = require('./cassandra');

const app = express();
const PORT = 3000;

// Middleware para ler JSON
app.use(express.json());

// Endpoint POST para receber dados do sensor
app.post('/dados', async (req, res) => {
  try {
    const { sensor, temperatura } = req.body;

    if (!sensor || temperatura === undefined) {
      return res.status(400).json({ erro: 'Campos sensor e temperatura são obrigatórios.' });
    }

    const timestamp = new Date();

    const query = 'INSERT INTO dados (sensor, timestamp, temperatura) VALUES (?, ?, ?)';
    const params = [sensor, timestamp, temperatura];

    await client.execute(query, params, { prepare: true });

    res.json({ mensagem: 'Dados inseridos com sucesso!', sensor, temperatura, timestamp });
  } catch (err) {
    console.error('Erro ao inserir dados:', err);
    res.status(500).json({ erro: 'Falha ao inserir dados no Cassandra.' });
  }
});

// Inicializa servidor
app.listen(PORT, () => {
  console.log(`Servidor rodando em http://localhost:${PORT}`);
});
```
- Iniciar o servidor (executando na porta **3000**)
```bash
node index.js
```
- Efetuar um teste simples via *curl* (obter a *URL* no *docker play*)
```bash
curl http://ip172-18-0-56-d3vtmic69qi000cqlahg-3000.direct.labs.play-with-docker.com:3000/ \
  -H "Content-Type: application/json" \
  -d '{"sensor":"ESP32","temperatura":27.4}'
```
- Criar um *endpoint* para exibir as temperaturas já lidas
```javascript
// Endpoint GET para listar todos os dados armazenados
app.get('/dados', async (req, res) => {
  try {
    const query = 'SELECT sensor, timestamp, temperatura FROM dados';
    const result = await client.execute(query);

    // Mapeia os resultados em um formato legível
    const dados = result.rows.map(row => ({
      sensor: row.sensor,
      temperatura: row.temperatura,
      timestamp: row.timestamp
    }));

    res.json(dados);
  } catch (err) {
    console.error('Erro ao buscar dados:', err);
    res.status(500).json({ erro: 'Falha ao consultar dados no Cassandra.' });
  }
});
```
- Criar um emulador **Arduino** [Woki - ESP 32](https://wokwi.com/projects/new/esp32) com um sensor de temperatura (**DS 18B20**) para enviar dados ao *endpoint* criado acima
```c++
#include <OneWire.h>
#include <DallasTemperature.h>
#include <WiFi.h>
#include <HTTPClient.h>

#define TEMPLATE "{\"sensor\":\"ESP32\",\"temperatura\":%d}"

OneWire oneWire(15);
DallasTemperature sensor(&oneWire);

void setup(void) {

  Serial.begin(115200);
  Serial.print("Conectando-se ao Wi-Fi");
  // Wokwi simula uma rede WiFi com acesso total à Internet com o usuário Wokwi-GUEST
  // não precisa de senha
  WiFi.begin("Wokwi-GUEST", "", 6);
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
  Serial.println(" Conectado!");
  Serial.println(WiFi.localIP());

  delay(2);
  sensor.begin();
  delay(20);

}

void loop() {

  // Realizar a requisição POST
  if (WiFi.status() == WL_CONNECTED) {

    sensor.requestTemperatures();
    Serial.print("Temperature is: ");
    delay(10);
    Serial.println(sensor.getTempCByIndex(0));
    delay(1000);

    HTTPClient http;

    // Defina o URL do servidor que receberá a requisição POST
    http.begin("http://ip172-18-0-56-d3vtmic69qi000cqlahg-3000.direct.labs.play-with-docker.com:3000/dados"); // Substitua pela URL do servidor

    // Defina o tipo de conteúdo (JSON, neste caso)
    http.addHeader("Content-Type", "application/json");

    // Dados JSON que serão enviados
    int temp = sensor.getTempCByIndex(0);
    char postData[100];
    // Copia a temperatura para o %d definido no template (TEMPLATE)
    sprintf(postData, TEMPLATE, temp);

    // Realiza a requisição POST
    int httpResponseCode = http.POST(postData);

    // Verifica a resposta do servidor

    if (httpResponseCode > 0) {
      String response = http.getString();  // Obtém a resposta
      Serial.println("Resposta do servidor: " + response);
    } else {
      Serial.println("Erro na requisição POST: " + httpResponseCode);
    }

    http.end();  // Fecha a conexão
  } else {
    Serial.println("Falha na conexão Wi-Fi");
  }

}
```
