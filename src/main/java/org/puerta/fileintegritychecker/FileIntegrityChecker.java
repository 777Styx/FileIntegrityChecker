package org.puerta.fileintegritychecker;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Permite al usuario calcular el checksum SHA-256 de un archivo y guardarlo, o
 * verificar si un archivo ha sido modificado comparando su checksum actual con
 * uno previamente guardado.
 *
 *
 * @author 777Styx
 */
public class FileIntegrityChecker {

    /**
     * Punto de entrada principal. Muestra el menú y gestiona la selección del
     * usuario.
     *
     * @param args
     */
    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            int choice = 0;

            // Bucle de validación de entrada
            // No saldrá del bucle hasta que se ingrese "1" o "2".
            while (true) {
                System.out.println("--- Verificador de Integridad de Archivos ---");
                System.out.println("1. Calcular y guardar el checksum");
                System.out.println("2. Verificar la integridad del archivo");
                System.out.print("Seleccione una opcion (1 o 2): ");

                String line = scanner.nextLine();

                try {
                    // Intenta convertirla a número
                    choice = Integer.parseInt(line);
                    if (choice == 1 || choice == 2) {
                        break;
                    } else {
                        System.out.println("Opcion no valida. Por favor, ingrese 1 o 2.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada no valida. Por favor, ingrese un numero.");
                }
            }

            // Una vez que tenemos una opción válida, llamamos al método correspondiente
            if (choice == 1) {
                handleCalculateAndSave(scanner);
            } else if (choice == 2) {
                handleVerifyIntegrity(scanner);
            }
        }
        System.out.println("Programa finalizado.");
    }

    /**
     * Maneja la lógica para la "Opción 1": Calcular y guardar el checksum.
     *
     * @param scanner El objeto Scanner para leer la entrada del usuario.
     */
    private static void handleCalculateAndSave(Scanner scanner) {
        System.out.print("Ingrese la ruta del archivo a procesar: ");
        String filePath = scanner.nextLine();
        File file = new File(filePath);

        // 1. Verificación de existencia
        if (!file.exists()) {
            System.out.println("Error: El archivo no existe.");
            return;
        }

        try {
            // 2. Calculo del Checksum (usa SHA-256), este puede ser otro tambien.
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String checksum = getFileChecksum(sha256, file);

            // 3. Guardado del Checksum
            try (FileWriter writer = new FileWriter(filePath + ".checksum")) {
                writer.write(checksum);
                System.out.println("Checksum calculado y guardado en " + filePath + ".checksum");
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: No se pudo encontrar el algoritmo SHA-256.");
        } catch (IOException e) {
            System.out.println("Error de E/S (lectura/escritura): " + e.getMessage());
        }
    }

    /**
     * Maneja la logica para la "Opcion 2": Verificar la integridad del archivo.
     *
     * @param scanner El objeto Scanner para leer la entrada del usuario.
     */
    private static void handleVerifyIntegrity(Scanner scanner) {
        System.out.print("Ingrese la ruta del archivo a verificar: ");
        String filePath = scanner.nextLine();
        File file = new File(filePath);
        File checksumFile = new File(filePath + ".checksum");

        // 1. Verificacion de existencia de ambos archivos
        if (!file.exists()) {
            System.out.println("Error: El archivo a verificar no existe.");
            return;
        }
        if (!checksumFile.exists()) {
            System.out.println("Error: El archivo .checksum correspondiente no existe.");
            return;
        }

        try {
            // 2. Recalcular el checksum actual
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String currentChecksum = getFileChecksum(sha256, file);

            // 3. Leer el checksum previamente guardado
            String savedChecksum;
            // try-with-resources asegura que el BufferedReader se cierre automáticamente.
            try (BufferedReader reader = new BufferedReader(new FileReader(checksumFile))) {
                savedChecksum = reader.readLine();
            }

            if (savedChecksum == null || savedChecksum.isEmpty()) {
                System.out.println("Error: El archivo .checksum está vacío.");
                return;
            }

            // 4. Comparar y mostrar el resultado
            System.out.println("Checksum actual: " + currentChecksum);
            System.out.println("Checksum guardado: " + savedChecksum);

            if (currentChecksum.equals(savedChecksum)) {
                System.out.println("El archivo esta integro. El checksum coincide.");
            } else {
                System.out.println("AGUAS! El archivo ha sido alterado. El checksum no coincide.");
            }

        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: No se pudo encontrar el algoritmo SHA-256.");
        } catch (IOException e) {
            // Maneja errores de lectura del archivo original o del .checksum
            System.out.println("Error de E/S (lectura): " + e.getMessage());
        }
    }

    /**
     * Calcula el checksum (hash) de un archivo utilizando un algoritmo de
     * MessageDigest. Lee el archivo en trozos para manejar archivos grandes
     * eficientemente.
     *
     * @param digest El objeto MessageDigest configurado.
     * @param file El archivo para el que se calculará el checksum.
     * @return Una cadena que representa el checksum hexadecimal del archivo.
     * @throws IOException Si ocurre un error de E/S al leer el archivo.
     */
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        // Usamos try-with-resources para asegurar que FileInputStream se cierre solo,
        // incluso si ocurre un error durante la lectura.
        try (FileInputStream fis = new FileInputStream(file)) {
            // Buffer para leer el archivo en bloques de 1024 bytes.
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            // Lee el archivo en el buffer y actualiza el MessageDigest.
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        // Finaliza el calculo del hash y obtiene el array de bytes del digest.
        byte[] bytes = digest.digest();
        // Usa StringBuilder para construir la representacion hexadecimal del hash.
        StringBuilder sb = new StringBuilder();
        // Convierte cada byte del hash a su representación hexadecimal de dos digitos.
        for (int i = 0; i < bytes.length; i++) {
            // "%02x" asegura dos digitos hexadecimales, rellenando con un cero si es necesario.
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}
