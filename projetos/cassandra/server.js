const express = require('express');
const bodyParser = require('body-parser');
const cassandra = require('cassandra-driver');
const path = require('path');

const app = express();
const port = 3000;

app.use(bodyParser.json());

app.use(express.static(path.join(__dirname, '/public')));

app.listen(port, () => {
    console.log(`Servidor rodando em http://localhost:${port}`);
});
