package com.carlosribeiro.apirestful.repository;

import com.carlosribeiro.apirestful.model.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("select p from Produto p left join fetch p.categoria order by p.id")
    List<Produto> recuperarProduos();

    @Query(
        value = "select p from Produto p left join fetch p.categoria where p.nome like :nome order by p.id",
        countQuery = "select count(p) from Produto p where p.nome like :nome "
    )
    Page<Produto> recuperarProdutosComPaginacao(PageRequest pageRequest, @Param("nome") String nome);

    // Sobrecarga com filtro por categoria. Usada quando a página lista apenas
    // os produtos de uma categoria (ex.: clicar no card "Placas de Vídeo").
    // Manter como método separado evita mexer na query original e mantém a
    // compatibilidade com qualquer chamada existente.
    @Query(
        value = "select p from Produto p left join fetch p.categoria where p.nome like :nome and p.categoria.id = :categoriaId order by p.id",
        countQuery = "select count(p) from Produto p where p.nome like :nome and p.categoria.id = :categoriaId"
    )
    Page<Produto> recuperarProdutosComPaginacaoECategoria(
        PageRequest pageRequest,
        @Param("nome") String nome,
        @Param("categoriaId") Long categoriaId
    );

    /**
     * Recupera um produto com lock pessimista de escrita (SELECT ... FOR
     * UPDATE). Usado no checkout para evitar condição de corrida no desconto
     * do estoque: duas transações concorrentes não podem ler o mesmo estoque
     * e depois ambas decrementá-lo.
     *
     * O join fetch da categoria evita um query extra logo após o lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p left join fetch p.categoria where p.id = :id")
    Optional<Produto> findByIdForUpdate(@Param("id") Long id);
}
