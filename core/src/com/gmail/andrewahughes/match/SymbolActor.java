package com.gmail.andrewahughes.match;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * SymbolActors are the main feature of the game. One group of symbolActors will be displayed in the
 * top half of the screen and another group in the bottom half.
 *
 */
public class SymbolActor extends Actor {
    /**
     *     all symbols in play should have a unique id, apart from the matching pair. the id will be used
     *      to decide what image is drawn on the symbol, these ids will be changed regularly as through
     *      each level of the game it will be randomised.
     */
    private int symbolId;
    /**
     * this will tell us if the symbolActor should be in the top or bottom half of the screen, this
     * can be declare final as it will never change
     */
    private final boolean topArea;
    /**
     * the symbolActor's position within its area will be derived from this id, these ids should
     * increment for each new symbol actor in this area and can be declared final
     */
    private final int positionId;
    /**
     *  the actual position Vector of the actor, will be derived from the actor's positionId
     *  and topArea bool
     */
    private final Vector2 pos;
    /**
     * the radius of the actor, will determine it's height and width and it's bounds
     */
    private float radius;

    /**
     * SymbolActors are the main feature of the game. One group of symbolActors will be displayed in
     * the top half of the screen and another group in the bottom half. Position within this half
     * of the screen is determined by positionId
     * @param inTopArea true for top, false for bottom
     * @param positionId this should be the index value of the symbolActor in its arrayList
     */
    SymbolActor(boolean inTopArea, final int positionId)
    {
        topArea =inTopArea;
        this.positionId=positionId;
        pos = MyGdxGame.setSymbolActorPos(this);
        Gdx.app.log("MYLOG","SymbolActor posX,posY;" +pos.x+","+pos.y);
        radius = MyGdxGame.RECOMMENDEDSYMBOLRADIUS;
        this.setBounds(pos.x-radius,pos.y-radius,radius*2,radius*2);
        this.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y)
            {
                Gdx.app.log("MYLOG","SymbolActor clicked: posId;"+ positionId+" symId;"+symbolId+" pos;"+pos.x+","+pos.y);
                MyGdxGame.testMatch(symbolId);
            }
        });
    }
    public int getSymbolId() {
        return symbolId;
    }
    public void setSymbolId(int symbolId) {
        this.symbolId = symbolId;
    }
    public boolean isTopArea() {
        return topArea;
    }
    public int getPositionId() {
        return positionId;
    }

    public Vector2 getPos() {
        return pos;
    }

    public float getRadius() {
        return radius;
    }

    /**
     * Radius of the actor is set to the constant MyGdxGame.SYMBOLRADIUS in the constructor
     * but you can override it
     * @param radius
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }
}

