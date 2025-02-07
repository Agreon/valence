package dev._00a.valence_extractor;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dev._00a.valence_extractor.extractors.Blocks;
import dev._00a.valence_extractor.extractors.Entities;
import dev._00a.valence_extractor.extractors.EntityData;
import dev._00a.valence_extractor.extractors.Packets;
import dev._00a.valence_extractor.extractors.Enchants;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.ReflectionFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main implements ModInitializer {
    public static final String MOD_ID = "valence_extractor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Magically creates an instance of a <i>concrete</i> class without calling its constructor.
     */
    public static <T> T magicallyInstantiate(Class<T> clazz) {
        var rf = ReflectionFactory.getReflectionFactory();
        try {
            var objCon = Object.class.getDeclaredConstructor();
            var con = rf.newConstructorForSerialization(clazz, objCon);
            return clazz.cast(con.newInstance());
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to magically instantiate " + clazz.getName(), e);
        }
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Starting extractors...");

        var extractors = new Extractor[]{new Blocks(), new Entities(), new EntityData(), new Packets(), new Enchants()};

        Path outputDirectory;
        try {
            outputDirectory = Files.createDirectories(Paths.get("valence_extractor_output"));
        } catch (IOException e) {
            LOGGER.info("Failed to create output directory.", e);
            return;
        }

        var gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

        for (var ext : extractors) {
            try {
                var out = outputDirectory.resolve(ext.fileName());
                var fileWriter = new FileWriter(out.toFile(), StandardCharsets.UTF_8);
                gson.toJson(ext.extract(), fileWriter);
                fileWriter.close();
                LOGGER.info("Wrote " + out.toAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("Extractor for \"" + ext.fileName() + "\" failed.", e);
            }
        }

        LOGGER.info("Done.");
        System.exit(0);
    }

    public interface Extractor {
        String fileName();

        JsonElement extract() throws Exception;
    }

    public record Pair<T, U>(T left, U right) {
    }
}
