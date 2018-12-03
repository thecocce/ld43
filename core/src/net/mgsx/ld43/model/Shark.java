package net.mgsx.ld43.model;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import net.mgsx.ld43.assets.AudioEngine;
import net.mgsx.ld43.assets.GameAssets;
import net.mgsx.ld43.utils.FloattingAction;

public class Shark {
	private float sharkTime;
	private Image imgShark;
	public Circle circleShaper = new Circle();
	
	private float hMotion;
	
	public boolean stunt;
	
	private float stuntTime;
	
	private float offsetX = -2000;
	private float offsetY;
	
	public boolean attacking;
	private float attackTime;
	
	public float sharkLifeMax = Rules.SHARK_LIFE_BASE;
	public float sharkLife = sharkLifeMax;
	public float sharkLifeSmooth;
	
	private float preRun = 3f;
	private float hMotionSpeed;
	private boolean leave;
	
	public Shark(int level) {
		hMotionSpeed = level * .5f;
	}

	public Image create(){
		imgShark = new Image();
		
		imgShark = new Image(new TextureRegionDrawable(GameAssets.i.sharkFrames.first()));
		imgShark.setTouchable(Touchable.disabled);
		imgShark.setOrigin(Align.center);
		imgShark.addAction(new FloattingAction());
		
		imgShark.setUserObject(this);
		
		return imgShark;
	}
	
	public void hurted(ShipPart part){
		
		// TODO
		stunt = true;
		
		stuntTime = 2; // TODO depends on part
		
		sharkLife -= part.damages;
		
		if("pirate".equals(part.name)){
			AudioEngine.i.playSFX(12);
		}
	}
	
	public void leave(){
		
		leave = true;
	}
	
	public void update(float delta)
	{
		preRun -= delta;
		
		sharkLifeSmooth = MathUtils.lerp(sharkLifeSmooth, sharkLife, delta * 3f);
		
		sharkTime += delta * 10;
		
		// imgShark.setDrawable(new D);
		
		circleShaper.set(imgShark.getX() + 780,  imgShark.getY() + 180, 160);

		if(leave){
			offsetX += delta * 900;
		}
		else if(preRun > 0){
			offsetX = MathUtils.lerp(offsetX, 0, delta);
		}
		else if(sharkLife <= 0){
			imgShark.clearActions();
			setFrame(2);
			offsetX -= delta * 500;
			offsetY -= delta * 100;
		}
		else if(stunt){
			setFrame(0);
			offsetX -= delta * 300;
			stuntTime -= delta;
			if(stuntTime < 0){
				stunt = false;
			}
		}else if(attacking){
			attackTime += delta * 4;
			if(attackTime > 1){
				attacking = false;
			}
			attackAnim();
			offsetX = MathUtils.lerp(offsetX, -200, delta);
		}else{
			offsetX = MathUtils.lerp(offsetX, 0, delta);
			setFrame(2);
			hMotion += delta * hMotionSpeed;
		}
		
		float hShark = MathUtils.lerp( -100,  300,  MathUtils.sin(hMotion));
		// TODO add other sins
		
		hShark += MathUtils.lerp( 0,  200,  MathUtils.sin(hMotion * .5f + 0.72f)*.5f+.5f);
		hShark += MathUtils.lerp( 0,  100,  MathUtils.sin(hMotion * .25f + 1.2f)*.5f+.5f);
		
		
		
		
		imgShark.setX(offsetX + hShark);
		
		imgShark.setY(offsetY);
		
		
//		float attackTime = sharkTime * .2f;
//		if(attackTime % 4f > 2f){
//			imgShark.setPosition(MathUtils.lerp(0, 500, MathUtils.sin(attackTime)), 0);
//		}else{
//			imgShark.setPosition(MathUtils.lerp(0, 10, MathUtils.sin(attackTime)), 0);
//		}
	}
	
	private void setFrame(int i){
		((TextureRegionDrawable)imgShark.getDrawable()).setRegion(GameAssets.i.sharkFrames.get(i));
	}
	
	private void attackAnim(){
		((TextureRegionDrawable)imgShark.getDrawable()).setRegion(GameAssets.i.sharkAnimation.getKeyFrame(sharkTime, true));
	}

	public void attack() {
		attacking = true;
		attackTime = 0f;
	}
}
