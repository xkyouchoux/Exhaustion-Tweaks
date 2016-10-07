package qur.tweaks.hunger;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.SortingIndex(value = 1300)
@IFMLLoadingPlugin.TransformerExclusions({"qur.tweaks.hunger"})
@IFMLLoadingPlugin.MCVersion("1.10.2")
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
}
