package mongoDB;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class GridFSService {
    private final GridFSBucket gridFSBucket;
    private static final int TARGET_WIDTH = 185;
    private static final int TARGET_HEIGHT = 115;

    public GridFSService(MongoDatabase database) {
        this.gridFSBucket = GridFSBuckets.create(database);
    }

    // Subir archivo a GridFS
    public ObjectId uploadFile(InputStream inputStream, String filename) {
        return gridFSBucket.uploadFromStream(filename, inputStream);
    }

    // Descargar archivo desde GridFS
    public void downloadFile(ObjectId fileId, String outputPath) throws IOException {
        GridFSFile gridFSFile = gridFSBucket.find(Filters.eq("_id", fileId)).first();
        if (gridFSFile != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(outputPath)) {
                gridFSBucket.downloadToStream(fileId, fileOutputStream);
            }
        } else {
            throw new IOException("Archivo no encontrado");
        }
    }

    public byte[] obtenerArchivo(ObjectId id) throws IOException {
        GridFSFile file = gridFSBucket.find(Filters.eq("_id", id)).first();
        
        if (file == null) {
            throw new IOException("Archivo no encontrado");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        gridFSBucket.downloadToStream(id, outputStream);
        return outputStream.toByteArray();
    }

    // Método para redimensionar y subir imagen
    public ObjectId uploadResizedImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        BufferedImage resizedImage = resizeImage(originalImage);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, getFileExtension(file.getOriginalFilename()), baos);
        
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            return gridFSBucket.uploadFromStream(file.getOriginalFilename(), is);
        }
    }

    // Método auxiliar para redimensionar la imagen
    private BufferedImage resizeImage(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(originalImage, 0, 0, TARGET_WIDTH, TARGET_HEIGHT, null);
        g.dispose();
        
        return resizedImage;
    }

    // Método auxiliar para obtener la extensión del archivo
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public boolean deleteFile(ObjectId fileId) {
        try {
            gridFSBucket.delete(fileId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
