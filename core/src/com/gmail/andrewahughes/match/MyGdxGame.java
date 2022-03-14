package com.gmail.andrewahughes.match;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;
import java.util.Random;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.IGameServiceListener;
import de.golfgl.gdxgamesvcs.MockGameServiceClient;
import de.golfgl.gdxgamesvcs.achievement.IAchievement;
import de.golfgl.gdxgamesvcs.achievement.IFetchAchievementsResponseListener;
import de.golfgl.gdxgamesvcs.gamestate.IFetchGameStatesListResponseListener;
import de.golfgl.gdxgamesvcs.gamestate.ILoadGameStateResponseListener;
import de.golfgl.gdxgamesvcs.gamestate.ISaveGameStateResponseListener;
import de.golfgl.gdxgamesvcs.leaderboard.IFetchLeaderBoardEntriesResponseListener;
import de.golfgl.gdxgamesvcs.leaderboard.ILeaderBoardEntry;

import static com.gmail.andrewahughes.match.Cell.aliveArray;
import static com.gmail.andrewahughes.match.Cell.cellRun;
import static com.gmail.andrewahughes.match.Cell.cellStep;
import static com.gmail.andrewahughes.match.Cell.cellTimer;
import static com.gmail.andrewahughes.match.Cell.drawCells;
import static com.gmail.andrewahughes.match.Cell.initialiseCells;
import static com.gmail.andrewahughes.match.Cell.neighbourArray;
import static com.gmail.andrewahughes.match.Cell.oscDuration;
import static com.gmail.andrewahughes.match.Cell.reduceBackground;
import static com.gmail.andrewahughes.match.Cell.remainArray;
import static com.gmail.andrewahughes.match.Cell.spawnChanceMax;
import static com.gmail.andrewahughes.match.Cell.spawnChanceMin;
import static com.gmail.andrewahughes.match.Cell.spawnChanceMod;
import static com.gmail.andrewahughes.match.Cell.spawnRandomRow;
import static com.gmail.andrewahughes.match.Cell.tempMaxIntensity;
import static com.gmail.andrewahughes.match.Cell.updateCells;
import static com.gmail.andrewahughes.match.Cell.osc;
import static com.gmail.andrewahughes.match.SpiralHelper.spiralSymbolList;

public class MyGdxGame extends ApplicationAdapter implements IGameServiceListener{
	public static final String LEADERBOARD1 = "BOARD1";
	public static final String ACHIEVEMENT1 = "ACH1";
	public static final String EVENT1 = "EVENT1";
	public static final String REPOLINK = "https://github.com/MrStahlfelge/gdx-gamesvcs";
	public static final String FILE_ID = "cloud";
	//best to use close to a common resolution, the fit viewport and projection matrix will display
	// this on screen without distorting or cropping the image, for the most part only the ratio is
	//important, so you could have 16 height and 9 width, but the font will be huge, but if you
	//got rid of the font everything would look normal
	public static final int HEIGHT = 1080;
	public static final int WIDTH = 720;
	public static final float UH = (float)(HEIGHT/100f);//1 percent of screen height
	public static final float UW = (float)(WIDTH/100f);//1 percent of screen width
	public static final float SYMBOLRADIUS = 5f*UW;
	public static float RECOMMENDEDSYMBOLRADIUS = SYMBOLRADIUS;
	public static final float AREAHEIGHT = HEIGHT/2-UH*2;
	public static final float AREAWIDTH = WIDTH;
	public static final float AREAMARGINX = (WIDTH-AREAWIDTH)/2;
	public static final float AREAMARGINY = (HEIGHT/2-AREAHEIGHT)/2;
	public static final float C30 = (float) Math.cos(Math.PI/6);
	private static int score=0;
	private static float timer=60;
	private static boolean timerPlaying=false;
	private static final float DISABLETOUCHTIMERMAX=1f;
	/**
	 * disable touch briefly when the game ends so you don't accidentally touch something
	 * this will be set to a brief time and count down to 0, during which time u can't touch
	 */
	private static float disableTouchTimer=DISABLETOUCHTIMERMAX;

