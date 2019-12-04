package agents.aponteTrautz;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

import java.util.Random;

public class Agent implements MarioAgent {
	private enum STATE {
		WALK_FORWARD, WALK_BACKWARD, JUMP, IDLE
	};

	private boolean facing_left;
	private int leftCounter;
	private int jumpCount;  // counter to determine if you've done a 'full' jump yet
	private int shootCounter;
	private STATE state;
	int totalCount = 0;
	private boolean[] action;

	@Override
	public void initialize(MarioForwardModel model, MarioTimer timer) {
		action = new boolean[MarioActions.numberOfActions()];
		state = STATE.WALK_FORWARD;
		facing_left = false;
		jumpCount = 0;
		leftCounter = 0;
		shootCounter = 0;
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

	private boolean bricksAboveMario(byte[][] levelSceneFromBitmap){
		for (int x = 8; x <= 8; x++){
			for(int y = 5; y <=7; y ++){
				if(levelSceneFromBitmap[x][y] == 1){
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
			for (int x = 8; x <= 12; x++) {
				if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
					return true;
				}
			}
		}
		return false;
	}

	private int locateEnemy(byte[][] enemiesFromBitmap) {
		for (int y = 5; y <= 9; y++) {
			for (int x = 8; x <= 12; x++) {
				if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
					return x;
				}
			}
		}
		return 0;
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

	// determines if it's safe to jump
	private boolean safeToJump(byte[][] levelSceneFromBitmap, byte[][] enemiesFromBitmap) {
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

	private int obsPos(byte[][] levelSceneFromBitmap) {
		for (int y = 8; y <= 8; y++) {
			for (int x = 9; x <= 12; x++) {
				if (levelSceneFromBitmap[x][y] == 1) {
					return x;
				}
			}
		}
		return 0;
	}

	private int obsHeight(byte[][] levelSceneFromBitmap, int currentX) {
		int height = 0;
		for (int x = currentX; x <= currentX; x++) {
			for (int y = 5; y <= 9; y++) {
				if (levelSceneFromBitmap[x][y] == 1) {
					height = height +1;
				}
			}
		}
		return height;
	}

	private boolean stuck(byte[][] levelSceneFromBitmap, int currentY) {
		int height = 0;
		for (int x = 8; x <= 8; x++) {
			for (int y = currentY ; y > currentY - 3; y--) {
				if (levelSceneFromBitmap[x][y] == 1) {
					return true;
				}
			}
		}
		return false;
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

	private int backValue(byte[][] levelSceneFromBitmap, int currentX){
		int spaces = 0;
		for (int x = currentX-4; x <= currentX; x++) {
			for (int y = 5; y <= 9; y++) {
				if (levelSceneFromBitmap[x][y] == 1) {
					spaces = spaces +1;
				}
			}
		}
		return spaces;
	}

	@Override
	public boolean[] getActions(MarioForwardModel model, MarioTimer timer) {
		byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
		byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies

		switch (state) {
			case WALK_FORWARD:
				System.out.println(state);
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());
				System.out.println(model.getMarioScreenTilePos()[1]);

				action[MarioActions.LEFT.getValue()] = false;
				action[MarioActions.RIGHT.getValue()] = true;
				action[MarioActions.JUMP.getValue()] = false;
				action[MarioActions.SPEED.getValue()] = false;

				if ((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
						&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap))
						&& model.mayMarioJump()){
					action[MarioActions.SPEED.getValue()] = true;
					state = STATE.JUMP;
				}
				else if((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap)) && !safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) {
					state = STATE.WALK_BACKWARD;
				}
				break;

			case WALK_BACKWARD:
				facing_left = true;
				action[MarioActions.LEFT.getValue()] = true;
				action[MarioActions.RIGHT.getValue()] = false;
				leftCounter++;

				System.out.println(state);
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());
				System.out.println(model.getMarioScreenTilePos()[1]);

				if (leftCounter > 8) {
					facing_left = false;
					state = STATE.IDLE;
				}

				break;

			case JUMP:
				System.out.println(state);
				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());
				System.out.println(model.getMarioScreenTilePos()[1]);

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
							&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap))
							&& model.mayMarioJump()) {
						state = STATE.WALK_FORWARD;
					}
					else {
						state = STATE.IDLE;
					}
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
				action[MarioActions.LEFT.getValue()] = false;
				action[MarioActions.SPEED.getValue()] = false;

				System.out.println("Enemy: " + dangerFromEnemies(enemiesFromBitmap));
				System.out.println("Block: " + block(levelSceneFromBitmap));
				System.out.println("Gaps: " + dangerFromGaps(levelSceneFromBitmap));
				System.out.println("SafeToJump: " + safeToJump(levelSceneFromBitmap, enemiesFromBitmap));
				System.out.println("MayMarioJump: " +  model.mayMarioJump());


					if((safeToJump(levelSceneFromBitmap, enemiesFromBitmap) != model.mayMarioJump()) && block(levelSceneFromBitmap) && !dangerFromEnemies(enemiesFromBitmap)){
						System.out.println("MayMarioJump: " +  obsHeight(levelSceneFromBitmap, model.getMarioScreenTilePos()[0]));
						jumpCount = 8 - (1 + obsHeight(levelSceneFromBitmap, model.getMarioScreenTilePos()[0]));
						action[MarioActions.RIGHT.getValue()] = true;
						state = STATE.JUMP;
						System.out.println("SHORT JUMP");
					}

					else if ((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
							&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap))
							&& model.mayMarioJump()){
//						jumpCount = 8 - ((model.getMarioScreenTilePos()[0] - locateEnemy(enemiesFromBitmap)));
						action[MarioActions.RIGHT.getValue()] = true;
						state = STATE.JUMP;
					}

					else if(dangerFromEnemies(enemiesFromBitmap) && !safeToJump(levelSceneFromBitmap, enemiesFromBitmap) && block(levelSceneFromBitmap)){
						state = STATE.WALK_BACKWARD;
					}

					else if(dangerFromEnemies(enemiesFromBitmap) && model.mayMarioJump()) {
						action[MarioActions.RIGHT.getValue()] = true;
						state = STATE.JUMP;
					}

					else if (!dangerFromEnemies(enemiesFromBitmap) && !dangerFromGaps(levelSceneFromBitmap)){
						state = STATE.WALK_FORWARD;
					}

					else {
						state = STATE.IDLE;
					}
					break;
				}

		return action;
	}

	@Override
	public String getAgentName() {
		return "aponteTrautz";
	}
}