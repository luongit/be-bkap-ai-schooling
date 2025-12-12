package com.bkap.aispark.service.AiLearningOs.Document_ingestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.AiLearningOs.Document_ingestion.AiMaterialNodeMatch;
import com.bkap.aispark.repository.AiLearningOs.Document_ingestion.AiMaterialNodeMatchRepository;

@Service
public class AiMaterialNodeMatchDocumentService {

    @Autowired
    private AiMaterialNodeMatchRepository matchRepository;

    public AiMaterialNodeMatch saveMatch(AiMaterialNodeMatch match) {
        return matchRepository.save(match);
    }
}