	public IGameServiceClient gsClient;
	Skin skin;
	Stage stage;
	static Stage gameStage;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;
	ArrayList<Texture> sym = new ArrayList<Texture>();
	BitmapFont font;
	Label gsStatus;
	Label gsUsername;
	private TextButton signInButton;
	private TextureAtlas atlas;
	private TextField scoreFillin;
	private TextField cloudData;
	//these are static so that the symbolActor can call testMatch without needing a reference to
	//MyGdxGame
	static ArrayList<SymbolActor> symbolActorListTop = new ArrayList<SymbolActor>();
	static ArrayList<SymbolActor> symbolActorListBottom = new ArrayList<SymbolActor>();
	static Random r = new Random();
	/**
	 * this variable will store the correct match symbolId, it will be set in
	 * giveSymbolActorsRandomSymbolId
	 */
	private static int matchSymbolId;
	/**
	 * i will use this to help create a grid of positions to assign to the symbolActors
	 */
	static SpiralHelper spiralHelper;
	/**
	 * this will determine how many symbols will fit in each half of the screen, it will in turn
	 * determine the radius of the symbols, if there are more symbols they will have to be smaller
	 * to use all the screen space, the options should be 1, 7, 19, 37, 61 etc (1+(n-1)*6)
	 */
	private static final int MAXNUMBEROFSYMBOLS =7;



	@Override
	public void create() {

		Gdx.app.log("MYLOG","C30 = "+C30);
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		font = new BitmapFont();
		stage = new Stage(new FitViewport(WIDTH,HEIGHT));
		gameStage = new Stage(new FitViewport(WIDTH, HEIGHT));
		gameStage.getViewport().update(WIDTH,HEIGHT,true);
		batch.setProjectionMatrix(gameStage.getCamera().combined);
		shapeRenderer.setProjectionMatrix(gameStage.getCamera().combined);
		spiralHelper = new SpiralHelper(MAXNUMBEROFSYMBOLS);
		Gdx.input.setInputProcessor(gameStage);
		Gdx.app.log("MYLOG","test");
		addNewSymbolActors();
		for(int si=1;si<10;si++)
		{
			sym.add(new Texture("0"+si+".gif"));
		}
		for(int si=10;si<14;si++)
		{
			sym.add(new Texture(""+si+".gif"));
		}
		Cell.initialiseCells();
		//bounding boxes for actors
		//gameStage.setDebugAll(true);


//******************GPGS setup**********************************************************************
		{
			//comment out this line so that the mrstahlfelge  menu does nothing
			//Gdx.input.setInputProcessor(stage);
			prepareSkin();
			//need this if (gsClient == null) code so that they game works in desktop mode
			if (gsClient == null)
				gsClient = new MockGameServiceClient(1) {
					@Override
					protected Array<ILeaderBoardEntry> getLeaderboardEntries() {
						return null;
					}

					@Override
					protected Array<String> getGameStates() {
						return null;
					}

					@Override
					protected byte[] getGameState() {
						return new byte[0];
					}

					@Override
					protected Array<IAchievement> getAchievements() {
						return null;
					}

					@Override
					protected String getPlayerName() {
						return null;
					}
				};
			gsClient.setListener(this);
			prepareUI();
			gsClient.resumeSession();
			// needed in case the connection is pending
			refreshStatusLabel();
		}
	}

	/**
	 * adds a new symbolActor in the top and one in the bottom hald
	 */
	private static void addNewSymbolActors()
	{
		if(symbolActorListTop.size()< MAXNUMBEROFSYMBOLS) {
			symbolActorListTop.add(new SymbolActor(true, symbolActorListTop.size()));
			gameStage.addActor(symbolActorListTop.get(symbolActorListTop.size() - 1));
			symbolActorListBottom.add(new SymbolActor(false, symbolActorListBottom.size()));
			gameStage.addActor(symbolActorListBottom.get(symbolActorListBottom.size() - 1));
		}
		giveSymbolActorsRandomSymbolId();
	}

