package org.puerta.fileintegritychecker;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Clase principal para la aplicación de Verificación de Integridad de Archivos.
 * Permite al usuario calcular el checksum SHA-256 de un archivo y guardarlo, o
 * verificar si un archivo ha sido modificado comparando su checksum actual con
 * uno previamente guardado.
 *
 * @author 777Styx
 */
public class FileIntegrityChecker {

    /**
     * Calcula el checksum (hash) de un archivo utilizando un algoritmo de
     * MessageDigest. Lee el archivo en trozos para manejar archivos grandes
     * eficientemente.
     *
     * @param digest El objeto MessageDigest configurado (e.g., para SHA-256).
     * @param file El archivo para el que se calculará el checksum.
     * @return Una cadena que representa el checksum hexadecimal del archivo.
     * @throws Exception Si ocurre un error de E/S al leer el archivo.
     */
    private static String getFileChecksum(MessageDigest digest, File file) throws Exception {
        // Inicializa un flujo de entrada para leer el archivo.
        FileInputStream fis = new FileInputStream(file);
        // Buffer para leer el archivo en bloques de 1024 bytes.
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        // Lee el archivo en el buffer y actualiza el MessageDigest (función hash) hasta el final del archivo.
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        // Cierra el flujo de entrada del archivo para liberar recursos.
        fis.close();

        // Finaliza el cálculo del hash y obtiene el array de bytes del digest.
        byte[] bytes = digest.digest();
        // Usa StringBuilder para construir la representación hexadecimal del hash.
        StringBuilder sb = new StringBuilder();
        // Convierte cada byte del hash a su representación hexadecimal de dos dígitos.
        for (int i = 0; i < bytes.length; i++) {
            // "%02x" asegura dos dígitos hexadecimales, rellenando con un cero si es necesario.
            sb.append(String.format("%02x", bytes[i]));
        }
        // Retorna la cadena de checksum final.
        return sb.toString();
    }

    /**
     * El método principal que ejecuta la aplicación de verificación de
     * integridad. Presenta un menú al usuario para seleccionar entre
     * calcular/guardar o verificar el checksum.
     *
     * @param args Argumentos de la línea de comandos (no utilizados).
     * @throws Exception Maneja excepciones de E/S, NoSuchAlgorithmException (si
     * SHA-256 no está disponible), etc.
     */
    public static void main(String[] args) throws Exception {
        // Crea un objeto Scanner para leer la entrada del usuario desde la consola.
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Verificador de Integridad de Archivos ---");
        System.out.println("1. Calcular y guardar el checksum");
        System.out.println("2. Verificar la integridad del archivo");
        System.out.print("Seleccione una opción: ");
        // Lee la opción seleccionada por el usuario.
        int choice = scanner.nextInt();
        // Consumir la línea restante para evitar problemas en la siguiente lectura de línea.
        scanner.nextLine();

        if (choice == 1) {
            // --- Opción 1: Calcular y guardar el checksum ---
            System.out.print("Ingrese la ruta del archivo a procesar: ");
            String filePath = scanner.nextLine();
            File file = new File(filePath);

            // 1. Verificación de existencia
            if (!file.exists()) {
                System.out.println("Error: El archivo no existe.");
                return;
            }

            // 2. Cálculo del Checksum (usa SHA-256)
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String checksum = getFileChecksum(sha256, file);

            // 3. Guardado del Checksum
            // try-with-resources asegura que el FileWriter se cierre automáticamente.
            try (FileWriter writer = new FileWriter(filePath + ".checksum")) {
                writer.write(checksum);
                System.out.println("Checksum calculado y guardado en " + filePath + ".checksum");
            }

        } else if (choice == 2) {
            // --- Opción 2: Verificar la integridad del archivo ---
            System.out.print("Ingrese la ruta del archivo a verificar: ");
            String filePath = scanner.nextLine();
            File file = new File(filePath);
            // El archivo de checksum se espera con la extensión .checksum.
            File checksumFile = new File(filePath + ".checksum");

            // 1. Verificación de existencia de ambos archivos
            if (!file.exists() || !checksumFile.exists()) {
                System.out.println("Error: El archivo o el archivo de checksum no existen.");
                return;
            }

            // 2. Recalcular el checksum actual
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            String currentChecksum = getFileChecksum(sha256, file);

            // 3. Leer el checksum previamente guardado
            String savedChecksum;
            // try-with-resources asegura que el BufferedReader se cierre automáticamente.
            try (BufferedReader reader = new BufferedReader(new FileReader(checksumFile))) {
                savedChecksum = reader.readLine(); // Lee la primera línea que contiene el hash.
            }

            // 4. Comparar y mostrar el resultado
            System.out.println("Checksum actual: " + currentChecksum);
            System.out.println("Checksum guardado: " + savedChecksum);

            if (currentChecksum.equals(savedChecksum)) {
                System.out.println("El archivo está integro! El checksum coincide.");
            } else {
                System.out.println("ADVERTENCIA! El archivo ha sido alterado. El checksum no coincide.");
            }
        }
        // Cierra el objeto Scanner al finalizar la interacción.
        scanner.close();
    }
}
