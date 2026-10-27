package com.exemplo.foodapi.dto;

import com.exemplo.foodapi.entity.Comida;
import com.exemplo.foodapi.enums.CategoriaComida;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComidaResponseDTO(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    CategoriaComida categoria,
    Boolean disponivel,
    Integer tempoPreparoMinutos,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao
) {
    public static ComidaResponseDTO fromEntity(Comida comida) {
        return new ComidaResponseDTO(
            comida.getId(),
            comida.getNome(),
            comida.getDescricao(),
            comida.getPreco(),
            comida.getCategoria(),
            comida.getDisponivel(),
            comida.getTempoPreparoMinutos(),
            comida.getDataCriacao(),
            comida.getDataAtualizacao()
        );
    }
}
