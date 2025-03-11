package com.example.chatbot.Embedding;

import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;
import org.bson.types.Binary;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import static java.nio.charset.StandardCharsets.UTF_8;


@ApplicationScoped
public class EmbeddingResource {



    public String extractText(String filePath) {

        System.out.println("📂 Lade Datei: " + filePath);
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                System.out.println("File does not exist" + filePath);
                return null;
            }


            PDDocument document = Loader.loadPDF(file); // PDFBox 3.x
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            return text;
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der PDF: " + e.getMessage(), e);
        }
    }

    public String extractTextMongoDB(Binary fileBytes) {
        try {
            // 1️⃣ Binary-Daten aus MongoDB als InputStream umwandeln
            InputStream inputStream = new ByteArrayInputStream(fileBytes.getData());

            // 2️⃣ InputStream in RandomAccessRead umwandeln
            RandomAccessReadBuffer randomAccessRead = new RandomAccessReadBuffer(inputStream);

            // 3️⃣ PDFBox lädt die Datei direkt aus dem RandomAccessRead
            PDDocument document = Loader.loadPDF(randomAccessRead);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            return text;
        } catch (IOException e) {
            throw new RuntimeException("❌ Fehler beim Lesen der PDF: " + e.getMessage(), e);
        }
    }


}