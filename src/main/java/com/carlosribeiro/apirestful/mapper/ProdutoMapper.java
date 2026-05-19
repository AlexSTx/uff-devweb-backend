package com.carlosribeiro.apirestful.mapper;

import com.carlosribeiro.apirestful.dto.ProdutoCreate;
import com.carlosribeiro.apirestful.dto.ProdutoDto;
import com.carlosribeiro.apirestful.model.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    List<ProdutoDto> toProdutosDto(List<Produto> produtos);

    @Mapping(source = "categoria", target = "categoriaResumo")
    ProdutoDto toProdutoDto(Produto produto);

    @Mapping(source = "categoriaResumo", target = "categoria")
    Produto toProduto(ProdutoCreate produtoCreate);

    @Mapping(source = "categoriaResumo", target = "categoria")
    Produto toProduto(ProdutoDto produtoDto);
}
