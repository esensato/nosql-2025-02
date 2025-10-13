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
- Para executar apenas um nó
```bash
docker run -d --name cassandra cassandra:latest
docker logs -f cassandra
docker exec -it cassandra cqlsh
```
- Para executar mais de um nó
```bash
docker network create cassandra-net
docker run -d --name cassandra_1 --hostname cassandra_1 --network cassandra-net cassandra:latest
docker run -d --name cassandra_2 --hostname cassandra_2 --network cassandra-net -e CASSANDRA_SEEDS=cassandra_1 cassandra:latest
docker exec -it cassandra_1 nodetool status
```
- Configurações específicas (caso necessário)
```bash
docker run -d --name cassandra_1 --hostname cassandra_1 --network cassandra-net \
  -e CASSANDRA_CLUSTER_NAME=MeuCluster \
  -e CASSANDRA_DC=dc1 \
  -e CASSANDRA_RACK=rack1 \
  -e CASSANDRA_NUM_TOKENS=8 \
  cassandra:latest
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
CREATE KEYSPACE demo
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
DESCRIBE KEYSPACE demo;
USE demo
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
        - data_hora - data e hora (timestamp) do valor da ação
        - tipo - tipo da orgem (V - venda ou C - compra)
        - preco - preço máximo para compra ou preço mínimo para a venda
- Criar um *keyspace* com o nome **bolsa_de_valores** com fator de replicação 1 e utilizando o *SimpleStrategy*
- Criar as tabelas levando em consideração alguns requisitos:
    - Consultar os valores das cotações de uma ação em um determinado dia;
    - Consutlar ordens de compra por empresa, dia e tipo

        