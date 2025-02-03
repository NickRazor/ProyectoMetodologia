package com.happymarket.MongoDB;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GridFSService {
    private GridFSBucket gridFSBucket;

    public GridFSService(MongoDatabase database) {
        this.gridFSBucket = GridFSBuckets.create(database);
    }

    public ObjectId uploadFile(String filePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        ObjectId fileId = gridFSBucket.uploadFromStream(new File(filePath).getName(), fileInputStream);
        fileInputStream.close();
        return fileId;
    }

    public void downloadFile(ObjectId fileId, String outputPath) throws IOException {
        GridFSFile gridFSFile = gridFSBucket.find(new ObjectId(fileId)).first();
        if (gridFSFile != null) {
            FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
            gridFSBucket.downloadToStream(gridFSFile.getId(), fileOutputStream);
            fileOutputStream.close();
        } else {
            System.out.println("Archivo no encontrado");
        }
    }
}