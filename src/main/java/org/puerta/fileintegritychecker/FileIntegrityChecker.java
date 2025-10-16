package org.puerta.fileintegritychecker;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author 777Styx
 */
public class FileIntegrityChecker {

    private static String getFileChecksum(MessageDigest digest, File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Verificador de Integridad de Archivos ---");
        System.out.println("1. Calcular y guardar el checksum");
        System.out.println("2. Verificar la integridad del archivo");
        System.out.print("Seleccione una opción: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            System.out.print("Ingrese la ruta del archivo a procesar: ");
            String filePath = scanner.nextLine();
            File file = new File(filePath);

            if (!file.exists()) {
                System.out.println("Error: El archivo no existe.");
                return;
            }

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String checksum = getFileChecksum(sha256, file);

            try (FileWriter writer = new FileWriter(filePath + ".checksum")) {
                writer.write(checksum);
                System.out.println("Checksum calculado y guardado en " + filePath + ".checksum");
            }

        } else if (choice == 2) {
            System.out.print("Ingrese la ruta del archivo a verificar: ");
            String filePath = scanner.nextLine();
            File file = new File(filePath);
            File checksumFile = new File(filePath + ".checksum");

            if (!file.exists() || !checksumFile.exists()) {
                System.out.println("Error: El archivo o el archivo de checksum no existen.");
                return;
            }

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String currentChecksum = getFileChecksum(sha256, file);

            String savedChecksum;
            try (BufferedReader reader = new BufferedReader(new FileReader(checksumFile))) {
                savedChecksum = reader.readLine();
            }

            System.out.println("Checksum actual: " + currentChecksum);
            System.out.println("Checksum guardado: " + savedChecksum);

            if (currentChecksum.equals(savedChecksum)) {
                System.out.println("El archivo está integro! El checksum coincide.");
            } else {
                System.out.println("ADVERTENCIA! El archivo ha sido alterado. El checksum no coincide.");
            }
        }
        scanner.close();
    }
}
