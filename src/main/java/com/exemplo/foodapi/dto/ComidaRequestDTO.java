package com.exemplo.foodapi.dto;

import com.exemplo.foodapi.enums.CategoriaComida;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ComidaRequestDTO(
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome não pode ter mais que 100 caracteres")
    String nome,

    String descricao,

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    BigDecimal preco,

    @NotNull(message = "A categoria é obrigatória")
    CategoriaComida categoria,

    Boolean disponivel,

    @Min(value = 1, message = "O tempo de preparo deve ser de no mínimo 1 minuto")
    Integer tempoPreparoMinutos
) {}
