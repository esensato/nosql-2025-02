package org.apache.cassandra.triggers;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;

import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.partitions.Partition;
import org.apache.cassandra.db.rows.Cell;
import org.apache.cassandra.db.rows.Row;
import org.apache.cassandra.db.rows.Unfiltered;
import org.apache.cassandra.db.rows.UnfilteredRowIterator;
import org.apache.cassandra.schema.ColumnMetadata;
import org.apache.cassandra.schema.Schema;
import org.apache.cassandra.schema.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerAtualizaEstoque implements ITrigger {

    private static final Logger logger = LoggerFactory.getLogger(TriggerAtualizaEstoque.class);

    @Override
    public Collection<Mutation> augment(Partition update) {
        TableMetadata meta = update.metadata();

        logger.info("TriggerAtualizaEstoque chamada para tabela: {}.{}", meta.keyspace, meta.name);

        if (!"loja".equals(meta.keyspace) || !"pedido".equals(meta.name))
            return Collections.emptyList();

        UnfilteredRowIterator it = update.unfilteredIterator();
        if (!it.hasNext())
            return Collections.emptyList();

        Unfiltered un = it.next();
        if (!(un instanceof Row))
            return Collections.emptyList();

        Row row = (Row) un;

        ByteBuffer idProdutoValue = null;
        ByteBuffer quantidadeValue = null;

        for (Cell<?> cell : row.cells()) {
            String colName = cell.column().name.toString();
            if (colName.equals("id_produto")) {
                idProdutoValue = cell.buffer();
            } else if (colName.equals("quantidade")) {
                quantidadeValue = cell.buffer();
            }
        }

        if (idProdutoValue == null || quantidadeValue == null)
            return Collections.emptyList();

        String idProduto = UTF8Type.instance.compose(idProdutoValue);
        int quantidade = Int32Type.instance.compose(quantidadeValue);

        TableMetadata metaEstoque = Schema.instance.getTableMetadata("loja", "estoque");
        if (metaEstoque == null)
            return Collections.emptyList();

        // obtém a referência da coluna counter "total"
        ColumnMetadata colTotal = metaEstoque.getColumn(new ColumnIdentifier("total", true));
        if (colTotal == null)
            return Collections.emptyList();

        logger.debug("Produto: {}, Quantidade: {}", idProduto, quantidade);

        String cql = String.format("UPDATE loja.estoque SET total = total + %d WHERE id_produto = '%s'",
                -(long) quantidade, idProduto);
        org.apache.cassandra.cql3.QueryProcessor.executeInternal(cql);

        return Collections.emptyList();

    }
}