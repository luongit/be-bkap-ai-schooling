package com.bkap.aispark.api;

import com.bkap.aispark.dto.AssistantCreateRequest;
import com.bkap.aispark.dto.AssistantResponse;
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
    @PostMapping(value = "/create-with-files", consumes = {"multipart/form-data"})
    public ResponseEntity<AiAssistant> createAssistantWithFiles(
            @RequestPart("dto") AssistantCreateRequest dto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @RequestPart(value = "knowledgeFiles", required = false) MultipartFile[] knowledgeFiles
    ) throws Exception {
   
        AiAssistant assistant = aiAssistantService.createAssistantWithFiles(dto, avatar, knowledgeFiles);
        return ResponseEntity.ok(assistant);
    }
    



    // API to get a list of all assistants
    @GetMapping
    public ResponseEntity<List<AssistantResponse>> getAllAssistants() {
        return ResponseEntity.ok(aiAssistantService.getAllAssistantResponses());
    }


    // API to get a specific assistant by ID
    @GetMapping("/{id}")
    public ResponseEntity<AiAssistant> getAssistantById(@PathVariable Integer id) {
        AiAssistant assistant = aiAssistantService.getById(id);
        return ResponseEntity.ok(assistant);
    }
    
    @PatchMapping("/{id}/view")
    public ResponseEntity<Void> increaseView(@PathVariable Integer id) {
        aiAssistantService.increaseView(id);
        return ResponseEntity.ok().build();
    }

}
