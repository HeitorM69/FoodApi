package com.exemplo.foodapi.controller;

import com.exemplo.foodapi.dto.ComidaRequestDTO;
import com.exemplo.foodapi.dto.ComidaResponseDTO;
import com.exemplo.foodapi.enums.CategoriaComida;
import com.exemplo.foodapi.service.ComidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comidas")
@RequiredArgsConstructor
public class ComidaController {

    private final ComidaService comidaService;

    @PostMapping
    public ResponseEntity<ComidaResponseDTO> criar(@Valid @RequestBody ComidaRequestDTO dto) {
        ComidaResponseDTO criada = comidaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<ComidaResponseDTO>> buscarTodas(
            @RequestParam(required = false) CategoriaComida categoria) {
        if (categoria != null) {
            return ResponseEntity.ok(comidaService.buscarPorCategoria(categoria));
        }
        return ResponseEntity.ok(comidaService.buscarTodas());
    }

    // GET por NOME
    @GetMapping("/nome/{nome}")
    public ResponseEntity<ComidaResponseDTO> buscarPorNome(@PathVariable String nome) {
        return ResponseEntity.ok(comidaService.buscarPorNome(nome));
    }

    // PUT por ID
    @PutMapping("/{id}")
    public ResponseEntity<ComidaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComidaRequestDTO dto) {
        return ResponseEntity.ok(comidaService.atualizarPorId(id, dto));
    }

    // DELETE por NOME
    @DeleteMapping("/nome/{nome}")
    public ResponseEntity<Void> deletarPorNome(@PathVariable String nome) {
        comidaService.deletarPorNome(nome);
        return ResponseEntity.noContent().build();
    }
}