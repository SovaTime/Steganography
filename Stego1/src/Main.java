import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Main {

    public static void main(String[] args) throws IOException {
        String message = "LSB (Least Significant Bit) - the essence of this method is to replace the last " +
                "significant bits in the container (in this work in the image) with bits of the hiding message.";
        encrypt("original.JPG", message);
        String decodedMessage = decrypt("encrypt.png");
        System.out.println("Hidden message: " + decodedMessage);

        SNR("original.JPG", "encrypt.png"); //Отношение сигнал-шум
        MSE("original.JPG", "encrypt.png"); //Среднее квадратичное отклонение
        //LMSE("original.JPG", "encrypt.png"); //Среднее квадратичное отклонение лапласиана
        LMSE("original.JPG", "encrypt.png");
    }

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

    public static void encrypt(String image, String message) throws IOException {
        BufferedImage img = ImageIO.read(new File(image));
        int width = img.getWidth();
        int height = img.getHeight();
        String binaryMess = toBinary(message);

        int index = 0;
        // Перебор пикселей изображения и встраивание битов сообщения в младшие биты
        for (int x = 0; x < width; x+=2) {
            for (int y = 0; y< height; y+=2) {
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
    }

    public static String decrypt(String image) throws IOException {
        BufferedImage img = ImageIO.read(new File(image));
        StringBuilder binaryMess = new StringBuilder();
        int stopIndex = -1;
        boolean flag = false;

        int width = img.getWidth();
        int height = img.getHeight();

        // Перебор пикселей и извлечение младших бит
        for (int x = 0; x < width; x+=2) {
            for (int y = 0; y< height; y+=2) {
                int pixel = img.getRGB(x, y);
                for (int i = 16; i >= 0; i -= 8) {
                    binaryMess.append((pixel >> i) & 1);
                    if (binaryMess.length() >= 16 && binaryMess.substring(binaryMess.length() - 16).equals("1111111111111110")) {
                        flag = true;
                        stopIndex = binaryMess.length() - 16;
                        break;
                    }
                }
                if (flag) {
                    break;
                }
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

    //Сравниваем изображения
    public static void compare(String originalImg, String encodedImg) throws IOException {
        BufferedImage orig = ImageIO.read(new File(originalImg));
        BufferedImage encode = ImageIO.read(new File(encodedImg));
        int width = orig.getWidth();
        int height = orig.getHeight();
        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Сравнение пикселей оригинального и закодированногоизображений
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int origPixel = orig.getRGB(x, y);
                int encodePixel = encode.getRGB(x, y);
                // Если пиксели различаются, устанавливаем белый цвет на изображение с различиями
                if (origPixel != encodePixel) {
                    diffImage.setRGB(x, y, 0xFFFFFF);
                }
            }
        }
        // Сохранение изображения с различиями
        ImageIO.write(diffImage, "png", new File("compare.png"));
    }

    public static void SNR (String originalImg, String modifiedImg) throws IOException {
        BufferedImage original = ImageIO.read(new File(originalImg));
        BufferedImage encrypt = ImageIO.read(new File(modifiedImg));
        int width = original.getWidth();
        int height = original.getHeight();
        double numerator = 0.0;
        double denominator = 0.0;
        // Перебор всех пикселей и вычисление числителя и знаменателя для SNR
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getRGB(x, y);
                int modifiedPixel = encrypt.getRGB(x, y);
                // Вычисление абсолютного отклонения для каждой компоненты цвета
                int deviationRed = Math.abs(new Color(originalPixel).getRed() - new Color(modifiedPixel).getRed());
                int deviationGreen = Math.abs(new Color(originalPixel).getGreen() - new Color(modifiedPixel).getGreen());
                int deviationBlue = Math.abs(new Color(originalPixel).getBlue() - new Color(modifiedPixel).getBlue());
                // Обновление числителя и знаменателя
                numerator += deviationRed * deviationRed + deviationGreen * deviationGreen + deviationBlue * deviationBlue;
                denominator += originalPixel * originalPixel + modifiedPixel * modifiedPixel;
            }
        }
        // Вычисление NMSE и возвращение SNR
        double nmse =  Math.abs(numerator / denominator);
        System.out.println("SNR: " + (1.0 / nmse));
    }

    public static void MSE (String originalImg, String modifiedImg) throws IOException {
        BufferedImage original = ImageIO.read(new File(originalImg));
        BufferedImage encrypt = ImageIO.read(new File(modifiedImg));
        int width = original.getWidth();
        int height = original.getHeight();
        double numerator = 0.0;

        // Перебор всех пикселей и вычисление множителя MSE
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getRGB(x, y);
                int modifiedPixel = encrypt.getRGB(x, y);
                // Вычисление абсолютного отклонения для каждой компоненты цвета
                int deviationRed = Math.abs(new Color(originalPixel).getRed() - new Color(modifiedPixel).getRed());
                int deviationGreen = Math.abs(new Color(originalPixel).getGreen() - new Color(modifiedPixel).getGreen());
                int deviationBlue = Math.abs(new Color(originalPixel).getBlue() - new Color(modifiedPixel).getBlue());
                // Обновление множителя
                numerator += deviationRed * deviationRed + deviationGreen * deviationGreen + deviationBlue * deviationBlue;
            }
        }
        double mseResult = (1.0 / (width*height)) * numerator;
        System.out.println("MSE: " + mseResult);
    }

    public static double[][] deltaSquared(double[][] imageArray) {
        double[][] kernel = {{0, 1, 0},
                             {1, -4, 1},
                             {0, 1, 0}};

        int height = imageArray.length;
        int width = imageArray[0].length;

        double[][] result = new double[height][width];

        // Применяем свертку
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sum = 0;
                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        sum += kernel[ky][kx] * imageArray[y + ky - 1][x + kx - 1];
                    }
                }
                result[y][x] = sum;
            }
        }

        return result;
    }

    // Функция вычисления среднего квадратичного отклонения лапласиана
    public static void LMSE(String originalImg, String modifiedImg) throws IOException {
        // Загружаем изображения
        BufferedImage original = ImageIO.read(new File(originalImg));
        BufferedImage encrypt = ImageIO.read(new File(modifiedImg));

        int width = original.getWidth();
        int height = original.getHeight();

        // Преобразование изображений в массивы
        double[][] imageArray1 = new double[height][width];
        double[][] imageArray2 = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                imageArray1[y][x] = original.getRGB(x, y) & 0xFF; // Преобразование в градации серого
                imageArray2[y][x] = encrypt.getRGB(x, y) & 0xFF; // Преобразование в градации серого
            }
        }

        // Вычисление дельт
        double[][] delta1 = deltaSquared(imageArray1);
        double[][] delta2 = deltaSquared(imageArray2);

        // Вычисление числителя и знаменателя формулы
        double numerator = 0;
        double denominator = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double diff = delta1[y][x] - delta2[y][x];
                numerator += diff * diff;
                denominator += delta1[y][x] * delta1[y][x];
            }
        }

        // Вычисление среднего квадратичного отклонения лапласиана
        double lmse = denominator != 0 ? numerator / denominator : 0;
        System.out.println("LMSE: " + lmse);
    }
}
