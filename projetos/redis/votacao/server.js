const express = require('express');
const path = require('path');

const app = express();
const port = 3000;

// Servir arquivos estáticos (HTML, CSS, JS)
app.use(express.static(path.join(__dirname, 'public')));

// Endpoint para registrar a cor
app.get('/registrar/cor=:cor', (req, res) => {
    const cor = req.params.cor;
    console.log(`Cor selecionada: ${cor}`);
    res.send(`Cor ${cor} registrada com sucesso!`);
});

app.listen(port, () => {
    console.log(`Servidor rodando em http://localhost:${port}`);
});
