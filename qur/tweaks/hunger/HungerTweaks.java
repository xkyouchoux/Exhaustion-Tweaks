package qur.tweaks.hunger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.SortingIndex(value = 1300)
@IFMLLoadingPlugin.TransformerExclusions({"qur.tweaks.hunger"})
public class HungerTweaks implements IFMLLoadingPlugin{
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{
				EClassTransformer.class.getName()
		};
	}

	@Override
	public String getModContainerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSetupClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}
	
	static{
		try{
			File configFolder = new File(".", "config");
			configFolder.mkdir();
			File configFile = new File(configFolder, "ExhaustionTweaks.cfg");
			configFile.createNewFile();
			Configuration config = new Configuration(configFile);
			config.load();
			config.setCategoryComment("cancelable", "Setting any of these values to 0 will remove the value.");
			ExhaustionValues.staticBlockBrakeValue = config.getFloat("valueBlockBreak", "cancelable", .005F, 0, 40F, "");
			ExhaustionValues.staticDealDamageValue = config.getFloat("valueDealDamage", "cancelable", .1F, 0, 40F, "");
			ExhaustionValues.staticDivingValue = config.getFloat("valueDiving", "cancelable", .1F, 0, 40F, "");
			ExhaustionValues.staticHungeringValue = config.getFloat("valueHungering", "cancelable", .005F, 0, 40F, "");
			ExhaustionValues.staticJumpingValue = config.getFloat("valueJumping", "cancelable", .05F, 0, 40F, "");
			ExhaustionValues.staticRunningValue = config.getFloat("valueRunning", "cancelable", .1F, 0, 40F, "");
			ExhaustionValues.staticRunningJumpValue = config.getFloat("valueRunningJump", "cancelable", .2F, 0, 40F, "");
			ExhaustionValues.staticSneakingValue = config.getFloat("valueSneaking", "cancelable", 0, 0, 40F, "");
			ExhaustionValues.staticSwimingValue = config.getFloat("valueSwimming", "cancelable", .1F, 0, 40F, "");
			ExhaustionValues.staticTakeDamageValue = config.getFloat("valueTakeDamage", "cancelable", .1F, 0, 40F, "");
			ExhaustionValues.staticWalkingValue = config.getFloat("valueWalking", "cancelable", 0, 0, 40F, "");
			ExhaustionValues.staticHealingStartValue = config.getInt("valueHealingStart", "healing", 18, 0, 20, "");
			ExhaustionValues.staticHealingValue = config.getInt("valueHealing", "healing", 6, 1, 20, "");
			config.save();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
