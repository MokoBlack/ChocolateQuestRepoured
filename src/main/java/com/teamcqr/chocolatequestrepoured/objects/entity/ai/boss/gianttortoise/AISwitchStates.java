package com.teamcqr.chocolatequestrepoured.objects.entity.ai.boss.gianttortoise;

import com.teamcqr.chocolatequestrepoured.objects.entity.boss.EntityCQRGiantTortoise;

import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationAI;

public class AISwitchStates extends AnimationAI<EntityCQRGiantTortoise> {

	protected EntityCQRGiantTortoise turtle;
	protected Animation animationIn;
	protected Animation animationOut;
	
	public AISwitchStates(EntityCQRGiantTortoise entity, Animation animIn, Animation animOut) {
		super(entity);
		setMutexBits(8);
		this.turtle = entity;
		this.animationIn = animIn;
		this.animationOut = animOut;
	}

	@Override
	public Animation getAnimation() {
		if(turtle.getTargetedState() != 0) {
			return turtle.getTargetedState() < 0 ? animationIn : animationOut;
		}
		return null;
	}

	@Override
	public boolean shouldExecute() {
		if(turtle.wantsToChangeState()) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isAutomatic() {
		return true;
	}
	
	@Override
	public void updateTask() {
		super.updateTask();
	}
	
	@Override
	public void startExecuting() {
		super.startExecuting();
		turtle.currentAnim = this;
		turtle.setAnimationTick(0);
		if(turtle.getTargetedState() < 0) {
			turtle.setAnimation(animationIn);
		} else {
			turtle.setAnimation(animationOut);
		}
		turtle.setBypassInShell(true);
	}
	
	@Override
	public void resetTask() {
		super.resetTask();
		turtle.setAnimationTick(0);
		turtle.setInShell(turtle.getTargetedState() < 0);
		turtle.changedState();
		turtle.targetNewState(0);
		turtle.setBypassInShell(false);
	}
	
}