	/**
	 * make list of ints from 0 to (number of symbols -1)
	 * assign these at random to all the symbolsActor, in both halves of the screen
	 * now copy an int from a random symbolActor in the bottom half and duplicate it in a random
	 * symbolActor in the top half
	 */
	private static void giveSymbolActorsRandomSymbolId()
	{
		//generate a list of random unique numbers from 0 to (number of symbols - 1)
		//so if there are 2 symbols in the top and 2 in the bottom we will get the numbers
		//0 to 3 in a random order
		ArrayList<Integer> tempArrayList = getUniqueRandomNumberArrayList(symbolActorListBottom.size()+symbolActorListTop.size());
		if(tempArrayList.size()==2)
		{
			matchSymbolId=0;
			symbolActorListTop.get(0).setSymbolId(0);
			symbolActorListBottom.get(0).setSymbolId(0);
		}
		else {
			//assign the first half of the random numbers to the symbolActors in the symbolActorListTop
			for (int i = 0; i < symbolActorListTop.size(); i++) {
				symbolActorListTop.get(i).setSymbolId(tempArrayList.get(i));
			}
			//assign the second half to the symbolActorListBottom
			for (int i = symbolActorListTop.size(); i < symbolActorListBottom.size() + symbolActorListTop.size(); i++) {
				symbolActorListBottom.get(i - symbolActorListTop.size()).setSymbolId(tempArrayList.get(i));
			}
			if (symbolActorListBottom.size() + symbolActorListTop.size() < 14) {
				//now all the symbols will have a unique random symbolId, but we want 2 of them to match
				//choose a random symbolActor in the symbolActorListTop ArrayList and set its symbolId
				//to match that of a random symbolActor in the symbolActorListBottom
				matchSymbolId = symbolActorListBottom.get(r.nextInt(symbolActorListBottom.size())).getSymbolId();
				symbolActorListTop.get(r.nextInt(symbolActorListTop.size())).setSymbolId(matchSymbolId);
			} else /*if there are 7 symbolActors then the numbers 0 to 13 will be randomly distributed.
		if the number 13 is left as a symbolId we will get index out of bounds in the draw method
		since i have not done art for 14 symbols, so we need to make sure to overwrite the 13 symbol
		with whatever the duplicate symbolId is*/ {
				boolean thirteenFound = false;
				/*first locate the 13 */
				for (int i = 0; i < symbolActorListTop.size(); i++) {
					if (symbolActorListTop.get(i).getSymbolId() == 13) {
						/*if the 13 is in this array, copy a value from the other array to replace 13*/
						matchSymbolId = symbolActorListBottom.get(r.nextInt(symbolActorListBottom.size())).getSymbolId();
						symbolActorListTop.get(i).setSymbolId(matchSymbolId);
						thirteenFound = true;
						break;/*break the loop*/
					}
				}
				if (thirteenFound == false) {
					/*if the 13 was not in the top array it must be in the bottom, so do the same here*/
					for (int i = 0; i < symbolActorListBottom.size(); i++) {
						if (symbolActorListBottom.get(i).getSymbolId() == 13) {
							/*if the 13 is in this array, copy a value from the other array to replace 13*/
							matchSymbolId = symbolActorListTop.get(r.nextInt(symbolActorListTop.size())).getSymbolId();
							symbolActorListBottom.get(i).setSymbolId(matchSymbolId);
							break;/*break the loop*/
						}
					}
				}

			}
		}
	}

	/**
	 * this method will be called from the click listener of the symbolActor, the symbolId of that
	 * actor will be compared with the correct matchSymbolId to see if the match is correct
	 * @param symbolId the symbolId of the clicked symbolActor
	 */
	public static void testMatch(int symbolId)
	{
		if(symbolId==matchSymbolId)
		{
			matchFound();
		}
		else
		{
			matchFailed();
		}
	}

	private static void matchFound()
	{
		if(disableTouchTimer<=0) {
			Gdx.app.log("MYLOG", "Match found!");
			timerPlaying = true;
			addNewSymbolActors();
			reduceBackground();
			score++;

		}
	}

	private static void matchFailed()
	{
		if(disableTouchTimer<=0) {
			tempMaxIntensity();
			Gdx.app.log("MYLOG", "Match failed!");
			score--;
		}
	}

