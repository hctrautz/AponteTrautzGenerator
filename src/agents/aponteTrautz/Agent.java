package agents.aponteTrautz;

import engine.core.MarioAgent;
import engine.core.MarioForwardModel;
import engine.core.MarioTimer;
import engine.helper.MarioActions;

public class Agent implements MarioAgent {
	private enum STATE {
		WALK_FORWARD, WALK_BACKWARD, JUMP, IDLE
	};

	private boolean facing_left;
	private int leftCounter;
	private int jumpCount;  // counter to determine if you've done a 'full' jump yet
	private int shootCounter;
	private STATE state;
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
		for (int y = 7; y <= 9; y++) {
			for (int x = 8; x <= 12; x++) {
				if (!(x == 8 && y == 8) && enemiesFromBitmap[x][y] == 1) {
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
		byte[][] levelSceneFromBitmap = decode(model, model.getMarioSceneObservation()); // map of the scene
		byte[][] enemiesFromBitmap = decode(model, model.getMarioEnemiesObservation()); // map of enemies

		switch (state) {
			case WALK_BACKWARD:
				facing_left = true;
				System.out.println(state);
				if (leftCounter > 5) {
					state = STATE.WALK_FORWARD;
					facing_left = false;
					leftCounter = 0;
				}
				leftCounter++;
				action[MarioActions.LEFT.getValue()] = true;
				action[MarioActions.RIGHT.getValue()] = false;

				if ((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
						&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap))
						&& model.mayMarioJump()){
					state = STATE.JUMP;
				} else if (dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap)){
					state = STATE.JUMP;
				}

				break;

			case WALK_FORWARD:
				System.out.println(state);

				action[MarioActions.LEFT.getValue()] = false;
				action[MarioActions.RIGHT.getValue()] = true;
				action[MarioActions.JUMP.getValue()] = false;
				action[MarioActions.SPEED.getValue()] = true;

				if ((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
						&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap))
						&& model.mayMarioJump()){
					state = STATE.JUMP;
				} else if((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap)) && !safeToJump(levelSceneFromBitmap, enemiesFromBitmap)){
					state = STATE.WALK_BACKWARD;
				}
				break;

			case JUMP:
				System.out.println(state);

				action[MarioActions.JUMP.getValue()] = true;
				action[MarioActions.RIGHT.getValue()] = true;
				action[MarioActions.SPEED.getValue()] = true;

				if (action[MarioActions.JUMP.getValue()] && jumpCount >= 8) {
					action[MarioActions.JUMP.getValue()] = false;
					action[MarioActions.SPEED.getValue()] = false;
					jumpCount = 0;

					if (!(((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
							&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap))
							&& model.mayMarioJump()) {
						state = STATE.WALK_FORWARD;
					} else {
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

				if ((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap))
						&& safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap))
						&& model.mayMarioJump()){
					action[MarioActions.RIGHT.getValue()] = true;
					state = STATE.JUMP;
				}

				if(dangerFromEnemies(enemiesFromBitmap) && model.mayMarioJump()){
					action[MarioActions.RIGHT.getValue()] = true;
					state = STATE.JUMP;
				}

				if((safeToJump(levelSceneFromBitmap, enemiesFromBitmap) != model.mayMarioJump()) && block(levelSceneFromBitmap)){
					jumpCount = 7;
					action[MarioActions.RIGHT.getValue()] = true;
					state = STATE.JUMP;
				}

				else if (!dangerFromEnemies(enemiesFromBitmap) && !dangerFromGaps(levelSceneFromBitmap)){
					state = STATE.WALK_FORWARD;
				}
//				else if(model.mayMarioJump() == false){
//					state = STATE.WALK_BACKWARD;
//				}
				break;
		}
		return action;
	}

	@Override
	public String getAgentName() {
		return "aponteTrautz";
	}
}