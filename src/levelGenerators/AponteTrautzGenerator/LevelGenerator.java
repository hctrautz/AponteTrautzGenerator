package levelGenerators.AponteTrautzGenerator;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LevelGenerator implements MarioLevelGenerator{
    private final int GROUND_Y_LOCATION = 13;
    private final float GROUND_PROB = 0.4f;
    private final int OBSTACLES_LOCATION = 10;
    private final float OBSTACLES_PROB = 0.1f;
    private final int COLLECTIBLE_LOCATION = 3;
    private final float COLLECTIBLE_PROB = 0.05f;
    private final float ENMEY_PROB = 0.1f;
    private final int FLOOR_PADDING = 3;

    //Frequency table
    Map<String, Integer> freq = new HashMap<String, Integer>() {
        @Override
        public Integer get(Object key) {
            return containsKey(key) ? super.get(key) : 0;
        }
    };

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {


        // the state transition matrix
        double[][] transition = { { 0.386, 0.147, 0.202, 0.062, 0.140, 0.047, 0.016},
                { 0.107, 0.267, 0.227, 0.120, 0.207, 0.052, 0.020},
                { 0.035, 0.101, 0.188, 0.191, 0.357, 0.067, 0.061},
                { 0.021, 0.039, 0.112, 0.212, 0.431, 0.124, 0.061},
                { 0.009, 0.024, 0.075, 0.123, 0.473, 0.171, 0.125},
                { 0.000, 0.103, 0.041, 0.088, 0.391, 0.312, 0.155},
                { 0.000, 0.008, 0.036, 0.083, 0.364, 0.235, 0.274}
        };

        int N = 7;                        // number of states
        int state = N - 1;                // current state
        int steps = 0;                    // number of transitions

        // run Markov chain
        while (state > 0) {
            System.out.println(state);
            steps++;
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

        Random random = new Random();
        model.clearMap();
        for(int x=0; x<model.getWidth(); x++) {
            for(int y=0; y<model.getHeight(); y++) {
                model.setBlock(x, y, MarioLevelModel.EMPTY);
                if(y > GROUND_Y_LOCATION) {
                    if(random.nextDouble() < GROUND_PROB) {
                        model.setBlock(x, y, MarioLevelModel.GROUND);
                    }
                }
                else if(y > OBSTACLES_LOCATION){
                    if(random.nextDouble() < OBSTACLES_PROB) {
                        model.setBlock(x, y, MarioLevelModel.PYRAMID_BLOCK);
                    }
                    else if(random.nextDouble() < ENMEY_PROB) {
                        model.setBlock(x, y,
                                MarioLevelModel.getEnemyCharacters()[random.nextInt(MarioLevelModel.getEnemyCharacters().length)]);
                    }
                }
                else if(y > COLLECTIBLE_LOCATION) {
                    if(random.nextDouble() < COLLECTIBLE_PROB) {
                        model.setBlock(x, y,
                                MarioLevelModel.getCollectablesTiles()[random.nextInt(MarioLevelModel.getCollectablesTiles().length)]);
                    }
                }
            }
        }
        model.setRectangle(0, 14, FLOOR_PADDING, 2, MarioLevelModel.GROUND);
        model.setRectangle(model.getWidth() - 1 - FLOOR_PADDING, 14, FLOOR_PADDING, 2, MarioLevelModel.GROUND);
        model.setBlock(FLOOR_PADDING/2, 13, MarioLevelModel.MARIO_START);
        model.setBlock(model.getWidth() - 1 - FLOOR_PADDING/2, 13, MarioLevelModel.MARIO_EXIT);
        return model.getMap();
    }

    @Override
    public String getGeneratorName() {
        return "AponteTrautzGenerator";
    }

}
