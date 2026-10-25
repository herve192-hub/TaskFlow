package com.taskflow.user_service.util;

import com.taskflow.user_service.exception.InvalidUserException;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

/**
 * Validates browser-supplied PNG/JPEG data URLs before embedding them in a profile document.
 * Enforces encoded size and actual decoded image format/dimensions with ImageIO; blanks clear
 * the photo. Frontend avatar.ts performs the user-friendly crop/compression, while this server
 * check remains the storage boundary and never fetches an external image URL.
 */
public final class AvatarImages {
    private AvatarImages() {}

    public static String validate(String value) {
        if (value == null || value.isBlank()) return null;
        if (value.length() > 262144) throw invalid();
        String format;
        String prefix;
        if (value.startsWith("data:image/png;base64,")) {
            format = "png"; prefix = "data:image/png;base64,";
        } else if (value.startsWith("data:image/jpeg;base64,")) {
            format = "JPEG"; prefix = "data:image/jpeg;base64,";
        } else throw invalid();
        try {
            byte[] bytes = Base64.getDecoder().decode(value.substring(prefix.length()));
            try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw invalid();
                var reader = readers.next();
                try {
                    reader.setInput(input, true, true);
                    if (!reader.getFormatName().equalsIgnoreCase(format)
                            || reader.getWidth(0) > 512 || reader.getHeight(0) > 512
                            || reader.getWidth(0) < 1 || reader.getHeight(0) < 1) throw invalid();
                    // Decode after checking dimensions so malformed/truncated files cannot be saved.
                    if (reader.read(0) == null) throw invalid();
                } finally { reader.dispose(); }
            }
            return value;
        } catch (Exception error) {
            throw invalid();
        }
    }

    private static InvalidUserException invalid() {
        return new InvalidUserException("Choose a PNG or JPEG profile photo up to 512 by 512 pixels");
    }
}
