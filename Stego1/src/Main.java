import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;
public class Main {
    private static ArrayList<Point> traversalOrder;
    // Сообщение в бинарную строку с фиксированной последовательностью в конце
    public static String toBinary(String message) {
        byte[] bytes = message.getBytes(); // Преобразование в массив байтов
        StringBuilder binaryMessage = new StringBuilder();
        // Преобразование байта в восьмеричное представление и добавление к общей бинарной строке
        for (byte b : bytes) {
            binaryMessage.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        // Фиксированная последовательности в конец сообщения
        binaryMessage.append("1111111111111110");
        return binaryMessage.toString();
    }
    // Создание псевдослучайного порядка обхода пикселей
    public static ArrayList<Point>
    generateRandomTO(int width, int height) {
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
                writer.println((int) point.getX() + " " + (int)
                        point.getY());
            }
        }
    }
    // Загрузка псевдослучайного порядка
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

    public static void encrypt(String image, String message) throws IOException {
        BufferedImage img = ImageIO.read(new File(image));
        int width = img.getWidth();
        int height = img.getHeight();
        String binaryMess = toBinary(message);

        traversalOrder = generateRandomTO(width, height);
        saveTO("key.txt", traversalOrder);

        int index = 0;
        // Перебор всех пикселей изображения в псевдослучайном порядке и встраивание битов сообщения в младшие биты
        for (Point point : traversalOrder) {
            int x = (int) point.getX();
            int y = (int) point.getY();
            int pixel = img.getRGB(x, y);
            for (int i = 16; i >= 0; i -= 8) {
                if (index < binaryMess.length()) {
                    // Извлекаем текущий бит, обнуляем младший для встраивания и меняем бит
                    int bit = Character.getNumericValue(binaryMess.charAt(index));
                    pixel = (pixel & ~(1 << i));
                    pixel |= (bit << i);
                    index++;
                }
            }
            img.setRGB(x, y, pixel);
            // Когда закодировано, сохраняем изображение и завершаем метод
            if (index >= binaryMess.length()) {
                ImageIO.write(img, "png", new File("encrypt.png"));
                return;
            }
        }
    }

    public static String decrypt(String image, String keyF) throws IOException {
        ArrayList<Point> traversalOrder = loadTO(keyF);
        BufferedImage img = ImageIO.read(new File(image));
        StringBuilder binaryMess = new StringBuilder();
        int stopIndex = -1;
        boolean flag = false;

        // Перебор всех пикселей в сохраненном порядке и извлечение младших бит
        for (Point point : traversalOrder) {
            int x = (int) point.getX();
            int y = (int) point.getY();
            int pixel = img.getRGB(x, y);
            for (int i = 16; i >= 0; i -= 8) {
                binaryMess.append((pixel >> i) & 1);
                if (binaryMess.length() >= 16 &&
                        binaryMess.substring(binaryMess.length() - 16).equals("1111111111111110")) {
                    flag = true;
                    stopIndex = binaryMess.length() - 16;
                    break;
                }
            }
            if (flag) {
                break;
            }
        }
        // Когда конец сообщения, декодируем бинарное сообщение в строку
        if (stopIndex != -1) {
            binaryMess.delete(stopIndex, binaryMess.length());
            StringBuilder message = new StringBuilder();
            for (int i = 0; i < binaryMess.length(); i += 8) {
                int endIndex = Math.min(i + 8, binaryMess.length());
                int byteValue = Integer.parseInt(binaryMess.substring(i, endIndex), 2);
                message.append((char) byteValue);
            }
            return message.toString();
        } else {
            System.out.println("Encoded message not found.");
            return "";
        }
    }

    public static void main(String[] args) throws IOException {
        String message = "algoritm LSB";
        String keyFile = "key.txt"; // Сохранение псевдослучайной последовательности
        encrypt("original.JPG", message);
        String decodedMessage = decrypt("encrypt.png", keyFile);
        System.out.println("Hidden message: " + decodedMessage);
    }
}
