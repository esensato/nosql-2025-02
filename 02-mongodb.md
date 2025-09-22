# MongoDB

## Referência

https://www.mongodb.com/

## Conceitos

- É um banco de dados orientado a documentos
- Projetado para proporcional escalabilidade horizontal (scale out), isto é, ao invés de investir recursos aumentando a capacidade de um servidor, novos computadores (nós) são adicionados ao cluster
- Escalabilidade transparente para a aplicação / desenvolvedor à o próprio MongoDB cuida disso
- Apresenta alta performance
- Recursos de indexação, replicação, uso de javascript nativo no lado servidor, consultas com expressões regulares, etc...
- Dados estruturados em **Documentos** agrupados em **Coleções**

```mermaid
graph TD;
    banco_dados-->collection;
    collection-->document;
```
## Instalação local
- Instruções para [Instalação do MongoDB](https://docs.mongodb.com/manual/installation/#mongodb-community-edition-installation-tutorials) localmente
## Inslatando no Linux (Alpine)

- [Docker Playground](https://labs.play-with-docker.com/)
- Executar a instalação dos repositórios e **MongoDB**
```bash
echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/main' >> /etc/apk/repositories
echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/community' >> /etc/apk/repositories
apk update
apk add mongodb mongodb-tools
```
- O servidor **MongoDB** é o *mongod* e o cliente é o *mongo*
- Também pode utilizada uma interface gráfica como cliente chamada [Compass](https://www.mongodb.com/products/tools/compass)
```mermaid
graph TD;
    mongod-->mongosh;
    mongod-->compass;
```
## Iniciando o servidor

- Criar um diretório para conter o banco de dados, por exemplo:
```bash
mkdir ~/mongodb
```
- Iniciar o servidor
```bash
mongod --dbpath /root/mongodb --fork --logpath /dev/null --bind_ip_all
```
- Verificar estatísticas
```bash
mongostat
```
- Verificar leituras e escritas em *collections*
```bash
mongotop
```
- Acessar o cliente
```bash
mongo
```
- Cliente aceita código em *javascript*
```javascript
function mensagem() {
    return "Alo Mongodb!!!"
}

mensagem()
``` 
## Comandos Básicos no Mongo (cliente)
- `help`: exibe descrição dos comandos
- `cls`: limpa a tela
- `show databases`: lista os bancos de dados
- `exit`: sair do mongo
- Para finaizar o servidor a partir do cliente (`mongo`)
```bash
use admin
db.shutdownServer()
```
- Criar / utilizar um banco de dados
```javascript
use imobiliaria
show collections
```
- Criar uma **collection** e um **documento**
```javascript
db.tipo_imovel.insertOne({nome: "Residencial"})
db.tipo_imovel.find()
db.tipo_imovel.findOne()
```
## _id
- Atributo `_id` deve ser obrigatório para cada documento
- Deve ser uma sequência de 12 bytes hexadecimais (24 caracteres)
- Pode-se fornecer um `_id` explicitamente ou ele pode ser gerado automaticamente (caso não seja fornecido explicitamente)
- `ObjectId` encapsula o `_id`
- Função `toString()` retorna a *String* representada pelo `ObjectId`
- Função `getTimestamp()` retorna o *timestamp* de um `ObjectId`

## Tipos de Dados

- null Valores nulos
- true / false Boolean
- 3.14, 3 Números
- "Teste" String
- `new Date()` / `ISODate()` (sem o new) Date
- `new Date("<YYYY-mm-dd>")` / `ISODate("<YYYY-mm-dd>")` (sem o new)  Date
- `ObjectId()` Chave única (_id)
- [1,2,3] Array
- {"novo": "documento"} Documento embutido - embedded

## Operações Sobre Datas
- `new Date().getUTCFullYear()`
- `new Date().getUTCMonth()`
- `new Date().getUTCDate()`
- `new Date().getUTCHours()`
- `new Date().getUTCMinutes()`
- `new Date().getUTCSeconds()`

# Operações

- Inserir mais de um documento:

```javascript
db.tipo_imovel.insertMany([{nome: "Comercial"}, {nome: "Temporada"}])
db.tipo_imovel.find().pretty()
```
- Atualizando um documento:

```javascript
db.tipo_imovel.updateOne({_id: ObjectId('64f888e0825db8d91dcfcb7f')}, {$set: {nome: "Air B&B"}})
```
- `$set` cria / atualiza um campo em um documento `$unset` remove um campo
- `$inc` incrementa o valor de um campo numérico (pode ser negativo também, decrementando)
```javascript
db.imovel.insertOne({endereco: "Rua Leste, 152"})
db.imovel.updateOne({endereco: "Rua Leste, 152"}, {$set:{quartos:1}})
db.imovel.updateOne({ endereco: "Rua Leste, 152" }, { $inc: {"quartos":1}})
db.imovel.updateOne({endereco: "Rua Leste, 152"}, {$unset:{quartos:-1}})
```
- Um documento pode ser substituído integralmente por outro com o `replaceOne({filtro}, novo_doc)`
```javascript
db.imovel.replaceOne({endereco: "Rua Leste, 152"}, {endereco: "Rua Oeste, 155", cidade: "São Paulo"})
db.imovel.find().pretty()
```
- A exclusão de um documento se dá com `deleteOne` passando um seletor como parâmetro Para excluir mais de um ao mesmo tempo utilizar o `deleteMany({filtro})`
- Uma coleção inteira pode ser excluída por meio da função `drop()`
```javascript
db.imovel.deleteOne({endereco: "Rua Oeste, 155"})
db.imovel.find().pretty()
show collections
db.imovel.drop()
```
## Operações com Arrays

- *Arrays* são suportados nativamente como um tipo de dados
```javascript
db.imovel.insertOne({endereco: "Rua Oeste, 155", comodos: ["sala", "quarto 1", "quarto 2"]})
db.imovel.find().pretty()
```
- `$push` cria / adiciona elementos a um array
- `$each` modificador de `$push` que permite incluir mais de um elemento no array
```javascript
db.imovel.updateOne({endereco: "Rua Oeste, 155"}, {$push: {comodos: "banheiro social"}})
db.imovel.updateOne({endereco: "Rua Oeste, 155"}, {$push: {comodos: {$each: ["cozinha", "banheiro serviço"]}}})
db.imovel.find().pretty()
```
- `$addToSet` é semelhante ao `$push` mas previne que valores duplicados sejam inseridos no array
- `$pull` remove um elemento do array baseado em um critério (item dentro do *array*)
- `$pop` também pode ser utilizado para remover elementos do final (`{"key": 1}`) ou início (`{"key": -1}`) do *array*, onde *key* indica o nome do atributo
```javascript
db.imovel.updateOne({endereco: "Rua Oeste, 155"}, {$pull: {comodos: "banheiro social"}})
db.imovel.find().pretty()
db.imovel.updateOne({endereco: "Rua Oeste, 155"}, {$pop: {comodos: -1}})
```
- Documentos podem ser definidos como atributos de outros documentos
    ```javascript
    db.imovel.updateOne({endereco: "Rua Oeste, 155"}, {$set: {metragem: {largura: 40, profundidade: 60}}})
    db.imovel.find().pretty()
    ```
## Carregar Documentos em Lote
- Para carregar documentos em lote utilizar o `mongoimport`
- Utilizar o arquivo [imoveis.json](https://github.com/esensato/nosql-2025-02/blob/main/imoveis.json) como base de dados
```bash
wget https://raw.githubusercontent.com/esensato/nosql-2025-02/refs/heads/main/imoveis.json
mongoimport --db imobiliaria --collection imovel --file imoveis.json
```
- Outro exemplo para importar dados de universidades brasileiras
```bash
wget http://universities.hipolabs.com/search?country=brazil -O universidades.json
mongoimport --db universidades --collection brasil --file universidades.json --jsonArray
```
- É possível exportar coleções inteiras para serem importadas em outros bancos de dados
```bash
mongoexport --collection imovel --db imobiliaria --out imobiliaria.json
```
- Para exportar um banco de dados completo, com todas as coleções
```bash
mongodump --db imobiliaria
```
- Para importar
```bash
mongorestore dump/imobiliaria/imobiliaria.bson
```
## Consultas Básicas

- A forma mais simples de efetuar consultas ao MongoDB é por meio do `find()`
- Na sua forma mais simples o `find()` retorna todos os documentos de uma coleção
- Um filtro pode ser fornecido como parâmetro na forma de chave: valor
- Mais de uma chave: valor pode ser fornecida e são tratadas como um **AND**
```javascript
db.imovel.find({tipo: "comercial"})
db.imovel.find({tipo: "comercial", endereco: "Rua Júpiter 441"})
```
- Os resultados podem ser orderados por `sort` (1 ordem crescente e -1 decrescente)
```javascript
db.imovel.find({tipo: "comercial"}).sort({endereco: 1})
```
- É possível selecionar determinados campos e excluir alguns no resultado da consulta
- Os campos são definidos no segundo parâmetro do `find()` onde o **1** indica a inclusão e **0** a exclusão
```javascript
db.imovel.find({tipo: "temporada"},{_id: 0, lazer: 1})
```
- Critérios podem ser fornecidos como filtros para as consultas: 
    - `$lt` <
    - `$lte` <=
    - `$gt` >
    - `$gte` >= 
    - `$ne` <>
    - `$in`(`$nin`) permite incluir / excluir um conjunto de valores na consulta de um campo 
    - `$or` define cláusulas inclusivas na consulta (operação OR)
    
```javascript
db.imovel.find({"configuracao.quartos": {$gt: 3}})
db.imovel.find({$or:[{"configuracao.quartos": {$eq: 1}}, {"configuracao.quartos": {$gt: 2}}]})
db.imovel.find({"configuracao.quartos": {$in: [1,3]}})
```
- As consultas permitem que os critérios de filtros sejam especificados por meio de expressões regulares compatíveis com Pearl [PCRE](https://www.pcre.org/)
- Dica: para [testar expressões regulares](https://regex101.com/)
- Para utilizar expressões regulares como critério basta especificar a expressão em `$regex`:
```javascript
db.imovel.find({endereco: {$regex: "^Rua Urano"}})
```
## Consultas Arrays

- `db.imovel.find({lazer: "piscina"})`: retorna todos os registros onde o *array* lazer possua ao menos um elemento
"piscina"
- `db.imovel.find({lazer:{$all:["academia", "piscina"]}})`: retorna todos os registros onde o *array* lazer possua "academia" **E** "piscina" (ao menos)
- `db.imovel.find({"lazer":["academia", "piscina"]})`: retorna todos os registros onde o *array* lazer possua **EXATAMENTE** "academia" e "piscina" (nesta ordem)
- `db.imovel.find({"lazer.0":"jardim"})`: busca por índice no array, retornando documentos onde o primeiro item de lazer na lista (índice 0) seja "jardim"
- `db.imovel.find({"lazer":{$size: 2}})`: retorna documentos que possuam dois elementos no array lazer

## Exercícios
- Realizar as seguintes consultas sobre a coleção de documentos `imovel`
    - Encontrar todos os imóveis com "churrasqueira" no campo "lazer"
    - Listar todos os "endereços" de imóveis na "cidade" de "Porto Alegre"
    - Encontrar imóveis onde o número de quartos seja maior que 2 ordenado por "cidade"
    - Encontrar imóveis com "aluguel" superior a 1000 e que possuam "jardim"
    - Encontrar imóveis que possuam "churrasqueira" e que tenham aluguel menor que 1500    
    - Encontrar imóveis cujo "bairro" contenha "Por" no início do nome
    - Encontrar todos os imóveis com "piscina" e "academia" no campo lazer com aluguel menor do que 1200
    - Encontrar imóveis que possuam "churrasqueira" no campo lazer ou que tenham aluguel menor que 1500
## Indices e Performance
- O plano de execução que uma consulta pode ser visualizado por `explain` acompanhado da consulta
```javascript
db.imovel.find({"aluguel": {$gte: 4500}})
db.imovel.explain().find({"aluguel": {$gte: 4500}})
```
- Estágios (*stages*)
    - `COLLSCAN`: varredura completa da coleção (collection scan)
    - `IXSCAN`: varredura de índice (index scan)
    - `FETCH`: busca do documento após índice
- Na consulta acima está sendo realizada uma busca por `COLLSCAN`, isto é, a consulta está varrendo a coleção inteira e pode ser lento em coleções grandes
- Para visualizar as estatísticas da execução:
```javascript
db.imovel.explain("executionStats").find({"aluguel": {$gte: 4500}})
```
- Observar que `totalDocsExamined` é bem maior que `nReturned` indicando que o filtro é neficiente
- Criar um índice (1 indica ordem crescente e -1 decrescente)
```javascript
db.imovel.createIndex({"aluguel": 1})
```
- Pode-se criar índices compostos por mais de um campo
- No caso dos índices compostos a ordem dos mesmos é levada em consideração no plano de execução
- Também é possível criar um índice parcial que será aplicado somente a uma condição específica
- No caso abaixo, o índice somente será aplicado quando o valor do aluguem for maior do que 4500 na pesquisa
```javascript
db.imovel.getIndexes()
db.imovel.dropIndex("aluguel_1")
db.imovel.createIndex({"aluguel": 1}, {"partialFilterExpression":{"aluguel":{$gte: 4500}}})
```
- Existindo mais de um índice para uma **collection** é possível forçar o uso de um índice específico ao executar um `find` por meio da função `hint` que recebe como parâmetro o nome do índice a ser utilizado
```javascript
db.imovel.find({ aluguel: { $gte: 4500 } }).hint("aluguel_1")
```
## Agregações

- [Agregações](https://www.mongodb.com/pt-br/docs/manual/aggregation/) permitem realizar várias operações sobre uma coleção
- Por exemplo, retornar o total de valores de aluguel por tipo de imóvel
```javascript
db.imovel.aggregate([
    {$group:{_id: "$tipo", total:{$sum: "$aluguel"}}}
])
```
- Outro exemplo: obter os tipos de imóvel por cidade
```javascript
db.imovel.aggregate([{
    $group: {
      _id: "$cidade", 
      tiposUnicos: { $addToSet: "$tipo" }
    }
  }])
```
- Algumas agregações possíveis
    - `$sum` soma 
    - `$avg` média
    - `$max` máximo
    - `$min` mínimo
    - `$first` primeiro item 
    - `$last` último item
- Um estágio de ordenação dos dados pode ser definido por `$sort` que recebe o atributo e 1 (crescente) ou -1 (descendente)
- Com o `$project` define-se quais atributos serão considerados
```javascript
db.imovel.aggregate([
    {$project:{_id: 0, endereco: 1, aluguel: 1}},
    {$sort:{aluguel: -1, endereco: 1}}
])
```
- Novos estágios podem ser adicionados por exemplo para filtrar os documentos com o `$match`
```javascript
db.imovel.aggregate([
    {$match:{lazer: "piscina"}},
    {$project:{_id: 0, endereco: 1, aluguel: 1}},
    {$sort:{aluguel: -1, endereco: 1}}
])
```
- Contadores podem ser utilizados para totalizar resultados com filtros aplicados utilizando o `$count`
```javascript
db.imovel.aggregate([
    {$match:{lazer: "piscina"}},
    {$count: "total_piscina"}
])
```
- É possível converter arrays em simples objetos com o `$unwind`
```javascript
db.imovel.aggregate([
    {$unwind: "$lazer"},
    {$group:{_id: "$lazer", total:{$sum: "$aluguel"}}}
])
```
- Para cálculos um pouco mais complexos pode-se criar variáveis (`$let`) para armazear valores inermediários como resultado de cálculos
- Por exemplo, obter o total de cômodos por unidade
```javascript
db.imovel.aggregate([
  {
    $project: {
      endereco: 1,
      cidade: 1,
      totalComodos: {
        $let: {
          vars: {
            q: "$configuracao.quartos",
            b: "$configuracao.banheiros",
            v: "$configuracao.vagas"
          },
          in: { $add: ["$$q", "$$b", "$$v"] }
        }
      }
    }
  }
])
```
- Também é possível criar coleções que resumem informações, por exemplo, a média dos valores de aluguel por cidade e armazená-las em uma outra coleção (`$out`)
```javascript
db.imovel.aggregate([
  {
    $group: {
      _id: "$cidade",
      mediaAluguel: { $avg: "$aluguel" }
    }
  },
  {
    $out: "media_aluguel_por_cidade"
  }
])
```
- Inserir uma nova cidade e executar novamente a agregação anterior para atualizar a `media_aluguel_por_cidade`
```javascript
db.imovel.insertOne({
    "endereco": "Rua Urano 830",
    "bairro": "Brasil",
    "cidade": "Maceió",
    "tipo": "temporada",
    "aluguel": 1000})
```
- Outra opção interessante é o `$lookup` que permite realizar *joins* entre **collections**
```javascript
$lookup:{   from:"Nome coleção destino",
            localField: "Nome do campo local (origem)",
            foreignField: "Nome do campo join (destino)",
            as: "Nome do atributo na coleção origem onde os resultados serão adicionados"
}
```
- Criar uma coleção para conter alguns valores de IPTU para os imóveis utilizando uma função *javascript*
```javascript
let imoveis = db.imovel.find({}, { _id: 1 }).limit(10).toArray();

let iptuDocs = imoveis.map(imovel => ({
    id_imovel: imovel._id,
    ano: 2025,
    valor: Math.floor(Math.random() * 5000) + 500
}));

db.iptu.insertMany(iptuDocs);
```
- Utilizar o `$lookup` para realizar o *join* entre as coleções
```javascript
db.imovel.aggregate([
    {
        $lookup: {
            from: "iptu",
            localField: "_id",
            foreignField: "id_imovel",
            as: "iptu_info"
        }
    },
    {
        $match: {
            iptu_info: { $ne: [] }
        }
    },
    {
        $project: {
            endereco: 1,
            bairro: 1,
            cidade: 1,
            tipo: 1,
            aluguel: 1,
            "iptu_info.valor": 1
        }
    }
]).pretty();
```
## Exercícios
- Realizar as seguintes consultas com o `aggregate`
    - Contar quantos imóveis têm mais de 2 banheiros
    - Agrupar os imóveis por tipo e calcular o aluguel médio de cada tipo
    - Contar o número de imóveis em cada bairro e ordenar em ordem decrescente de quantidade
## Consultas Where
- Neste tipo de consulta é possível incluir código javascript para processar cada um dos documentos de uma coleção É um recurso muito poderoso que permite implementar consultas com maior complexidade
    ```javascript
    db.imovel.find({$where: function () {
        if (this.endereco.indexOf("500") === -1)
            return false;
        else return true;
    }})
    ```
## Cursores
- Permitem manipular o resultado de uma consulta de forma flexível
```javascript
var imoveis = db.imovel.find()
imoveis.forEach(function(imovel) { print(imovel.endereco) })
```
## Validação de Schema
- Apesar da característica flexível do *schema* baseado em documentos é possível [Definir Regras](https://www.mongodb.com/docs/manual/core/schema-validation/specify-json-schema/#std-label-schema-validation-json) para a validação
```javascript
db.createCollection("proprietario", {
validator: {
    $jsonSchema: {
        bsonType: "object",
        title: "Validação de Proprietário",
        required: [ "nome", "cpf", "total_imoveis"],
        properties: {
            nome: {
            bsonType: "string",
            description: "'nome' deve ser uma string e obrigatório"
            },
            cpf: {
            bsonType: "int",
            description: "'cpf' deve ser um inteiro e obrigatório"
            },
            total_imoveis: {
            bsonType: "int",
            minimum: 1,
            description: "'total_imoveis' deve ser maior do que zero"
            }
        }
    }
}
} )
```
- Inserir um documento inválido
```javascript
db.proprietario.insertOne({nome: "Joao"})`
```
- Resultado
```javascript
Additional information: {
    failingDocumentId: ObjectId("651d49ce119516947ca0f0c3"),
    details: {
    operatorName: '$jsonSchema',
    title: 'Validação de Proprietário',
    schemaRulesNotSatisfied: [
        {
        operatorName: 'required',
        specifiedAs: { required: [ 'nome', 'cpf', 'total_imoveis' ] },
        missingProperties: [ 'cpf', 'total_imoveis' ]
        }
    ]
    }
}
```
## Mapeando Relacionamentos
- Bancos de dados **noSQL** não possuem nativamente o recurso de relacionamento por chaves estrangeiras como os relacionais (SQL)
- Pode-se utilizar o recurso de documentos embutidos (*embeded*)
- Também é possível criar referências utilizando `ObjectId()`

## Mongodb na Cloud
- [Mongodb Atlas](https://www.mongodb.com/pt-br/atlas/database)
- Efetuar o cadastro com email (*gmail*)
- Criar um projeto e instanciar o cluster
- Habilitar o acesso ao cluster de qualquer local (menu **Security** -> **Network Access** -> **Edit** -> **Allow Access From Anywhere**)
#### Conexão
- A coneção pode ser feita via **mongo** (como feito até então) ou utilizando o [Mongodb Compass](https://www.mongodb.com/try/download/compass)
- Abrir o [Docker Playground](https://labs.play-with-docker.com/)
- Executar a instalação dos repositórios e **MongoDB**
```bash
echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/main' >> /etc/apk/repositories
echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/community' >> /etc/apk/repositories
apk update
apk add mongodb mongodb-tools
```
- No **Atlas** selecionar no menu lateral **DATABASE** -> **Cluster** e escolher **Connect**
- Clicar sobre o item **Shell** e observar a string de conexão exibida que deve ter o seguinte formato:
```bash
mongo "mongodb+srv://cluster0.hjjrtbi.mongodb.net/" --username <db_username>
```
- Utilizar o `mongoimport` para realizar a importação dos dados dos imóveis
```bash
wget https://raw.githubusercontent.com/esensato/nosql-2025-02/refs/heads/main/imoveis.json
mongoimport --uri "mongodb+srv://<username>:<password>@<cluster-url>" --db imobiliaria --collection imovel --file imoveis.json
```
- Após a importação clicar sobre o botão **Browse Collections** e verificar se os dados foram importados para a coleção **imovel**
- Criar uma consulta que exporte o resultado para uma nova coleção chamada **media_aluguel_por_cidade**
```javascript
db.imovel.aggregate([
  {
    $group: {
      _id: "$cidade",
      mediaAluguel: { $avg: "$aluguel" }
    }
  },
  {
    $out: "media_aluguel_por_cidade"
  }
])
```
- Criar um **Dashboard** para exibir um gráfico com a média de aluguel por cidade
- Exibir o **Dashboard** em uma página de uma aplicação *nodejs*
- Iniciar uma nova instância para instalar a aplicação `nodejs`
```shell
apk add nodejs npm
mkdir app
cd app
mkdir public
touch public/index.html
touch server.js
npm init -y
npm install --save express path
```
- Ediar o arquivo `server.js` e inserir o conteúdo
```javascript
const express = require('express');
const path = require('path');

const app = express();

// Servir arquivos estáticos (HTML, CSS, JS)
app.use(express.static(path.join(__dirname, 'public')));

app.listen(3000, async () => {
    console.log('Servidor rodando...');
});
```
- Editar o arquivo `public/index.html` e incluir o `iframe` conforme indicado pela opção *embed* do **Dashboard**
```html
<iframe style="background: #F1F5F4;border: none;border-radius: 2px;box-shadow: 0 2px 10px 0 rgba(70, 76, 79, .2);width: 100vw;height: 100vh;"  src="https://charts.mongodb.com/charts-imobiliaria-ompguxh/embed/dashboards?id=68d1910a-0591-48b0-87d2-9774202032f7&theme=light&autoRefresh=true&maxDataAge=14400&showTitleAndDesc=false&scalingWidth=fixed&scalingHeight=fixed"></iframe>
```
## Use Cases

## Integração Aplicações (Nodejs)

- **MongoDB** possui uma série de [drivers](https://docs.mongodb.com/drivers/) para várias linguagens de programação
- Especificamente para **Nodejs** existe o [driver mongodb](https://mongodb.github.io/node-mongodb-native/)
- Criar uma pasta `mkdir imobiliaria`
- Acessar a pasta `cd imobiliaria`
- Iniciar um projeto **Nodejs** `npm init -y`
- Instalar o cliente **MongoDB** para **Nodejs** `npm install mongodb --save`
- Criar o arquivo abaixo com o nome `testeMongodb.js`

```javascript
const MongoClient = require('mongodb').MongoClient;

async function testeConexao() {

    // obter a string de conexão do Atlas ou utilizar a conexão local
    const url = 'mongodb+srv://teste:teste@cluster0.wnbdk2i.mongodb.net/?retryWrites=true&w=majority';
    // Instancia um cliente para o mongo (como um shell mongo)
    let client = new MongoClient(url);
    // Abre a conexão
    await client.connect();
    // cria uma coleção
    const collection = client.db('imobiliaria').collection('imovel');
    // insere um documento na coleção
    const doc = { tipo: "Casa", endereco: "Rua Leste, 123", configuracao: { quartos: 1, banheiro: 1 }, lazer: ["jardim"], valor: 100000 };
    //const ret = await collection.insertOne(doc);
    console.log(doc);
    const cursor = await collection.find();
    // outra alternativa: const cursor = await collection.find().toArray();
    console.log(await cursor.next());
    // Fecha a conexão
    client.close();

}

testeConexao();
```
- Executar `node testeMongodb.js`
- Exemplo com **Express**
```javascript
const express = require('express');
const path = require('path');
const app = express();
const MongoClient = require('mongodb').MongoClient;
let client = null;

// Servir arquivos estáticos (HTML, CSS, JS)
app.use(express.static(path.join(__dirname, 'public')));

app.get('/imoveis', async (req, res) => {
    const collection = client.db('imobiliaria').collection('imovel');
    const imoveis = await collection.find().toArray();
    res.json(imoveis);
});

const start = async () => {

    const url = 'mongodb+srv://<username>:<senha>@cluster0.hjjrtbi.mongodb.net/?retryWrites=true&w=majority';
    // Instancia um cliente para o mongo (como um shell mongo)
   client = new MongoClient(url);

    await client.connect();
    app.listen(3000, () => {
        console.log('Servidor iniciado')
    });

}
start();
```
## Exercício
- Imagine uma aplicação para ajudar na realização de um *reality show* implementada pelo modelo entidade relacionamento abaixo:
<div style="text-align: center">
<img src="img/mongo-ex-1.png" width="800px" height="700px">
</div>
- Considerando que esta aplicação deve utilizar um banco de dados **noSQL** implementado em **MongoDB** pede-se:
    - Elaborar um *json schema* para o diagrama acima
    - Criar 3 *reality shows* com 10 participantes cada e uma lista com 50 prêmios potenciais (dica: utilizar algum prompt de IA para gerar os arquivos no formato *json* e realizar a importação)
    - Gerar uma consulta que exiba o nome do *reality show* e os seus participantes
    - Elaborar uma consulta que exiba, por *reality show* qual o participante mais novo e o mais velho (de acrodo com a idade)
    - Exibir o nome da emissora e o nome do reality onde alguém tenha ganho um prêmio maior ou igual a 50000 (se não retornar documentos teste com outros valores)
    - Exibir o total de votos distribuídos por reality show
    - Exibir o total de prêmios distribuídos por reality show
    - Exibir o nome e o total de pontos de audiência das emissoras
    - Criar uma página para votação nos candidatos de um reality show
    - Exibir um gráfico para acompanhar os votos recebidos pelos candidados de um reality show

## Replica Sets

- Iniciar cada servidor com a opção `replSet`
`mongod --replSet "rs0" --dbpath /root/mongodb --fork --logpath /dev/null --bind_ip_all`
- Acessar o servidor master via `mongosh` e adicionar os membros (replicas)
    ```javascript
        rs.initiate( {
        _id : "rs0",
        members: [
            { _id: 0, host: "192.168.0.6:27017" },
            { _id: 1, host: "192.168.0.7:27017" }
            { _id: 1, host: "192.168.0.8:27017" }
        ]
        })
    ```
- **Obs:** Trocar os endereços de IP
- Para visualizar a configuração do **Replica Set**

    ```javascript
    rs.config()
    ```
- Parar o sevidor primário
    ```javascript
    db.shutdownServer()
    ```
    
## Sharding

- É uma técnica de replicação de dados que permite estala horizontal
- Cada banco de dados é denominado *shard* e funciona de forma independente
- São necessários três componentes:
    - Config Server
    - Roteador (`mongos`)
    - Shards

- Configurando os componentes
    ```javascript
    mongod --configsvr --replSet configserver --port 27017 --dbpath ~/mongodb --fork --logpath /dev/null --bind_ip_all

    mongod --shardsvr --replSet shard --port 27017 --dbpath ~/mongodb --fork --logpath /dev/null --bind_ip_all

    mongod --shardsvr --replSet shard --port 27017 --dbpath ~/mongodb --fork --logpath /dev/null --bind_ip_all

    mongod --shardsvr --replSet shard --port 27017 --dbpath ~/mongodb --fork --logpath /dev/null --bind_ip_all

    mongos --configdb configserver/[IP_CONFIG_SERVER]:27017 --bind_ip_all --port 27017 --fork --logpath /dev/null
    ```

- Executar no config

    ```javascript

    ip_host='192.168.0.8:27017';

    rs.initiate({
      _id: 'configserver',
      configsvr: true,
      version: 1,
      members: [
        {
          _id: 0,
          host: ip_host,
        },
      ],
    });
    ```

- Configurar os shards

    ```javascript
         rs.initiate( {
        _id : "shard",
        members: [
            { _id: 0, host: "192.168.0.6:27017" },
            { _id: 1, host: "192.168.0.7:27017" },
            { _id: 2, host: "192.168.0.8:27017" }
        ]
        })
    ```
    
- Configurando o roteador

    ```javascript
    sh.addShard('shard/192.168.0.7:27017');
    sh.addShard('shard/192.168.0.6:27017');
    sh.addShard('shard/192.168.0.8:27017');
    ```
- Habilitar o sharding em um banco de dados

```javascript
sh.enableSharding('clientes')

```
    
- Adicionando sharding em uma Collection
```javascript
sh.shardCollection("clientes.ficha", { cpf : "hashed" } )
sh.status()
```
- Verificando a distribuição no router

```javascript
db.ficha.getShardDistribution()
```
    