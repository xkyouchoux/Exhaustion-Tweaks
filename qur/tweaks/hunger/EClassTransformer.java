package qur.tweaks.hunger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class EClassTransformer implements IClassTransformer{

	public static final String PLAYER = "net.minecraft.entity.player.EntityPlayer";
	public static final String BLOCK = "net.minecraft.block.Block";
	public static final String BLOCK_ICE = "net.minecraft.block.BlockIce";
	public static final String POTION = "net.minecraft.potion.Potion";
	public static final String DAMAGE_SOURCE = "net.minecraft.util.DamageSource";
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(BLOCK.equals(transformedName) || BLOCK_ICE.equals(transformedName)){			
			return writeClassToBytes(writeBlockBreaks(readClassFromBytes(basicClass)));
		}
		if(POTION.equals(transformedName)){
			return writeClassToBytes(writePotionEffects(readClassFromBytes(basicClass)));
		}
		if(PLAYER.equals(transformedName)){
			return writeClassToBytes(writePlayerMovement(writePlayerDealDamage(writePlayerJump(readClassFromBytes(basicClass)))));
		}
		if(DAMAGE_SOURCE.equals(transformedName)){
			return writeClassToBytes(writeDamageSource(readClassFromBytes(basicClass)));
		}
		return basicClass;
	}
	
	public ClassNode writePotionEffects(ClassNode cNode){
		MethodNode mNode = findMethodOfName(cNode, "func_76394_a", "performEffect");
		int size = mNode.instructions.size();
		int target = -1;
		for(int i = 0; i < size; i++){
			if(mNode.instructions.get(i).getType() == AbstractInsnNode.LDC_INSN){
				target = i;
				break;
			}
		}
		if(target != -1){
			LdcInsnNode node = (LdcInsnNode)mNode.instructions.get(target);
			node.cst = new Float(.005F);
		}
		return cNode;
	}
	
	public ClassNode writePlayerDealDamage(ClassNode cNode){
		MethodNode mNode = findMethodOfName(cNode, "func_71059_n", "attackTargetEntityWithCurrentItem");
		int target = -1;
		for(int i = 0; i < mNode.instructions.size(); i++){
			if(mNode.instructions.get(i).getType() == AbstractInsnNode.LDC_INSN){
				target = i;
			}
		}
		if(target != -1){
			((LdcInsnNode)mNode.instructions.get(target)).cst = .1F;
		}
		return cNode;
	}
	
	public ClassNode writePlayerJump(ClassNode cNode){
		MethodNode mNode = findMethodOfName(cNode, "func_70664_aZ", "jump");
		int size = mNode.instructions.size();
		int target1 = -1;
		int target2 = -1;
		for(int i = 0; i < size; i++){
			if(mNode.instructions.get(i).getType() == AbstractInsnNode.LDC_INSN){
				if(target1 == -1){
					target1 = i;
				}else {
					target2 = i;
					break;
				}
			}
		}
		if(target1 != -1){
			LdcInsnNode node = (LdcInsnNode)mNode.instructions.get(target1);
			node.cst = new Float(.2F);
		}
		if(target2 != -1){
			LdcInsnNode node = (LdcInsnNode)mNode.instructions.get(target2);
			node.cst = new Float(.05F);
		}
		return cNode;
	}
	
	public ClassNode writePlayerMovement(ClassNode cNode){
		MethodNode mNode = findMethodOfName(cNode, "func_71000_j", "addMovementStat");
		int size = mNode.instructions.size();
		for(int i = 0; i < size; i++){
			if(mNode.instructions.get(i).getType() != AbstractInsnNode.METHOD_INSN){
				continue;
			}
			MethodInsnNode mNodei = (MethodInsnNode)mNode.instructions.get(i);
			if(mNodei.name.equals("func_71020_j") || mNodei.name.equals("addExhaustion")){
				AbstractInsnNode start = mNodei;
				while(start.getType() != AbstractInsnNode.LABEL){
					start = start.getPrevious();
				}
				LabelNode node = (LabelNode)start;
				AbstractInsnNode target = start;
				while(target.getType() != AbstractInsnNode.LDC_INSN){
					target = target.getNext();
				}
				LdcInsnNode cstNode = (LdcInsnNode)target;
				if(cstNode.cst.equals(.005F) || cstNode.cst.equals(.01F)){
					ArrayList<AbstractInsnNode> tmp = new ArrayList<AbstractInsnNode>();
					AbstractInsnNode index = mNodei;
					tmp.add(index);
					while(index.getType() != AbstractInsnNode.LABEL){
						tmp.add(index = index.getPrevious());
					}
					ArrayList<AbstractInsnNode> inst = new ArrayList<AbstractInsnNode>();
					for(int max = tmp.size() - 1; max >= 0; max--){
						inst.add(tmp.get(max));
					}
					tmp.clear();
					queueInstructionsRemoval(inst, mNode);
				}else if(cstNode.cst.equals(.015F)){
					cstNode.cst = .01F;
				}
			}
		}
		removeInstructions(mNode);
		return cNode;
	}
	
	public ClassNode writeBlockBreaks(ClassNode cNode){
		MethodNode mNode = findMethodOfName(cNode, "func_180657_a", "harvestBlock");
		int size = mNode.instructions.size();
		int target = -1;
		for(int i = 0; i < size; i++){
			if(mNode.instructions.get(i).getType() == AbstractInsnNode.LDC_INSN){
				target = i;
				break;
			}
		}
		if(target != -1){
			LdcInsnNode node = (LdcInsnNode)mNode.instructions.get(target);
			node.cst = new Float(.005F);
		}
		return cNode;
	}
	
	public ClassNode writeDamageSource(ClassNode cNode){
		FieldNode fNode = findFieldOfName(cNode, "field_76384_q", "hungerDamage");
		fNode.value = .1F;
		return cNode;
	}
	
	public byte[] writeClassToBytes(ClassNode cNode){
		return writeClassToBytes(cNode, 0);
	}
	
	public byte[] writeClassToBytes(ClassNode cNode, int flags){
		ClassWriter writer = new ClassWriter(flags);
		cNode.accept(writer);
		return writer.toByteArray();
	}
	
	public ClassNode readClassFromBytes(byte[] bytes){
		ClassNode cNode = new ClassNode();
		ClassReader cReader = new ClassReader(bytes);
		cReader.accept(cNode, 0);
		return cNode;
	}
	
	private ArrayList<AbstractInsnNode> removal = new ArrayList<AbstractInsnNode>();
	
	public void queueInstructionsRemoval(ArrayList<AbstractInsnNode> instructions, MethodNode mNode){
		for(AbstractInsnNode node : instructions){
			removal.add(node);
		}
	}
	
	public void removeInstructions(MethodNode mNode){
		for(AbstractInsnNode node : removal){
			mNode.instructions.remove(node);
		}
		mNode.instructions.resetLabels();
	}
	
	public MethodNode findMethodOfName(ClassNode cNode, String obfName, String deobfName){
		for(MethodNode mNode : cNode.methods){
			if(mNode.name.equals(obfName) || mNode.name.equals(deobfName)){
				return mNode;
			}
		}
		return null;
	}
	
	public FieldNode findFieldOfName(ClassNode cNode, String obfName, String deobfName){
		for(FieldNode fNode : cNode.fields){
			if(fNode.name.equals(obfName) || fNode.name.equals(deobfName)){
				return fNode;
			}
		}
		return null;
	}
}
