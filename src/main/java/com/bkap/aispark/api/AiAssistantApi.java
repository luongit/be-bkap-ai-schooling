package com.bkap.aispark.api;

import com.bkap.aispark.dto.AssistantCreateRequest;
import com.bkap.aispark.entity.AiAssistant;
import com.bkap.aispark.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/assistants")
public class AiAssistantApi {

    private final AiAssistantService aiAssistantService;

    @Autowired
    public AiAssistantApi(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    // API to create a new assistant
    @PostMapping
    public ResponseEntity<AiAssistant> createAssistant(
            @RequestBody AssistantCreateRequest dto, 
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws Exception {
        AiAssistant assistant = aiAssistantService.createAssistant(dto, avatar);
        return ResponseEntity.ok(assistant);
    }

    // API to get a list of all assistants
    @GetMapping
    public ResponseEntity<List<AiAssistant>> getAllAssistants() {
        List<AiAssistant> assistants = aiAssistantService.getAllAssistants();
        return ResponseEntity.ok(assistants);
    }

    // API to get a specific assistant by ID
    @GetMapping("/{id}")
    public ResponseEntity<AiAssistant> getAssistantById(@PathVariable Integer id) {
        AiAssistant assistant = aiAssistantService.getById(id);
        return ResponseEntity.ok(assistant);
    }
}
