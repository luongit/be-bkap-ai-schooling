package com.bkap.aispark.api.AiLearningOs.Document_ingestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.AiLearningOs.Document_ingestion.AiMaterialNodeMatch;
import com.bkap.aispark.service.AiLearningOs.Document_ingestion.AiMaterialNodeMatchDocumentService;

@RestController
@RequestMapping("/materials/node-matches")
public class AiMaterialNodeMatchController {

    @Autowired
    private AiMaterialNodeMatchDocumentService matchService;

    @PostMapping
    public ResponseEntity<?> createMatch(@RequestBody AiMaterialNodeMatch match) {
        AiMaterialNodeMatch savedMatch = matchService.saveMatch(match);
        return ResponseEntity.ok(savedMatch);
    }
}
