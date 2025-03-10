package com.example.chatbot.Embedding;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;




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


}