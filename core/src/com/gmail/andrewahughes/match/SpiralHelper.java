package com.gmail.andrewahughes.match;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import static com.gmail.andrewahughes.match.MyGdxGame.AREAHEIGHT;
import static com.gmail.andrewahughes.match.MyGdxGame.AREAWIDTH;
import static com.gmail.andrewahughes.match.MyGdxGame.C30;
import static com.gmail.andrewahughes.match.MyGdxGame.RECOMMENDEDSYMBOLRADIUS;

/**
 * This is a helper class, created this in a separte class to neaten up the code
 * This helper will create a list of Vector2 values that will help to space out the
 * symbolActors. just need to initialise this class with number of symbols as the argument
 */
public class SpiralHelper {
    static ArrayList<Vector2> spiralSymbolList;
    SpiralHelper(int numberOfSymbols)
    {
        spiralSymbolList = new ArrayList<Vector2>();
        getSymbolSpiralPositions(numberOfSymbols);
    }

    /**
     * symbols will be hex shaped
     * return a list of centre points for symbols arranged in a spiral pattern. first  will be in
     * the centre, then the rule is we go to the right of the top right most hex and spiral around
     * clockwise until we complete a layer. this is designed to use the horizontal space more than
     * the vertical space due to our screen layout, so it will also add some extra hexes to the left
     * and right even if there is no space at the top and bottom.
     * @param numberOfHexes
     * @return the returned result should be multiplied by the SYMBOLRADIUS before use
     */
    public ArrayList<Vector2> getSymbolSpiralPositions(int numberOfHexes)
    {
        spiralSymbolList.add(new Vector2(0f, 0f));
        int numberOfLayers=1;
        // the number of hexes in each layer increases exponentially, this will figure out how many
        // layers there will be
        int hexInLayers=1;
        for(int layers=1;layers<10;layers++)
        {
            hexInLayers=hexInLayers+6*(layers-1);
            if(numberOfHexes>hexInLayers)
            {
                spiralLayer(layers);
                numberOfLayers=layers;
            }
            else
            {
                break;
            }
        }
        //work out how big the symbol radius could be given the amount of hexes we display
        int spiralHeight = 2+ numberOfLayers * 3 ;
        int spiralWidth = (int) (C30* (2+ numberOfLayers * 4 ));
        RECOMMENDEDSYMBOLRADIUS = AREAHEIGHT/spiralHeight;
        if(RECOMMENDEDSYMBOLRADIUS> AREAWIDTH/spiralWidth)
        {
            RECOMMENDEDSYMBOLRADIUS=AREAWIDTH/spiralWidth;
        }
        Gdx.app.log("MYLOG","RECOMMENDEDSYMBOLRADIUS "+RECOMMENDEDSYMBOLRADIUS);
        return spiralSymbolList;
    }
    /**
     * helper function to create the spiralHexList
     * @param layerNumber will begin at 0 which will be the single central hex, then 1 for the ring
     *                    of 6 hexes, then 2 for the ring of 12 hexes etc
     */
    private void spiralLayer(int layerNumber)
    {
        sr();
        for(int sli=1;sli<layerNumber;sli++)
        {
            sdr();
        }
        for(int sli=0;sli<layerNumber;sli++)
        {
            sdl();
        }
        for(int sli=0;sli<layerNumber;sli++)
        {
            sl();
        }
        for(int sli=0;sli<layerNumber;sli++)
        {
            sul();
        }
        for(int sli=0;sli<layerNumber;sli++)
        {
            sur();
        }
        for(int sli=0;sli<layerNumber;sli++)
        {
            sr();
        }
    }
    /**
     * helper function for the spiralHexList
     */
    private void sr()
    {
        spiralSymbolList.add(new Vector2(spiralSymbolList.get(spiralSymbolList.size()-1).x+C30*2, spiralSymbolList.get(spiralSymbolList.size()-1).y));
    }
    /**
     * helper function for the spiralHexList
     */
    private void sdl()
    {
        spiralSymbolList.add(new Vector2(spiralSymbolList.get(spiralSymbolList.size()-1).x-C30, spiralSymbolList.get(spiralSymbolList.size()-1).y-1.5f));
    }
    /**
     * helper function for the spiralHexList
     */
    private void sl()
    {
        spiralSymbolList.add(new Vector2(spiralSymbolList.get(spiralSymbolList.size()-1).x-C30*2, spiralSymbolList.get(spiralSymbolList.size()-1).y));
    }
    /**
     * helper function for the spiralHexList
     */
    private void sul()
    {
        spiralSymbolList.add(new Vector2(spiralSymbolList.get(spiralSymbolList.size()-1).x-C30, spiralSymbolList.get(spiralSymbolList.size()-1).y+1.5f));
    }
    /**
     * helper function for the spiralHexList
     */
    private void sur()
    {
        spiralSymbolList.add(new Vector2(spiralSymbolList.get(spiralSymbolList.size()-1).x+C30, spiralSymbolList.get(spiralSymbolList.size()-1).y+1.5f));
    }
    /**
     * helper function for the spiralHexList
     */
    private void sdr()
    {
        spiralSymbolList.add(new Vector2(spiralSymbolList.get(spiralSymbolList.size()-1).x+C30, spiralSymbolList.get(spiralSymbolList.size()-1).y-1.5f));
    }

}
