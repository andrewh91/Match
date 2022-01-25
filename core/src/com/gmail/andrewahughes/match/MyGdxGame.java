package com.gmail.andrewahughes.match;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

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

public class MyGdxGame extends ApplicationAdapter implements IGameServiceListener{
	public static final String LEADERBOARD1 = "BOARD1";
	public static final String ACHIEVEMENT1 = "ACH1";
	public static final String EVENT1 = "EVENT1";
	public static final String REPOLINK = "https://github.com/MrStahlfelge/gdx-gamesvcs";
	public static final String FILE_ID = "cloud";
	public static final int HEIGHT = 1080/2;//my laptop is too small to display 1080, so half the display size
	public static final int WIDTH = 720/2;
	public static final float UH = (float)(HEIGHT/100f);//1 percent of screen height
	public static final float UW = (float)(WIDTH/100f);//1 percent of screen width
	public static final float SYMBOLRADIUS = 5f*UW;//1 percent of screen width

	public IGameServiceClient gsClient;
	Skin skin;
	Stage stage;
	static Stage gameStage;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;
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

	@Override
	public void create() {
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		font = new BitmapFont();
		stage = new Stage(new ExtendViewport(800, 450));
		gameStage = new Stage(new ExtendViewport(WIDTH, HEIGHT));
		addNewSymbolActors();
		addNewSymbolActors();
		addNewSymbolActors();
		giveSymbolActorsRandomSymbolId();
		Gdx.input.setInputProcessor(gameStage);
		Gdx.app.log("MYLOG","test");
		gameStage.setDebugAll(true);


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
		symbolActorListTop.add(new SymbolActor(true,symbolActorListTop.size()));
		gameStage.addActor(symbolActorListTop.get(symbolActorListTop.size()-1));
		symbolActorListBottom.add(new SymbolActor(false,symbolActorListBottom.size()));
		gameStage.addActor(symbolActorListBottom.get(symbolActorListBottom.size()-1));
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
		//assign the first half of the random numbers to the symbolActors in the symbolActorListTop
		for(int i = 0; i < symbolActorListTop.size();i++) {
			symbolActorListTop.get(i).setSymbolId(tempArrayList.get(i));
		}
		//assign the second half to the symbolActorListBottom
		for(int i = symbolActorListTop.size(); i < symbolActorListBottom.size()+symbolActorListTop.size();i++) {
			symbolActorListBottom.get(i-symbolActorListTop.size()).setSymbolId(tempArrayList.get(i));
		}
		//now all the symbols will have a unique random symbolId, but we want 2 of them to match
		//choose a random symbolActor in the symbolActorListTop ArrayList and set its symbolId
		//to match that of a random symbolActor in the symbolActorListBottom
		matchSymbolId=symbolActorListBottom.get(r.nextInt(symbolActorListBottom.size())).getSymbolId();
		symbolActorListTop.get(r.nextInt(symbolActorListTop.size())).setSymbolId(matchSymbolId);
	}

	/**
	 * this method will be called from the click listener of the symbolActor, the symbolId of that
	 * actor will be compared with the correct matchSymbolId to see if the match is correct
	 * @param symbolId the symbolId of the clicked symbolActor
	 * @return
	 */
	public static Boolean testMatch(int symbolId)
	{
		if(symbolId==matchSymbolId)
		{
			Gdx.app.log("MYLOG","Match found!");
			matchFound();
			return true;
		}
		else
		{
			Gdx.app.log("MYLOG","Match failed!");
			matchFailed();
			return false;
		}
	}

	private static void matchFound()
	{
		addNewSymbolActors();
		giveSymbolActorsRandomSymbolId();
	}

	private static void matchFailed()
	{
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
		Gdx.gl.glClearColor(0.99f, 0.99f, 0.8f, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapeRenderer.setColor(new Color(0.99f,0.8f,0.8f,0f));
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.rect(0,HEIGHT/2-(2*UH)/2,WIDTH,2*UH);//draw the mid line across the screen
		drawSymbolActorsShape(symbolActorListBottom);
		drawSymbolActorsShape(symbolActorListTop);
		shapeRenderer.end();
		batch.begin();
		drawSymbolActorsFont(symbolActorListBottom);
		drawSymbolActorsFont(symbolActorListTop);
		batch.end();
		gameStage.act(Math.min(Gdx.graphics.getDeltaTime(),1/30f));
		gameStage.draw();
	}

	/**
	 * derives a Vector2 position from the symbolActor's positionId and topArea bool
	 * @param sa the SymbolActor that you want the position of
	 * @return Vector2
	 */
	public static Vector2 getSymbolActorPos(SymbolActor sa)
	{
		return new Vector2(sa.getPositionId()*10*UH+10*UH, (sa.isTopArea()?50*UH:0)+10*UH);
	}

	/**
	 * draw a circle in the position of the SymbolActor
	 * @param symbolActorList will need to pass in symbolActorListBottom and then symbolActorListTop
	 */
	public void drawSymbolActorsShape(ArrayList<SymbolActor> symbolActorList)
	{
		for (int dsash = 0; dsash<symbolActorList.size();dsash++)
		{
			shapeRenderer.circle(symbolActorList.get(dsash).getPos().x, symbolActorList.get(dsash).getPos().y, 3*UW);

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
		}
	}
	@Override
	public void resize(int width, int height)
	{
		//stage.getViewport().update(width,height,true);
		gameStage.getViewport().update(width,height,true);
	}
	@Override
	public void dispose()
	{
		stage.dispose();
		gameStage.dispose();
		skin.dispose();
		atlas.dispose();

	}
	@Override
	public void pause(){
		super.pause();
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