package com.exemplo.foodapi.entity;

import com.exemplo.foodapi.enums.CategoriaComida;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_comidas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaComida categoria;

    private Boolean disponivel;

    private Integer tempoPreparoMinutos;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        if (this.disponivel == null) {
            this.disponivel = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
