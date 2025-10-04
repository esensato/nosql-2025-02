- Encontrar todos os imóveis com "churrasqueira" no campo "lazer"

`db.imovel.find({lazer: "churrasqueira"})`

- Listar todos os "endereços" de imóveis na "cidade" de "Porto Alegre"

`db.imovel.find({cidade: "Porto Alegre"}, {endereco: 1})`

- Encontrar imóveis onde o número de quartos seja maior que 2 ordenado por "cidade"
`db.imovel.find({"configuracao.quartos": {$gt: 2}}, {cidade: 1, "configuracao.quartos": 1}).sort({cidade: 1})`

- Encontrar imóveis com "aluguel" superior a 1000 e que possuam "jardim"

`db.imovel.find({"aluguel": {$gt: 1000}, lazer: "jardim"}).pretty()`

- Encontrar imóveis que possuam "churrasqueira" e que tenham aluguel menor que 1500

`db.imovel.find({"aluguel": {$lt: 1500}, lazer: "churrasqueira"}).pretty()`
 
- Encontrar imóveis cujo "bairro" contenha "Por" no início do nome

`db.imovel.find({"bairro": {$regex: "^Por"}}).pretty()`

- Encontrar todos os imóveis com "piscina" e "academia" no campo lazer com aluguel menor do que 1200

`db.imovel.find({"aluguel": {$lt: 1200}, lazer: {$all: ["piscina", "academia"]}}).pretty()`

- Encontrar imóveis que possuam "churrasqueira" no campo lazer ou que tenham aluguel menor que 1500

`db.imovel.find({$or:[{"aluguel": {$lt: 1500}}, {lazer: "churrasqueira"}]}).pretty()`


> db.imovel.aggregate([ {$match: {"configuracao.banheiros": {$gt: 2}}},
... {$count: "total_mais_2_banheiros"}
... ])

> db.imovel.aggregate([
... {$group: {_id: "$tipo", media_aluguel:{$avg: "$aluguel"}}}
... ])

> db.imovel.aggregate([ {$group: {_id: "$bairro", total:{$sum: 1}}},
... {$sort:{total: -1}}
... ])

db.createCollection("proprietario", {
validator: {
    $jsonSchema: {
        bsonType: "object",
        title: "Validação de Proprietário",
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