	private void gameOver()
	{
		disableTouch();
		Gdx.app.log("MYLOG","gameover, score:"+timer);
		/*game finishes after score reaches 7 or timer goes from 60 to 9 seconds
		, time remaining is submitted to leaderboard. if you fail a match score -1,
		so you will need more matches, will take more time, maximum theoretical score is
		60,000 which would be impossible*/
		int newScore=(int)(timer*1000);
		gsClient.submitToLeaderboard(LEADERBOARD1, newScore, gsClient.getGameServiceId());
		try {
			Gdx.app.log("MYLOG","start showing leaderboard, arg="+LEADERBOARD1);
			gsClient.showLeaderboards(LEADERBOARD1);
		} catch (GameServiceException e) {
			e.printStackTrace();
		}
		resetGame();
	}
	private static void disableTouch()
	{
		disableTouchTimer=DISABLETOUCHTIMERMAX;
	}
	private static void resetGame()
	{
		for(int sa=0;sa<symbolActorListTop.size();sa++)
		{
			symbolActorListTop.get(sa).addAction(Actions.removeActor());
		}
		for(int sa=0;sa<symbolActorListBottom.size();sa++)
		{
			symbolActorListBottom.get(sa).addAction(Actions.removeActor());
		}
		symbolActorListTop=new ArrayList<SymbolActor>();
		symbolActorListBottom=new ArrayList<SymbolActor>();
		addNewSymbolActors();
		score=0;
		timerPlaying=false;
		timer=60;
		initialiseCells();
	}

