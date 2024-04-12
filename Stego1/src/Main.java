import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Main {

    public static void main(String[] args) throws IOException {
        String message = "The LSB method. LSB (Least Significant Bit, least significant bit) — " +
                "the essence of this method is to replace the last significant bits in the container " +
                "(images, audio or video recordings) with the bits of the hidden message.";
        String keyFile = "key.txt"; // Сохранение псевдослучайной последовательности
        encryption("original.JPG", message);
    }

    public static String toBinary(String message){
        byte[] bytes = message.getBytes(); // Преобразование в массив байтов
        StringBuilder binaryMess = new StringBuilder();
        // Преобразование каждого байта в восьмеричное представление
        for (byte b : bytes) {
            binaryMess.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        // Добавление последовательности в конец сообщения
        binaryMess.append("1111111111111110");
        return binaryMess.toString();
    }

    private static ArrayList<Point> traversalOrder;
    // Создание псевдослучайного порядка
    public static ArrayList<Point> generateRandomTO(int width, int height) {
        ArrayList<Point> order = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                order.add(new Point(x, y));
            }
        }
        Collections.shuffle(order);
        return order;
    }
    // Сохранение псевдослучайного порядка
    private static void saveTO(String file, ArrayList<Point> order) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (Point point : order) {
                writer.println((int) point.getX() + " " + (int) point.getY());
            }
        }
    }
    // Загрузка из файла
    private static ArrayList<Point> loadTO(String file) throws IOException {
        ArrayList<Point> order = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" "); // разделитель может быть любым
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                order.add(new Point(x, y));
            }
        }
        return order;
    }

    public static void encryption (String image, String message) throws IOException {
        BufferedImage img = ImageIO.read(new File(image));
        // byte[] pixels = ((DataBufferByte) encryptImg.getRaster().getDataBuffer()).getData();
        int width = img.getWidth();
        int height = img.getHeight();
        String binaryMess = toBinary(message);

        traversalOrder = generateRandomTO(width, height);
        saveTO("key.txt", traversalOrder);

        int index = 0;
        // Перебор всех пикселей изображения в псевдослучайном порядке и встраивание битов сообщения в младшие биты каждой компоненты RGB
        for (Point point : traversalOrder) {
            int x = (int) point.getX();
            int y = (int) point.getY();
            int pixel = img.getRGB(x, y);
            for (int i = 16; i >= 0; i -= 8) {
                if (index < binaryMess.length()) {
                    // Извлекаем текущий бит из бинарной строки сообщения, обнуляем младший для встраивания и меняем бит
                    int bit = Character.getNumericValue(binaryMess.charAt(index));
                    pixel = (pixel & ~(1 << i));
                    pixel |= (bit << i);
                    index++;
                }
            }
            img.setRGB(x, y, pixel);
            // Если сообщение закодировано, сохраняем изображение и завершаем метод
            if (index >= binaryMess.length()) {
                ImageIO.write(img, "JPG", new
                        File("encode.JPG"));
                return;
            }
        }
    }

    public static void decryption (BufferedImage img){

    }
}