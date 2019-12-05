package agents.aponteTrautz;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;
import engine.helper.SpriteType;

import java.util.Arrays;
import java.util.Random;

public class Agent implements MarioAgent {
	private enum STATE {
		WALK_FORWARD, WALK_BACKWARD, JUMP, IDLE, JUMP_SHROOM
	};

	private boolean facing_left;
	private int leftCounter;
	private int jumpCount;  // counter to determine if you've done a 'full' jump yet
	private int shootCounter;
	private int powerUp;
	private int marioPos;
	private STATE state;
	int totalCount = 0;
	boolean waitingForPowerUp;
	boolean enemyAbove;
	private boolean[] action;

	@Override
	public void initialize(MarioForwardModel model, MarioTimer timer) {
		action = new boolean[MarioActions.numberOfActions()];
		state = STATE.WALK_FORWARD;
		facing_left = false;
		jumpCount = 0;
		leftCounter = 0;
		shootCounter = 0;
		powerUp = 0;
		boolean enemyAbove = false;
		boolean waitingForPowerUp = false;
	}

	// determines if jumping will cause you to hit an enemy or not
	private boolean safeToJumpFromEnemies(byte[][] enemiesFromBitmap) {
		for (int y = 5; y <= 9; y++) {
			for (int x = 11; x <= 14; x++) {
				if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean bricksAboveMario(int[][] levelSceneFromBitmap){
		for (int x = 10; x <= 10; x++){
			for(int y = 5; y <=6; y ++){
				if(levelSceneFromBitmap[x][y] == 24){
					return true;
				}
			}
		}
		return false;
	}

	private boolean isShroom(int[][] levelSceneFromBitmap){
		for (int x = 8; x <= 15; x++){
			for(int y = 0; y <=15; y ++){
				if(levelSceneFromBitmap[x][y] == 62){
					return true;
				}
			}
		}
		return false;
	}

	private boolean solidBrick(int[][] levelSceneFromBitmap){
		for (int x = 10; x <= 10; x++){
			for(int y = 5; y <=6; y ++){
				if(levelSceneFromBitmap[x][y] == 17 || levelSceneFromBitmap[x][y] == 22){
					return true;
				}
			}
		}
		return false;
	}

	private boolean powerUpBehind(int[][] levelSceneFromBitmap){
		for (int x = 0; x <= 12; x++){
			for(int y = 0; y <=12; y ++){
				if(levelSceneFromBitmap[x][y] == 12){
					return true;
				}
			}
		}
		return false;
	}

	private boolean stairsAhead(byte[][] levelSceneFromBitmap){
		for (int x = 11; x <= 11; x++){
			for(int y = 5; y <=7; y ++){
				if(levelSceneFromBitmap[x][y] == 17){
					return true;
				}
			}
		}
		return false;
	}

	// determines if jumping will land you in a gap
	private boolean safeToJumpFromGaps(byte[][] levelSceneFromBitmap) {
		for (int y = 9; y <= 9; y++) {
			boolean b = false;
			for (int x = 11; x <= 14; x++) {
				if (levelSceneFromBitmap[x][y] == 1) {
					b = true;
					break;
				}
			}
			if (!b) {
				return false;
			}
		}

		return true;
	}

	// determines if there are enemies close enough to pose a danger to you -
	// implies you should jump
	private boolean dangerFromEnemies(byte[][] enemiesFromBitmap) {
		for (int y = 5; y <= 9; y++) {
			for (int x = 7; x <= 12; x++) {
				if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
					return true;
				}
			}
		}
		return false;
	}

	// determines if there are enemies close enough to pose a danger to you -
	// implies you should jump
	private boolean dangerFromAbove(byte[][] enemiesFromBitmap, int marioY) {
		for (int y = marioY-1; y > 0 ; y--) {
			for (int x = 6; x <= 12; x++) {
				if (enemiesFromBitmap[x][y] == 1) {
					return true;
				}
			}
		}
		return false;
	}

	// determines if there is a gap close enough to pose a danger to you - implies
	// you should jump
	private boolean dangerFromGaps(byte[][] levelSceneFromBitmap) {
		for (int y = 9; y <= 10; y++) {
			for (int x = 9; x <= 12; x++) {
				if (levelSceneFromBitmap[x][y] == 0) {
					return true;
				}
			}
		}
		return false;
	}

	// determines if there is a gap close enough to pose a danger to you - implies
	// you should jump
	private int gapLength(byte[][] levelSceneFromBitmap, int marioX, int marioY) {
		int length = 0;
		for (int y = marioY; y <= marioY; y++) {
			for (int x = marioX+1; x <= marioX+6; x++) {
				if (levelSceneFromBitmap[x][y] == 0) {
					length ++;
				}
			}
		}
		return length;
	}

	// determines if it's safe to jump
	private boolean safeToJump(byte[][] levelSceneFromBitmap, byte[][] enemiesFromBitmap, int[][] completeObs) {
		return safeToJumpFromGaps(levelSceneFromBitmap) && safeToJumpFromEnemies(enemiesFromBitmap);
	}

	// determines if there is something blocking your path that you need to jump
	// over
	private boolean block(byte[][] levelSceneFromBitmap) {
		for (int y = 8; y <= 8; y++) {
			for (int x = 9; x <= 12; x++) {
				if (levelSceneFromBitmap[x][y] == 1) {
					return true;
				}
			}
		}

		return false;
	}

	private int obsHeight(byte[][] levelSceneFromBitmap, int currentX) {
		int height = 0;
		for (int x = 8; x <= 8; x++) {
			for (int y = 5; y <= 9; y++) {
				if (levelSceneFromBitmap[x][y] == 1) {
					height = height +1;
				}
			}
		}
		return height;
	}

	// function from ForwardAgent.java - I did not write this
	private byte[][] decode(MarioForwardModel model, int[][] state) {
		byte[][] dstate = new byte[model.obsGridWidth][model.obsGridHeight];
		for (int i = 0; i < dstate.length; ++i)
			for (int j = 0; j < dstate[0].length; ++j)
				dstate[i][j] = 2;

		for(int x=0; x<state.length; x++) {
			for(int y=0; y<state[x].length; y++) {
				if(state[x][y] != 0) {
					dstate[x][y] = 1;
				}
				else {
					dstate[x][y] = 0;
				}
			}
		}
		return dstate;
	}

	@Override
	public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
		int[][] completeObs = model.getMarioCompleteObservation(0,0);
		byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
		byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies
		System.out.println(Arrays.deepToString(completeObs));

		switch (state) {
			case WALK_FORWARD:
				System.out.println(state);
				System.out.println("DangerAbove: " + dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1]));
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());