	/**
	 * This will return an array of random numbers from 0 to (size-1), no number will be repeated
	 * @param size determines the size of the array to be returned, also determines the maximum
	 *             value to be returned which will be (size-1)
	 */
	private static ArrayList<Integer> getUniqueRandomNumberArrayList(int size)
	{
		ArrayList<Integer> orderedNumbers = new ArrayList<Integer>();
		ArrayList<Integer> randomNumbers = new ArrayList<Integer>();
		//create a list of numbers from 0 to (size-1)
		for(int i = 0; i < size;i++)
		{
			orderedNumbers.add(i);
		}
		//remove each number from the orderedNumbers ArrayList at random and add them to the
		//randomNumbers ArrayList
		for(int i = 0; i < size;i++)
		{
			randomNumbers.add(orderedNumbers.remove(r.nextInt(orderedNumbers.size())));
		}
		return randomNumbers;
	}
 	private void prepareUI() {
		gsStatus = new Label("", skin);
		gsUsername = new Label("", skin);
		scoreFillin = new TextField("100", skin);
		cloudData = new TextField("", skin);

		Label repoLink = new Label(REPOLINK, skin);
		repoLink.setColor(.3f, .3f, 1f, 1f);
		repoLink.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.net.openURI(REPOLINK);
			}
		});

		signInButton = new TextButton("", skin);
		signInButton.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				gsSignInOrOut();
			}
		});

		TextButton showLeaderBoards = new TextButton("Show", skin);
		showLeaderBoards.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					Gdx.app.log("MYLOG","start showing leaderboard, arg="+LEADERBOARD1);
					gsClient.showLeaderboards(LEADERBOARD1);
				} catch (GameServiceException e) {
					e.printStackTrace();
				}
			}
		});
		showLeaderBoards.setVisible(
				gsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.ShowAllLeaderboardsUI));

		TextButton fetchLeaderboards = new TextButton("Fetch", skin);
		fetchLeaderboards.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				fetchLeaderboard(false);
			}
		});
		fetchLeaderboards.setVisible(
				gsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.FetchLeaderBoardEntries));

		TextButton submitToLeaderboard = new TextButton("Submit", skin);
		submitToLeaderboard.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int score;
				try {
					score = Integer.valueOf(scoreFillin.getText());
				} catch (NumberFormatException nfe) {
					score = 0;
				}

				if (score > 0)
					gsClient.submitToLeaderboard(LEADERBOARD1, score, gsClient.getGameServiceId());
			}
		});

		TextButton showAchievements = new TextButton("Show", skin);
		showAchievements.setVisible(
				gsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.ShowAchievementsUI));
		showAchievements.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					gsClient.showAchievements();
				} catch (GameServiceException e) {
					e.printStackTrace();
				}
			}
		});

		TextButton fetchAchievements = new TextButton("Fetch", skin);
		fetchAchievements.setVisible(
				gsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.FetchAchievements));
		fetchAchievements.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final MyDialog dialog = new MyDialog("Achievements");
				boolean fetchingNow = gsClient.fetchAchievements(new IFetchAchievementsResponseListener() {
					@Override
					public void onFetchAchievementsResponse(final Array<IAchievement> achievements) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								showAchievementsList(dialog, achievements);
							}
						});

					}
				});

				dialog.text(fetchingNow ? "Fetching..." : "Could not fetch");
				dialog.show(stage);
			}
		});

		TextButton unlockAchievement = new TextButton("unlock", skin);
		unlockAchievement.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gsClient.unlockAchievement(ACHIEVEMENT1);
			}
		});

		TextButton submitEvent1Btn = new TextButton("submit", skin);
		submitEvent1Btn.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gsClient.submitEvent(EVENT1, 1);
			}
		});

		TextButton loadFromCloud = new TextButton("load", skin);
		loadFromCloud.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gsClient.loadGameState(FILE_ID, new ILoadGameStateResponseListener() {
					@Override
					public void gsGameStateLoaded(byte[] gameState) {
						cloudData.setText(gameState != null ? new String(gameState) : "failed");
					}
				});
			}
		});
		TextButton saveToCloud = new TextButton("save", skin);
		saveToCloud.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gsClient.saveGameState(FILE_ID, cloudData.getText().getBytes(), 0,
						new ISaveGameStateResponseListener() {
							@Override
							public void onGameStateSaved(boolean success, String errorCode) {
								if (!success) {
									Dialog dialog = new MyDialog("Save");
									dialog.text("Failure: " + errorCode);
								}
							}
						});
			}
		});
		TextButton deleteFromCloud = new TextButton("delete", skin);
		deleteFromCloud.setVisible(gsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.GameStateDelete));
		deleteFromCloud.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gsClient.deleteGameState(FILE_ID, null);
			}
		});
		TextButton fetchCloudList = new TextButton("list", skin);
		fetchCloudList.setVisible(gsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.FetchGameStates));
		fetchCloudList.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final MyDialog dialog = new MyDialog("Cloud saved states");
				boolean fetchingNow = gsClient.fetchGameStates(new IFetchGameStatesListResponseListener() {
					@Override
					public void onFetchGameStatesListResponse(final Array<String> gameStates) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								dialog.getContentTable().clear();
								if (gameStates == null)
									dialog.text("Error fetching game states");
								else {
									String allKeys = "";
									for (int i = 0; i < gameStates.size; i++)
										allKeys += gameStates.get(i) + "\n";

									if (allKeys.isEmpty())
										allKeys = "(no saved game state)";

									dialog.text(allKeys);
								}
								dialog.reshow();
							}
						});
					}
				});
				dialog.text(fetchingNow ? "Fetching..." : "Could not fetch");
				dialog.show(stage);
			}
		});

		// Create a table that fills the screen. Everything else will go inside this table.
		Table table = new Table();
		table.setFillParent(true);
		table.defaults().pad(5);
		stage.addActor(table);

		table.add(new Label("Gdx-GameServices Demo App", skin)).colspan(3).padBottom(0);
		table.row();
		table.add(repoLink).padBottom(20).colspan(3);
		table.row();

		table.add(new Label("Game Service ID:", skin)).right();
		table.add(new Label(gsClient.getGameServiceId(), skin)).left();
		table.add();

		table.row();
		table.add(new Label("Status:", skin)).right();
		table.add(gsStatus).left();
		table.add(signInButton);

		table.row();
		table.add(new Label("User name:", skin)).right();
		table.add(gsUsername).left();
		table.add();

		table.row().padTop(10);
		table.add(new Label("Leaderboard:", skin)).right();
		table.add(new Label(LEADERBOARD1, skin)).left();

		Table leaderBoardButtons = new Table();
		leaderBoardButtons.defaults().uniform().pad(5);
		leaderBoardButtons.add(showLeaderBoards);
		leaderBoardButtons.add(fetchLeaderboards);
		table.add(leaderBoardButtons);

		table.row();
		table.add();
		table.add(scoreFillin);
		table.add(submitToLeaderboard);

		table.row().padTop(10);
		table.add(new Label("Achievements:", skin)).right();
		table.add(new Label(ACHIEVEMENT1, skin)).left();

		Table achievementsButtons = new Table();
		achievementsButtons.defaults().uniform().pad(5);
		achievementsButtons.add(showAchievements);
		achievementsButtons.add(fetchAchievements);
		achievementsButtons.add(unlockAchievement);
		table.add(achievementsButtons);

		table.row();
		table.add(new Label("Events:", skin)).right();
		table.add(new Label(EVENT1, skin));
		table.add(submitEvent1Btn);

		if (gsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.GameStateStorage)) {
			table.row();
			table.add(new Label("Cloud storage:", skin)).right();
			table.add(cloudData);
			Table storageButtons = new Table();
			storageButtons.add(saveToCloud);
			storageButtons.add(loadFromCloud);
			storageButtons.add(deleteFromCloud);
			storageButtons.add(fetchCloudList);
			table.add(storageButtons);
		}
	}
	private void fetchLeaderboard(boolean playerRelated) {
		final MyDialog dialog = new MyDialog("Leaderboard");

		if (!playerRelated) {
			TextButton nowPlayer = new TextButton("Player related", skin);
			nowPlayer.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					fetchLeaderboard(true);
				}
			});
			dialog.button(nowPlayer);
		}

		boolean fetchingNow = gsClient.fetchLeaderboardEntries(LEADERBOARD1, 8, playerRelated,
				new IFetchLeaderBoardEntriesResponseListener() {
					@Override
					public void onLeaderBoardResponse(final Array<ILeaderBoardEntry> leaderBoard) {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								showLeaderBoardEntries(dialog, leaderBoard);
							}
						});
					}
				});

		dialog.text(fetchingNow ? "Fetching..." : "Could not fetch");
		dialog.show(stage);
	}
	private void showAchievementsList(MyDialog dialog, Array<IAchievement> achievements) {
		dialog.getContentTable().clear();

		if (achievements == null) {
			dialog.text("Could not fetch achievements");
		} else if (achievements.size > 0) {
			Table resultTable = new Table();
			resultTable.defaults().pad(3, 5, 3, 5);

			for (int i = 0; i < achievements.size; i++) {
				IAchievement ach = achievements.get(i);
				resultTable.row();
				resultTable.add(new Label(ach.getTitle(), skin));
				resultTable.add(new Label(ach.isUnlocked() ? "unlocked" : "locked", skin));
				resultTable.add(new Label(Integer.toString((int) (ach.getCompletionPercentage() * 100)) + "%", skin));
			}

			dialog.getContentTable().add(resultTable);
		} else
			dialog.text("No achievements");

		dialog.reshow();
	}
	private void showLeaderBoardEntries(MyDialog dialog, Array<ILeaderBoardEntry> leaderBoard) {
		dialog.getContentTable().clear();

		if (leaderBoard == null) {
			dialog.text("Could not fetch leaderboard");
		} else if (leaderBoard.size > 0) {
			Table resultTable = new Table();
			resultTable.defaults().pad(3, 5, 3, 5);

			for (int i = 0; i < leaderBoard.size; i++) {
				ILeaderBoardEntry le = leaderBoard.get(i);
				resultTable.row();
				resultTable.add(new Label(le.getScoreRank(), skin));

				String userDisplayName = le.getUserDisplayName();
				if (le.getUserId() == null)
					userDisplayName = "(" + userDisplayName + ")";
				else if (le.isCurrentPlayer())
					userDisplayName = "*" + userDisplayName;
				resultTable.add(new Label(userDisplayName, skin));

				resultTable.add(new Label(le.getFormattedValue(), skin));
				resultTable.add(new Label(le.getScoreTag(), skin));
			}

			dialog.getContentTable().add(resultTable);
		} else
			dialog.text("No leaderboard entries");

		dialog.reshow();
	}
	private void gsSignInOrOut() {
		if (gsClient.isSessionActive())
			gsClient.logOff();
		else {
			if (!gsClient.logIn())
				Gdx.app.error("GS_ERROR", "Cannot sign in: No credentials or session id given.");

			refreshStatusLabel();
		}
	}
	private void refreshStatusLabel() {
		String newStatusText;
		String newUserText;

		if (gsClient.isSessionActive())
			newStatusText = "SESSION ACTIVE";
		else if (gsClient.isConnectionPending())
			newStatusText = "CONNECTING SESSION...";
		else
			newStatusText = "NO SESSION";

		gsStatus.setText(newStatusText);

		signInButton.setText(gsClient.isSessionActive() ? "Sign out" : "Sign in");

		newUserText = gsClient.getPlayerDisplayName();
		gsUsername.setText(newUserText != null ? newUserText : "(none)");
	}
	private void prepareSkin() {
		// skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but
		// strongly
		// recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
		skin = new Skin();
		atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin.addRegions(atlas);
		skin.load(Gdx.files.internal("skin/uiskin.json"));

	}
	@Override
	public void render() {
		//comment this out so that we dont see the gpgs menu

//		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 0);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//		stage.act(Math.min(Gdx.graphics.getDeltaTime(),1/30f));
//		stage.draw();
		Gdx.gl.glClearColor(0.8f, 0.8f, 0.8f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		/*this allows transparent shapes*/
		Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		drawCells(shapeRenderer);
		//shapeRenderer.setColor(new Color(0.0f,0.8f,0.99f,0f));
		//shapeRenderer.rect(0,0,WIDTH,HEIGHT);//draw a rect the size of the screen
		shapeRenderer.setColor(new Color(0.8f,0.8f,0.8f,0.2f));
        drawSymbolActorsShape(symbolActorListBottom);
        drawSymbolActorsShape(symbolActorListTop);
		shapeRenderer.rect(0,HEIGHT/2-(2*UH)/2,WIDTH,2*UH);//draw the mid line across the screen
        shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);

		gameStage.act(Math.min(Gdx.graphics.getDeltaTime(),1/30f));
		gameStage.draw();
		if(timerPlaying)
		{
			timer=timer-Gdx.graphics.getDeltaTime();
			if(score==7||timer<=0)
			{
				timerPlaying=false;
				gameOver();
			}
		}
		if(disableTouchTimer>0)
		{
			disableTouchTimer=disableTouchTimer-Gdx.graphics.getDeltaTime();
		}
		batch.begin();
		drawSymbolActorsFont(symbolActorListBottom);
		drawSymbolActorsFont(symbolActorListTop);
		/*
		drawScore();
		drawTimer();
		font.draw(batch,""+neighbourArray,10f*UW,HEIGHT/2f+10*UH);
		font.draw(batch,""+aliveArray,10f*UW,HEIGHT/2f+20*UH);
		font.draw(batch,""+remainArray,10f*UW,HEIGHT/2f+30*UH);
		font.draw(batch,""+spawnChanceMin,10f*UW,HEIGHT/2f+40*UH);
		font.draw(batch,""+spawnChanceMax,10f*UW,HEIGHT/2f+50*UH);
		font.draw(batch,""+symbolActorListTop.size(),10f*UW,HEIGHT/2f-10*UH);
		*/
		batch.end();
		spawnChanceMod=osc(oscDuration,0,0,timer*1000);
		if(cellRun&&timerPlaying)
		{
			cellTimer++;
			if(cellTimer>cellStep)
			{
				cellTimer = 0;
				updateCells();
				spawnRandomRow(0, spawnChanceMin + (spawnChanceMax - spawnChanceMin) * spawnChanceMod);
			}
		}
	}

	/**
	 * derives a Vector2 position from the symbolActor's positionId and topArea bool
	 * uses the helper array spiralHexList
	 * @param sa the SymbolActor that you want the position of
	 * @return Vector2
	 */
	public static Vector2 setSymbolActorPos(SymbolActor sa)
	{
		return new Vector2(spiralSymbolList.get(sa.getPositionId()).x*RECOMMENDEDSYMBOLRADIUS+AREAWIDTH/2+AREAMARGINX, spiralSymbolList.get(sa.getPositionId()).y*RECOMMENDEDSYMBOLRADIUS+(sa.isTopArea()?50*UH:0)+AREAHEIGHT/2+AREAMARGINY);
	}
	/**
	 * draw a 'point up' hex, centred on the x and y arg, radius r (max radius)
	 * @param x x pos
	 * @param y y pos
	 * @param r the longest radius
	 */
	public void drawHexOutline(float x, float y,float r)
	{
		shapeRenderer.line(	x- C30 *r,	y-r/2,	x,	y-r);			// \
		shapeRenderer.line(	x,	y-r,x+ C30 *r,y-r/2);				//  /
		shapeRenderer.line(	x+ C30 *r,y-r/2,x+ C30 *r,y+r/2);		//  |
		shapeRenderer.line(	x+ C30 *r,y+r/2,x,y+r);					//  \
		shapeRenderer.line(	x,y+r,x- C30 *r,y+r/2);					// /
		shapeRenderer.line(	x- C30 *r,y+r/2,x- C30 *r,	y-r/2);	// |
	}
	/**
	 * draw a filled 'point up' hex, centred on the x and y arg, radius r (max radius)
	 * @param x x pos
	 * @param y y pos
	 * @param r the longest radius
	 */
	public void drawHex(float x, float y,float r)
	{
		shapeRenderer.triangle(	x- C30 *r,	y-r/2,	x,	y-r,x,y);			// \
		shapeRenderer.triangle(	x,	y-r,x+ C30 *r,y-r/2,x,y);				//  /
		shapeRenderer.triangle(	x+ C30 *r,y-r/2,x+ C30 *r,y+r/2,x,y);	//  |
		shapeRenderer.triangle(	x+ C30 *r,y+r/2,x,y+r,x,y);					//  \
		shapeRenderer.triangle(	x,y+r,x- C30 *r,y+r/2,x,y);					// /
		shapeRenderer.triangle(	x- C30 *r,y+r/2,x- C30 *r,	y-r/2,x,y);	// |
	}

	/**
	 * draw a circle in the position of the SymbolActor
	 * @param symbolActorList will need to pass in symbolActorListBottom and then symbolActorListTop
	 */
	public void drawSymbolActorsShape(ArrayList<SymbolActor> symbolActorList)
	{
		for (int dsash = 0; dsash<symbolActorList.size();dsash++)
		{
			drawHex(symbolActorList.get(dsash).getPos().x, symbolActorList.get(dsash).getPos().y, RECOMMENDEDSYMBOLRADIUS);
		}
	}
	/**
	 * draw text in the position of the SymbolActor, this method will be replaced by an actual symbol bitmap eventually
	 * @param symbolActorList will need to pass in symbolActorListBottom and then symbolActorListTop
	 */
	public void drawSymbolActorsFont(ArrayList<SymbolActor> symbolActorList)
	{
		for (int dsaf = 0; dsaf<symbolActorList.size();dsaf++)
		{
			font.draw(batch, ""+symbolActorList.get(dsaf).getSymbolId(), symbolActorList.get(dsaf).getPos().x, symbolActorList.get(dsaf).getPos().y);
			batch.draw(sym.get(symbolActorList.get(dsaf).getSymbolId()),symbolActorList.get(dsaf).getPos().x-RECOMMENDEDSYMBOLRADIUS/2,symbolActorList.get(dsaf).getPos().y-RECOMMENDEDSYMBOLRADIUS/2,RECOMMENDEDSYMBOLRADIUS,RECOMMENDEDSYMBOLRADIUS);
		}
	}
	public void drawScore()
	{
		font.draw(batch,""+score,10f*UW,HEIGHT/2f);
	}
	public void drawTimer()
	{
		font.draw(batch,""+Math.floor(timer*100)/100,90f*UW,HEIGHT/2f);
	}
	@Override
	public void resize(int width, int height)
	{
		//stage.getViewport().update(width,height,true);
		gameStage.getViewport().update(width,height,true);
		batch.setProjectionMatrix(gameStage.getCamera().combined);
		shapeRenderer.setProjectionMatrix(gameStage.getCamera().combined);
	}
	@Override
	public void dispose()
	{
		stage.dispose();
		gameStage.dispose();
		skin.dispose();
		atlas.dispose();
		batch.dispose();
		shapeRenderer.dispose();
		for(int di=0;di<13;di++)
		{
			sym.get(di).dispose();
		}
	}
	@Override
	public void pause(){
		super.pause();
		resetGame();
		gsClient.pauseSession();
	}
	@Override
	public void resume(){
		super.resume();
		gsClient.resumeSession();
	}
	@Override
	public void gsOnSessionActive() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				refreshStatusLabel();
			}
		});
	}
	@Override
	public void gsOnSessionInactive() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				refreshStatusLabel();
			}
		});
	}
	@Override
	public void gsShowErrorToUser(GsErrorType et, String msg, Throwable t) {
		Dialog dialog = new MyDialog("Error");
		dialog.text(et.toString() + ": " + msg);
		dialog.show(stage);

	}
	public class MyDialog extends Dialog{
		public MyDialog(String title){
			super(title,skin);
			super.button("OK");
		}
		public void reshow(){
			this.show(stage,Actions.alpha(1)).setPosition(Math.round((stage.getWidth()-this.getWidth())/2),
					Math.round((stage.getHeight() - this.getHeight()) / 2));
		}
	}
}