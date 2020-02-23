package com.teamcqr.chocolatequestrepoured.objects.entity.ai.boss.gianttortoise;

import com.teamcqr.chocolatequestrepoured.objects.entity.boss.EntityCQRGiantTortoise;
import com.teamcqr.chocolatequestrepoured.objects.entity.boss.EntityCQRGiantTortoise.ETortoiseAnimState;
import com.teamcqr.chocolatequestrepoured.objects.entity.projectiles.ProjectileBubble;

import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationAI;
import net.minecraft.util.math.Vec3d;

public class AISpinAttackTurtle extends AnimationAI<EntityCQRGiantTortoise> {
	
	private Vec3d movementVector;
	
	private static final int COOLDOWN = 40;
	private int cooldown = COOLDOWN /2;

	public AISpinAttackTurtle(EntityCQRGiantTortoise entity) {
		super(entity);
		//setMutexBits(8);
	}

	private EntityCQRGiantTortoise getBoss() {
		return (EntityCQRGiantTortoise) this.entity;
	}
	
	@Override
	public Animation getAnimation() {
		return EntityCQRGiantTortoise.ANIMATION_SPIN;
	}

	@Override
	public boolean shouldExecute() {
		cooldown--;
		if(!getBoss().isStunned() && getBoss().getAttackTarget() != null && !getBoss().getAttackTarget().isDead && cooldown <= 0) {
			getBoss().setWantsToSpin(true);
			if(getBoss().isInShell() && getBoss().isReadyToSpin()) {
				getBoss().setCanBeStunned(false);
				getBoss().setSpinning(true);
				getBoss().setWantsToSpin(false);
				return true;
			} else {
				getBoss().targetNewState(EntityCQRGiantTortoise.TARGET_MOVE_IN);
			}
		}
		return false;
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return getBoss() != null && !getBoss().isStunned() && getBoss().getSpinsBlocked() <= 1 && super.shouldContinueExecuting() && !getBoss().isDead && getBoss().getAttackTarget() != null && !getBoss().getAttackTarget().isDead;
	}
	
	private void calculateVelocity() {
		this.movementVector = getBoss().getAttackTarget().getPositionVector().subtract(getBoss().getPositionVector());
		this.movementVector = this.movementVector.normalize();
		this.movementVector = this.movementVector.scale(1.125D);
	}
	
	@Override
	public boolean isAutomatic() {
		return false;
	}
	
	@Override
	public boolean isInterruptible() {
		return true;
	}
	
	@Override
	public void startExecuting() {
		super.startExecuting();
		this.getBoss().setSpinning(true);
		this.getBoss().setCanBeStunned(false);
		this.getBoss().setInShell(true);
		this.getBoss().setReadyToSpin(false);
		getBoss().setAnimation(getAnimation());
		getBoss().currentAnim = this;
		getBoss().setAnimationTick(0);
	}
	
	@Override
	public void updateTask() {
		super.updateTask();
		//this.getBoss().setSpinning(false);
		if(getBoss().getSpinsBlocked() >= 1) {
			this.getBoss().setSpinning(false);
			this.getBoss().setStunned(true);
		}
		else if(getBoss().getAnimationTick() > 20 && getAnimation().getDuration() - getBoss().getAnimationTick() > 20) {
			if(getBoss().collidedHorizontally || movementVector == null || getBoss().getDistance(getBoss().getAttackTarget()) >= 20) {
				calculateVelocity();
			}
			this.getBoss().setSpinning(true);
			this.getBoss().setCanBeStunned(false);
			this.getBoss().setInShell(true);
			getBoss().motionX = movementVector.x;
			getBoss().motionZ = movementVector.z;
			getBoss().motionY = 3* movementVector.y /2;
			getBoss().velocityChanged = true;
		} else if(getBoss().getAnimationTick() < 20) {
			this.getBoss().setSpinning(false);
			Vec3d v = new Vec3d(entity.getRNG().nextDouble() -0.5D, 0.125D * (entity.getRNG().nextDouble() -0.5D), entity.getRNG().nextDouble() -0.5D);
			v = v.normalize();
			v = v.scale(1.4);
			entity.faceEntity(entity.getAttackTarget(), 30, 30);
			ProjectileBubble bubble = new ProjectileBubble(entity.world, entity);
			bubble.motionX = v.x;
			bubble.motionY = v.y;
			bubble.motionZ = v.z;
			bubble.velocityChanged = true;
			entity.world.spawnEntity(bubble);
			
		} else {
			this.getBoss().setSpinning(false);
			getBoss().resetSpinsBlocked();
		}
	}
	
	@Override
	public void resetTask() {
		super.resetTask();
		this.getBoss().setSpinning(false);
		this.getBoss().setReadyToSpin(true);
		this.getBoss().setCanBeStunned(true);
		this.getBoss().setCurrentAnimation(ETortoiseAnimState.NONE);
		cooldown = COOLDOWN;
		if(!(getBoss().getAttackTarget() != null && !getBoss().getAttackTarget().isDead)) {
			cooldown /= 3;
		}
		getBoss().setAnimationTick(0);
		if(getBoss().getSpinsBlocked() >= 1) {
			cooldown *= 6;
			this.getBoss().setStunned(true);
		}
		getBoss().resetSpinsBlocked();
	}

}
