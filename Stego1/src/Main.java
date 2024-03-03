import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            BufferedImage startImg = ImageIO.read(new File("IMG_2586.JPG"));
            String message = getMessage();
            String binaryMessage = toBinary(message);

            byte[] pixels = ((DataBufferByte) startImg.getRaster().getDataBuffer()).getData();
            int width = startImg.getWidth();
            int height = startImg.getHeight();

            for (int y = 0; y<height; y++){
                for (int x = 0; x < width; x++){
                    Color color = new Color(startImg.getRGB(x, y));
                    
                }
            }
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
}