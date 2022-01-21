package com.gmail.andrewahughes.match;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * SymbolActors are the main feature of the game. One group of symbolActors will be displayed in the
 * top half of the screen and another group in the bottom half.
 *
 */
public class SymbolActor extends Actor {
    //all symbols in play should have a unique id, apart from the matching pair. the id will be used
    // to decide what image is drawn on the symbol, these ids will be changed regularly as through
    // each level of the game it will be randomised.
    private int symbolId;
    //this will tell us if the symbolActor should be in the top or bottom half of the screen, this
    // can be declare final as it will never change
    private final boolean topArea;
    //the symbolActor's position within its area will be derived from this id, these ids should
    // increment for each new symbol actor in this area and can be declared final
    private final int positionId;

    /**
     * SymbolActors are the main feature of the game. One group of symbolActors will be displayed in
     * the top half of the screen and another group in the bottom half. Position within this half
     * of the screen is determined by positionId
     * @param inTopArea true for top, false for bottom
     * @param positionId this should be the index value of the symbolActor in its arrayList
     */
    SymbolActor(boolean inTopArea, int positionId)
    {
        topArea =inTopArea;
        this.positionId=positionId;
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
}

