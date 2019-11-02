package levelGenerators.AponteTrautzGenerator;
import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

public class LevelGenerator implements MarioLevelGenerator{
    int [] order = runMarkov();
    private Random rnd;
    private int sampleWidth = 30;
    private String folderName = "levels/original/";
    private final int GROUND_Y_LOCATION = 13;
    private final float GROUND_PROB = 0.4f;
    private final int OBSTACLES_LOCATION = 10;
    private final float OBSTACLES_PROB = 0.1f;
    private final int COLLECTIBLE_LOCATION = 3;
    private final float COLLECTIBLE_PROB = 0.05f;
    private final float ENMEY_PROB = 0.1f;
    private final int FLOOR_PADDING = 3;

    public LevelGenerator() throws IOException {}

    private String getLevel(int lvl) throws IOException {
        File[] listOfFiles = new File(folderName).listFiles();
        List<String> lines = Files.readAllLines(listOfFiles[lvl].toPath());
        String result = "";
        for(int i=0; i<lines.size(); i++) {
            result += lines.get(i) + "\n";
        }
        return result;
    }

    public int [] runMarkov() throws IOException{
        int [] output = new int[999];
        // the state transition matrix
        //TODO: Expand to using all 15 original levels and actually calculate values regarding the probability
        double[][] transition = { { 0.386, 0.147, 0.202, 0.062, 0.140, 0.047, 0.016},
                { 0.107, 0.267, 0.227, 0.120, 0.207, 0.052, 0.020},
                { 0.035, 0.101, 0.188, 0.191, 0.357, 0.067, 0.061},
                { 0.021, 0.039, 0.112, 0.212, 0.431, 0.124, 0.061},
                { 0.009, 0.024, 0.075, 0.123, 0.473, 0.171, 0.125},
                { 0.000, 0.103, 0.041, 0.088, 0.391, 0.312, 0.155},
                { 0.000, 0.008, 0.036, 0.083, 0.364, 0.235, 0.274}
        };

        int N = 7; // number of states
        int state = N - 1; // current state
        int transitionCount = 0; // number of transitions

        // run Markov chain
        while (state > 0) {

            output[transitionCount] = state;
            transitionCount++;

            double r = Math.random();
            double sum = 0.0;
            // determine next state
            for (int j = 0; j < N; j++) {
                sum += transition[state][j];
                if (r <= sum) {
                    state = j;
                    break;
                }
            }
        }
        return output;
    }

    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) throws IOException {
        rnd = new Random();
        model.clearMap();
        for(int i=0; i<model.getWidth() / sampleWidth; i++){
            try {
                //TODO: Only copy the lines of code from the level that will match up with what we already have
                model.copyFromString(i*sampleWidth, 0, i*sampleWidth, 0, sampleWidth, model.getHeight(), getLevel(order[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "AponteTrautzGenerator";
    }

}
