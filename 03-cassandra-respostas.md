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
    - Consultar os valores das cotações de uma ação (pelo id_empresa) em um determinado dia;
    - Consultar ordens de compra por empresa, dia e tipo

```sql
CREATE KEYSPACE bolsa_de_valores
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE bolsa_de_valores;

CREATE TABLE Acao (id_empresa text, nome text, PRIMARY KEY (id_empresa));

INSERT INTO Acao (id_empresa, nome) VALUES ('PETR4', 'Petrobras');
INSERT INTO Acao (id_empresa, nome) VALUES ('ELET3', 'Eletrobrás');
INSERT INTO Acao (id_empresa, nome) VALUES ('VALE3', 'Vale do Rio Doce');

SELECT * FROM Acao;

CREATE TABLE Cotacao (id_cotacao int, id_empresa text, data_hora timestamp, preco float, PRIMARY KEY (id_empresa, data_hora)) WITH CLUSTERING ORDER BY (data_hora ASC);

INSERT INTO Cotacao (id_cotacao, id_empresa, data_hora, preco) VALUES (1, 'PETR4', '2025-10-13T21:51:00.000Z', 100.00);
INSERT INTO Cotacao (id_cotacao, id_empresa, data_hora, preco) VALUES (2, 'PETR4', '2025-10-12T21:51:00.000Z', 90.00);
INSERT INTO Cotacao (id_cotacao, id_empresa, data_hora, preco) VALUES (3, 'VALE3', '2025-10-13T21:51:00.000Z', 120.00);

SELECT * FROM Cotacao;

CREATE TABLE Ordem (id_ordem int, id_empresa text, data date, tipo ascii, preco float, PRIMARY KEY (id_empresa, data, tipo)) WITH CLUSTERING ORDER BY (data ASC);

INSERT INTO Ordem (id_ordem, id_empresa, data, tipo, preco) VALUES (1, 'PETR4', '2025-10-13', 'C', 120.00);
INSERT INTO Ordem (id_ordem, id_empresa, data, tipo, preco) VALUES (2, 'VALE3', '2025-10-12', 'V', 100.00);
INSERT INTO Ordem (id_ordem, id_empresa, data, tipo, preco) VALUES (3, 'PETR4', '2025-10-13', 'V', 200.00);

SELECT * FROM Cotacao WHERE id_empresa = 'PETR4' AND data_hora >= '2025-10-12 00:00:00' AND data_hora <= '2025-10-12 23:59:00';

SELECT * FROM Ordem WHERE id_empresa = 'PETR4' AND data = '2025-10-13' AND tipo = 'V';
```


        