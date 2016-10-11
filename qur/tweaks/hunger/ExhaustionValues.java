package qur.tweaks.hunger;

import net.minecraft.entity.player.EntityPlayer;

public class ExhaustionValues {
	public static float staticWalkingValue;
	public static float staticSneakingValue;
	public static float staticRunningValue;
	public static float staticJumpingValue;
	public static float staticRunningJumpValue;
	public static float staticSwimingValue;
	public static float staticDivingValue;
	public static float staticBlockBrakeValue;
	public static float staticTakeDamageValue;
	public static float staticDealDamageValue;
	public static float staticHungeringValue;
	
	public static int staticHealingValue;
	public static int staticHealingStartValue;
	
	public float walkingModifier = 1F;
	public float sneakingModifier = 1F;
	public float runningModifier = 1F;
	public float jumpingModifier = 1F;
	public float runningJumpModifier = 1F;
	public float swimmingModifier = 1F;
	public float divingModifier = 1F;
	public float blockBreakModifier = 1F;
	public float takeDamageModfier = 1F;
	public float dealDamageModifier = 1F;
	public float hungeringModifier = 1F;
	public float exhaustionModifier = 1F;
	
	public static ExhaustionValues getValues(EntityPlayer player){
		try{
			return (ExhaustionValues)player.getClass().getField("values").get(player);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