				action[MarioActions.LEFT.getValue()] = false;
				action[MarioActions.RIGHT.getValue()] = true;
				action[MarioActions.JUMP.getValue()] = false;
				action[MarioActions.SPEED.getValue()] = true;

				// now, if you're in danger from enemies, or blocked by landscape, jump if it's
				// safe to. If there's danger of falling, jump no matter what

				if ((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
						&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs)) ||  levelSceneFromBitmap[model.getMarioScreenTilePos()[0]+1][9] == 0)
						&& model.mayMarioJump()) {
					action[MarioActions.SPEED.getValue()] = true;
					state = STATE.JUMP;
				}

				else if(!powerUpBehind(completeObs)&& !dangerFromEnemies(enemiesFromBitmap) && !safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs) && isShroom(completeObs) == true){
					jumpCount = 5;
					action[MarioActions.RIGHT.getValue()] = true;
					state = STATE.JUMP;
					System.out.println("SHORT JUMP");
				}

				//if there is a brick directly above mario and there are no enemies then we want to jump
				else if(model.getMarioMode() == 0 && bricksAboveMario(completeObs) && !dangerFromEnemies(enemiesFromBitmap)){
					System.out.println("IM A BRICK!!!");
					state = STATE.JUMP;
				}

//				else if(!dangerFromEnemies(enemiesFromBitmap) && !safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs) && dangerFromGaps(levelSceneFromBitmap)){
//					if(gapLength(levelSceneFromBitmap, model.getMarioScreenTilePos()[0], model.getMarioScreenTilePos()[1]) <=3){
//						action[MarioActions.RIGHT.getValue()] = true;
//						state = STATE.JUMP;
//						System.out.println("SHORT JUMP");
//						jumpCount = 8 - (gapLength(levelSceneFromBitmap, model.getMarioScreenTilePos()[0], model.getMarioScreenTilePos()[1]));
//
//					} else {
//						jumpCount = 0;
//						action[MarioActions.RIGHT.getValue()] = true;
//						state = STATE.JUMP;
//						System.out.println("SHORT JUMP");
//					}
//				}

				//if there is a enemy or obstacle, and we are unable to safely jump over it then move backwards.
				else if((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap)) && (safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs) != model.mayMarioJump())) {
					state = STATE.WALK_BACKWARD;
				}
				else if(model.getMarioMode() == 1 && levelSceneFromBitmap[9][model.getMarioScreenTilePos()[1]] ==1){
					state = STATE.JUMP;
				}
				break;

			case WALK_BACKWARD:
				facing_left = true;
				action[MarioActions.LEFT.getValue()] = true;
				action[MarioActions.RIGHT.getValue()] = false;
				leftCounter++;

				System.out.println(state);
				System.out.println("DangerAbove: " + dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1]));
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());

				if (leftCounter > 8) {
					facing_left = false;
					state = STATE.IDLE;
				}
				break;

			case JUMP:
				System.out.println(state);
				System.out.println("DangerAbove: " + dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1]));
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());

				action[MarioActions.JUMP.getValue()] = true;
				action[MarioActions.RIGHT.getValue()] = true;
				action[MarioActions.LEFT.getValue()] = false;
				action[MarioActions.SPEED.getValue()] = true;

				if (action[MarioActions.JUMP.getValue()] && jumpCount >= 8) {
					totalCount++;
					action[MarioActions.JUMP.getValue()] = false;
					action[MarioActions.SPEED.getValue()] = false;

					jumpCount = 0;

					if (!(((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
							&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs)) || dangerFromGaps(levelSceneFromBitmap))
							&& model.mayMarioJump()) {
						state = STATE.WALK_FORWARD;
					}
					else if(isShroom(completeObs) && dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1])){
						System.out.println(model.getMarioScreenTilePos()[1]);
						enemyAbove = true;
						state = STATE.JUMP_SHROOM;
					}
					else {
						state = STATE.IDLE;
					}
				}

				else if(powerUpBehind(completeObs) && !dangerFromEnemies(enemiesFromBitmap) && model.getMarioMode()==0){
					System.out.println("POWER UP DETECTED");
					waitingForPowerUp = true;
					action[MarioActions.RIGHT.getValue()] = false;
					action[MarioActions.JUMP.getValue()] = false;

					jumpCount = 0;
					state = STATE.IDLE;
				}

				// otherwise you're in the middle of jump, increment jumpCount
				else if (action[MarioActions.JUMP.getValue()]) {
					jumpCount++;
				}
				break;

			case IDLE:
				System.out.println(state);
				action[MarioActions.RIGHT.getValue()] = false;
				action[MarioActions.LEFT.getValue()] = false;
				action[MarioActions.JUMP.getValue()] = false;
				action[MarioActions.SPEED.getValue()] = false;

				System.out.println("DangerAbove: " + dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1]));
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());

