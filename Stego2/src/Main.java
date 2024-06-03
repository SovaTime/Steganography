import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    private static final double LAMDA = 1;

    private static final int SIGMA = 2;

    public static void main(String[] args) throws IOException {
        String message = "some secret information with secret data";
        encryptKJB("original.JPG", message);
        String decodedMessage = decryptKJB("encryptKJB.png");
        System.out.println("Hidden message: " + decodedMessage);

        LP("original.JPG", "encryptKJB.png"); //Норма Минковского (p=2)
        MSE("original.JPG", "encryptKJB.png"); //Среднее квадратичное отклонение
        maxD("original.JPG", "encryptKJB.png"); //Максимальное абсолютное отклонение
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

    public static void encryptKJB(String image, String message) throws IOException {
        BufferedImage img = ImageIO.read(new File(image));
        int width = img.getWidth();
        int height = img.getHeight();
        String binaryMess = toBinary(message);

        int x1 = 0;
        int y1 = 0;

        for (int y = SIGMA; y < height - SIGMA; y += SIGMA + 1) {
            for (int x = SIGMA; x < width - SIGMA; x += SIGMA + 1) {

                // Получаем синюю цветовую компоненту пикселя
                int pixel = img.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                // Извлекаем бит сообщения
                if ((y1 * width + x1) < binaryMess.length()) {
                    char msgBit = binaryMess.charAt(y1 * width + x1);
                    blue = changeBlueValue(blue, calculateBrightness(red, green, blue), msgBit);

                    // Обновляем пиксель с измененной синей компонентой
                    int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    img.setRGB(x, y, newPixel);
                } else {
                    break;
                }
                x1++;
            }

            y1++;
            x1 = 0;
        }
        ImageIO.write(img, "png", new File("encryptKJB.png"));
    }

    private static int changeBlueValue(int bxy, double Lxy, char bit) {
        int result;
        if (bit == '1') {
            result = (int) (bxy + LAMDA * Lxy);
            if (result > 255) {
                result = 255;
            }
        } else {
            result = (int) (bxy - LAMDA * Lxy);
            if (result < 0) {
                result = 0;
            }
        }
        return result;
    }

    private static int calculateBrightness(int r, int g, int b) {
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    public static String decryptKJB(String image) throws IOException {
        BufferedImage img = ImageIO.read(new File(image));
        int width = img.getWidth();
        int height = img.getHeight();
        StringBuilder binaryMess = new StringBuilder();

        for (int y = SIGMA; y < height - SIGMA; y += SIGMA + 1) {
            for (int x = SIGMA; x < width - SIGMA; x += SIGMA + 1) {
                int pixel = img.getRGB(x, y);
                int blue = pixel & 0xFF;
                if (blue > average(img, x, y)) {
                    binaryMess.append(1);
                } else {
                    binaryMess.append(0);
                }
                if (binaryMess.toString().contains("111111111111110")) {
                    break;
                }
            }
        }

        int index = binaryMess.indexOf("111111111111110");
        String bin = binaryMess.substring(0, index+1);

        StringBuilder message = new StringBuilder();
        for (int i = 0; i < bin.length(); i += 8) {
            int endIndex = Math.min(i + 8, bin.length());
            int byteValue = Integer.parseInt(bin.substring(i, endIndex), 2);
            message.append((char) byteValue);
        }
        return message.toString();
    }

    private static double average(BufferedImage image, int x, int y) {
        double answer = 0;
        for (int i = 1; i <= SIGMA; i++) {
            int pixel1 = image.getRGB(x, y + i);
            int blue1 = pixel1 & 0xFF;
            int pixel2 = image.getRGB(x, y - i);
            int blue2 = pixel2 & 0xFF;
            int pixel3 = image.getRGB(x + i, y);
            int blue3 = pixel3 & 0xFF;
            int pixel4 = image.getRGB(x - i, y);
            int blue4 = pixel4 & 0xFF;
            answer += (blue1 + blue2 + blue3 + blue4);
        }
        return answer / (4 * SIGMA);
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

    //Вычисление максимального абсолютного отклонения между оригинальным и модифицированным изображением
    public static void maxD(String originalImg, String modifiedImg) throws IOException {
        BufferedImage original = ImageIO.read(new File(originalImg));
        BufferedImage encrypt = ImageIO.read(new File(modifiedImg));
        int width = original.getWidth();
        int height = original.getHeight();
        int maxDeviation = 0;
        // Перебор всех пикселей и вычисление абсолютного отклонения для каждой компоненты цвета
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getRGB(x, y);
                int modifiedPixel = encrypt.getRGB(x, y);
                // Вычисление абсолютного отклонения для каждой компоненты цвета
                int deviationRed = Math.abs(new Color(originalPixel).getRed() - new Color(modifiedPixel).getRed());
                int deviationGreen = Math.abs(new Color(originalPixel).getGreen() - new Color(modifiedPixel).getGreen());
                int deviationBlue = Math.abs(new Color(originalPixel).getBlue() - new Color(modifiedPixel).getBlue());
                // Выбор максимального абсолютного отклонения из всех компонент
                int mD = Math.max(deviationRed, Math.max(deviationGreen, deviationBlue));
                if (mD > maxDeviation){
                    maxDeviation = mD;
                }
            }
        }
        System.out.println("maxD: " + maxDeviation);
    }

    public static void LP  (String originalImg, String modifiedImg) throws IOException {
        BufferedImage original = ImageIO.read(new File(originalImg));
        BufferedImage encrypt = ImageIO.read(new File(modifiedImg));
        int width = original.getWidth();
        int height = original.getHeight();
        int max = 0;
        // Перебор всех пикселей и вычисление абсолютного отклонения для каждой компоненты цвета
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = original.getRGB(x, y);
                int modifiedPixel = encrypt.getRGB(x, y);
                // Вычисление абсолютного отклонения для каждой компоненты цвета
                int deviationRed = Math.abs(new Color(originalPixel).getRed() - new Color(modifiedPixel).getRed());
                int deviationGreen = Math.abs(new Color(originalPixel).getGreen() - new Color(modifiedPixel).getGreen());
                int deviationBlue = Math.abs(new Color(originalPixel).getBlue() - new Color(modifiedPixel).getBlue());
                // Выбор максимального абсолютного отклонения из всех компонент
                int mD = Math.max(deviationRed, Math.max(deviationGreen, deviationBlue));
                if (mD > max){
                    max = mD;
                }
            }
        }
        double ext = Math.sqrt(max);
        System.out.println("LP: " + ext);
    }
}