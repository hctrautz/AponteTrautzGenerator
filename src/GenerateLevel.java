import engine.core.MarioGame;
import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.core.MarioTimer;

import java.io.IOException;

public class GenerateLevel {
    public static void printResults(MarioResult result) {
	System.out.println("****************************************************************");
	System.out.println("Game Status: " + result.getGameStatus().toString() + 
		" Percentage Completion: " + result.getCompletionPercentage());
	System.out.println("Lives: " + result.getCurrentLives() + " Coins: " + result.getCurrentCoins() + 
		" Remaining Time: " + (int)Math.ceil(result.getRemainingTime() / 1000f)); 
	System.out.println("Mario State: " + result.getMarioMode() +
		" (Mushrooms: " + result.getNumCollectedMushrooms() + " Fire Flowers: " + result.getNumCollectedFireflower() + ")");
	System.out.println("Total Kills: " + result.getKillsTotal() + " (Stomps: " + result.getKillsByStomp() + 
		" Fireballs: " + result.getKillsByFire() + " Shells: " + result.getKillsByShell() + 
		" Falls: " + result.getKillsByFall() + ")");
	System.out.println("Bricks: " + result.getNumDestroyedBricks() + " Jumps: " + result.getNumJumps() + 
		" Max X Jump: " + result.getMaxXJump() + " Max Air Time: " + result.getMaxJumpAirTime());
	System.out.println("****************************************************************");
    }
    
    public static void main(String[] args) throws IOException {
	MarioLevelGenerator generator = new levelGenerators.AponteTrautzGenerator.LevelGenerator();
	String level = generator.getGeneratedLevel(new MarioLevelModel(192, 16), new MarioTimer(5*60*60*1000));
	MarioGame game = new MarioGame();
	System.out.println("\n\n");
	System.out.println(level);

	printResults(game.runGame(new agents.robinBaumgarten.Agent(), level, 20, 0, true));
    }
}
