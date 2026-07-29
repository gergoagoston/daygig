package edu.bbte.daygig.backend.service.avatar;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import edu.bbte.daygig.backend.exception.ServiceException;

@Component
public class AvatarImageProcessor {

    public static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024;

    private static final int MAX_AVATAR_DIMENSION = 256;
    private static final long MAX_SOURCE_PIXELS = 25_000_000;
    private static final String JPEG_FORMAT = "JPEG";
    private static final String PNG_FORMAT = "PNG";

    public ProcessedAvatar process(MultipartFile file) {
        validateFile(file);

        try (ImageInputStream imageInput = ImageIO.createImageInputStream(file.getInputStream())) {
            if (imageInput == null) {
                throw invalidImage();
            }

            ImageReader reader = supportedReader(imageInput);
            try {
                reader.setInput(imageInput, true, true);
                validateDimensions(reader.getWidth(0), reader.getHeight(0));

                String format = normalizedFormat(reader.getFormatName());
                BufferedImage sourceImage = reader.read(0);
                BufferedImage resizedImage = resize(sourceImage, format);
                return encode(resizedImage, format);
            } finally {
                reader.dispose();
            }
        } catch (IOException ex) {
            throw invalidImage();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Avatar image is required");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ServiceException("Avatar image cannot exceed 2 MB");
        }
    }

    private ImageReader supportedReader(ImageInputStream imageInput) {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
        if (!readers.hasNext()) {
            throw invalidImage();
        }

        ImageReader reader = readers.next();
        String format;
        try {
            format = normalizedFormat(reader.getFormatName());
        } catch (IOException ex) {
            reader.dispose();
            throw invalidImage();
        }

        if (!JPEG_FORMAT.equals(format) && !PNG_FORMAT.equals(format)) {
            reader.dispose();
            throw new ServiceException("Avatar image must be a JPEG or PNG file");
        }

        return reader;
    }

    private String normalizedFormat(String format) {
        if ("JPG".equalsIgnoreCase(format) || JPEG_FORMAT.equalsIgnoreCase(format)) {
            return JPEG_FORMAT;
        }

        return format.toUpperCase(Locale.ROOT);
    }

    private void validateDimensions(int width,
                                    int height) {
        if (width <= 0 || height <= 0 || (long) width * height > MAX_SOURCE_PIXELS) {
            throw new ServiceException("Avatar image dimensions are too large");
        }
    }

    private BufferedImage resize(BufferedImage source,
                                 String format) {
        double scale = Math.min(1.0,
                (double) MAX_AVATAR_DIMENSION / Math.max(source.getWidth(), source.getHeight()));
        int targetWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int targetHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int imageType = PNG_FORMAT.equals(format) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, imageType);

        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }

        return target;
    }

    private ProcessedAvatar encode(BufferedImage image,
                                   String format) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, format, output)) {
            throw invalidImage();
        }

        String contentType = PNG_FORMAT.equals(format) ? "image/png" : "image/jpeg";
        return new ProcessedAvatar(output.toByteArray(), contentType);
    }

    private ServiceException invalidImage() {
        return new ServiceException("Uploaded file is not a valid JPEG or PNG image");
    }
}