//				if(dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1])){
//					enemyAbove = true;
//					state = STATE.IDLE;
//				}

//				 else if(enemyAbove){
//				 	state = STATE.IDLE;
//				 }

				if(waitingForPowerUp){
					if(isShroom(completeObs)){
						action[MarioActions.LEFT.getValue()] = true;
					}

					if(!powerUpBehind(completeObs) && isShroom(completeObs)){
						action[MarioActions.LEFT.getValue()] = false;
						waitingForPowerUp = false;
						jumpCount = 0;
						state = STATE.JUMP;
					}
					else if(!powerUpBehind(completeObs)){
						action[MarioActions.LEFT.getValue()] = false;
						waitingForPowerUp = false;
						state = STATE.WALK_FORWARD;
					}
					else {
						System.out.println("WAITING");
						state = STATE.IDLE;
					}
				}

				//If there is an obstacle in front of you and no enemy, jump exactly the height of the obstacle. Used for stairs/pipes.
				else if((safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs) != model.mayMarioJump()) && block(levelSceneFromBitmap) && !dangerFromEnemies(enemiesFromBitmap) && !dangerFromGaps(levelSceneFromBitmap)){
					System.out.println("MayMarioJump: " +  obsHeight(levelSceneFromBitmap, model.getMarioScreenTilePos()[0]));
					jumpCount = 8 - (1 + obsHeight(levelSceneFromBitmap, model.getMarioScreenTilePos()[0]));
					action[MarioActions.RIGHT.getValue()] = true;
					state = STATE.JUMP;
					System.out.println("SHORT JUMP");
				}
				else if(isShroom(completeObs) && dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1])){
					enemyAbove = true;
					state = STATE.JUMP_SHROOM;
				}

				// now, if you're in danger from enemies, or blocked by landscape, jump if it's
				// safe to. If there's danger of falling, jump no matter what
				else if (((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
						&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs)) || (levelSceneFromBitmap[model.getMarioScreenTilePos()[0]+1][9] == 0)))
						&& model.mayMarioJump()){
					action[MarioActions.RIGHT.getValue()] = true;
					state = STATE.JUMP;
					System.out.println("NORMAL JUMP");
				}

				//Run backwards if Mario is unable to jump and there is an enemy ahead, even if it is safe jump
				else if(dangerFromEnemies(enemiesFromBitmap) && !model.mayMarioJump() && safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs)) {
					state = STATE.WALK_BACKWARD;
					System.out.println("R");
				}

				//If there is an enemy ahead and it is not safe to jump it, jump as long as we are able to.
				else if(dangerFromEnemies(enemiesFromBitmap) && !safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs) && model.mayMarioJump() && !dangerFromGaps(levelSceneFromBitmap)){
					action[MarioActions.RIGHT.getValue()] = true;
					state = STATE.JUMP;
					System.out.println("Enemy Panic Jump");
				}
				else if (!dangerFromEnemies(enemiesFromBitmap) && !dangerFromGaps(levelSceneFromBitmap)){
					state = STATE.WALK_FORWARD;
				}
				else if((completeObs[model.getMarioScreenTilePos()[0]][model.getMarioScreenTilePos()[1]+1] == 34) || (completeObs[model.getMarioScreenTilePos()[0]][model.getMarioScreenTilePos()[1]+1] == 35) || (completeObs[model.getMarioScreenTilePos()[0]][model.getMarioScreenTilePos()[1]+1] == 36) || (completeObs[model.getMarioScreenTilePos()[0]][model.getMarioScreenTilePos()[1]+1] == 37)){
					state = STATE.JUMP;
				}

				else if(model.getMarioScreenTilePos()[1] < 8 &&levelSceneFromBitmap[model.getMarioScreenTilePos()[0]+1][9] != 0){
					state = STATE.JUMP;
				}
				else {
					state = STATE.IDLE;
				}
				break;


			case JUMP_SHROOM:
				System.out.println(state);
				System.out.println(model.getMarioScreenTilePos()[1]);
				System.out.println("DangerAbove: " + dangerFromAbove(enemiesFromBitmap, model.getMarioScreenTilePos()[1]));
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap, completeObs));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());

				action[MarioActions.RIGHT.getValue()] = false;
				action[MarioActions.LEFT.getValue()] = false;
				action[MarioActions.JUMP.getValue()] = true;
				action[MarioActions.SPEED.getValue()] = false;

				// if jump is active and jumpCount is too big, deactivate - jump is over and
				// you'll need to get ready for next one
				if (action[MarioActions.JUMP.getValue()] && jumpCount >= 8) {
					action[MarioActions.JUMP.getValue()] = false;
					jumpCount = 0;
					state = STATE.IDLE;
				}
				// otherwise you're in the middle of jump, increment jumpCount
				else if (action[MarioActions.JUMP.getValue()]) {
					jumpCount++;
				}
		}
		return action;
	}

	@Override
	public String getAgentName() {
		return "aponteTrautz";
	}
}