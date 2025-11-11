# Neo4j

## Referência

https://neo4j.com/

- Acesso na núvem (sem necessidade de instalação) [AuraDB](https://console.neo4j.io/)
- Existe também o [Sandbox](https://sandbox.neo4j.com/)

## Conceitos
- Banco de dados baseado em *grafo*
- Um *grafo* é um conjunto de nós (vértices) conectados por arestas (ligação entre os nós)
- As arestas possuem valores associados denominados *pesos*
- Muito úteis para sistemas de rotas, classificações, mapas mentais, etc...
- Possui uma linguagem de consulta e definição de dados denominada *Cypher*

## Instalação
- A forma mais fácil é por meio de um container
- [Docker Playground](https://labs.play-with-docker.com/)
- Executar a criação da imagem / *container*
```bash

docker run -d \
  --name neo4j \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/senha123 \
  -e NEO4JLABS_PLUGINS='["apoc"]' \
  -e "NEO4J_dbms_security_procedures_unrestricted=apoc.*" \
  neo4j:5


docker logs neo4j
```
- Acessar a interface interativa `http://localhost:7474/browser/`
## Principais elementos do Cypher
- Os principais elementos (nós, arestas e atributos) são:
    - () - nós
    - [] - arestas
    - {} - atributos
- Relacionamentos entre nós são representados por `(:n1)-[:r]->(:n2)` ou `(:n1)<-[:r]-(:n2)` ou `(:n1)-[:r]-(:n2)`
- Nós são agrupados por meio de *tags*, por exemplo, `(:Proprietario)`, `(:Inquilino)`, `(:Imovel)`, etc...
- Tanto os nós quanto as arestas podem ter atributos, por exemplo, `(:Imovel {endereco: "Rua Bahia, 123")}`
- Criar nós
```javascript
CREATE (:Imovel {endereco: "Rua Bahia, 123", metragem: 90});
CREATE (:Imovel {endereco: "Rua Sao Paulo, 300", metragem: 130});

CREATE (p:Proprietario {nome: "Joao da Cruz"}) RETURN p;

MATCH (n) RETURN n
```
- Pesquisando por atributos
```javascript
MATCH (i:Imovel) WHERE i.metragem = 90 return i.endereco;
MATCH (i:Imovel {metragem: 90}) return i;
MATCH (i:Imovel) WHERE i.metragem > 90 return i;
```
- Conectar os nós
```javascript
MATCH (i:Imovel {endereco: "Rua Bahia, 123"}) WITH i
MATCH (p:Proprietario {nome: "Joao da Cruz"})
CREATE (p)-[:E_PROPRIETARIO_DE]->(i)
RETURN p, i

MATCH (i:Imovel {endereco: "Rua Sao Paulo, 300"}) WITH i
MATCH (p:Proprietario {nome: "Joao da Cruz"})
CREATE (p)-[:E_PROPRIETARIO_DE]->(i)
RETURN p, i
```
- Criar um novo nó contendo um inquilino para o imóvel
```javascript
CREATE (:Inquilino {nome: "Paulo Barros"});

MATCH (q:Inquilino {nome: "Paulo Barros"}) WITH q
MATCH (i:Imovel {endereco: "Rua Bahia, 123"})
CREATE (q)-[:ALUGA]->(i);
```
- Incluir atributos nas arestas *ALUGA* e *E_PROPRIETARIO_DE*
```javascript
MATCH (:Inquilino {nome: "Paulo Barros"})-[r:ALUGA]->(:Imovel {endereco: "Rua Bahia, 123"})
SET r.aluguel = 2500;

MATCH (:Proprietario {nome: "Joao da Cruz"})-[r:E_PROPRIETARIO_DE]->(:Imovel {endereco: "Rua Bahia, 123"})
SET r.venda = 250000;

MATCH (:Proprietario {nome: "Joao da Cruz"})-[r:E_PROPRIETARIO_DE]->(:Imovel {endereco: "Rua Sao Paulo, 300"})
SET r.venda = 500000;

```
- A consulta pode também ser feita pelas propriedades das arestas
```javascript
match (p:Proprietario)-[:E_PROPRIETARIO_DE {venda: 250000}]->(i:Imovel) return p,i
```
- Para remover todos os nós e arestas de um modelo
```javascript
MATCH (n) DETACH DELETE n
```
## Importando Dados
- Obter o arquivo *csv*
- Copiar os arquivos para o diretório `/var/lib/neo4j/import/` do **Neo4j**
```bash
docker cp imoveis.csv neo4j:/var/lib/neo4j/import/
docker cp pessoas.csv neo4j:/var/lib/neo4j/import/
docker cp relacionamentos.csv neo4j:/var/lib/neo4j/import/
```
- Executar a importação dos arquivos
- Carregar os imóveis
```javascript
docker exec -it neo4j cypher-shell -u neo4j -p teste123 "
LOAD CSV WITH HEADERS FROM 'https://raw.githubusercontent.com/esensato/nosql-2025-02/refs/heads/main/imoveis.csv' AS linha
MERGE (:Imovel {endereco: linha.endereco, estado: linha.estado});"
```
- Carregar as pessoas (proprietários e inquilinos)
```javascript
docker exec -it neo4j cypher-shell -u neo4j -p teste123 "
LOAD CSV WITH HEADERS FROM 'https://raw.githubusercontent.com/esensato/nosql-2025-02/refs/heads/main/pessoas.csv' AS linha
MERGE (p:Pessoa {nome: linha.nome, tipo: linha.tipo});"
```
- Finalmente, carregar as arestas
```javascript
docker exec -it neo4j cypher-shell -u neo4j -p teste123 "
LOAD CSV WITH HEADERS FROM 'https://raw.githubusercontent.com/esensato/nosql-2025-02/refs/heads/main/relacionamentos.csv' AS linha
WITH linha WHERE linha.tipoRelacao = 'ALUGA'
MATCH (p:Pessoa {nome: linha.nomePessoa})
MATCH (i:Imovel {endereco: linha.enderecoImovel})
MERGE (p)-[r:ALUGA]->(i)
SET r.valor = toInteger(linha.valor);
"

docker exec -it neo4j cypher-shell -u neo4j -p teste123 "
LOAD CSV WITH HEADERS FROM 'file:///relacionamentos.csv' AS linha
WITH linha WHERE linha.tipoRelacao = 'VENDE'
MATCH (p:Pessoa {nome: linha.nomePessoa})
MATCH (i:Imovel {endereco: linha.enderecoImovel})
MERGE (p)-[r:VENDE]->(i)
SET r.valor = toInteger(linha.valor);
"
```
- Retornar os imóveis que não estão nem sendo vendidos e nem alugados
```javascript
MATCH (i:Imovel)
WHERE NOT ( ()-[:ALUGA|VENDE]->(i) )
RETURN i.endereco AS endereco, i.estado AS estado;
```
- Para retornar os nós, o `RETURN` deve retornar o nó inteiro
```javascript
MATCH (i:Imovel)
WHERE NOT ( ()-[:ALUGA|VENDE]->(i) )
RETURN i
```
- Descobrir quais imóveis estão sendo alugados e vendidos também
```javascript
MATCH (inq:Pessoa)-[:ALUGA]->(i:Imovel)<-[:VENDE]-(prop:Pessoa)
RETURN inq, i, prop
```
- Quais imóveis possuem mais de um proprietário?
```javascript
MATCH (p:Pessoa)-[:VENDE]->(i:Imovel)
WITH i, collect(p.nome) AS proprietarios, count(p) AS qtdProprietarios
WHERE qtdProprietarios > 1
RETURN i.endereco AS endereco, i.estado AS estado, proprietarios, qtdProprietarios
ORDER BY qtdProprietarios DESC;
```
- Para retornar graficamente
```javascript
MATCH (p:Pessoa)-[r:VENDE]->(i:Imovel)
WITH i, collect(p) AS proprietarios, count(p) AS qtd
WHERE qtd > 1
UNWIND proprietarios AS prop
MATCH (prop)-[r:VENDE]->(i)
RETURN DISTINCT prop, r, i;
```
***
### Linhas Aéreas
- Criar nós destino
```javascript
CREATE (d:destino {cidade: "Belo Horizonte"});
CREATE (:destino {cidade: "Rio de Janeiro"});
CREATE (:destino {cidade: "São Paulo"});
CREATE (:destino {cidade: "Porto Alegre"});
CREATE (:destino {cidade: "Salvador"});
```
- Criar relacionamentos entre destinos
```javascript
MATCH (d1:destino {cidade: "Salvador"})
MATCH (d2:destino {cidade: "Rio de Janeiro"})
CREATE (d1)-[:voo {empresa: "Passarinho", distancia: 1600, preco: 300}]->(d2);

MATCH (d1:destino {cidade: "Rio de Janeiro"})
MATCH (d2:destino {cidade: "São Paulo"})
CREATE (d1)-[:voo {empresa: "Passarinho", distancia: 430, preco: 200}]->(d2);

MATCH (d1:destino {cidade: "São Paulo"})
MATCH (d2:destino {cidade: "Rio de Janeiro"})
CREATE (d1)-[:voo {empresa: "Passarinho", distancia: 430, preco: 250}]->(d2);

MATCH (d1:destino {cidade: "São Paulo"})
MATCH (d2:destino {cidade: "Porto Alegre"})
CREATE (d1)-[:voo {empresa: "Passarinho", distancia: 1130, preco: 400}]->(d2);

MATCH (d1:destino {cidade: "São Paulo"})
MATCH (d2:destino {cidade: "Belo Horizonte"})
CREATE (d1)-[:voo {empresa: "Borboleta", distancia: 590, preco: 700}]->(d2);

MATCH (d1:destino {cidade: "Belo Horizonte"})
MATCH (d2:destino {cidade: "Porto Alegre"})
CREATE (d1)-[:voo {empresa: "Borboleta", distancia: 1700, preco: 650}]->(d2);

MATCH (d1:destino {cidade: "Salvador"})
MATCH (d2:destino {cidade: "Porto Alegre"})
CREATE (d1)-[:voo {empresa: "Borboleta", distancia: 3100, preco: 480}]->(d2);
```
### Consultar destinos
- Quais os vôos partindo de São Paulo para Belo Horizonte?
```javascript
MATCH (d1:destino)-[v:voo]->(d2:destino) 
WHERE d1.cidade="São Paulo" and d2.cidade="Belo Horizonte" 
RETURN d1,d2;
```
- Mesma coisa que
```javascript
MATCH (d1:destino {cidade: "São Paulo"})-[v:voo]->(d2:destino {cidade: "Belo Horizonte"})
RETURN d1,d2;
```
- Existem vôos partindo de Belo Horizonte e chegando a São Paulo? E no sentido contrário?
```javascript
MATCH (d1:destino)<-[v:voo]-(d2:destino) 
WHERE d1.cidade="São Paulo" AND d2.cidade="Belo Horizonte" 
RETURN d1,d2;
```
- Quais os vôos entre cidades cuja distância seja menor do que 1000 km?
```javascript
MATCH (d1:destino)-[v:voo WHERE v.distancia < 1000]->(d2:destino) RETURN *;
```
- Quais os vôos entre cidades cujo preço seja menor do que R$ 500,00?
```javascript
MATCH (d1:destino)-[v:voo]->(d2:destino) WHERE v.preco < 500 RETURN *
```
- Quals vôos partem de Salvador e custam menos do que R$ 400,00?
```javascript
MATCH (d1:destino)-[v:voo]->(d2:destino) WHERE v.preco < 400 AND d1.cidade = "Salvador" RETURN *;
```
- Opções de vôo entre Rio de Janeito e Porto Alegre contemplando até 5 escalas
```javascript
MATCH caminho = (d1:destino)-[:voo*1..5]->(d2:destino)
WHERE d1.cidade = "Rio de Janeiro"
AND d2.cidade = "Porto Alegre"
RETURN caminho;
```
- Qual seria o menor caminho para ligar as duas cidades?
```javascript
MATCH caminho = shortestPath((d1:destino)-[:voo*1..5]->(d2:destino))
WHERE d1.cidade = "Rio de Janeiro"
  AND d2.cidade = "Porto Alegre"
RETURN caminho;
```
- Opções de vôo entre Rio de Janeito e Porto Alegre com uma escala
```javascript
MATCH (d1:destino)-[v1:voo]->(d2:destino)-[v2:voo]->(d3:destino) 
WHERE d1.cidade = "Rio de Janeiro" 
AND d3.cidade = "Porto Alegre" 
RETURN *
```
- Como ir do Rio de Janeiro a Porto Alegre pagando menos do que R$ 500,00 pelo trecho?
```javascript
MATCH p=(d1:destino)-[*..6]->(d3:destino) 
WHERE d1.cidade = "Rio de Janeiro" 
AND   d3.cidade = "Porto Alegre" 
AND ALL(r IN RELATIONSHIPS(p) WHERE r.preco < 500)
RETURN p;
```
- Como ir do Rio de Janeiro a Porto Alegra pagando o menor preço?
```javascript
MATCH p = (d1:destino)-[:voo*..6]->(d3:destino)
WHERE d1.cidade = "Rio de Janeiro"
AND d3.cidade = "Porto Alegre"
WITH p,
     REDUCE(soma = 0, r IN relationships(p) | soma + r.preco) AS preco_total
RETURN p, preco_total
ORDER BY preco_total ASC
LIMIT 1;
```
***
## Integração Nodejs
- Criar uma aplicação para associar pessoas a projetos
```bash
mkdir neo4j
cd neo4j
npm init -y
npm install -y neo4j-driver express
```
- Eftuar a conexão com o **Neo4j** (criar um arquivo `conexao.js`)
```javascript
const neo4j = require('neo4j-driver');

const driver = neo4j.driver(
  'bolt://localhost:7687',
  neo4j.auth.basic('neo4j', 'teste123')
);

module.exports = driver;
```
- Criar um *endpoint* para retornar todas as conexões de uma pessoa
```javascript
const express = require('express');
const router = express.Router();
const driver = require('./conexao');

app.use(express.json());

app.get('/:nome/conexoes', async (req, res) => {

  const session = driver.session();
  const nome = req.params.nome;

  try {
    const result = await session.run(
      `
      MATCH (p:Pessoa {nome: $nome})-[:TRABALHA_COM]->(outro:Pessoa)
      RETURN outro.nome AS nome
      `,
      { nome }
    );

    res.json(result.records.map(r => r.get('nome')));
  } catch (e) {
    res.status(500).send(e.message);
  } finally {
    await session.close();
  }
});
app.listen(3000, () => console.log('Servidor rodando em http://localhost:3000'));
```
- Existe também a possibilidade de utilizar *javascript* para exibir o *grafo* dos relacionamentos
```html
<div id="viz"></div>
<script src="https://cdn.neo4jlabs.com/neovis.js/v1.6.0/neovis.js"></script>
<script>
  const config = {
    container_id: "viz",
    server_url: "bolt://localhost:7687",
    server_user: "neo4j",
    server_password: "teste123",
    labels: {
      Pessoa: { caption: "nome" },
      Projeto: { caption: "nome" }
    },
    relationships: {
      PARTICIPOU_EM: { caption: false },
      TRABALHA_COM: { caption: false }
    },
    initial_cypher: "MATCH (n)-[r]->(m) RETURN n,r,m"
  };
  const viz = new NeoVis.default(config);
  viz.render();
</script>
```
- Popular a base de dados inicialmente
```javascript
const neo4j = require("neo4j-driver");

// Configurações de conexão com o Neo4j
const uri = "bolt://localhost:7687";
const user = "neo4j";
const password = "senha123"; // ajuste conforme seu container

const driver = neo4j.driver(uri, neo4j.auth.basic(user, password));

async function main() {
  const session = driver.session();

  try {
    console.log("Limpando o banco de dados...");
    await session.run("MATCH (n) DETACH DELETE n");

    console.log("Criando pessoas...");
    const pessoas = [
      "Ana", "Bruno", "Carla", "Daniel", "Eduardo",
      "Fernanda", "Gabriel", "Helena", "Igor", "Joana"
    ];

    for (const nome of pessoas) {
      await session.run("CREATE (:Pessoa {nome: $nome})", { nome });
    }

    console.log("Criando projetos...");
    const projetos = [
      "Plataforma IoT", "Sistema Web", "App Mobile",
      "Chatbot IA", "Painel Analítico"
    ];

    for (const titulo of projetos) {
      await session.run("CREATE (:Projeto {titulo: $titulo})", { titulo });
    }

    console.log("🔗 Criando relacionamentos entre pessoas e projetos...");
    await session.run(`
      MATCH (p:Pessoa), (pr:Projeto)
      WITH p, pr
      WHERE rand() < 0.3  // cada pessoa trabalha em cerca de 30% dos projetos
      CREATE (p)-[:TRABALHA_EM]->(pr)
    `);

    console.log("Dados inseridos com sucesso!");
  } catch (err) {
    console.error("Erro:", err);
  } finally {
    await session.close();
    await driver.close();
  }
}

main();
```
