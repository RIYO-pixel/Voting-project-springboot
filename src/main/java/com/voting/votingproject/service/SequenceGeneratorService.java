package com.voting.votingproject.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public long getNextSequence(String seqName) {

        Query query = new Query(Criteria.where("_id").is(seqName));
        Update update = new Update().inc("value", 1);

        FindAndModifyOptions options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(true);

        Document seq = mongoTemplate.findAndModify(
                query,
                update,
                options,
                Document.class,
                "sequences"
        );

        if (seq == null || seq.get("value") == null) {
            throw new IllegalStateException("Sequence generation failed for: " + seqName);
        }

        // ✅ SAFE numeric conversion
        return ((Number) seq.get("value")).longValue();
    }
}
