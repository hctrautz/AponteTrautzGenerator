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
    private int sampleWidth = 20;
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
        System.out.print(lvl + "  ");
        String result = "";
        for(int i=0; i<lines.size(); i++) {
            result += lines.get(i) + "\n";
        }
        return result;
    }

    public int [] runMarkov() throws IOException{
        int [] output = new int[999];
        // the state transition matrix
        //TODO: add new values to calculate regarding the probability of state transitions
        double[][] transition = {
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
                {0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.066, 0.067},
        };

        int N = 15; // number of states
        int state = N-1; // current state
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
        MarioLevelModel oldGen = new MarioLevelModel(100,100);

        model.clearMap();

        for(int i=0; i<model.getWidth() / sampleWidth; i++){
            try {
                String lvl = getLevel(order[i]);
                char [] lvlArr = lvl.toCharArray();

                int lvlLength = 0;
                for(char c: lvlArr){
                    if(c == '\n'){
                        break;
                    } else{
                        lvlLength ++;
                    }
                }

                MarioLevelModel newGen = new MarioLevelModel(lvlLength,16);
                newGen.copyFromString(lvl);

                //TODO: Only copy the lines of code from the level that will match up with what we already have
                //TODO: Only copy last lines of code from a level when generating the new last lines. i.e flag should always and only be at the end.

                //needs if statements that compare old model to new model to see if it is okay to continue
                //oldGen.getBlock(oldGen.getWidth(), 4) == 'F'
                if(i+1 == model.getWidth()/sampleWidth){
                    System.out.println("\n Last State! \n" + "State #: " + order[i] + "\n");
                    System.out.println(newGen.getWidth());
                    model.copyFromString(i*sampleWidth, 0, newGen.getWidth()-sampleWidth, 0, sampleWidth, newGen.getHeight(), lvl);
                    System.out.print(i);
                } else {
                    model.copyFromString(i*sampleWidth, 0, i*sampleWidth, 0, sampleWidth, model.getHeight(), lvl);
                    oldGen = model.clone(); //saves a copy of the model
                    System.out.print(i);
                }

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
