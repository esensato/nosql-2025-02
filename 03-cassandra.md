# Cassandra

## Referência

https://cassandra.apache.org/

## Conceitos

- É um banco de dados baseado em colunas

## Instalação

- [Instalação Cassandra](https://cassandra.apache.org/doc/latest/cassandra/getting_started/installing.html)

## Utilizando Docker

[Docker Playground](https://labs.play-with-docker.com/)

## Preparando o Ambiente

- Criar uma imagem baseada no centOS

`docker pull ubuntu`

- Criar uma rede local
```bash
docker network create cassandra-net
docker network list
```
- Iniciar um `container`
```bash
docker run -it -p 9042:9042 -p 7000:7000 -p 7199:7199 --name cassandra1 --network cassandra-net ubuntu
```
- Atualizar o repositório do `yum`
```bash
apt upgrade -y
apt install openjdk-11-jdk -y
```
#### Criar a imagem com o *Dockerfile*
- Efetuar download dos pacotes necessários
```bash
mkdir cassandra
cd cassandra
wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz
wget https://archive.apache.org/dist/cassandra/5.0.0/apache-cassandra-5.0.0-bin.tar.gz
```
- Preparar o *Dockerfile*
```bash
cat <<EOF > Dockerfile
FROM debian
WORKDIR /cassandra
COPY *.gz .
RUN tar -xvf apache-cassandra*
RUN tar -xvf openjdk*
RUN rm -f *.gz
RUN apt-get install -y iputils-ping procps unzip wget gcc make ruby-dev
ENV JAVA_HOME=/cassandra/jdk-11.0.2
ENV PATH=$PATH:/cassandra/jdk-11.0.2/bin:/cassandra/apache-cassandra-5.0.0/bin
EOF
```
- Criar a imagem
```bash
docker build . -t cassandra
```
- Instanciar o *container*

## Instalando as Dependências

```bash
wget https://www.python.org/ftp/python/3.11.4/Python-3.11.4.tgz
tar -xvf Python-3.11.4.tgz
cd Python-3.11.4
./configure --enable-optimizations
make install
```

`yum install -y python3`


- Verificando a instalação
```bash
cassandra -v
```
 
## Configuração Java JRE

- Efetuar um *backup* do arquivo de configuração original e criar um novo para substituí-lo

    - Acessar o diretório `apache-cassandra-4.1.3/conf`
    - Mover o arquivo original `mv jvm-server.options jvm-server-original.options`
    - Criar um novo arquivo `vi jvm-server.options`
    - Copiar o código abaixo
```bash
cat <<EOF > jvm-server.options
-ea
-da:net.openhft...
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

## Iniciando o servidor

- Acessar o diretório `apache-cassandra-4.1.3/bin`
- Executar `./cassandra -R`