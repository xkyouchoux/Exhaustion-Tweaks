package qur.tweaks.hunger;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FMUL;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class EClassTransformer implements IClassTransformer{
	
	public static final String PLAYER = "net.minecraft.entity.player.EntityPlayer";
	public static final String BLOCK = "net.minecraft.block.Block";
	public static final String BLOCK_ICE = "net.minecraft.block.BlockIce";
	public static final String POTION = "net.minecraft.potion.Potion";
	public static final String DAMAGE_SOURCE = "net.minecraft.util.DamageSource";
	public static final String FOOD_STATS = "net.minecraft.util.FoodStats";
	
	public static final HashMap<String, ArrayList<String>> mappings = new HashMap<String, ArrayList<String>>();
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(BLOCK.equals(transformedName) || BLOCK_ICE.equals(transformedName)){
			ClassNode cNode = readClassFromBytes(basicClass);
			writeBlockBreaks(cNode);
			return writeClassToBytes(cNode);
		}else if(POTION.equals(transformedName)){
			ClassNode cNode = readClassFromBytes(basicClass);
			writeHungering(cNode);
			return writeClassToBytes(cNode);
		}else if(PLAYER.equals(transformedName)){
			ClassNode cNode = readClassFromBytes(basicClass);
			cNode.visitField(ACC_PUBLIC + ACC_FINAL, "values", "Lqur/tweaks/hunger/ExhaustionValues;", null, null);
			for(int index = 0; index < cNode.methods.size(); index++){
				if(cNode.methods.get(index).name.equals("<init>")){
					LabelNode lNode = new LabelNode();
					MethodNode mNode = (MethodNode)cNode.methods.get(index);
					VarInsnNode var = new VarInsnNode(ALOAD, 0);
					TypeInsnNode type = new TypeInsnNode(NEW, "qur/tweaks/hunger/ExhaustionValues");
					InsnNode insn = new InsnNode(DUP);
					MethodInsnNode mNodei = new MethodInsnNode(INVOKESPECIAL, "qur/tweaks/hunger/ExhaustionValues", "<init>", "()V", false);
					FieldInsnNode fNodei = new FieldInsnNode(PUTFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;");
					AbstractInsnNode test = mNode.instructions.getFirst().getNext();
					while(test.getType() != AbstractInsnNode.LABEL){
						test = test.getNext();
					}
					LineNumberNode lnNode = new LineNumberNode(((LineNumberNode)test.getNext()).line - 1, lNode);
					mNode.instructions.insertBefore(test, fNodei);
					mNode.instructions.insertBefore(fNodei, mNodei);
					mNode.instructions.insertBefore(mNodei, insn);
					mNode.instructions.insertBefore(insn, type);
					mNode.instructions.insertBefore(type, var);
					mNode.instructions.insertBefore(var, lnNode);
					mNode.instructions.insertBefore(lnNode, lNode);
					mNode.instructions.resetLabels();
				}
			}
			writeWalking(cNode);
			writeRunning(cNode);
			writeSneaking(cNode);
			writeJumping(cNode);
			writeRunningJump(cNode);
			writeSwimming(cNode);
			writeDiving(cNode);
			writeDealDamage(cNode);
			writeTakeDamage(cNode);
			writeTotalModifier(cNode);
			return writeClassToBytes(cNode);
		}else if(DAMAGE_SOURCE.equals(transformedName)){
			ClassNode cNode = readClassFromBytes(basicClass);
			MethodNode mNode = this.findMethodOfName(cNode, "<init>");
			FieldInsnNode fNodei = null;
			for(int index = 0; index < mNode.instructions.size(); index++){
				if(mNode.instructions.get(index).getType() == AbstractInsnNode.FIELD_INSN && matchs(((FieldInsnNode)mNode.instructions.get(index)).name, "hungerDamage")){
					fNodei = (FieldInsnNode)mNode.instructions.get(index);
					break;
				}
			}
			mNode.instructions.remove(fNodei.getPrevious());
			mNode.instructions.insertBefore(fNodei, new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticTakeDamageValue", "F"));
			return writeClassToBytes(cNode);
		}else if(FOOD_STATS.equals(transformedName)){
			ClassNode cNode = this.readClassFromBytes(basicClass);
			MethodNode mNode = this.findMethodOfName(cNode, "onUpdate");
			for(int index = 0; index < mNode.instructions.size(); index++){
				if(mNode.instructions.get(index).getType() == AbstractInsnNode.INT_INSN){
					IntInsnNode iNode = (IntInsnNode)mNode.instructions.get(index);
					if(iNode.operand == 18){
						iNode.operand = ExhaustionValues.staticHealingStartValue;
					}
				}
				if(mNode.instructions.get(index).getType() == AbstractInsnNode.METHOD_INSN){
					MethodInsnNode mNodei = (MethodInsnNode)mNode.instructions.get(index);
					if(mNodei.name.equals("addExhaustion") || mNodei.name.equals("func_75113_a")){
						AbstractInsnNode tmp = mNodei;
						while(tmp.getType() != AbstractInsnNode.LABEL){
							tmp = tmp.getPrevious();
							if(tmp.getType() == AbstractInsnNode.LDC_INSN){
								LdcInsnNode ldcNode = (LdcInsnNode)tmp;
								if(ldcNode.cst.equals(4.0F)){
									ldcNode.cst = (float)ExhaustionValues.staticHealingValue;
									break;
								}
							}
						}
					}
				}
			}
			return writeClassToBytes(cNode);
		}
		return basicClass;
	}
	
	public void writeBlockBreaks(ClassNode cNode){
		MethodNode mNode = findMethodOfName(cNode, "harvestBlock");
		MethodInsnNode mNodei = this.getFirstWithName(mNode, "addExhaustion");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticBlockBrakeValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 2));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "blockBreakModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .025F), instructions);
	}
	
	public void writeWalking(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "addMovementStat");
		MethodInsnNode mNodei = this.getMovementNode(cNode, "WALK_ONE_CM");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticWalkingValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "walkingModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .01F), instructions);
	}
	
	public void writeSneaking(ClassNode cNode){
		MethodInsnNode mNodei = this.getMovementNode(cNode, "CROUCH_ONE_CM");
		MethodNode mNode = this.findMethodOfName(cNode, "addMovementStat");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticSneakingValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "sneakingModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .005F), instructions);
	}
	
	public void writeRunning(ClassNode cNode){
		MethodInsnNode mNodei = this.getMovementNode(cNode, "SPRINT_ONE_CM");
		MethodNode mNode = this.findMethodOfName(cNode, "addMovementStat");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticRunningValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "runningModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .099999994F), instructions);
	}
	
	public void writeSwimming(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "addMovementStat");
		MethodInsnNode mNodei = this.getMovementNode(cNode, "SWIM_ONE_CM");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticSwimingValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "swimmingModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .015F), instructions);
	}
	
	public void writeDiving(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "addMovementStat");
		MethodInsnNode mNodei = this.getMovementNode(cNode, "DIVE_ONE_CM");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticDivingValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "divingModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .015F), instructions);
	}
	
	public void writeRunningJump(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "jump");
		MethodInsnNode mNodei = this.getWithLdcValue(mNode, .8F);
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticRunningJumpValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "runningJumpModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .8F), instructions);
	}
	
	public void writeJumping(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "jump");
		MethodInsnNode mNodei = this.getWithLdcValue(mNode, .2F);
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticJumpingValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "jumpingModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .2F), instructions);
	}
	
	public void writeDealDamage(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "attackTargetEntityWithCurrentItem");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticDealDamageValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "dealDamageModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getFirstWithName(mNode, "addExhaustion").getPrevious(), instructions);
	}
	
	public void writeTakeDamage(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "damageEntity");
		InsnList instructions = new InsnList();
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "takeDamageModfier", "F"));
		instructions.add(new InsnNode(FMUL));
		mNode.instructions.insertBefore(this.getFirstWithName(mNode, "addExhaustion"), instructions);
	}
	
	public void writeHungering(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "performEffect");
		MethodInsnNode mNodei = this.getFirstWithName(mNode, "addExhaustion");
		InsnList instructions = new InsnList();
		instructions.add(new FieldInsnNode(GETSTATIC, "qur/tweaks/hunger/ExhaustionValues", "staticHungeringValue", "F"));
		instructions.add(new VarInsnNode(ALOAD, 1));
		instructions.add(new TypeInsnNode(CHECKCAST, PLAYER.replace('.', '/')));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "hungeringModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		this.replaceNodeWithInsnList(mNode, this.getLdcWithValue(mNodei, .025F), instructions);
	}
	
	public void writeTotalModifier(ClassNode cNode){
		MethodNode mNode = this.findMethodOfName(cNode, "addExhaustion");
		MethodInsnNode mNodei = null;
		for(int index = 0; index < mNode.instructions.size(); index++){
			if(mNode.instructions.get(index).getType() == AbstractInsnNode.METHOD_INSN){
				mNodei = (MethodInsnNode)mNode.instructions.get(index);
			}
		}
		InsnList instructions = new InsnList();
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new FieldInsnNode(GETFIELD, PLAYER.replace('.', '/'), "values", "Lqur/tweaks/hunger/ExhaustionValues;"));
		instructions.add(new FieldInsnNode(GETFIELD, "qur/tweaks/hunger/ExhaustionValues", "exhaustionModifier", "F"));
		instructions.add(new InsnNode(FMUL));
		mNode.instructions.insertBefore(mNodei, instructions);
	}
	
	public MethodInsnNode getFirstWithName(MethodNode mNode, String name){
		MethodInsnNode mNodei = null;
		for(int index = 0; index < mNode.instructions.size(); index++){
			if(mNode.instructions.get(index).getType() == AbstractInsnNode.METHOD_INSN && matchs(((MethodInsnNode)mNode.instructions.get(index)).name, name)){
				mNodei = (MethodInsnNode)mNode.instructions.get(index);
				break;
			}
		}
		return mNodei;
	}
	
	public void replaceNodeWithInsnList(MethodNode mNode, AbstractInsnNode node, InsnList list){
		mNode.instructions.insert(node, list);
		mNode.instructions.remove(node);
	}
	
	public LdcInsnNode getLdcWithValue(MethodInsnNode mNodei, Object value){
		AbstractInsnNode node = mNodei;
		LdcInsnNode ldcNode = null;
		while(node.getType() != AbstractInsnNode.LABEL){
			node = node.getPrevious();
			if(node.getType() == AbstractInsnNode.LDC_INSN && ((LdcInsnNode)node).cst.equals(value)){
				ldcNode = (LdcInsnNode)node;
			}
		}
		return ldcNode;
	}
	
	public MethodInsnNode getWithLdcValue(MethodNode mNode, Object value){
		for(int index = 0; index < mNode.instructions.size(); index++){
			AbstractInsnNode node = mNode.instructions.get(index);
			if(node.getType() == AbstractInsnNode.LDC_INSN){
				LdcInsnNode ldcNode = (LdcInsnNode)node;
				if(ldcNode.cst.equals(value)){
					AbstractInsnNode tmpNode = ldcNode;
					while(true){
						if(tmpNode.getType() == AbstractInsnNode.METHOD_INSN && matchs(((MethodInsnNode)tmpNode).name, "addExhaustion")){
							break;
						}
						tmpNode = tmpNode.getNext();
					}
					return (MethodInsnNode)tmpNode;
				}
			}
		}
		return null;
	}
	
	public MethodInsnNode getMovementNode(ClassNode cNode, String name){
		MethodNode mNode = findMethodOfName(cNode, "addMovementStat");
		for(int index = 0; index < mNode.instructions.size(); index++){
			AbstractInsnNode node = mNode.instructions.get(index);
			if(node.getType() == AbstractInsnNode.FIELD_INSN){
				FieldInsnNode fNodei = (FieldInsnNode)node;
				if(matchs(fNodei.name, name)){
					AbstractInsnNode tmpNode = fNodei;
					while(true){
						if(tmpNode.getType() == AbstractInsnNode.METHOD_INSN && matchs(((MethodInsnNode)tmpNode).name, "addExhaustion")){
							break;
						}
						tmpNode = tmpNode.getNext();
					}
					return (MethodInsnNode)tmpNode;
				}
			}
		}
		return null;
	}
	
	public byte[] writeClassToBytes(ClassNode cNode){
		ClassWriter writer = new ClassWriter(0);
		cNode.accept(writer);
		return writer.toByteArray();
	}
	
	public ClassNode readClassFromBytes(byte[] bytes){
		ClassNode cNode = new ClassNode();
		ClassReader cReader = new ClassReader(bytes);
		cReader.accept(cNode, 0);
		return cNode;
	}
	
	public MethodNode findMethodOfName(ClassNode cNode, String name){
		for(MethodNode mNode : cNode.methods){
			if(matchs(mNode.name, name)){
				return mNode;
			}
		}
		return null;
	}
	
	public static boolean matchs(String name1, String name2){
		if(!mappings.containsKey(name2)){
			return name1.equals(name2);
		}
		for(String value : mappings.get(name2)){
			if(name1.equals(value)){
				return true;
			}
		}
		return name1.equals(name2);
	}
	
	public static void addMapping(String name, String value){
		if(!mappings.containsKey(name)){
			mappings.put(name, new ArrayList<String>());
		}
		mappings.get(name).add(value);
	}
	
	static{
		addMapping("hungerDamage", "field_76384_q");
		addMapping("DIVE_ONE_CM", "field_188105_q");
		addMapping("SWIM_ONE_CM", "field_75946_m");
		addMapping("WALK_ONE_CM", "field_188100_j");
		addMapping("SPRINT_ONE_CM", "field_188102_l");
		addMapping("CROUCH_ONE_CM", "field_188101_k");
		addMapping("JUMP", "field_75953_u");
		addMapping("harvestBlock", "func_180657_a");
		addMapping("addExhaustion", "func_71020_j");
		addMapping("addMovementStat", "func_71000_j");
		addMapping("jump", "func_70664_aZ");
		addMapping("attackTargetEntityWithCurrentItem", "func_71059_n");
		addMapping("performEffect", "func_76394_a");
		addMapping("addStat", "func_71029_a");
		addMapping("isSprinting", "func_70051_ag");
		addMapping("damageEntity", "func_70665_d");
		addMapping("onUpdate", "func_75118_a");
	}
}
