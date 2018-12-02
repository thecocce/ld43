package net.mgsx.ld43.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import net.mgsx.ld43.assets.GameAssets;
import net.mgsx.ld43.utils.FloattingAction;
import net.mgsx.ld43.utils.ThrowAction;

public class Ship {

	public Group shipGround;
	
	public Array<Canon> canons = new Array<Canon>();

	public Array<ShipPart> parts = new Array<ShipPart>();
	
	public ShipPart leftPart = null;

	public Rectangle r;
	
	private float hTime;

	private float baseX;

	private float baseY;

	public float endFactor;

	private FloattingAction floatAction;

	private boolean disabled;

	public boolean isDead;
	
	public Ship() {
		Array<TiledMapTileMapObject> mos = new Array<TiledMapTileMapObject>();
		for(MapLayer layer : GameAssets.i.shipMap.getLayers()){
			// if(layer.getName().equals("canons")){ // XXX
				mos.addAll(layer.getObjects().getByType(TiledMapTileMapObject.class));
			// }
		}
		
		shipGround  = new Group();
		
		{
			
			Image img = new Image(new TextureRegionDrawable(GameAssets.i.regionBoatBase));
			img.setPosition(-50, 20); // XXX 
			img.setOrigin(Align.center);
			shipGround.addActor(img);
		}
		
		r = new Rectangle();
		
		for(TiledMapTileMapObject mo : mos){
			
			TextureRegion region = mo.getTextureRegion();
			Image img = new Image(new TextureRegionDrawable(region));
			img.setPosition(mo.getX(), mo.getY());
			img.setOrigin(Align.center);
			shipGround.addActor(img);
			
			ShipPart part;
			
			if("canon".equals(mo.getName())){
				Canon canon = new Canon();
				canon.img = img;
				canons.add(canon);
				part = canon;
			}else{
				part = new ShipPart();
			}
			
			Rules.configurePart(part, mo.getName());
			part.name = mo.getName();
			
			part.baseX = mo.getX();
			part.baseY = mo.getY();
			
			part.img = img;
			img.setUserObject(part);
			
			parts.add(part);
			
			r.merge(img.getX(), img.getY());
			r.merge(img.getX(Align.right), img.getY(Align.bottom));
			
			
		}
		
		shipGround.setOrigin(r.x + r.width/2, r.y+r.height);
		
		shipGround.addAction(floatAction = new FloattingAction());
	}

	public Actor create() {

		return shipGround;
	}
	
	public void update(float delta){
		
		hTime += delta * .5f;
		
		float recule = MathUtils.lerp(-300, 300, MathUtils.sin(hTime) * .5f + .5f);
		
		shipGround.setPosition(baseX - recule, baseY);
		
		leftPart = null;
		for(ShipPart part : parts){
			if(part.disabled) continue;
			float px = part.img.getX();
			if(leftPart == null || px < leftPart.img.getX()){
				leftPart = part;
			}
		}
		
		floatAction.amp = MathUtils.lerp(3, 0, endFactor);
		
		shipGround.setRotation(MathUtils.lerp(shipGround.getRotation(), 20f, endFactor));
		
		shipGround.setPosition(
				MathUtils.lerp(shipGround.getX(), 700, endFactor), 
				baseY);
		
		isDead = true;
		for(ShipPart part : parts){
			if(!part.disabled){
				isDead = false;
				break;
			}
		}
	}

	public void setBase(float x, float y) {
		baseX = x;
		baseY = y;
	}

	public Image ejectPart() {
		Vector2 p = leftPart.img.localToStageCoordinates(new Vector2());
		leftPart.img.setPosition(p.x, p.y);
		Image img = leftPart.img;
		
		img.setTouchable(Touchable.disabled);
		
		leftPart.disabled = true;
		leftPart = null;
		
		
		img.addAction(Actions.sequence(Actions.parallel(new ThrowAction(900, 1600, 4500, 720)), Actions.removeActor()));
		
		return img;
	}

	public void disable() {
		disabled = true;
		shipGround.setTouchable(Touchable.disabled);
		for(Canon canon : canons){
			canon.idle();
		}
	}

	public void enable() {
		disabled = false;
		shipGround.setTouchable(Touchable.enabled);
		for(Canon canon : canons){
			canon.activate();
		}
		endFactor = 0;
	}

	public void restorePart(ShipPart part) {
		part.restore();
	}
	
	
	
}