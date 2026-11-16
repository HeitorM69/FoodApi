package com.exemplo.foodapi.service;

import com.exemplo.foodapi.dto.ComidaRequestDTO;
import com.exemplo.foodapi.dto.ComidaResponseDTO;
import com.exemplo.foodapi.entity.Comida;
import com.exemplo.foodapi.enums.CategoriaComida;
import com.exemplo.foodapi.exception.ResourceNotFoundException;
import com.exemplo.foodapi.repository.ComidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComidaService {

    private final ComidaRepository comidaRepository;

    public ComidaResponseDTO criar(ComidaRequestDTO dto) {
        Comida comida = Comida.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .preco(dto.preco())
                .categoria(dto.categoria())
                .disponivel(dto.disponivel() != null ? dto.disponivel() : true)
                .tempoPreparoMinutos(dto.tempoPreparoMinutos())
                .build();

        Comida salva = comidaRepository.save(comida);
        return ComidaResponseDTO.fromEntity(salva);
    }

    public List<ComidaResponseDTO> buscarTodas() {
        return comidaRepository.findAll()
                .stream()
                .map(ComidaResponseDTO::fromEntity)
                .toList();
    }

    // Buscar pelo ID
    public ComidaResponseDTO buscarPorId(Long id) {
        Comida comida = comidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de comida não encontrado com ID: " + id));
        return ComidaResponseDTO.fromEntity(comida);
    }

    // Buscar pelo NOME
    public ComidaResponseDTO buscarPorNome(String nome) {
        Comida comida = comidaRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new ResourceNotFoundException("Item de comida não encontrado com o nome: " + nome));
        return ComidaResponseDTO.fromEntity(comida);
    }

    public List<ComidaResponseDTO> buscarPorCategoria(CategoriaComida categoria) {
        return comidaRepository.findByCategoria(categoria)
                .stream()
                .map(ComidaResponseDTO::fromEntity)
                .toList();
    }

    // Atualizar pelo ID
    public ComidaResponseDTO atualizarPorId(Long id, ComidaRequestDTO dto) {
        Comida comida = comidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de comida não encontrado com ID: " + id));

        comida.setNome(dto.nome());
        comida.setDescricao(dto.descricao());
        comida.setPreco(dto.preco());
        comida.setCategoria(dto.categoria());
        if (dto.disponivel() != null) {
            comida.setDisponivel(dto.disponivel());
        }
        if (dto.tempoPreparoMinutos() != null) {
            comida.setTempoPreparoMinutos(dto.tempoPreparoMinutos());
        }

        Comida atualizada = comidaRepository.save(comida);
        return ComidaResponseDTO.fromEntity(atualizada);
    }

    // Deletar pelo ID
    @Transactional
    public void deletarPorId(Long id) {
        Comida comida = comidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de comida não encontrado com ID: " + id));
        comidaRepository.delete(comida);
    }

    // Deletar pelo NOME
    @Transactional
    public void deletarPorNome(String nome) {
        Comida comida = comidaRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new ResourceNotFoundException("Item de comida não encontrado com o nome: " + nome));
        comidaRepository.delete(comida);
    }
}