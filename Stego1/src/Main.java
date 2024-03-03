import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            BufferedImage startImg = ImageIO.read(new File("IMG_2586.JPG"));
            String message = getMessage();
            String binaryMessage = toBinary(message);

            encryption(startImg, binaryMessage);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMessage(){
        String message = null;

        try (BufferedReader reader = new BufferedReader(new FileReader("message.txt"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            message = content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    public static String toBinary(String message){
            StringBuilder binMessage = new StringBuilder();
            for (int i = 0; i<message.length(); i++){
                char c = message.charAt(i);

                binMessage.append(Integer.toBinaryString(c));
            }

        return binMessage.toString();
    }

    public static void encryption (BufferedImage sttImg, String binaryMess) throws IOException {
        BufferedImage encryptImg = sttImg;
        // byte[] pixels = ((DataBufferByte) encryptImg.getRaster().getDataBuffer()).getData();
        int width = encryptImg.getWidth();
        int height = encryptImg.getHeight();
        int i = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                while (i < binaryMess.length() - 1){
                    i++;
                    Color color = new Color(encryptImg.getRGB(x, y));
                    int red;
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    if (binaryMess.charAt(i) == '1') {
                        red = color.getRed() | 1; //если 1
                    } else {
                        red = color.getRed() & ~1; //если 0
                    }

                    Color newColor = new Color(red, green, blue);

                    encryptImg.setRGB(x, y, newColor.getRGB());
                }
            }
        }


        File outputImg = new File("encryptIMG_2585.JPG");
        ImageIO.write(encryptImg, "JPG", outputImg);
    }
}