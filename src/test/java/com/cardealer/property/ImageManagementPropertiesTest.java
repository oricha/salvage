package com.cardealer.property;

import com.cardealer.util.FileUploadUtil;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Provide;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.lifecycle.BeforeTry;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageManagementPropertiesTest {

    private FileUploadUtil fileUploadUtil;
    private Path uploadDir;

    @BeforeTry
    void setUp() throws IOException {
        fileUploadUtil = new FileUploadUtil();
        uploadDir = Files.createTempDirectory("image-props");
        ReflectionTestUtils.setField(fileUploadUtil, "uploadDir", uploadDir.toString());
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 22: Image Format and Size Validation")
    void imageFormatAndSizeValidation(@ForAll("contentTypes") String contentType,
                                      @ForAll boolean oversize) {
        MockMultipartFile file = oversize
            ? new MockMultipartFile("image", "vehicle.png", contentType, new byte[(10 * 1024 * 1024) + 10])
            : buildImageFile("vehicle.png", contentType, FileUploadUtil.MIN_WIDTH, FileUploadUtil.MIN_HEIGHT);

        boolean allowedType = contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp");
        if (allowedType && !oversize) {
            assertDoesNotThrow(() -> fileUploadUtil.validateFile(file));
        } else {
            assertThrows(IllegalArgumentException.class, () -> fileUploadUtil.validateFile(file));
        }
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 23: Image Dimension Validation")
    void imageDimensionValidation(@ForAll @IntRange(min = 100, max = 1400) int width,
                                  @ForAll @IntRange(min = 100, max = 1200) int height) {
        MockMultipartFile file = buildImageFile("vehicle.png", "image/png", width, height);

        if (width >= FileUploadUtil.MIN_WIDTH && height >= FileUploadUtil.MIN_HEIGHT) {
            assertDoesNotThrow(() -> fileUploadUtil.validateFile(file));
        } else {
            assertThrows(IllegalArgumentException.class, () -> fileUploadUtil.validateFile(file));
        }
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 24: Thumbnail Generation")
    void thumbnailGeneration(@ForAll @IntRange(min = 900, max = 1600) int width,
                             @ForAll @IntRange(min = 700, max = 1200) int height) throws IOException {
        MockMultipartFile file = buildImageFile("vehicle.png", "image/png", width, height);
        String saved = fileUploadUtil.saveFile(file);

        Path smallThumb = fileUploadUtil.getThumbnailPath(saved, FileUploadUtil.GRID_THUMBNAIL_WIDTH, FileUploadUtil.GRID_THUMBNAIL_HEIGHT);
        Path largeThumb = fileUploadUtil.getThumbnailPath(saved, FileUploadUtil.LIST_THUMBNAIL_WIDTH, FileUploadUtil.LIST_THUMBNAIL_HEIGHT);

        assertTrue(Files.exists(smallThumb));
        assertTrue(Files.exists(largeThumb));
        BufferedImage smallImage = ImageIO.read(smallThumb.toFile());
        BufferedImage largeImage = ImageIO.read(largeThumb.toFile());
        assertEquals(FileUploadUtil.GRID_THUMBNAIL_WIDTH, smallImage.getWidth());
        assertEquals(FileUploadUtil.GRID_THUMBNAIL_HEIGHT, smallImage.getHeight());
        assertEquals(FileUploadUtil.LIST_THUMBNAIL_WIDTH, largeImage.getWidth());
        assertEquals(FileUploadUtil.LIST_THUMBNAIL_HEIGHT, largeImage.getHeight());
    }

    @Provide
    Arbitrary<String> contentTypes() {
        return Arbitraries.of("image/jpeg", "image/png", "image/webp", "text/plain", "application/pdf");
    }

    private MockMultipartFile buildImageFile(String fileName, String contentType, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, new Color((x + y) % 255, x % 255, y % 255).getRGB());
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return new MockMultipartFile("image", fileName, contentType, outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
