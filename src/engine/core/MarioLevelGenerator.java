package engine.core;

import java.io.IOException;

public interface MarioLevelGenerator {
    /**
     * Generate a playable mario level
     * @param model contain a model of the level
     */
    String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) throws IOException;
    
    /**
     * Return the name of the level generator
     * @return the name of the level generator
     */
    String getGeneratorName();
}
