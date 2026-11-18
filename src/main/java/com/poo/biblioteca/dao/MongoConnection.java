package com.poo.biblioteca.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class MongoConnection {
    private static final String URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "biblioteca_db";
    private static MongoClient mongoClient;

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(URI);
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static int gerarProximoId(String nomeColecao) {
        MongoCollection<Document> counters = getDatabase().getCollection("counters");
        Document doc = counters.findOneAndUpdate(
                Filters.eq("_id", nomeColecao),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        );
        return doc != null ? doc.getInteger("seq") : 1;
    }
}
