package com.gmail.andrewahughes.match;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.gmail.andrewahughes.match.Cell.influenceWind;
import static com.gmail.andrewahughes.match.MyGdxGame.C30;
import static com.gmail.andrewahughes.match.MyGdxGame.RECOMMENDEDSYMBOLRADIUS;

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
     * the innerradius of the actor, the actor is a hex, so the radius is total radius, this
     * innerRadius is the radius of the circle that fits perfectly inside the hex
     */
    private float innerRadius;

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
        innerRadius=radius*C30;
        this.setBounds(pos.x-innerRadius,pos.y-radius,innerRadius*2,radius*2);
        this.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y)
            {
                Gdx.app.log("MYLOG","SymbolActor clicked: posId;"+ positionId+" symId;"+symbolId+" pos;"+pos.x+","+pos.y+" mousex;"+x+" mousey;"+y);
                Gdx.app.log("MYLOG","rec rad "+RECOMMENDEDSYMBOLRADIUS+", rad "+radius+", inner rad "+innerRadius);
                //the bounding box has been hit, but test if the x and y is in the hex
                if(inHex(x,y)) {
                    Gdx.app.log("MYLOG", "SymbolActor hex clicked: posId;" + positionId + " symId;" + symbolId + " pos;" + pos.x + "," + pos.y);
                    MyGdxGame.testMatch(symbolId);
                }
                /*regardless of if the touch is in a hex or not, have the touch influence the wind
                 for the background*/
                influenceWind(x+pos.x);
            }
        });
    }
    public void draw(ShapeRenderer shapeRenderer)
    {
        //i only drew this to help set up collision detection
       /* float ax=0;
        float ax2=100;
        float ay = -ax*C30*2/3+radius/2		;
        float ay2 = -ax2*C30*2/3+radius/2	;
        float by = -ax*C30*2/3+radius*5/2	;
        float by2 = -ax2*C30*2/3+radius*5/2	;
        float cy = ax*C30*2/3-radius/2		;
        float cy2 = ax2*C30*2/3-radius/2	;
        float dy = ax*C30*2/3+radius*3/2	;
        float dy2 = ax2*C30*2/3+radius*3/2	;

        shapeRenderer.setColor(new Color(0.2f,0.8f,0.8f,0f));
        shapeRenderer.line(ax+this.pos.x-innerRadius,ay+this.pos.y-radius,ax2+this.pos.x-innerRadius,ay2+this.pos.y-radius);
        shapeRenderer.line(ax+this.pos.x-innerRadius,by+this.pos.y-radius,ax2+this.pos.x-innerRadius,by2+this.pos.y-radius);
        shapeRenderer.line(ax+this.pos.x-innerRadius,cy+this.pos.y-radius,ax2+this.pos.x-innerRadius,cy2+this.pos.y-radius);
        shapeRenderer.line(ax+this.pos.x-innerRadius,dy+this.pos.y-radius,ax2+this.pos.x-innerRadius,dy2+this.pos.y-radius);*/
    }
    /**
     * the click listener only tells us if a touch is in the bounding box rectangle, but our symbols
     * are hexagons, this method will tell us if the hex is touched
     * @return
     */
    public boolean inHex(float x, float y)
    {
        if(inInnerCircle(x,y,this.pos.x,this.pos.y)) {
            return true;
        }
        else
        {
            if(y<-x*C30*2/3+radius/2)
            {
                Gdx.app.log("MYLOG","click outside hex, bottom left");
                Gdx.app.log("MYLOG","x;"+(int)x+" y;"+(int)y+"|y<-x*C30+radius/2");
                Gdx.app.log("MYLOG",y+"<"+(-x*C30+radius/2));
                return false;
            }
            if(y>-x*C30*2/3+radius*5/2)
            {
                Gdx.app.log("MYLOG","click outside hex, top right");
                Gdx.app.log("MYLOG","x;"+(int)x+" y;"+(int)y+"|y>-x*C30+radius*5/2");
                Gdx.app.log("MYLOG",y+">"+(-x*C30+radius*5/2));
                return false;
            }
            if(y<x*C30*2/3-radius/2)
            {
                Gdx.app.log("MYLOG","click outside hex, bottom right");
                Gdx.app.log("MYLOG","x;"+(int)x+" y;"+(int)y+"|y<x*C30-radius/2");
                Gdx.app.log("MYLOG",y+"<"+(x*C30-radius/2));
                return false;
            }
            if(y>x*C30*2/3+radius*3/2)
            {
                Gdx.app.log("MYLOG","click outside hex, top left");
                Gdx.app.log("MYLOG","x;"+(int)x+" y;"+(int)y+"|y>x*C30+radius*3/2");
                Gdx.app.log("MYLOG",y+">"+(x*C30+radius*3/2));
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * this method will tell us if the touch is in the hex's inner circle, that is the circle that
     * will fit neatly inside the hex, if so the touch must be in the hex and we don't need to do
     * further calculations
     * @return
     */
    public boolean inInnerCircle(float x, float y,float ox, float oy)
    {
        if(dist(x,y,ox,oy)<innerRadius*innerRadius) {
            return true;
        }
        else
        {
            return false;
        }
    }
    public float dist(float x1,float y1,float x2,float y2)
    {
        return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
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

