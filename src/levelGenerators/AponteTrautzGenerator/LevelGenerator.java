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
    private int sampleWidth = 9;
    private String folderName = "src/levelGenerators/AponteTrautzGenerator/levels/";

    public LevelGenerator() throws IOException {}

    //Retrieves the specified level from the /levels/original/ folder
    private String getLevel(int lvl) throws IOException {
        File[] listOfFiles = new File(folderName).listFiles();
        List<String> lines = Files.readAllLines(listOfFiles[lvl].toPath());
        String result = "";
        for(int i=0; i<lines.size(); i++) {
            result += lines.get(i) + "\n";
        }
        return result;
    }

    //Generates the sequence of levels that we will take from in order to construct our final level.
    public int [] runMarkov() throws IOException{
        int [] output = new int[999];
        // the state transition matrix for mapping probability of any given level chunk following immediately from the current one
        double[][] transition = {
                {0.004, 0.017, 0.023, 0.036, 0.046, 0.072, 0.091, 0.123, 0.177, 0.124, 0.116, 0.086, 0.062, 0.013, 0.010},
                {0.091, 0.177, 0.072, 0.017, 0.116, 0.013, 0.023, 0.036, 0.004, 0.010, 0.086, 0.046, 0.124, 0.123, 0.062},
                {0.072, 0.004, 0.091, 0.116, 0.062, 0.123, 0.017, 0.046, 0.010, 0.086, 0.036, 0.124, 0.023, 0.013, 0.177},
                {0.116, 0.086, 0.062, 0.072, 0.013, 0.023, 0.123, 0.010, 0.046, 0.124, 0.013, 0.017, 0.177, 0.091, 0.004},
                {0.036, 0.023, 0.177, 0.010, 0.124, 0.072, 0.046, 0.091, 0.086, 0.013, 0.116, 0.123, 0.004, 0.017, 0.062},
                {0.046, 0.036, 0.004, 0.177, 0.023, 0.116, 0.017, 0.123, 0.013, 0.091, 0.062, 0.072, 0.086, 0.124, 0.010},
                {0.062, 0.091, 0.023, 0.004, 0.046, 0.010, 0.116, 0.036, 0.072, 0.123, 0.177, 0.124, 0.017, 0.013, 0.086},
                {0.023, 0.072, 0.010, 0.017, 0.091, 0.046, 0.036, 0.062, 0.116, 0.013, 0.004, 0.086, 0.124, 0.177, 0.123},
                {0.177, 0.023, 0.086, 0.062, 0.017, 0.123, 0.091, 0.116, 0.046, 0.036, 0.013, 0.124, 0.072, 0.004, 0.010},
                {0.004, 0.123, 0.046, 0.086, 0.116, 0.013, 0.177, 0.036, 0.072, 0.124, 0.023, 0.062, 0.010, 0.017, 0.091},
                {0.023, 0.062, 0.091, 0.116, 0.036, 0.046, 0.004, 0.086, 0.017, 0.013, 0.072, 0.177, 0.123, 0.010, 0.124},
                {0.017, 0.046, 0.116, 0.036, 0.177, 0.072, 0.013, 0.123, 0.062, 0.086, 0.091, 0.004, 0.023, 0.124, 0.010},
                {0.062, 0.091, 0.023, 0.123, 0.004, 0.036, 0.046, 0.177, 0.124, 0.116, 0.013, 0.017, 0.086, 0.010, 0.072},
                {0.124, 0.123, 0.036, 0.010, 0.013, 0.177, 0.062, 0.004, 0.023, 0.046, 0.086, 0.116, 0.072, 0.091, 0.017},
                {0.046, 0.036, 0.072, 0.116, 0.023, 0.004, 0.091, 0.013, 0.086, 0.177, 0.062, 0.010, 0.123, 0.017, 0.124},
        };


        //The following code is inspired from https://introcs.cs.princeton.edu/java/98simulation/MarkovChain.java.html
        // Total # of States
        int N = 15;
        //Current state
        int state = N-1;
        //Total number of transitions preformed
        int transitionCount = 0;

        // run Markov chain
        for (int i = 0; i < N+1; i++) {
            output[transitionCount] = state;
            transitionCount++;

            double r = Math.random();
            //System.out.print(r);
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

    //Constructs our final level by pulling chunks from each level in the order that we generated using the Markov Chain
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) throws IOException {
        //Clear the previously constructed map
        model.clearMap();
        //Loop through until we have a level whose length/width matches the length we desire
        for(int i=0; i<model.getWidth() / sampleWidth; i++){
            try {
                //Calculate the width of the current level in the markov chain, that we want copy into our generated level
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

                //Create a model from the current level in the markov chain, so that we can compare it to what we already have created from the generated level
                MarioLevelModel newGen = new MarioLevelModel(lvlLength,16);
                newGen.copyFromString(lvl);

                //On the last iteration of the loop, copy the last 5 lines of the level that was selected by the markov chain. This ensures that every level we generate has a flag at the end.
                if(i+1 == model.getWidth()/sampleWidth){
                    model.copyFromString(i*sampleWidth, 0, newGen.getWidth()-sampleWidth, 0,  sampleWidth, newGen.getHeight(), lvl);
                } else {
                    //If there exists a gap made from Mushrooms that is 4 blocks long in the chunk we are about to copy, then chose another chunk from earlier in the level
                    if((newGen.getBlock((i * sampleWidth), 16) == '|' && newGen.getBlock((i * sampleWidth)+1, 16) == '|') && (newGen.getBlock((i * sampleWidth)+2, 16) == '|' && newGen.getBlock((i * sampleWidth)+3, 16) == '|') && newGen.getBlock((i * sampleWidth)+4, 16) == '|'){
                        model.copyFromString(i * sampleWidth, 0, (i * sampleWidth)/5, 0, sampleWidth, model.getHeight(), lvl);
                    }
                    //Prevents pipes from being placed at the start of a chunk
                    if((newGen.getBlock((i * sampleWidth), 3) == 'T' || newGen.getBlock((i * sampleWidth), 4) == '|')){
                        model.copyFromString(i * sampleWidth, 0, (i * sampleWidth)-1, 0, sampleWidth, model.getHeight(), lvl);
                    }

                    //Looks through the chunk we are about to copy, if there is a flag then chose another chunk from earlier in the level
                    for (int k = 0; k < sampleWidth+1; k++){
                        if(newGen.getBlock((i * sampleWidth)+k, 14) == 'F' || newGen.getBlock((i * sampleWidth)+k, 13) == 'F' || newGen.getBlock((i * sampleWidth)+k, 12) == 'F' || newGen.getBlock((i * sampleWidth)+k, 11) == 'F'){
                            model.copyFromString(i * sampleWidth, 0, (i * sampleWidth)/5, 0, sampleWidth, model.getHeight(), lvl);
                        }
                    }

                    //If there is 5 block wide gap in the chunk we are about to copy, then chose another chunk from earlier in the level
                    if((newGen.getBlock((i * sampleWidth), 16) == '-' && newGen.getBlock((i * sampleWidth)+1, 16) == '-') && (newGen.getBlock((i * sampleWidth)+2, 16) == '-' && newGen.getBlock((i * sampleWidth)+3, 16) == '-') && newGen.getBlock((i * sampleWidth)+4, 16) == '-'){
                        model.copyFromString(i * sampleWidth, 0, (i * sampleWidth)/5, 0, sampleWidth, model.getHeight(), lvl);
                    }

                    model.copyFromString(i * sampleWidth, 0, i * sampleWidth, 0, sampleWidth, model.getHeight(), lvl);
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
